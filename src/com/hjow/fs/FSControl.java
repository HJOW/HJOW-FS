package com.hjow.fs;
/*
Copyright 2024 HJOW (Heo Jin Won)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import com.hjow.fs.console.FSConsole;
import com.hjow.fs.console.FSConsoleResult;
import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

import hjow.common.json.JsonArray;
import hjow.common.json.JsonObject;
import hjow.common.util.SecurityUtil;

public class FSControl {
	public static final int[] VERSION = {0, 1, 4, 11};
	
	private static FSControl instance = null;
	
	protected JsonObject conf = new JsonObject();
	protected volatile long    confReads    = 0L;
	protected volatile boolean confChanging = false;
	protected volatile boolean accChanging  = false;
	protected boolean installed = false;
	
	protected String cs = "UTF-8";

	// Title
	protected String title = "File Storage";

	// Installation Status
	protected File fileConfigPath = null;

	// Storage Root Directory
	protected String storPath  = "/fsdir/storage/";

	// Reading configuration file time gap (milliseconds)
	protected long   refreshConfGap = 4000L;
	
	// Read and display file icon
	protected boolean readFileIcon = true;

	// Download limit size (KB)
	protected long   limitSize = 10 * 1024 * 1024;
	
	// Display limit count on one page
	protected int    limitCount = 1000;

	// Perform downloading buffer size (bytes)
	protected int  bufferSize   = 1024 * 10;

	// Perform downloading thread's sleeping gap (milliseconds) - Downloading speed faster when this value is become lower.
	protected long sleepGap     = 100;

	// Perform downloading thread's sleeping cycle (times) - Downloading speed faster when this value is become lower.
	protected int  sleepRoutine = 100;

	// Captcha
	protected boolean captchaDownload = true, captchaLogin = true;
	protected int  captchaWidth     = 250;
	protected int  captchaHeight    = 40;
	protected int  captchaFontSize  = 30;
	protected int  captchaNoises    = 20;
	protected long captchaLimitTime = 1000 * 60 * 5;
	
	// Login Policy
	protected boolean noLogin = false;
	protected int loginFailCountLimit = 10;
	protected int loginFailOverMinute = 10;
	
	protected boolean noConsole = true;
	
	protected String salt = "fs";
	protected File rootPath  = null;
	protected File garbage   = null;
	protected File uploadd   = null;
	protected String ctxPath = "";
	
	protected boolean useJDBC = false;
	protected String dbType = null, jdbcClass = null, jdbcUrl = null, jdbcId = null, jdbcPw = null;
	
	private FSControl() {
		
	}
	
	public static FSControl getInstance() { return instance; }
	public static void init(HttpServletRequest request) {
		init(request, false);
	}
	public static void init(HttpServletRequest request, boolean forceInit) {
		if(instance == null || forceInit) {
			if(instance != null) instance.conf.clear();
			instance = new FSControl();
		}
		instance.initialize(request);
	}
	
	protected synchronized void initialize(HttpServletRequest request) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			long now = System.currentTimeMillis();
			
			// Set Global Variables
			ctxPath = request.getContextPath();
			
			if(fileConfigPath == null || Math.abs(now - confReads) >= refreshConfGap) {
				confReads = now;
				
				// Check installation and trying to find configuration file
				InputStream  propIn   = null;
				OutputStream fileOut  = null;
				Reader       rd1      = null;
				Reader       rd2      = null;
				try {
					String s1 = "";
					String s2 = "";
					String s3 = "";
					
				    propIn = FSControl.class.getResourceAsStream("/fs.properties");
				    if(propIn != null) {
				    	Properties propTest = new Properties();
				        propTest.load(propIn);
				        // Check fs.properties
				        String t1 = propTest.getProperty("FS");
				        String t2 = propTest.getProperty("RD");
				        String t3 = propTest.getProperty("PW");
				        s1 = propTest.getProperty("S1");
				        s2 = propTest.getProperty("S2");
				        s3 = propTest.getProperty("S3");
				        if(t1 != null && t2 != null && t3 != null && s1 != null && s2 != null && s3 != null) {
				            if(t1.trim().equals("FileStorage") && t2.trim().equals("SetConfigPathBelow")) {
				                System.out.println("Configuration Found !");
				                
				                // Set CS
				                cs = propTest.getProperty("CS");
				                if(cs == null) cs = "UTF-8";
				                cs = cs.trim();
				                
				                // Get configuration directory from fs.properties
				                String cf = propTest.getProperty("CF");
				                if(cf != null) {
				                    cf = cf.trim();
				                    if(cf.startsWith("ENV:")) {
				                        // If CF starts with 'ENV:', then get directory from system environment.
				                        cf = cf.replace("ENV:", "").trim();
				                        fileConfigPath = new File(System.getProperty(cf) + File.separator + ".fs" + File.separator);
				                    } else {
				                        // Else, then get directory as absolute path.
				                    	fileConfigPath = new File(cf + File.separator + ".fs" + File.separator);
				                    }
				                    if(! fileConfigPath.exists()) fileConfigPath.mkdirs();
				                }
				            }
				        }
				        
				        // Check JDBC option exists and avail
				        String pDB = propTest.getProperty("DB");
				        if(pDB != null) {
				        	if(pDB.equalsIgnoreCase("Y") || pDB.equalsIgnoreCase("YES") || pDB.equalsIgnoreCase("TRUE")) {
				        		useJDBC = true;
				        		
				        		dbType    = propTest.getProperty("DB_TYPE");
				        		jdbcClass = propTest.getProperty("DB_CLASS");
				        		jdbcUrl   = propTest.getProperty("DB_URL");
				        		jdbcId    = propTest.getProperty("DB_USER");
				        		jdbcPw    = propTest.getProperty("DB_PW");
				        		
				        		if(dbType == null || jdbcClass == null || jdbcUrl == null || jdbcId == null || jdbcPw == null) useJDBC = false;
				        		if(useJDBC) {
				        			dbType    = dbType.trim().toLowerCase();
				        			jdbcClass = jdbcClass.trim();
					        		jdbcUrl   = jdbcUrl.trim();
					        		jdbcId    = jdbcId.trim();
					        		jdbcPw    = jdbcPw.trim();
					        		
					        		try { Class.forName(jdbcClass); } catch(ClassNotFoundException e) { System.out.println("No jdbc driver found - " + jdbcClass); useJDBC = false; } 
				        		}
				        	}
				        }
				        
				        propTest.clear();
				        propTest = null;
				        
				        // Close fs.properties
			            propIn.close();
			            propIn = null;
				        
			            // Read configs
				        if(useJDBC) {
				        	conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
				        	
				        	pstmt = conn.prepareStatement("SELECT JSONCONFIG FROM FS_CONFIG");
							rs    = pstmt.executeQuery();
							
							String confJson = null;
							while(rs.next()) {
								confJson = rs.getString("JSONCONFIG");
							}
							
							rs.close(); rs = null;
							pstmt.close(); pstmt = null;
							conn.close(); conn = null;
							
							conf = (JsonObject) JsonObject.parseJson(confJson);
				            conf.put("S1", s1);
				            conf.put("S2", s2);
				            conf.put("S3", s3);
				        	
				        } else if(fileConfigPath != null) {
				            // Find config.json from configuration path
				            File fJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "config.json");
				            if(! fJson.exists()) {
				                // Not exist, create
				                fileOut = new FileOutputStream(fJson);
				                fileOut.write("{}".getBytes(cs));
				                fileOut.close(); fileOut = null;
				            }
				            
				            propIn = new FileInputStream(fJson);
				            rd1 = new InputStreamReader(propIn, cs);
				            rd2 = new BufferedReader(rd1);
				            
				            StringBuilder lineCollection = new StringBuilder("");
				            String line;
				            while(true) {
				            	line = ((BufferedReader) rd2).readLine();
				            	if(line == null) break;
				            	lineCollection = lineCollection.append("\n").append(line); 
				            }
				            
				            rd2.close(); rd2 = null;
				            rd1.close(); rd1 = null;
				            propIn.close(); propIn = null;
				            
				            conf = (JsonObject) JsonObject.parseJson(lineCollection.toString().trim());
				            conf.put("S1", s1);
				            conf.put("S2", s2);
				            conf.put("S3", s3);
				            
				            lineCollection.setLength(0);
				            lineCollection = null;
				            
				            // Find accounts.json from configuration path
			                File faJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "accounts");
			                if(! faJson.exists()) {
			                    faJson.mkdirs();
			                }
				        }
				    } else {
				        System.out.println("No fs.properties !");
				    }
				} catch(Throwable t) {
					fileConfigPath = null;
				    t.printStackTrace(); 
				} finally {
					if(rd2     != null) rd2.close();
					if(rd1     != null) rd1.close();
				    if(propIn  != null) propIn.close();
				    if(fileOut != null) fileOut.close();
				    if(rs      != null) rs.close();
					if(pstmt   != null) pstmt.close();
					if(conn    != null) conn.close();
				    if(conf    == null) conf = new JsonObject();
				}
				propIn = null;
			}
			
			// Applying Configs
			if(conf.get("Installed") != null) {
				installed = Boolean.parseBoolean(conf.get("Installed").toString().trim());
			}
			if(installed) {
				if(conf.get("Path") != null) {
					storPath = conf.get("Path").toString().trim();
				}
				rootPath  = new File(storPath);
				if(! rootPath.exists()) rootPath.mkdirs();
				garbage = new File(rootPath.getCanonicalPath() + File.separator + ".garbage");
			    if(! garbage.exists()) garbage.mkdirs();
			    uploadd = new File(rootPath.getCanonicalPath() + File.separator + ".upload");
			    if(! uploadd.exists()) uploadd.mkdirs();
				if(conf.get("UseAccount") != null) {
					noLogin = (! Boolean.parseBoolean(conf.get("UseAccount").toString().trim()));
				}
				if(conf.get("UseConsole") != null) {
					noConsole = (! Boolean.parseBoolean(conf.get("UseConsole").toString().trim()));
				}
				if(conf.get("UseCaptchaDown") != null) {
					captchaDownload = Boolean.parseBoolean(conf.get("UseCaptchaDown").toString().trim());
				}
				if(conf.get("UseCaptchaLogin") != null) {
					captchaLogin = Boolean.parseBoolean(conf.get("UseCaptchaLogin").toString().trim());
				}
				if(conf.get("LimitUploadSize") != null) {
					limitSize = Long.parseLong(conf.get("LimitUploadSize").toString().trim());
				}
				if(conf.get("LimitFilesSinglePage") != null) {
					limitCount = Integer.parseInt(conf.get("LimitFilesSinglePage").toString().trim());
				}
				if(conf.get("ReadFileIcon") != null) {
					readFileIcon = Boolean.parseBoolean(conf.get("ReadFileIcon").toString().trim());
				}
				if(conf.get("Salt") != null) {
					salt = conf.get("Salt").toString().trim();
				}
				if(conf.get("Title") != null) {
					String tx = conf.get("Title").toString().trim();
					if(! tx.equals("")) title = tx;
				}
			}

			if(noLogin) {
				Object sessionMap = request.getSession().getAttribute("fssession");
				if(sessionMap != null) request.getSession().removeAttribute("fssession");
			}
			
			List<String> cmdList = new ArrayList<String>();
			if(conf.get("CommandClass") != null) {
				JsonArray arr = null;
				if(conf.get("CommandClass") instanceof JsonArray) arr = (JsonArray) conf.get("CommandClass");
				else arr = (JsonArray) JsonObject.parseJson(conf.get("CommandClass").toString().trim());
				for(Object a : arr) {
					cmdList.add(a.toString().trim());
				}
			}
			
			FSConsole.init(rootPath, cmdList);
		} catch(Throwable thx) {
			thx.printStackTrace();
			throw new RuntimeException(thx.getMessage(), thx);
		}
	}
	
	public JsonObject install(HttpServletRequest request) throws Exception {
		JsonObject json = new JsonObject();
		if(! installed) {
			Properties   propTest = new Properties();
		    InputStream  propIn   = null;
			OutputStream fileOut  = null;
			
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try {
				String passwords = request.getParameter("pw");
				if(passwords == null) {
					String rex = "Please input Password !";
					if(getLanguage(request).equals("ko")) rex = "비밀번호를 입력해 주세요.";
					throw new RuntimeException(rex);
				}
				passwords = passwords.trim();
				
				propIn = this.getClass().getResourceAsStream("/fs.properties");
		        if(propIn == null) {
		        	String rex = "No fs.properties found at ./WEB-INF/classes/";
					if(getLanguage(request).equals("ko")) rex = "fs.properties 파일을 찾을 수 없습니다. ./WEB-INF/classes/ 경로 상에 이 파일이 있어야 합니다.";
		        	throw new FileNotFoundException(rex);
		        }
		        
		        propTest = new Properties();
		        propTest.load(propIn);
		        
		        propIn.close(); propIn = null;
		        
		        // Check fs.properties
		        String tx1 = propTest.getProperty("FS");
		        String tx2 = propTest.getProperty("RD");
		        String tx3 = propTest.getProperty("PW");
		        String s1  = propTest.getProperty("S1");
		        String s2  = propTest.getProperty("S2");
		        String s3  = propTest.getProperty("S3");
		        
		        propTest.clear();
		        propTest = null;
		        
		        if(tx1 == null || tx2 == null || tx3 == null || s1 == null || s2 == null || s3 == null) {
		        	String rex = "No correct fs.properties found at ./WEB-INF/classes/ ! Please check values !";
					if(getLanguage(request).equals("ko")) rex = "fs.properties 파일 내용이 올바르지 않습니다. ./WEB-INF/classes/ 경로 상에 이 파일이 있습니다. 파일 내용을 점검해 주세요.";
		        	throw new FileNotFoundException(rex);
		        } else if(! (tx1.trim().equals("FileStorage") && tx2.trim().equals("SetConfigPathBelow"))) {
		        	String rex = "No correct fs.properties found at ./WEB-INF/classes/ ! Please check values !";
					if(getLanguage(request).equals("ko")) rex = "fs.properties 파일 내용이 올바르지 않습니다. ./WEB-INF/classes/ 경로 상에 이 파일이 있습니다. 파일 내용을 점검해 주세요.";
		        	throw new FileNotFoundException(rex);
		        }
		        
		        // Get Title, Root Dir
		        
		        String titles = request.getParameter("title");
		        if(titles == null) titles = "File Storage";
		        titles = titles.trim();
		        if(titles.equals("")) titles = "File Storage";
		        
				String roots = request.getParameter("rootdir");
				if(roots == null) {
					throw new RuntimeException("Please input the Root Directory !");
				}
				
				// Check Installation Password
				if(! passwords.equals(tx3.trim())) {
					if(! SecurityUtil.hash(passwords, "SHA-256").equals(tx3.trim())) {
						throw new RuntimeException("Wrong installation password !");
					}
				}
				
				// Check JDBC
				if(useJDBC) {
					Class.forName(jdbcClass);
					conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
					
		        	// Check FS_CONFIG
		        	try {
		        		// Check FS_CONFIG exist
		        		pstmt = conn.prepareStatement("SELECT COUNT(*) AS CNT FROM FS_CONFIG");
			        	rs = pstmt.executeQuery();
			        	
			        	int count = 0;
			        	while(rs.next()) {
			        		count = rs.getInt("CNT");
			        	}
			        	
			        	rs.close(); rs = null;
			        	pstmt.close(); pstmt = null;
			        	
			        	if(count >= 2) {
			        		pstmt = conn.prepareStatement("DELETE FROM FS_CONFIG");
			        		pstmt.execute();
			        		conn.commit();
			        		pstmt.close(); pstmt = null;
			        		count = 0;
			        	}
			        	if(count <= 0) {
			        		pstmt = conn.prepareStatement("INSERT INTO FS_CONFIG (JSONCONFIG) VALUES ('{}')");
			        		pstmt.execute();
			        		conn.commit();
			        		pstmt.close(); pstmt = null;
			        	}
		        	} catch(SQLException e) {
		        		if(dbType.equals("oracle")) pstmt = conn.prepareStatement("CREATE TABLE FS_CONFIG ( JSONCONFIG VARCHAR2(4000) )");
		        		else                        pstmt = conn.prepareStatement("CREATE TABLE FS_CONFIG ( JSONCONFIG VARCHAR(4000) )");
		        		pstmt.execute();
		        		conn.commit();
		        		pstmt.close(); pstmt = null;
		        		
		        		pstmt = conn.prepareStatement("INSERT INTO FS_CONFIG (JSONCONFIG) VALUES ('{}')");
		        		pstmt.execute();
		        		conn.commit();
		        		pstmt.close(); pstmt = null;
		        	}
		        	
		        	// Check FS_USER
		        	
		        	try {
		        		// Check FS_USER exist
		        		pstmt = conn.prepareStatement("SELECT COUNT(*) AS CNT FROM FS_USER");
			        	rs = pstmt.executeQuery();
			        	
			        	while(rs.next()) {
			        		rs.getInt("CNT");
			        	}
			        	
			        	rs.close(); rs = null;
			        	pstmt.close(); pstmt = null;
			        	
			        	pstmt = conn.prepareStatement("SELECT USERID, USERPW, USERNICK, USERTYPE, FAILCNT, FAILTIME, PRIVILEGES AS CNT FROM FS_USER");
			        	rs = pstmt.executeQuery();
			        	
			        	rs.close(); rs = null;
			        	pstmt.close(); pstmt = null;
		        	} catch(SQLException e) {
		        		if(dbType.equals("oracle")) pstmt = conn.prepareStatement("CREATE TABLE FS_USER ( USERID VARCHAR2(40) PRIMARY KEY, USERPW VARCHAR2(512), USERNICK VARCHAR2(40), USERTYPE VARCHAR2(5), FAILCNT NUMBER(10) , FAILTIME NUMBER(20) , PRIVGROUP VARCHAR2(4000), PRIVILEGES VARCHAR2(4000) )");
		        		else                        pstmt = conn.prepareStatement("CREATE TABLE FS_USER ( USERID VARCHAR(40)  PRIMARY KEY, USERPW VARCHAR(512) , USERNICK VARCHAR(40) , USERTYPE VARCHAR(5) , FAILCNT NUMERIC(10), FAILTIME NUMERIC(20), PRIVGROUP VARCHAR(4000) , PRIVILEGES VARCHAR(4000)  )");
		        		pstmt.execute();
		        		conn.commit();
		        		pstmt.close(); pstmt = null;
		        	}
		        }
				
				// Check Others
				
				rootPath = new File(roots);
				if(! rootPath.exists()) rootPath.mkdirs();
				
				garbage = new File(rootPath.getCanonicalPath() + File.separator + ".garbage");
				if(! garbage.exists()) garbage.mkdirs();
				
				String sMaxSize = request.getParameter("limitsize");
				if(sMaxSize == null) sMaxSize = "" + (1024 * 1024);
				Long.parseLong(sMaxSize); // Checking valid number
				
				String sMaxCount = request.getParameter("limitcount");
				if(sMaxCount == null) sMaxCount = "1000";
				Integer.parseInt(sMaxCount); // Checking valid number
				
				String sUseCaptchaDown  = request.getParameter("usecaptchadown");
				String sUseCaptchaLogin = request.getParameter("usecaptchalogin");
				String sReadFileIcon    = request.getParameter("readfileicon");
				
				boolean useCaptchaDown  = false;
				boolean useCaptchaLogin = false;
				boolean useReadFileIcon = false;
				
				if(sUseCaptchaDown != null) useCaptchaDown  = Boolean.parseBoolean(sUseCaptchaDown.trim());
				if(sReadFileIcon   != null) useReadFileIcon = Boolean.parseBoolean(sReadFileIcon.trim());
				
				String sUseAccounts = request.getParameter("useaccount");
				if(sUseAccounts != null) {
					noLogin = (! Boolean.parseBoolean(sUseAccounts.trim()));
					if(! noLogin) {
						if(sUseCaptchaLogin != null) useCaptchaLogin = Boolean.parseBoolean(sUseCaptchaLogin.trim());
						
						String adminId, adminPw, aSalt;
						adminId = request.getParameter("adminid");
						adminPw = request.getParameter("adminpw");
						aSalt   = request.getParameter("salt");
						
						if(adminId == null || adminPw == null) {
							noLogin = true;
							throw new RuntimeException("Please input the administration user's account information !");
						}
						
						if(aSalt == null) {
							throw new RuntimeException("Please input the Salt value !");
						}
						
						salt = aSalt;
						
						adminId = adminId.trim();
						adminPw = adminPw.trim();
						salt    = salt.trim();
						
						if(adminId.equals("") || adminPw.equals("")) {
		                    noLogin = true;
		                    throw new RuntimeException("Please input the administration user's account information !");
		                }
						
						if(adminId.contains("'") || adminId.contains("\"") || adminId.contains("/") || adminId.contains("\\") || adminId.contains(File.separator) || adminId.contains(".") || adminId.contains(" ") || adminId.contains("\n") || adminId.contains("\t")) 
							throw new RuntimeException("ID can only contains alphabets and numbers !");
						
						String adminNick = request.getParameter("adminnick");
						if(adminNick == null) adminNick = "Admin";
						adminNick = adminNick.trim();
						
						JsonObject adminAc = new JsonObject();
						adminAc.put("id"    , adminId);
						adminAc.put("idtype", "A");
						adminAc.put("nick"  , adminNick);
						adminAc.put("fail_cnt", "0");
						adminAc.put("fail_time", "0");
						
						JsonArray prvgroup = new JsonArray();
						prvgroup.add("user");
						prvgroup.add("admin");
						adminAc.put("privgroup", prvgroup);
						adminAc.put("pw", SecurityUtil.hash(s1 + adminPw + s2 + salt + adminId + s3, "SHA-256"));
						
						if(useJDBC) {
							pstmt = conn.prepareStatement("INSERT INTO FS_USER (USERID, USERPW, USERNICK, USERTYPE, FAILCNT , FAILTIME, PRIVGROUP, PRIVILEGES) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
							pstmt.setString(1, adminId);
							pstmt.setString(2, adminAc.get("pw").toString());
							pstmt.setString(3, adminNick);
							pstmt.setString(4, "A");
							pstmt.setInt(5, 0);
							pstmt.setInt(6, 0);
							pstmt.setString(7, "[\"user\", \"admin\"]");
							pstmt.setString(8, "[]");
							
							pstmt.executeUpdate();
							conn.commit();
							
							pstmt.close(); pstmt = null;
						} else {
							File faJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "accounts");
					        if(! faJson.exists()) faJson.mkdirs();
					        
					        File fileAcc = new File(fileConfigPath.getCanonicalPath() + File.separator + "accounts" + File.separator + adminId + ".json");
					        fileAcc.getCanonicalPath(); // Check valid
					        fileOut = new FileOutputStream(fileAcc);
					        fileOut.write(adminAc.toJSON().getBytes(cs));
					        fileOut.close(); fileOut = null;
						}
					}
				} else {
					noLogin = true;
				}
				
				conf.clear();
				
				String sHiddenDir = "";
				sHiddenDir += "# Hidden Directory List" + "\n";
				sHiddenDir += "#    These directories will be hidden and revealed only for privileged user (and administrator)." + "\n";
				sHiddenDir += "#    As a relative path from FS root directory." + "\n";
				sHiddenDir += "# For example" + "\n";
				sHiddenDir += "# [" + "\n";
				sHiddenDir += "#     \"/HiddenDir1\"" + "\n";
				sHiddenDir += "#     \"/Updates/HiddenDir2\"" + "\n";
				sHiddenDir += "# ]" + "\n";
				sHiddenDir += "[]";
				
				conf.put("Title", titles);
				conf.put("sHiddenDirs", sHiddenDir);
				conf.put("HiddenDirs", new JsonArray());
				conf.put("Path", rootPath.getCanonicalPath());
				conf.put("UseAccount", new Boolean(! noLogin));
				conf.put("UseCaptchaDown" , new Boolean(useCaptchaDown));
				conf.put("UseCaptchaLogin", new Boolean(useCaptchaLogin));
				conf.put("LimitUploadSize", sMaxSize);
				conf.put("LimitFilesSinglePage", sMaxCount);
				conf.put("ReadFileIcon", new Boolean(useReadFileIcon));
				conf.put("S1", s1);
		        conf.put("S2", s2);
		        conf.put("S3", s3);
		        conf.put("Salt", salt);
		        conf.put("Installed", new Boolean(true));
				
		        if(useJDBC) {
		        	pstmt = conn.prepareStatement("UPDATE FS_CONFIG SET JSONCONFIG = ?");
		        	pstmt.setString(1, conf.toJSON());
		        	pstmt.executeUpdate();
		        	conn.commit();
		        	pstmt.close(); pstmt = null;
		        } else {
		        	File fJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "config.json");
					fileOut = new FileOutputStream(fJson);
					fileOut.write(conf.toJSON().getBytes(cs));
					fileOut.close(); fileOut = null;
		        }
				
				installed = true;
				json.put("success", new Boolean(true));
				json.put("message", "Installation Success");
			} catch(Throwable t) {
				json.put("success", new Boolean(false));
				if(t instanceof RuntimeException) {
					json.put("message", t.getMessage());
				} else {
					json.put("message", "Error : " + t.getMessage());
				}
			} finally {
				if(fileOut != null) fileOut.close();
				if(propIn  != null) propIn.close();
				if(rs      != null) rs.close();
				if(pstmt   != null) pstmt.close();
				if(conn    != null) conn.close();
			}
		}
		return json;
	}
	
	public JsonObject admin(HttpServletRequest request) throws Exception {
		JsonObject json       = new JsonObject();
		JsonObject jsonConfig = new JsonObject();
		
		Properties   propTest = new Properties();
	    InputStream  propIn   = null;
		OutputStream fileOut  = null;
		Reader rd1 = null, rd2 = null;
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		JsonObject sessionMap = null;
		String lang = "en";
		
		try {
			if(noLogin) {
				if(lang.equals("ko")) throw new RuntimeException("이 작업을 수행할 권한이 없습니다.");
				else                  throw new RuntimeException("No privilege");
			}
			
			sessionMap = getSessionObject(request);
			lang       = getLanguage(request);
			
			if(sessionMap == null) {
				if(lang.equals("ko")) throw new RuntimeException("이 작업을 수행할 권한이 없습니다.");
				else                  throw new RuntimeException("No privilege");
			}
			
			Object idtype = sessionMap.get("idtype");
			if(idtype == null) {
				if(lang.equals("ko")) throw new RuntimeException("이 작업을 수행할 권한이 없습니다.");
				else                  throw new RuntimeException("No privilege");
			}
			if(! idtype.toString().equals("A")) {
				if(lang.equals("ko")) throw new RuntimeException("이 작업을 수행할 권한이 없습니다.");
				else                  throw new RuntimeException("No privilege");
			}
			
			String req = request.getParameter("req");
			if(req == null) {
				String rex = "Wrong request !";
				if(getLanguage(request).equals("ko")) rex = "올바르지 않은 네트워크 요청으로 작업이 거부되었습니다. 새로고침 후 다시 이용해 주세요.";
				throw new RuntimeException(rex);
			}
			
			propIn = this.getClass().getResourceAsStream("/fs.properties");
	        if(propIn == null) {
	        	String rex = "No fs.properties found at ./WEB-INF/classes/";
				if(getLanguage(request).equals("ko")) rex = "fs.properties 파일을 찾을 수 없습니다. ./WEB-INF/classes/ 경로 상에 이 파일이 있어야 합니다.";
	        	throw new FileNotFoundException(rex);
	        }
	        
	        propTest = new Properties();
	        propTest.load(propIn);
	        
	        propIn.close(); propIn = null;
	        
	        // Check fs.properties
	        String tx1 = propTest.getProperty("FS");
	        String tx2 = propTest.getProperty("RD");
	        String tx3 = propTest.getProperty("PW");
	        String s1  = propTest.getProperty("S1");
	        String s2  = propTest.getProperty("S2");
	        String s3  = propTest.getProperty("S3");
	        
	        propTest.clear();
	        propTest = null;
	        
	        if(tx1 == null || tx2 == null || tx3 == null || s1 == null || s2 == null || s3 == null) {
	        	String rex = "No correct fs.properties found at ./WEB-INF/classes/ ! Please check values !";
				if(getLanguage(request).equals("ko")) rex = "fs.properties 파일 내용이 올바르지 않습니다. ./WEB-INF/classes/ 경로 상에 이 파일이 있습니다. 파일 내용을 점검해 주세요.";
	        	throw new FileNotFoundException(rex);
	        } else if(! (tx1.trim().equals("FileStorage") && tx2.trim().equals("SetConfigPathBelow"))) {
	        	String rex = "No correct fs.properties found at ./WEB-INF/classes/ ! Please check values !";
				if(getLanguage(request).equals("ko")) rex = "fs.properties 파일 내용이 올바르지 않습니다. ./WEB-INF/classes/ 경로 상에 이 파일이 있습니다. 파일 내용을 점검해 주세요.";
	        	throw new FileNotFoundException(rex);
	        }
	        
	        File fJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "config.json");
	        
	        if(! fJson.exists()) {
                // Not exist, create
                fileOut = new FileOutputStream(fJson);
                fileOut.write("{}".getBytes(cs));
                fileOut.close(); fileOut = null;
            }
            
            propIn = new FileInputStream(fJson);
            rd1 = new InputStreamReader(propIn, cs);
            rd2 = new BufferedReader(rd1);
            
            StringBuilder lineCollection = new StringBuilder("");
            String line;
            while(true) {
            	line = ((BufferedReader) rd2).readLine();
            	if(line == null) break;
            	lineCollection = lineCollection.append("\n").append(line); 
            }
            
            rd2.close(); rd2 = null;
            rd1.close(); rd1 = null;
            propIn.close(); propIn = null;
            
            jsonConfig = (JsonObject) JsonObject.parseJson(lineCollection.toString().trim());
            
            json.put("message", "");
			
			if(req.equals("update")) {
				String titles = request.getParameter("title");
		        if(titles == null) titles = "File Storage";
		        titles = titles.trim();
		        if(titles.equals("")) titles = "File Storage";
		        
				if(! rootPath.exists()) rootPath.mkdirs();
				
				garbage = new File(rootPath.getCanonicalPath() + File.separator + ".garbage");
				if(! garbage.exists()) garbage.mkdirs();
				
				String sHiddenDirs = request.getParameter("hiddendirs");
				if(sHiddenDirs == null) sHiddenDirs = "[]";
				sHiddenDirs = sHiddenDirs.trim();
				if(sHiddenDirs.equals("")) sHiddenDirs = "[]";
				Object hiddenDirs = JsonObject.parseJson(FSUtils.removeLineComments(sHiddenDirs, '#').trim()); // Checking valid JSON
				if(! (hiddenDirs instanceof JsonArray)) throw new RuntimeException("'Hidden Folders' Should be a JSON array.");
				
				String sMaxSize = request.getParameter("limitsize");
				if(sMaxSize == null) sMaxSize = "" + (1024 * 1024);
				Long.parseLong(sMaxSize); // Checking valid number
				
				String sMaxCount = request.getParameter("limitcount");
				if(sMaxCount == null) sMaxCount = "1000";
				Integer.parseInt(sMaxCount); // Checking valid number
				
				String sUseCaptchaDown  = request.getParameter("usecaptchadown");
				String sUseCaptchaLogin = request.getParameter("usecaptchalogin");
				String sReadFileIcon    = request.getParameter("readfileicon");
				String sUseConsole      = request.getParameter("useconsole");
				
				boolean useCaptchaDown  = false;
				boolean useCaptchaLogin = false;
				boolean useReadFileIcon = false;
				boolean useConsole      = false;
				
				if(sUseCaptchaDown  != null) useCaptchaDown  = Boolean.parseBoolean(sUseCaptchaDown.trim());
				if(sUseCaptchaLogin != null) useCaptchaLogin = Boolean.parseBoolean(sUseCaptchaLogin.trim());
				if(sReadFileIcon    != null) useReadFileIcon = Boolean.parseBoolean(sReadFileIcon.trim());
				if(sUseConsole      != null) useConsole      = Boolean.parseBoolean(sUseConsole.trim());
				
				conf.put("Title", titles);
				conf.put("sHiddenDirs", sHiddenDirs);
				conf.put("HiddenDirs", hiddenDirs);
				conf.put("UseCaptchaDown" , new Boolean(useCaptchaDown));
				if(! noLogin) conf.put("UseCaptchaLogin", new Boolean(useCaptchaLogin));
				conf.put("LimitUploadSize", sMaxSize);
				conf.put("LimitFilesSinglePage", sMaxCount);
				conf.put("ReadFileIcon", new Boolean(useReadFileIcon));
				conf.put("UseConsole", new Boolean(useConsole));
				conf.put("Installed", new Boolean(true));
				
				if(useJDBC) {
					Class.forName(jdbcClass);
					conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
					pstmt = conn.prepareStatement("UPDATE FS_CONFIG SET JSONCONFIG = ?");
		        	pstmt.setString(1, conf.toJSON());
		        	pstmt.executeUpdate();
		        	conn.commit();
		        	pstmt.close(); pstmt = null;
		        	conn.close(); conn = null;
				} else {
					fileOut = new FileOutputStream(fJson);
					fileOut.write(conf.toJSON().getBytes(cs));
					fileOut.close(); fileOut = null;
				}
				
				System.out.println("Configuration Updated by " + sessionMap.get("id") + " when " + System.currentTimeMillis());
				jsonConfig.clear();
				jsonConfig = (JsonObject) conf.cloneObject();
				
				json.put("message", "Update Success !");
			} else if(req.equals("reset")) {
				String passwords = request.getParameter("pw");
				if(passwords == null) {
					String rex = "Please input Password !";
					if(lang.equals("ko")) rex = "비밀번호를 입력해 주세요.";
					throw new RuntimeException(rex);
				}
				passwords = passwords.trim();
				
				if(! passwords.equals(tx3.trim())) {
					if(! SecurityUtil.hash(passwords, "SHA-256").equals(tx3.trim())) {
						String rex = "Wrong installation password !";
						if(lang.equals("ko")) rex = "비밀번호를 올바르게 입력해 주세요.";
						throw new RuntimeException(rex);
					}
				}
				
				System.out.println("Reset requested by " + sessionMap.get("id") + " when " + System.currentTimeMillis());
				
				installed = false;
				conf.clear();
				
				File[] listConfigs = fileConfigPath.listFiles();
				for(File f : listConfigs) {
					if(f.getName().startsWith("backup") || f.getName().startsWith("bak")) continue;
					if(f.isDirectory()) {
						File[] children = f.listFiles();
						for(File c : children) {
							if(c.getName().startsWith("backup") || c.getName().startsWith("bak")) continue;
							if(c.isDirectory()) {
								File[] grands = c.listFiles();
								for(File g : grands) {
									if(g.isDirectory()) continue;
									g.delete();
								}
							} else {
								c.delete();
							}
						}
					} else {
						f.delete();
					}
				}
				
				if(useJDBC) {
					Class.forName(jdbcClass);
					conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
					pstmt = conn.prepareStatement("DROP TABLE FS_CONFIG");
		        	pstmt.executeUpdate();
		        	conn.commit();
		        	pstmt.close(); pstmt = null;
		        	
		        	pstmt = conn.prepareStatement("DROP TABLE FS_USER");
		        	pstmt.executeUpdate();
		        	conn.commit();
		        	pstmt.close(); pstmt = null;
		        	conn.close(); conn = null;
				}
				
				System.out.println("Reset completed.");
				json.put("reset", new Boolean(true));
				json.put("message", "Reset Success !");
				request.getSession().invalidate();
			}
			
			if(! req.equals("reset")) {
				jsonConfig.remove("Path");
				jsonConfig.remove("S1");
				jsonConfig.remove("S2");
				jsonConfig.remove("S3");
				jsonConfig.remove("Salt");
				json.put("config" , jsonConfig);
			}
			
			json.put("success", new Boolean(true));
			json.put("message", "Apply Success");
		} catch(Throwable t) {
			json.put("success", new Boolean(false));
			if(t instanceof RuntimeException) {
				json.put("message", t.getMessage());
			} else {
				json.put("message", "Error : " + t.getMessage());
			}
		} finally {
			if(fileOut != null) fileOut.close();
			if(propIn  != null) propIn.close();
			if(rs      != null) rs.close();
			if(pstmt   != null) pstmt.close();
			if(conn    != null) conn.close();
		}
		
		return json;
	}
	
	public JsonObject console(HttpServletRequest request) throws Exception {
		JsonObject json       = new JsonObject();
				
		JsonObject sessionMap = null;
		String lang = "en";
		
		try {
			if(noLogin) {
				if(lang.equals("ko")) throw new RuntimeException("이 작업을 수행할 권한이 없습니다.");
				else                  throw new RuntimeException("No privilege");
			}
			
			sessionMap = getSessionObject(request);
			lang       = getLanguage(request);
			
			if(sessionMap == null) {
				if(lang.equals("ko")) throw new RuntimeException("이 작업을 수행할 권한이 없습니다.");
				else                  throw new RuntimeException("No privilege");
			}
			
			Object idtype = sessionMap.get("idtype");
			if(idtype == null) {
				if(lang.equals("ko")) throw new RuntimeException("이 작업을 수행할 권한이 없습니다.");
				else                  throw new RuntimeException("No privilege");
			}
			if(! idtype.toString().equals("A")) {
				if(lang.equals("ko")) throw new RuntimeException("이 작업을 수행할 권한이 없습니다.");
				else                  throw new RuntimeException("No privilege");
			}
			
            json.put("message", "");
			
            FSConsole console = (FSConsole) request.getSession().getAttribute("fsscen");
            if(console == null) {
            	console = FSConsole.getInstance();
            	request.getSession().setAttribute("fsscen", console);
            }
            
            String path, command;
            path    = request.getParameter("path");
            command = request.getParameter("command");
            
            if(path    == null) throw new RuntimeException("Wrong parameter. Please refresh the page !");
            if(command == null) command = "";
            
            console.setPath(path);
            
            Map<String, Object> sessionNewMap = new HashMap<String, Object>();
            sessionNewMap.put("id"        , sessionMap.get("id"));
            sessionNewMap.put("idtype"    , sessionMap.get("idtype"));
            sessionNewMap.put("privileges", sessionMap.get("privileges"));
            sessionNewMap.put("privgroup" , sessionMap.get("privgroup"));
            sessionNewMap.put("lang"      , lang);
            
            FSConsoleResult rs = console.run(sessionNewMap, command);
            
			json.put("success", new Boolean(true));
			json.put("path"   , rs.getPath());
			json.put("display", rs.getDisplay());
			json.put("displaynull", new Boolean(rs.isNulll()));
			
			if(rs.getDownloadAccepted() != null) {
				json.put("downloadaccept", new Boolean(true));
				json.put("downloadfile"  , rs.getDownloadAccepted());
				request.getSession().setAttribute("fsd_captcha_code", "SKIP");
			} else {
				json.put("downloadaccept", new Boolean(false));
			}
			
		} catch(Throwable t) {
			json.put("success", new Boolean(false));
			if(t instanceof RuntimeException) {
				json.put("message", t.getMessage());
			} else {
				json.put("message", "Error : " + t.getMessage());
			}
		}
		
		return json;
	}
	
	public JsonObject list(HttpServletRequest request, String pPath, String pKeyword, String pExcept) {
		initialize(request);
		
		JsonObject json = new JsonObject();
		
		String pathParam = pPath;
		if(pathParam == null) pathParam = "";
		pathParam = pathParam.trim();
		if(pathParam.equals("/")) pathParam = "";
		pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
		if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);

		String keyword = pKeyword;
		if(keyword == null) keyword = "";
		keyword = keyword.replace("'", "").replace("\"", "").replace("<", "").replace(">", "").trim();

		List<String> excepts = new ArrayList<String>();
		pExcept = pExcept.trim();
		if(! pExcept.equals("")) {
			StringTokenizer excTokenizer = new StringTokenizer(pExcept, ",");
			while(excTokenizer.hasMoreTokens()) {
				String eOne = excTokenizer.nextToken().trim();
				if(! excepts.contains(eOne)) excepts.add(eOne);
			}
			excTokenizer = null;
		}
		pExcept = null;
		
		JsonObject jsonSess = new JsonObject();
		
		try {
		    File dir = new File(instance.rootPath.getCanonicalPath() + File.separator + pathParam);
            
		    File[] list = dir.listFiles();
		    List<File> fileList = new ArrayList<File>();
		    if(list == null) list = new File[0];
		    
		    int excepted  = 0;
		    
		    for(File f : list) {
		    	String nm = f.getName();
		    	if(excepts.contains(nm)) {
		    		excepted++;
		    		continue;
		    	}
		    	fileList.add(f);
		    }
		    list = null;
		    
		    Collections.sort(fileList, new Comparator<File>() {
		    	@Override
		    	public int compare(File o1, File o2) {
		    		if(o1.isDirectory() && (! o2.isDirectory())) return -1;
		    		if((! o1.isDirectory()) && o2.isDirectory()) return 1;
		    		return (o1.getName().compareTo(o2.getName()));
		    	}
		    });
		    
		    List<File> chDirs  = new ArrayList<File>();
		    List<File> chFiles = new ArrayList<File>();
		    
		    int fileIndex = 0;
		    int skipped   = 0;
		    for(File f : fileList) {
		    	String nm = f.getName();
		    	if(limitCount >= 0 && fileIndex >= limitCount) {
		    		skipped++;
		    		continue;
		    	}
		        if(f.isDirectory()) {
		            if(nm.indexOf(".") >= 0) continue;
		            chDirs.add(f);
		        } else {
		            if(f.length() / 1024 >= instance.limitSize) continue;
		            chFiles.add(f);
		        }
		        fileIndex++;
		    }
            
		    fileList.clear();
		    fileList = null;
		    excepts.clear();
		    excepts = null;
		    
		    json.put("skipped" , new Integer(skipped ));
		    json.put("excepted", new Integer(excepted));
            
		    Collections.sort(chDirs);
		    Collections.sort(chFiles);
            
		    String pathDisp = pathParam; // 화면 출력용
		    if(pathDisp.startsWith("//")) pathDisp = pathDisp.substring(1);
		    if(! pathDisp.startsWith("/")) pathDisp = "/" + pathDisp;
		    
		    json.put("type", "list");
		    json.put("keyword", keyword);
		    json.put("path"   , pathParam);
		    json.put("dpath"  , pathDisp);

			jsonSess = getSessionObject(request);
			JsonArray dirPrv = null;
			String idtype = "U";
			if(jsonSess != null) {
				if(jsonSess.get("idtype") != null) {
					idtype = jsonSess.get("idtype").toString();
					if(! idtype.equals("A")) {
						Object oDirPrv = (Object) jsonSess.get("privileges");
					    if(oDirPrv != null) {
					    	dirPrv = null;
				            if(oDirPrv instanceof JsonArray) {
				                dirPrv = (JsonArray) oDirPrv;
				            } else {
				            	dirPrv = (JsonArray) JsonObject.parseJson(oDirPrv.toString().trim());
				            }
				            
				            for(Object row : dirPrv) {
				            	JsonObject dirOne = null;
				            	if(row instanceof JsonObject) dirOne = (JsonObject) row;
				            	else                          dirOne = (JsonObject) JsonObject.parseJson(row.toString().trim());
				            	
				            	try {
				            		String dPath = dirOne.get("path"     ).toString();
				            		String dPrv  = dirOne.get("privilege").toString();
				            		
				            		if(pathParam.startsWith(dPath) || ("/" + pathParam).startsWith(dPath)) {
				            			if(dPrv.equals("edit")) {
				            				json.put("privilege", "edit");
				            				break;
				            			}
				            		}
				            	} catch(Throwable t) {
				            		System.out.println("Wrong account configuration - " + t.getMessage());
				            	}
				            }
					    }
					} else {
						json.put("privilege", "edit");
					}
				}
			}
			if(dirPrv == null) dirPrv = new JsonArray();
			
			Object oHiddenDir = conf.get("HiddenDirs");
			List<String> hiddenDirList = new ArrayList<String>();
			
			if(oHiddenDir != null) {
				JsonArray hiddenDir = null;
				if(oHiddenDir instanceof JsonArray) hiddenDir = (JsonArray) oHiddenDir;
				else                                hiddenDir = (JsonArray) JsonObject.parseJson(oHiddenDir.toString().trim());
				oHiddenDir = null;
				
				if(idtype.equals("A")) {
					hiddenDirList.clear();
				} else {
					if(hiddenDir != null) {
						for(Object obj : hiddenDir) {
							if(obj == null) continue;
							hiddenDirList.add(obj.toString().trim());
						}
					}
					
					for(Object row : dirPrv) {
		            	JsonObject dirOne = null;
		            	if(row instanceof JsonObject) dirOne = (JsonObject) row;
		            	else                          dirOne = (JsonObject) JsonObject.parseJson(row.toString().trim());
		            	
		            	try {
		            		String dPath = dirOne.get("path"     ).toString();
		            		String dPrv  = dirOne.get("privilege").toString();
		            		
		            		int hdx=0;
		            		while(hdx < hiddenDirList.size()) {
		            			String hiddenDirOne = hiddenDirList.get(hdx);
		            			if(hiddenDirOne.startsWith(dPath) || ("/" + hiddenDirOne).startsWith(dPath)) {
			            			if(dPrv.equals("view") || dPrv.equals("edit")) {
			            				hiddenDirList.remove(hdx);
			            				continue;
			            			}
			            		}
		            			hdx++;
		            		}
		            	} catch(Throwable t) {
		            		System.out.println("Wrong account configuration - " + t.getMessage());
		            	}
		            }
				}
			}

			JsonArray dirs = new JsonArray();
			for(File f : chDirs) {
				String name = f.getName();
			    if(! keyword.equals("")) { if(! name.contains(keyword)) continue; }
			    if(name.equals(".garbage")) continue;
			    if(name.equals(".upload" )) continue;
			    
				String linkDisp = f.getCanonicalPath().replace(instance.rootPath.getCanonicalPath(), "").replace("\\", "/").replace("'", "").replace("\"", "");
			    if(linkDisp.indexOf(".") >= 0) continue;
			    if(linkDisp.indexOf("/") == 0) linkDisp = linkDisp.substring(1);
			    
			    for(String h : hiddenDirList) {
			    	if(linkDisp.startsWith(h)) continue;
			    	if(("/" + linkDisp).startsWith(h)) continue;
			    }
			    
			    JsonObject child = new JsonObject();
			    child.put("type", "dir");
			    child.put("name", name);
			    child.put("value", linkDisp);
			    try { child.put("elements", f.list().length); } catch(Exception ignores) { }
			    dirs.add(child);
			}
			json.put("directories", dirs);
			dirs = null;

			JsonArray files = new JsonArray();
			for(File f : chFiles) {
				String name     = f.getName();
			    if(! keyword.equals("")) { if(! name.toLowerCase().contains(keyword.toLowerCase())) continue; }
			    
			    String linkDisp = name.replace("\"", "'");
			    
			    JsonObject fileOne = new JsonObject();
			    
			    fileOne.put("type", "file");
			    fileOne.put("name", linkDisp);
			    fileOne.put("size", FSUtils.getFileSize(f));
			    
			    files.add(fileOne);
			}
			json.put("files", files);
			if(json.get("privilege") == null) json.put("privilege", "view");
		} catch(Throwable t) {
			t.printStackTrace();
			if(jsonSess != null) jsonSess.clear();
		}
		json.put("session", jsonSess);
		
		return json;
	}
	
	public Map<File, String> getIcons(List<File> files, Color background) {
		Map<File, String> iconMap = new HashMap<File, String>();
		
		for(File f : files) {
			Icon icon = FileSystemView.getFileSystemView().getSystemIcon(f);
			BufferedImage img = null;
			Graphics2D g = null;
			try {
				// Icon to BufferedImage
				int w = icon.getIconWidth();
	            int h = icon.getIconHeight();
	            int gp = 2;
	            int imgWidth  = w + (gp * 2);
	            int imgHeight = h + (gp * 2);
	            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	            GraphicsDevice gd = ge.getDefaultScreenDevice();
	            GraphicsConfiguration gc = gd.getDefaultConfiguration();
	            img = gc.createCompatibleImage(imgWidth, imgHeight);
	            g = img.createGraphics();
	            g.setColor(background);
	            g.fillRect(0, 0, imgWidth, imgHeight);
	            icon.paintIcon(null, g, gp, gp);
	            g.dispose(); g = null;
	            ge = null;
	            gd = null;
	            gc = null;
	            
				ByteArrayOutputStream collector = new ByteArrayOutputStream();
				ImageIO.write(img, "png", collector);
				img = null;
				
				String binary = SecurityUtil.base64String(collector.toByteArray());
				collector = null;
				
				iconMap.put(f, "data:image/png;base64, " + binary);
			} catch(Throwable t) {
				t.printStackTrace();
				continue;
			} finally {
				if(g != null) g.dispose();
			}
		}
		
		return iconMap;
	}
	
	public String createCaptchaBase64(HttpServletRequest request, String key, String code, long time, double scale, String theme) {
		initialize(request);
		
		try {
			boolean captDarkMode  = false;
			if(theme != null) {
				if(theme.equals("dark")) captDarkMode =  true;
			}

			if(code == null) {
			    code = "REFRESH";
			}
			
			long now = System.currentTimeMillis();

			if(now - time >= captchaLimitTime) {
			    code = "REFRESH";
			    request.getSession().setAttribute(key + "_captcha_code", code);
			}

			int colorPad = 0;
			if(captDarkMode) colorPad = 100;
			
			int captchaWidth     = (int) (this.captchaWidth    * scale);
			int captchaHeight    = (int) (this.captchaHeight   * scale);
			int captchaFontSize  = (int) (this.captchaFontSize * scale);

			BufferedImage image    = new BufferedImage(captchaWidth, captchaHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D    graphics = image.createGraphics();

			if(captDarkMode) graphics.setColor(new Color(0, 0, 0));
			else graphics.setColor(new Color(250, 250, 250));
			graphics.fillRect(0, 0, captchaWidth, captchaHeight);

			FontMetrics metrics = graphics.getFontMetrics();
			int fontWidth = metrics.stringWidth(code);
			int gap       = (captchaWidth - fontWidth) / (code.length() + 1);
			int x         = gap;
			int y         = (captchaHeight - metrics.getHeight()) / 2 + metrics.getAscent();

			Font font = new Font("Serif", Font.BOLD, captchaFontSize);

			// 방해물 출력
			for(int ndx=0; ndx<captchaNoises; ndx++) {
			    int x1, y1, x2, y2;
			    x1 = (int) (Math.random() * captchaWidth);
			    y1 = (int) (Math.random() * captchaHeight);
			    x2 = x1 + (int) (Math.random() * (captchaWidth  / 2));
			    y2 = y1 + (int) (Math.random() * (captchaHeight / 2));
			    graphics.setColor(new Color( (colorPad + (int) (Math.random() * 120)), (colorPad + (int) (Math.random() * 120)), (colorPad + (int) (Math.random() * 120))  ));
			    graphics.drawLine(x1, y1, x2, y2);
			}

			// 글자 출력
			for(int idx=0; idx<code.length(); idx++) {
			    char charNow = code.charAt(idx);
			    graphics.setColor(new Color( (colorPad + (int) (Math.random() * 120)), (colorPad + (int) (Math.random() * 120)), (colorPad + (int) (Math.random() * 120))  ));
			    
			    int nowX = x + metrics.charWidth(charNow) / 2;
			    int ang  = ((int) (Math.random() * 41)) - 20;
			    
			    graphics.rotate(Math.toRadians(ang), nowX, y);
			    graphics.setFont(font.deriveFont(Font.BOLD, captchaFontSize + ((int) Math.random() * 4) - 2));
			    graphics.drawString(String.valueOf(charNow), nowX, y + ((int) ((Math.random() * captchaHeight) / 3.0)));
			    graphics.rotate(Math.toRadians(ang) * (-1), nowX, y);
			    
			    x += metrics.charWidth(charNow) + gap;
			}

			ByteArrayOutputStream binary = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", binary);
			image    = null;
			graphics = null;

			String bs64str = SecurityUtil.base64String(binary.toByteArray());
			binary = null;
			return bs64str;
		} catch(Throwable t) {
			t.printStackTrace();
			return "Error : " + t.getMessage();
		}
	}
	
	public String upload(HttpServletRequest request) {
		initialize(request);
		
		String uIdType = "", msg = "";
		JsonArray dirPrv = null;
		try {
			JsonObject sessionMap = getSessionObject(request);
			
		    if(sessionMap != null) {
	            // uId     = sessionMap.get("id"    ).toString();
	            uIdType = sessionMap.get("idtype").toString();
	            
	            Object oDirPrv = (Object) sessionMap.get("privileges");
	            if(oDirPrv != null) {
	                if(oDirPrv instanceof JsonArray) {
	                    dirPrv = (JsonArray) oDirPrv;
	                } else {
	                    dirPrv = (JsonArray) JsonObject.parseJson(oDirPrv.toString().trim());
	                }
	            }
	        }
		    
		    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		    File dest = new File(uploadd.getCanonicalPath() + File.separator + dateFormat.format(new java.util.Date(System.currentTimeMillis())));
		    if(! dest.exists()) dest.mkdirs();
		    
		    int maxSize = Integer.MAX_VALUE;
		    if(limitSize * 1024L < maxSize) maxSize = (int) (limitSize * 1024L); 
		    MultipartRequest mReq = new MultipartRequest(request, dest.getCanonicalPath(), maxSize, cs, new DefaultFileRenamePolicy());
		    
		    String pathParam = mReq.getParameter("path");
		    
		    if(! uIdType.equals("A")) {
		    	if(dirPrv == null) throw new RuntimeException("No privilege");
		    	
		    	boolean hasPriv = false;
		    	for(Object row : dirPrv) {
		    		JsonObject dirOne = null;
		            if(row instanceof JsonObject) dirOne = (JsonObject) row;
		            else                          dirOne = (JsonObject) JsonObject.parseJson(row.toString().trim());
		            
		            try {
		                String dPath = dirOne.get("path"     ).toString();
		                String dPrv  = dirOne.get("privilege").toString();
		                
		                if(pathParam.startsWith(dPath) || ("/" + pathParam).startsWith(dPath)) {
		                    if(dPrv.equals("edit")) {
		                    	hasPriv = true;
		                        break;
		                    }
		                }
		            } catch(Throwable t) {
		                System.out.println("Wrong account configuration - " + t.getMessage());
		            }
		    	}
		    	dirPrv.clear();
		    	if(! hasPriv) throw new RuntimeException("No privilege");
		    }
		    if(dirPrv != null) dirPrv.clear();
		    
		    Enumeration<?> files = mReq.getFileNames();
		    while(files.hasMoreElements()) {
		    	String fileElem = files.nextElement().toString();
		    	
		    	String fileName = mReq.getFilesystemName(fileElem);
		    	String fileOrig = mReq.getOriginalFileName(fileElem);
		    	
		    	File fileOne = new File(dest.getCanonicalPath() + File.separator + fileName);
		    	File destFil = new File(rootPath.getCanonicalPath() + File.separator + pathParam.replace("/", File.separator) + File.separator + fileOrig);
		    	
		    	if(destFil.exists()) {
		    		int dupx = 0;
		    		while(destFil.exists()) {
		    			dupx++;
		    			destFil = new File(rootPath.getCanonicalPath() + File.separator + pathParam.replace("/", File.separator) + File.separator + fileOrig + "." + dupx);
		    		}
		    	}
		    	
		    	if(fileOne.exists()) {
		    		fileOne.renameTo(destFil);
		    	} else {
		    		System.out.println("Upload complete but cannot move the file from temp directory to destination !");
		    		System.out.println("File : " + fileOne.getCanonicalPath());
		    		System.out.println("Dest : " + destFil.getCanonicalPath());
		    		System.out.println("COS File System Name : " + fileName);
		    		System.out.println("COS Original Name : " + fileOrig);
		    	}
		    }
		    msg = "Success !";
		} catch(Throwable t) {
		    t.printStackTrace();
		    msg = "Error : " + t.getMessage();
		}

		msg = msg.replace("<", "&lt;").replace(">", "&gt;");
		return msg;
	}
	
	public String download(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		request.setCharacterEncoding("UTF-8");
		
		long now = System.currentTimeMillis();
		StringBuilder results = new StringBuilder("");
		
		String clients = request.getHeader("User-Agent");

		String pathParam = request.getParameter("path");
		String fileName  = request.getParameter("filename");
		String speed     = request.getParameter("speed");
		String mode      = request.getParameter("mode");

		String capt      = request.getParameter("captcha");
		String code      = (String) request.getSession().getAttribute("fsd_captcha_code");
		Long   time      = (Long)   request.getSession().getAttribute("fsd_captcha_time");

		if(code == null) code = "REFRESH";
		if(capt == null) capt = "";
		if(mode == null) mode = "DOWNLOAD";
		mode = mode.trim().toUpperCase();

		if(pathParam == null) pathParam = "";
		pathParam = pathParam.trim();
		if(pathParam.equals("/")) pathParam = "";
		pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
		if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);

		if(speed != null) {
			speed = speed.trim();
			if(speed.equalsIgnoreCase("andante")) {
				sleepRoutine = sleepRoutine / 10;
			} else if(speed.equalsIgnoreCase("slow")) {
		    	sleepRoutine = sleepRoutine / 5;
		    } else if(speed.equalsIgnoreCase("")) {
		        sleepRoutine = sleepRoutine / 2;
		    }
		}

		FileInputStream fIn = null;
		OutputStream outputs = null;
		File file = null;
		byte[] buffers = new byte[bufferSize];
		try {
			if(captchaDownload) {
				if(code.equals("SKIP")) {
					capt = code;
					time = new Long(now);
				}
				
				if(! code.equals(capt)) {
					throw new RuntimeException("Wrong captcha code !");
			    }
				
				if(now - time.longValue() >= captchaLimitTime) {
				    code = "REFRESH";
				    request.getSession().setAttribute("fsd_captcha_code", code);
				}
				
			    if(code.equals("REFRESH")) {
			    	throw new RuntimeException("Too old captcha code !");
			    }
			    
			    if(code.equals("SKIP")) {
			    	code = "REFRESH";
				    request.getSession().setAttribute("fsd_captcha_code", code);
			    }
			}
			
			Object oHiddenDir = conf.get("HiddenDirs");
			List<String> hiddenDirList = new ArrayList<String>();
			
			if(oHiddenDir != null) {
				JsonObject jsonSess = getSessionObject(request);
				String idtype = "U";
				if(jsonSess != null) {
					JsonArray dirPrv = null;
					if(jsonSess.get("idtype") != null) {
						idtype = jsonSess.get("idtype").toString();
						if(! idtype.equals("A")) {
							Object oDirPrv = (Object) jsonSess.get("privileges");
						    if(oDirPrv != null) {
						    	dirPrv = null;
					            if(oDirPrv instanceof JsonArray) {
					                dirPrv = (JsonArray) oDirPrv;
					            } else {
					            	dirPrv = (JsonArray) JsonObject.parseJson(oDirPrv.toString().trim());
					            }
						    }
						}
					}
					if(dirPrv == null) dirPrv = new JsonArray();
					
					JsonArray hiddenDir = null;
					if(oHiddenDir instanceof JsonArray) hiddenDir = (JsonArray) oHiddenDir;
					else                                hiddenDir = (JsonArray) JsonObject.parseJson(oHiddenDir.toString().trim());
					oHiddenDir = null;
					
					if(idtype.equals("A")) {
						hiddenDirList.clear();
					} else {
						if(hiddenDir != null) {
							for(Object obj : hiddenDir) {
								hiddenDirList.add(obj.toString().trim());
							}
						}
						
						for(Object row : dirPrv) {
			            	JsonObject dirOne = null;
			            	if(row instanceof JsonObject) dirOne = (JsonObject) row;
			            	else                          dirOne = (JsonObject) JsonObject.parseJson(row.toString().trim());
			            	
			            	try {
			            		String dPath = dirOne.get("path"     ).toString();
			            		String dPrv  = dirOne.get("privilege").toString();
			            		
			            		int hdx=0;
			            		while(hdx < hiddenDirList.size()) {
			            			String hiddenDirOne = hiddenDirList.get(hdx);
			            			if(hiddenDirOne.startsWith(dPath) || ("/" + hiddenDirOne).startsWith(dPath)) {
				            			if(dPrv.equals("view") || dPrv.equals("edit")) {
				            				hiddenDirList.remove(hdx);
				            				continue;
				            			}
				            		}
			            			hdx++;
			            		}
			            	} catch(Throwable t) {
			            		System.out.println("Wrong account configuration - " + t.getMessage());
			            	}
			            }
					}
				}
			}
			
			for(String h : hiddenDirList) {
		    	if(pathParam.startsWith(h))         throw new RuntimeException("No privilege");
		    	if(("/" + pathParam).startsWith(h)) throw new RuntimeException("No privilege");
		    }
			
		    if(fileName == null || fileName.equals("")) {
		        throw new FileNotFoundException("File name is needed.");
		    }
		    fileName = fileName.replace("/", "").replace("\\", "").replace("..", "");

		    file = new File(rootPath.getCanonicalPath() + File.separator + pathParam.replace("/", File.separator) + File.separator + fileName);
		    if(! file.exists()) {
		    	System.out.println("No File ! " + file.getCanonicalPath() + " <-- " + rootPath.getCanonicalPath() + File.separator + pathParam.replace("/", File.separator) + File.separator + fileName);
		        throw new FileNotFoundException("There is no file !");
		    }
		    if(file.isDirectory()) {
		        throw new FileNotFoundException("Cannot download directory !");
		    }

		    long dataLength = file.length();
		    if(dataLength / 1024 >= limitSize) throw new RuntimeException("File size is bigger than limits.");
		    
		    String contentType = "application/octet-stream";
		    String contentDisp = "attachment";
		    
			response.reset();
			
			if(mode.equals("DOWNLOAD")) {
				contentDisp = "attachment";
				contentType = "application/octet-stream";
			} else if(mode.equals("VIEWER") || mode.equals("VIEW")) {
				// Get Extension of file
				StringTokenizer dotTokenizer = new StringTokenizer(fileName, ".");
				String ext = "";
				if(dotTokenizer.countTokens() >= 2) {
					while(dotTokenizer.hasMoreTokens()) {
						ext = dotTokenizer.nextToken();
					}
				}
				ext = ext.trim().toLowerCase();
				
				contentType = "application/octet-stream";
				
				if(ext.equals("jpg") || ext.equals("jpeg")) contentType = "image/jpeg";
				if(ext.equals("png"))  contentType = "image/png";
				if(ext.equals("gif"))  contentType = "image/gif";
				if(ext.equals("pdf"))  contentType = "application/pdf";
				if(ext.equals("rtf"))  contentType = "application/rtf";
				if(ext.equals("ppt"))  contentType = "application/vnd.ms-powerpoint";
				if(ext.equals("xls"))  contentType = "application/vnd.ms-excel";
				if(ext.equals("doc"))  contentType = "application/msword";
				if(ext.equals("pptx")) contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
				if(ext.equals("xlsx")) contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
				if(ext.equals("docx")) contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
				if(ext.equals("odt"))  contentType = "application/vnd.oasis.opendocument.text";
				if(ext.equals("ods"))  contentType = "application/vnd.oasis.opendocument.spreadsheet";
				if(ext.equals("odp"))  contentType = "application/vnd.oasis.opendocument.presentation";
				if(ext.equals("epub")) contentType = "application/epub+zip";
				if(ext.equals("wav"))  contentType = "audio/wav";
				if(ext.equals("mp3"))  contentType = "audio/mpeg";
				if(ext.equals("mp4"))  contentType = "video/mp4";
				if(ext.equals("mpeg")) contentType = "video/mpeg";
				if(ext.equals("avi"))  contentType = "video/x-msvideo";
				
				if(! contentType.equals("application/octet-stream")) contentDisp = "inline";
			} else {
				contentDisp = "attachment";
				contentType = "application/octet-stream";
			}
			
			response.setContentType(contentType);
			response.setHeader("Content-Description", "Download Broker");
			response.setHeader("Content-Length", dataLength + "");
			
			if(clients.indexOf("MSIE") >= 0) {
				response.setHeader("Content-Disposition", contentDisp + "; filename=\"" + fileName + "\"");
			} else {
				response.setHeader("Content-Type", contentType + "; charset=utf-8");
				response.setHeader("Content-Disposition", contentDisp + "; filename=" + fileName);
			}
			
			outputs = response.getOutputStream();
			
			int sleepCnt = sleepRoutine;
		    fIn = new FileInputStream(file);
			while(true) {
				int readLengths = fIn.read(buffers, 0, bufferSize);
				if(readLengths < 0) break;
				outputs.write(buffers, 0, readLengths);
				
				sleepCnt--;
				if(sleepCnt <= 0) {
					sleepCnt = sleepRoutine;
					Thread.sleep(sleepGap);
				}
			}
			
			try { fIn.close();       } catch(Throwable te) {}
		    try { outputs.close();   } catch(Throwable te) {}
		    return null;
		} catch(Throwable tx) {
			System.out.println("Exception message while sending file : " + tx.getMessage());
			if((tx instanceof java.net.SocketException) && tx.getMessage().indexOf("socket write error") >= 0) {
				// NOTHING
			} else {
				System.out.println("Exception message while sending file : " + tx.getMessage());
				response.reset();
				response.setContentType("text/html;charset=UTF-8");
				
				results = results.append("<pre>").append("Error : ").append(tx.getMessage()).append("</pre>");
			}
			
			/*
			StackTraceElement[] elements = tx.getStackTrace();
			for(StackTraceElement e : elements) {
				out.println(" at " + e);
			}
			*/
		} finally {
			if(fIn     != null) { try { fIn.close();       } catch(Throwable te) {}}
			if(outputs != null) { try { outputs.close();   } catch(Throwable te) {}}
		}
		return results.toString();
	}
	
	public JsonObject mkdir(HttpServletRequest request) {
		JsonObject json = new JsonObject();
		String uIdType = "";
		JsonArray dirPrv = null;
		try {
			JsonObject sessionMap = getSessionObject(request);
			
		    if(sessionMap != null) {
	        	// uId     = sessionMap.get("id"    ).toString();
	        	uIdType = sessionMap.get("idtype").toString();
	        	
	        	Object oDirPrv = (Object) sessionMap.get("privileges");
	            if(oDirPrv != null) {
	                if(oDirPrv instanceof JsonArray) {
	                    dirPrv = (JsonArray) oDirPrv;
	                } else {
	                    dirPrv = (JsonArray) JsonObject.parseJson(oDirPrv.toString().trim());
	                }
	            }
	        }
		} catch(Throwable t) {
		    t.printStackTrace();
		}

		json.put("success", new Boolean(false));
		json.put("message", "");
		
		try {
			String pathParam = request.getParameter("path");
		    if(pathParam == null) pathParam = "";
		    pathParam = pathParam.trim();
		    if(pathParam.equals("/")) pathParam = "";
		    pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
		    if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);
		    
			boolean hasPriv = false;
			if(uIdType.equals("A")) hasPriv = true;
			
			if(! hasPriv) {
		        for(Object row : dirPrv) {
		            JsonObject dirOne = null;
		            if(row instanceof JsonObject) dirOne = (JsonObject) row;
		            else                          dirOne = (JsonObject) JsonObject.parseJson(row.toString().trim());
		            
		            try {
		                String dPath = dirOne.get("path"     ).toString();
		                String dPrv  = dirOne.get("privilege").toString();
		                
		                if(pathParam.startsWith(dPath) || ("/" + pathParam).startsWith(dPath)) {
		                    if(dPrv.equals("edit")) {
		                        hasPriv = true;
		                        break;
		                    }
		                }
		            } catch(Throwable t) {
		                System.out.println("Wrong account configuration - " + t.getMessage());
		            }
		        }
			}
			if(dirPrv != null) dirPrv.clear();
			
			if(hasPriv) {
			    String dirName = request.getParameter("name");
			    dirName = FSUtils.removeSpecials(dirName, false, true, true, true, true);
			    
			    File file = new File(rootPath.getCanonicalPath() + File.separator + pathParam.replace("/", File.separator) + File.separator + dirName);
			    file.mkdirs();
			    json.put("success", new Boolean(true));
			} else {
				json.put("success", new Boolean(false));
		        json.put("message", "No privilege");
			}
		} catch(Throwable t) {
			t.printStackTrace();
			json.put("success", new Boolean(false));
			json.put("message", "Error : " + t.getMessage());
		}
		return json;
	}
	
	public JsonObject remove(HttpServletRequest request) {
		JsonObject json = new JsonObject();
		String uIdType = "";
		JsonArray dirPrv = null;
		try {
			JsonObject sessionMap = getSessionObject(request);
			
		    if(sessionMap != null) {
	        	// uId     = sessionMap.get("id"    ).toString();
	        	uIdType = sessionMap.get("idtype").toString();
	        	
	        	Object oDirPrv = (Object) sessionMap.get("privileges");
	            if(oDirPrv != null) {
	                if(oDirPrv instanceof JsonArray) {
	                    dirPrv = (JsonArray) oDirPrv;
	                } else {
	                    dirPrv = (JsonArray) JsonObject.parseJson(oDirPrv.toString().trim());
	                }
	            }
	        }
		} catch(Throwable t) {
		    t.printStackTrace();
		}

		json.put("success", new Boolean(false));
		json.put("message", "");

		try {
			String pathParam = request.getParameter("path");
		    if(pathParam == null) pathParam = "";
		    pathParam = pathParam.trim();
		    if(pathParam.equals("/")) pathParam = "";
		    pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
		    if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);
		    
		    String delType = request.getParameter("dels");
		    if(delType == null) delType = "file";
			
			boolean hasPriv = false;
			if(uIdType.equals("A")) hasPriv = true;
			
			if(! hasPriv) {
		        for(Object row : dirPrv) {
		            JsonObject dirOne = null;
		            if(row instanceof JsonObject) dirOne = (JsonObject) row;
		            else                          dirOne = (JsonObject) JsonObject.parseJson(row.toString().trim());
		            
		            try {
		                String dPath = dirOne.get("path"     ).toString();
		                String dPrv  = dirOne.get("privilege").toString();
		                
		                if(pathParam.startsWith(dPath) || ("/" + pathParam).startsWith(dPath)) {
		                    if(dPrv.equals("edit")) {
		                        hasPriv = true;
		                        break;
		                    }
		                }
		            } catch(Throwable t) {
		                System.out.println("Wrong account configuration - " + t.getMessage());
		            }
		        }
			}
			if(dirPrv != null) dirPrv.clear();
			
			if(hasPriv) {
			    String fileName = request.getParameter("name");
			    File file;
			    
			    if(delType.equals("dir")) file = new File(rootPath.getCanonicalPath() + File.separator + pathParam.replace("/", File.separator));
			    else                      file = new File(rootPath.getCanonicalPath() + File.separator + pathParam.replace("/", File.separator) + File.separator + fileName);
			    
			    if(! file.exists()) {
			    	System.out.println("There is no file ! " + file.getCanonicalPath());
			        throw new FileNotFoundException("There is no file !");
			    }
			    
			    if(file.isDirectory()) {
			    	if(! delType.equals("dir")) throw new RuntimeException("Wrong parameter. Please reload the page !");
			    	
			    	File[] children = file.listFiles();
			    	if(children.length >= 1) throw new FileNotFoundException("Cannot delete non-empty directory !");
			    } else {
			    	if(! delType.equals("file")) throw new RuntimeException("Wrong parameter. Please reload the page !");
			    }
			    
			    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			    File dest = new File(garbage.getCanonicalPath() + File.separator + dateFormat.format(new java.util.Date(System.currentTimeMillis())));
			    if(! dest.exists()) dest.mkdirs();

			    File fdest = new File(dest.getCanonicalPath() + File.separator + file.getName());
			    if(fdest.exists()) {
			    	int index = 0;
			    	while(fdest.exists()) {
			    		index++;
			    		fdest = new File(dest.getCanonicalPath() + File.separator + file.getName() + "." + index);
			    	}
			    }
			    file.renameTo(fdest);
			    json.put("success", new Boolean(true));
			} else {
				json.put("success", new Boolean(false));
		        json.put("message", "No privilege");
			}
		} catch(Throwable t) {
			t.printStackTrace();
			json.put("success", new Boolean(false));
			json.put("message", "Error : " + t.getMessage());
		}
		return json;
	}
	
	public JsonObject account(HttpServletRequest request) throws IOException {
		JsonObject sessionMap = null;
		JsonObject accountOne = null;
		boolean needInvalidate = false;
		try {
			String sessionJson = (String) request.getSession().getAttribute("fssession");
		    
		    if(sessionJson != null) {
		    	sessionMap = (JsonObject) JsonObject.parseJson(sessionJson.trim());
		    	
		    	if(sessionMap != null) { if(sessionMap.get("id"    ) == null) sessionMap = null;         }
		        if(sessionMap != null) { if(sessionMap.get("idtype") == null) sessionMap = null;         }
		        if(sessionMap != null) { if(sessionMap.get("nick"  ) == null) sessionMap = null;         }
		        if(sessionMap != null) { if(sessionMap.get("idtype").equals("block")) sessionMap = null; } 
		        if(sessionMap == null) needInvalidate = true;
		    }
		} catch(Throwable t) {
			needInvalidate = true;
		    t.printStackTrace();
		}
		long now = System.currentTimeMillis();
		String req = request.getParameter("req");
		if(req == null) req = "status";
		req = req.trim().toLowerCase();

		JsonObject json = new JsonObject();
		json.put("success", new Boolean(false));
		json.put("message", "");
		
		String lang = getLanguage(request);

		FileInputStream fIn = null;
		Reader r1 = null, r2 = null;
		FileOutputStream fOut = null;
		try {
			String msg = "";
			
			if(! installed) {
				if(lang.equals("ko")) throw new RuntimeException("FS 설치를 먼저 진행해 주세요.");
				else                  throw new RuntimeException("Please install first !");
			}
			
			if(fileConfigPath == null) {
				if(lang.equals("ko")) throw new RuntimeException("FS 설치를 먼저 진행해 주세요.");
				else                  throw new RuntimeException("Please install first !");
			}
			
			File faJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "accounts");
			if(! faJson.exists()) faJson.mkdirs();
			
			if(req.equals("status")) {
				json.put("success", new Boolean(true));
			}
			
			if(req.equals("language")) {
				String tlng = request.getParameter("language");
				String frce = request.getParameter("force");
				if(tlng == null) tlng = "";
				tlng = FSUtils.removeSpecials(tlng);
				
				if(frce == null) frce = "false";
				
				boolean applys = false;
				if(request.getSession().getAttribute("fslanguage") != null) {
					if(Boolean.parseBoolean(frce)) {
						applys = true;
					}
				} else {
					applys = true;
				}
				
				if(applys) {
					if(tlng.equals("")) {
						request.getSession().removeAttribute("fslanguage");
					} else {
						request.getSession().setAttribute("fslanguage", tlng);
					}
				}
				
				json.put("success", new Boolean(true));
			}
			
			if(req.equals("logout")) {
			    sessionMap = null;
			    needInvalidate = true;
			    if(lang.equals("ko")) msg = "로그아웃 되었습니다."; 
			    else                  msg = "Log out complete";
			    System.out.println("Session log out from " + request.getRemoteAddr());
			    json.put("success", new Boolean(true));
			}
			
			if(req.equals("login")) {
				if(sessionMap != null) needInvalidate = true;
				sessionMap = null;
				
				if(noLogin) {
					if(lang.equals("ko")) throw new RuntimeException("계정 기능이 비활성화되어 있습니다.");
					else                  throw new RuntimeException("No-Login Mode !");
		        }
				
				String id = request.getParameter("id");
				String pw = request.getParameter("pw");
				
				if(id == null) id = "";
				if(pw == null) pw = "";
				
				id = id.trim(); pw = pw.trim();
				
				if(id.equals("")) {
					if(lang.equals("ko")) msg = "ID를 입력해 주세요.";
					else                  msg = "Please input ID !";
				}
				
				if(id.contains("'") || id.contains("\"") || id.contains("/") || id.contains("\\") || id.contains(File.separator) || id.contains(".") || id.contains(" ") || id.contains("\n") || id.contains("\t")) throw new RuntimeException("ID can only contains alphabets and numbers !");
				if(msg.equals("")) { 
					if(pw.equals("")) {
						if(lang.equals("ko")) msg = "암호를 입력해 주세요.";
						else                  msg = "Please input Password !";
					}
				}
				
				if(captchaLogin) {
					String ccapt = request.getParameter("captcha");
					String ccode = (String) request.getSession().getAttribute("fsl_captcha_code");
					Long   ctime = (Long)   request.getSession().getAttribute("fsl_captcha_time");
					
					if(ccode == null) ccode = "REFRESH";
					if(ccapt == null) ccapt = "";
					
					if(! ccode.equals(ccapt)) {
						if(lang.equals("ko")) throw new RuntimeException("Wrong captcha code !");
						else                  throw new RuntimeException("코드를 올바르게 입력해 주세요.");
				    }
					
					if(now - ctime.longValue() >= captchaLimitTime) {
					    ccode = "REFRESH";
					    request.getSession().setAttribute("fsl_captcha_code", ccode);
					}
					
				    if(ccode.equals("REFRESH")) {
				        if(lang.equals("ko")) throw new RuntimeException("Too old captcha code !");
						else                  throw new RuntimeException("코드가 오래되었습니다. 새로 고침 후 이용해 주세요.");
				    }
				}
				
				System.out.println("Login requested ! " + id + " at " + now + " from " + request.getRemoteAddr());
				
				if(msg.equals("")) {
					File fileAcc = new File(faJson.getCanonicalPath() + File.separator + id + ".json");
					fileAcc.getCanonicalPath(); // Check valid
				    if(fileAcc.exists()) {
				        StringBuilder lineCollector = new StringBuilder("");
				        String line;
				        
				        fIn = new FileInputStream(fileAcc);
				        r1 = new InputStreamReader(fIn, cs);
				        r2 = new BufferedReader(r1);
				        while(true) {
				            line = ((BufferedReader) r2).readLine();
				            if(line == null) break;
				            lineCollector = lineCollector.append("\n").append(line);
				        }
				        r2.close(); r2 = null;
				        r1.close(); r1 = null;
				        fIn.close(); fIn = null;
				        
				        accountOne = (JsonObject) JsonObject.parseJson(lineCollector.toString().trim());
				        lineCollector.setLength(0);
				        lineCollector = null;
				    }
					
					if(accountOne == null) {
						msg = "No account found !";
					} else if(accountOne.get("id") == null) {
						msg = "No account found !";
					} else if(! accountOne.get("id").toString().trim().equals(id)) {
						msg = "No account found !";
					}
				}
				
				if(accountOne != null) {
					int  failCnt  = 0;
			        long failTime = 0L;
			        
			        if(msg.equals("")) {
			            Object objFailCnt = accountOne.get("fail_cnt");
			            String strFailCnt = null;
			            if(objFailCnt == null) strFailCnt = "0";
			            else strFailCnt = objFailCnt.toString().trim();
			            if(strFailCnt.equals("")) strFailCnt = "0";
			            objFailCnt = null;
			            failCnt = Integer.parseInt(strFailCnt);
			            
			            Object objFailTime = accountOne.get("fail_time");
			            String strFailTime = null;
			            if(objFailTime == null) strFailTime = "0";
			            else strFailTime = objFailTime.toString().trim();
			            if(strFailTime.equals("")) strFailTime = "0";
			            objFailTime = null;
			            failTime = Long.parseLong(strFailTime);
			            
			            if(now - failTime >= 1000L * 60 * loginFailOverMinute) failCnt = 0;
			            if(failCnt >= loginFailCountLimit) {
			            	if(lang.equals("ko")) msg = "지금 로그인할 수 없습니다. 잠시 후 다시 시도해 주세요.";
			            	else                  msg = "Cannot login now ! Please try later.";
			            }
			        }
			        
			        if(msg.equals("")) {
			            String s1, s2, s3;
			            s1 = conf.get("S1").toString();
			            s2 = conf.get("S2").toString();
			            s3 = conf.get("S3").toString();
			            String pwInput = SecurityUtil.hash(s1 + pw + s2 + salt + id + s3, "SHA-256");
			            
			            if(! accountOne.get("pw").equals(pwInput)) {
			                if(! accountOne.get("pw").equals(pw)) {
			                    failCnt++;
			                    failTime = now;
			                    accountOne.put("fail_cnt", ""  + failCnt);
			                    accountOne.put("fail_time", "" + failTime);
			                    
			                    if(accChanging) {
			                    	long loops = 0L;
			                    	while(accChanging) {
			                    		loops++;
			                    		if(!accChanging) break;
			                    		if(loops >= 10000L) {
			                    			if(lang.equals("ko")) throw new RuntimeException("서버가 아직 로그인 처리를 할 수 없습니다. 잠시 후 다시 시도해 주세요.");
			                    			else                  throw new RuntimeException("The server is busy. Please try later.");
			                    		}
			                    		Thread.sleep(100L);
			                    	}
			                    }
			                    
			                    accChanging = true;
			                    				
			                    File fileAcc = new File(faJson.getCanonicalPath() + File.separator + id + ".json");
			                    fOut = new FileOutputStream(fileAcc);
			                    fOut.write(accountOne.toJSON().getBytes(cs));
			                    fOut.close(); fOut = null;
			                    
			                    accChanging = false;
			                    msg = "Wrong password !";
			                }
			            }
			        }
			        
			        if(msg.equals("")) {
			        	if(failCnt >= 1) {
			        		accountOne.put("fail_cnt", "0");
			        		
			        		if(accChanging) {
		                        long loops = 0L;
		                        while(accChanging) {
		                            loops++;
		                            if(!accChanging) break;
		                            if(loops >= 10000L) {
		                            	if(lang.equals("ko")) throw new RuntimeException("서버가 아직 로그인 처리를 할 수 없습니다. 잠시 후 다시 시도해 주세요.");
		                    			else                  throw new RuntimeException("The server is busy. Please try later.");
		                            }
		                            Thread.sleep(100L);
		                        }
		                    }
			        		
			        		accChanging = true;
		                    
		                    File fileAcc = new File(faJson.getCanonicalPath() + File.separator + id + ".json");
		                    fOut = new FileOutputStream(fileAcc);
		                    fOut.write(accountOne.toJSON().getBytes(cs));
		                    fOut.close(); fOut = null;
		                    
		                    accChanging = false;
			        	}
			        	
			        	JsonObject accountJsonNew = (JsonObject) accountOne.cloneObject();
			        	accountJsonNew.remove("pw");
			        	
			        	sessionMap = accountOne;
			        	request.getSession().setAttribute("fssession", accountJsonNew.toJSON());
			            System.out.println("Login Accept : " + id + " at " + now);
			            needInvalidate = false;
			            json.put("success", new Boolean(true));
			        }
				}
			}
			
			json.put("type", "sessionstatus");
			json.put("logined", new Boolean((sessionMap != null)));
			
			if(sessionMap != null) {
				json.put("id"    , sessionMap.get("id"  ));
				json.put("idtype", sessionMap.get("idtype"));
				json.put("nick"  , sessionMap.get("nick"));
			}
			
			if(request.getSession().getAttribute("fslanguage") != null) json.put("language", request.getSession().getAttribute("fslanguage"));
			
			json.put("message", msg);
		} catch(Throwable t) {
			json.put("success", new Boolean(false));
		    json.put("message", "Error : " + t.getMessage());
			t.printStackTrace();
		} finally {
			if(needInvalidate) {
				request.getSession().invalidate();
				System.out.println("Session Invalidated");
				json.put("invalidated", new Boolean(true));
			}
			if(r2 != null) r2.close();
			if(r1 != null) r1.close();
			if(fIn  != null) fIn.close();
			if(fOut != null) fOut.close();
			fOut = null;
			accChanging = false;
		}
		return json;
	}
	
	public JsonObject getSessionObject(HttpServletRequest request) {
		String sessionJson = (String) request.getSession().getAttribute("fssession");
		if(sessionJson != null) {
			sessionJson = sessionJson.trim();
			if(! sessionJson.equals("")) {
				JsonObject obj = (JsonObject) JsonObject.parseJson(sessionJson);
				if(obj != null) { if(obj.get("id"    ) == null) obj = null;         }
			    if(obj != null) { if(obj.get("idtype") == null) obj = null;         }
			    if(obj != null) { if(obj.get("nick"  ) == null) obj = null;         }
			    if(obj != null) {
			    	JsonObject jsonSess = (JsonObject) obj.cloneObject();
			    	jsonSess.remove("pw");
			    	
			    	if(jsonSess.get("privgroup") == null) jsonSess.put("privgroup", new JsonArray());
			    	
			    	return jsonSess;
			    }
			}
		}
		return null;
	}
	
	public String getLanguage(HttpServletRequest request) {
    	String lang = (String) request.getSession().getAttribute("fslanguage");
    	if(lang == null) lang = "en";
    	return lang;
    }

	public JsonObject getConfig() {
		return conf;
	}

	public void setConfig(JsonObject conf) {
		this.conf = conf;
	}

	public long getConfigReadDate() {
		return confReads;
	}

	public void setConfigReadDate(long confReads) {
		this.confReads = confReads;
	}

	public boolean isConfigChanging() {
		return confChanging;
	}

	public void setConfigChanging(boolean confChanging) {
		this.confChanging = confChanging;
	}

	public boolean isAccountChanging() {
		return accChanging;
	}

	public void setAccountChanging(boolean accChanging) {
		this.accChanging = accChanging;
	}

	public boolean isInstalled() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}

	public String getCharset() {
		return cs;
	}

	public void setCharset(String cs) {
		this.cs = cs;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public File getFileConfigPath() {
		return fileConfigPath;
	}

	public void setFileConfigPath(File fileConfigPath) {
		this.fileConfigPath = fileConfigPath;
	}

	public String getStorePath() {
		return storPath;
	}

	public void setStorePath(String storPath) {
		this.storPath = storPath;
	}

	public long getRefreshConfigGap() {
		return refreshConfGap;
	}

	public void setRefreshConfigGap(long refreshConfGap) {
		this.refreshConfGap = refreshConfGap;
	}

	public boolean isReadFileIconOn() {
		return readFileIcon;
	}

	public void setReadFileIconUsage(boolean readFileIcon) {
		this.readFileIcon = readFileIcon;
	}

	public long getLimitSize() {
		return limitSize;
	}

	public void setLimitSize(long limitSize) {
		this.limitSize = limitSize;
	}

	public int getLimitCount() {
		return limitCount;
	}

	public void setLimitCount(int limitCount) {
		this.limitCount = limitCount;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public long getSleepGap() {
		return sleepGap;
	}

	public void setSleepGap(long sleepGap) {
		this.sleepGap = sleepGap;
	}

	public int getSleepRoutine() {
		return sleepRoutine;
	}

	public void setSleepRoutine(int sleepRoutine) {
		this.sleepRoutine = sleepRoutine;
	}

	public boolean isCaptchaDownloadOn() {
		return captchaDownload;
	}

	public void setCaptchaDownloadUsage(boolean captchaDownload) {
		this.captchaDownload = captchaDownload;
	}

	public boolean isCaptchaLoginOn() {
		return captchaLogin;
	}

	public void setCaptchaLoginUsage(boolean captchaLogin) {
		this.captchaLogin = captchaLogin;
	}

	public int getCaptchaWidth() {
		return captchaWidth;
	}

	public void setCaptchaWidth(int captchaWidth) {
		this.captchaWidth = captchaWidth;
	}

	public int getCaptchaHeight() {
		return captchaHeight;
	}

	public void setCaptchaHeight(int captchaHeight) {
		this.captchaHeight = captchaHeight;
	}

	public int getCaptchaFontSize() {
		return captchaFontSize;
	}

	public void setCaptchaFontSize(int captchaFontSize) {
		this.captchaFontSize = captchaFontSize;
	}

	public int getCaptchaNoises() {
		return captchaNoises;
	}

	public void setCaptchaNoises(int captchaNoises) {
		this.captchaNoises = captchaNoises;
	}

	public long getCaptchaLimitTime() {
		return captchaLimitTime;
	}

	public void setCaptchaLimitTime(long captchaLimitTime) {
		this.captchaLimitTime = captchaLimitTime;
	}

	public boolean isNoLoginMode() {
		return noLogin;
	}

	public void setNoLoginMode(boolean noLogin) {
		this.noLogin = noLogin;
	}

	public int getLoginFailCountLimit() {
		return loginFailCountLimit;
	}

	public void setLoginFailCountLimit(int loginFailCountLimit) {
		this.loginFailCountLimit = loginFailCountLimit;
	}

	public int getLoginFailOverMinute() {
		return loginFailOverMinute;
	}

	public void setLoginFailOverMinute(int loginFailOverMinute) {
		this.loginFailOverMinute = loginFailOverMinute;
	}

	public boolean isNoConsoleMode() {
		return noConsole;
	}

	public void setNoConsoleMode(boolean noConsole) {
		this.noConsole = noConsole;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public File getRootPath() {
		return rootPath;
	}

	public void setRootPath(File rootPath) {
		this.rootPath = rootPath;
	}

	public File getGarbage() {
		return garbage;
	}

	public void setGarbage(File garbage) {
		this.garbage = garbage;
	}

	public File getUploadd() {
		return uploadd;
	}

	public void setUploadd(File uploadd) {
		this.uploadd = uploadd;
	}

	public String getContextPath() {
		return ctxPath;
	}

	public void setContextPath(String ctxPath) {
		this.ctxPath = ctxPath;
	}

	public boolean isUseJDBC() {
		return useJDBC;
	}

	public void setUseJDBC(boolean useJDBC) {
		this.useJDBC = useJDBC;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public String getJdbcClass() {
		return jdbcClass;
	}

	public void setJdbcClass(String jdbcClass) {
		this.jdbcClass = jdbcClass;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getJdbcId() {
		return jdbcId;
	}

	public void setJdbcId(String jdbcId) {
		this.jdbcId = jdbcId;
	}

	public String getJdbcPw() {
		return jdbcPw;
	}

	public void setJdbcPw(String jdbcPw) {
		this.jdbcPw = jdbcPw;
	}
}
