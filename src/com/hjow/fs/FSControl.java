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
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

public class FSControl {
	public static final int[] VERSION = {1, 0, 0};
	
	private static FSControl instance = null;
	
	public JSONObject conf = new JSONObject();
	public volatile long    confReads    = 0L;
	public volatile boolean confChanging = false;
	public volatile boolean accChanging  = false;
	public boolean installed = false;
	
	public String cs = "UTF-8";

	// Title
	public String title = "File Storage";

	// Installation Status
	public File fileConfigPath = null;

	// Storage Root Directory
	public String storPath  = "/fsdir/storage/";

	public long   refreshConfGap = 4000L;

	// Download limit size (KB)
	public long   limitSize = 10 * 1024 * 1024;

	// Perform downloading buffer size (bytes)
	public int  bufferSize   = 1024 * 10;

	// Perform downloading thread's sleeping gap (milliseconds) - Downloading speed faster when this value is become lower.
	public long sleepGap     = 100;

	// Perform downloading thread's sleeping cycle (times) - Downloading speed faster when this value is become lower.
	public int  sleepRoutine = 100;

	public int  captchaWidth     = 250;
	public int  captchaHeight    = 100;
	public int  captchaFontSize  = 80;
	public long captchaLimitTime = 1000 * 60 * 5;

	// Login Policy
	public boolean noLogin = false;
	public int loginFailCountLimit = 10;
	public int loginFailOverMinute = 10;
	
	public String salt = "fs";
	public File rootPath  = null;
	public File garbage   = null;
	public File uploadd   = null;
	public String ctxPath = "";

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
	
	@SuppressWarnings("unchecked")
	protected synchronized void initialize(HttpServletRequest request) {
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
				        
				        propTest.clear();
				        propTest = null;
				        
				        if(fileConfigPath != null) {
				            // Close fs.properties
				            propIn.close();
				            propIn = null;
				            
				            // Find config.json from configuration path
				            File fJson = new File(fileConfigPath.getAbsolutePath() + File.separator + "config.json");
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
				            
				            JSONParser parser = new JSONParser();
				            conf = (JSONObject) parser.parse(lineCollection.toString().trim());
				            conf.put("S1", s1);
				            conf.put("S2", s2);
				            conf.put("S3", s3);
				            
				            lineCollection.setLength(0);
				            lineCollection = null;
				            
				            // Find accounts.json from configuration path
			                File faJson = new File(fileConfigPath.getAbsolutePath() + File.separator + "accounts");
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
					if(rd2 != null) rd2.close();
					if(rd1 != null) rd1.close();
				    if(propIn != null) propIn.close();
				    if(fileOut != null) fileOut.close();
				    if(conf == null) conf = new JSONObject();
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
				garbage = new File(rootPath.getAbsolutePath() + File.separator + ".garbage");
			    if(! garbage.exists()) garbage.mkdirs();
			    uploadd = new File(rootPath.getAbsolutePath() + File.separator + ".upload");
			    if(! uploadd.exists()) uploadd.mkdirs();
				if(conf.get("UseAccount") != null) {
					noLogin = (! Boolean.parseBoolean(conf.get("UseAccount").toString().trim()));
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
		} catch(Throwable thx) {
			thx.printStackTrace();
			throw new RuntimeException(thx.getMessage(), thx);
		}
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject install(HttpServletRequest request) throws Exception {
		JSONObject json = new JSONObject();
		if(! installed) {
			Properties   propTest = new Properties();
		    InputStream  propIn   = null;
			OutputStream fileOut  = null;
			try {
				String passwords = request.getParameter("pw");
				if(passwords == null) {
					String rex = "Please input Password !";
					if(getLanguage(request).equals("ko")) rex = "비밀번호를 입력해 주세요.";
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
		        
		        String titles = request.getParameter("title");
		        if(titles == null) titles = "File Storage";
		        titles = titles.trim();
		        if(titles.equals("")) titles = "File Storage";
		        
				String roots = request.getParameter("rootdir");
				if(roots == null) {
					throw new RuntimeException("Please input the Root Directory !");
				}
				
				if(! passwords.trim().equals(tx3.trim())) {
					throw new RuntimeException("Wrong installation password !");
				}
				
				rootPath = new File(roots);
				if(! rootPath.exists()) rootPath.mkdirs();
				
				garbage = new File(rootPath.getAbsolutePath() + File.separator + ".garbage");
				if(! garbage.exists()) garbage.mkdirs();
				
				String useAccounts = request.getParameter("useaccount");
				if(useAccounts != null) {
					noLogin = (! Boolean.parseBoolean(useAccounts.trim()));
					if(! noLogin) {
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
						
						JSONObject adminAc = new JSONObject();
						adminAc.put("id"    , adminId);
						adminAc.put("idtype", "A");
						adminAc.put("nick"  , adminNick);
						adminAc.put("fail_cnt", "0");
						adminAc.put("fail_time", "0");
						
						MessageDigest digest = MessageDigest.getInstance("SHA-256");
						byte[] res = digest.digest((s1 + adminPw + s2 + salt + adminId + s3).getBytes(cs));
						adminAc.put("pw", Base64.encodeBase64String(res));
						
						File faJson = new File(fileConfigPath.getAbsolutePath() + File.separator + "accounts");
				        if(! faJson.exists()) faJson.mkdirs();
				        
				        File fileAcc = new File(fileConfigPath.getAbsolutePath() + File.separator + "accounts" + File.separator + adminId + ".json");
				        fileAcc.getCanonicalPath(); // Check valid
				        fileOut = new FileOutputStream(fileAcc);
				        fileOut.write(adminAc.toJSONString().getBytes(cs));
				        fileOut.close(); fileOut = null;
					}
				} else {
					noLogin = true;
				}
				
				conf.clear();
				
				conf.put("Title", titles);
				conf.put("Path", rootPath.getAbsolutePath());
				conf.put("UseAccount", new Boolean(! noLogin));
				conf.put("Installed", new Boolean(true));
				conf.put("S1", s1);
		        conf.put("S2", s2);
		        conf.put("S3", s3);
		        conf.put("Salt", salt);
				
				File fJson = new File(fileConfigPath.getAbsolutePath() + File.separator + "config.json");
				fileOut = new FileOutputStream(fJson);
				fileOut.write(conf.toJSONString().getBytes(cs));
				fileOut.close(); fileOut = null;
				
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
			}
		}
		return json;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject list(HttpServletRequest request, String pPath, String pKeyword) {
		initialize(request);
		
		String pathParam = pPath;
		if(pathParam == null) pathParam = "";
		pathParam = pathParam.trim();
		if(pathParam.equals("/")) pathParam = "";
		pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
		if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);

		String keyword = pKeyword;
		if(keyword == null) keyword = "";
		keyword = keyword.replace("'", "").replace("\"", "").replace("<", "").replace(">", "").trim();

		File dir = new File(instance.rootPath.getAbsolutePath() + File.separator + pathParam);

		File[] list = dir.listFiles();
		if(list == null) list = new File[0];

		List<File> chDirs  = new ArrayList<File>();
		List<File> chFiles = new ArrayList<File>();

		for(File f : list) {
		    if(f.isDirectory()) {
		        String nm = f.getName();
		        if(nm.indexOf(".") >= 0) continue;
		        chDirs.add(f);
		    } else {
		        if(f.length() / 1024 >= instance.limitSize) continue;
		        chFiles.add(f);
		    }
		}

		list = null;

		Collections.sort(chDirs);
		Collections.sort(chFiles);

		String pathDisp = pathParam; // 화면 출력용
		if(pathDisp.startsWith("//")) pathDisp = pathDisp.substring(1);
		if(! pathDisp.startsWith("/")) pathDisp = "/" + pathDisp;

		JSONObject json = new JSONObject();
		json.put("type", "list");
		json.put("keyword", keyword);
		json.put("path"   , pathParam);
		json.put("dpath"  , pathDisp);

		JSONArray dirs = new JSONArray();
		for(File f : chDirs) {
			String name = f.getName();
		    if(! keyword.equals("")) { if(! name.contains(keyword)) continue; }
		    if(name.equals(".garbage")) continue;
		    if(name.equals(".upload")) continue;
		    
			String linkDisp = f.getAbsolutePath().replace(instance.rootPath.getAbsolutePath(), "").replace("\\", "/").replace("'", "").replace("\"", "");
		    if(linkDisp.indexOf(".") >= 0) continue;
		    if(linkDisp.indexOf("/") == 0) linkDisp = linkDisp.substring(1);
		    
		    JSONObject child = new JSONObject();
		    child.put("type", "dir");
		    child.put("name", name);
		    child.put("value", linkDisp);
		    dirs.add(child);
		}
		json.put("directories", dirs);
		dirs = null;

		JSONArray files = new JSONArray();
		for(File f : chFiles) {
			String name     = f.getName();
		    if(! keyword.equals("")) { if(! name.toLowerCase().contains(keyword.toLowerCase())) continue; }
		    
		    String linkDisp = name.replace("\"", "'");
		    
		    JSONObject fileOne = new JSONObject();
		    
		    //  파일 이름 옆에 출력할 용량 텍스트 생성
		    long   lSize = f.length();
		    String sUnit = "byte";
		    String comp  = "" + lSize + " " + sUnit;
		    
		    if(lSize < 0) lSize = 0;
		    if(lSize <= 1) {
		        sUnit = "byte";
		        comp = lSize + " " + sUnit;
		    }
		    
		    if(lSize >= 1024) {
		        sUnit = "KB";
		        comp  = (Math.round(( lSize / 1024.0 ) * 10) / 10.0) + " " + sUnit;
		        lSize = lSize / 1024;
		    }
		    
		    if(lSize >= 1024) {
		        sUnit = "MB";
		        comp  = (Math.round(( lSize / 1024.0 ) * 10) / 10.0) + " " + sUnit;
		        lSize = lSize / 1024;
		    }
		    
		    if(lSize >= 1024) {
		        sUnit = "GB";
		        comp  = (Math.round(( lSize / 1024.0 ) * 10) / 10.0) + " " + sUnit;
		        lSize = lSize / 1024;
		    }
		    
		    fileOne.put("type", "file");
		    fileOne.put("name", linkDisp);
		    fileOne.put("size", comp);
		    
		    files.add(fileOne);
		}
		json.put("files", files);
		json.put("privilege", "view");

		JSONObject jsonSess = new JSONObject();
		JSONParser parser   = new JSONParser();
		try {
			String sessionJson = (String) request.getSession().getAttribute("fssession");
			if(sessionJson != null) {
				sessionJson = sessionJson.trim();
				if(! sessionJson.equals("")) {
					JSONObject obj = (JSONObject) parser.parse(sessionJson);
					if(obj != null) { if(obj.get("id"    ) == null) obj = null;         }
				    if(obj != null) { if(obj.get("idtype") == null) obj = null;         }
				    if(obj != null) { if(obj.get("nick"  ) == null) obj = null;         }
				    if(obj != null) { if(obj.get("idtype").equals("block")) obj = null; } 
				    if(obj != null) {
				    	jsonSess.put("id"    , obj.get("id"));
				    	jsonSess.put("idtype", obj.get("idtype"));
				    	jsonSess.put("nick"  , obj.get("nick"));
				    }
				    
				    if(jsonSess.get("idtype").toString().equals("A")) json.put("privilege", "edit");
				    
				    Object oDirPrv = (Object) obj.get("privileges");
				    if(oDirPrv != null) {
				    	JSONArray dirPrv = null;
			            if(oDirPrv instanceof JSONArray) {
			                dirPrv = (JSONArray) oDirPrv;
			            } else {
			            	dirPrv = (JSONArray) parser.parse(oDirPrv.toString().trim());
			            }
			            
			            for(Object row : dirPrv) {
			            	JSONObject dirOne = null;
			            	if(row instanceof JSONObject) dirOne = (JSONObject) row;
			            	else                          dirOne = (JSONObject) parser.parse(row.toString().trim());
			            	
			            	try {
			            		String dPath = dirOne.get("path"     ).toString();
			            		String dPrv  = dirOne.get("privilege").toString();
			            		
			            		if(pathParam.startsWith(dPath)) {
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
				}
			}
		} catch(Throwable t) {
			t.printStackTrace();
			jsonSess.clear();
		}
		json.put("session", jsonSess);
		
		return json;
	}
	
	public String createCaptchaBase64(HttpServletRequest request, String code, long time, String theme) {
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
			    request.getSession().setAttribute("captcha_code", code);
			}

			int colorPad = 0;
			if(captDarkMode) colorPad = 100;

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
			for(int ndx=0; ndx<10; ndx++) {
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
			    int ang  = (((int) Math.random()) * 41) - 20;
			    
			    graphics.rotate(Math.toRadians(ang), nowX, y);
			    graphics.setFont(font);
			    graphics.drawString(String.valueOf(charNow), nowX, y + ((int) ((Math.random() * captchaHeight) / 2.0)));
			    graphics.rotate(Math.toRadians(ang) * (-1), nowX, y);
			    
			    x += metrics.charWidth(charNow) + gap;
			}

			ByteArrayOutputStream binary = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", binary);
			image    = null;
			graphics = null;

			String bs64str = Base64.encodeBase64String(binary.toByteArray());
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
		JSONArray dirPrv = null;
		try {
		    String sessionJson = (String) request.getSession().getAttribute("fssession");
		    
		    if(sessionJson != null) {
		        JSONParser parser = new JSONParser();
		        JSONObject sessionMap = (JSONObject) parser.parse(sessionJson.trim());
		        
		        if(sessionMap != null) { if(sessionMap.get("id"    ) == null) sessionMap = null;         }
		        if(sessionMap != null) { if(sessionMap.get("idtype") == null) sessionMap = null;         }
		        if(sessionMap != null) { if(sessionMap.get("nick"  ) == null) sessionMap = null;         }
		        if(sessionMap != null) { if(sessionMap.get("idtype").equals("block")) sessionMap = null; } 
		        
		        if(sessionMap != null) {
		            // uId     = sessionMap.get("id"    ).toString();
		            uIdType = sessionMap.get("idtype").toString();
		            
		            Object oDirPrv = (Object) sessionMap.get("privileges");
		            if(oDirPrv != null) {
		                if(oDirPrv instanceof JSONArray) {
		                    dirPrv = (JSONArray) oDirPrv;
		                } else {
		                    dirPrv = (JSONArray) parser.parse(oDirPrv.toString().trim());
		                }
		            }
		        }
		    }
		    
		    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		    File dest = new File(uploadd.getAbsolutePath() + File.separator + dateFormat.format(new java.util.Date(System.currentTimeMillis())));
		    if(! dest.exists()) dest.mkdirs();
		    
		    int maxSize = Integer.MAX_VALUE;
		    if(limitSize * 1024L < maxSize) maxSize = (int) (limitSize * 1024L); 
		    MultipartRequest mReq = new MultipartRequest(request, dest.getAbsolutePath(), maxSize, cs, new DefaultFileRenamePolicy());
		    
		    String pathParam = mReq.getParameter("path");
		    
		    if(! uIdType.equals("A")) {
		    	if(dirPrv == null) throw new RuntimeException("No privilege");
		    	
		    	boolean hasPriv = false;
		    	JSONParser parser = new JSONParser();
		    	for(Object row : dirPrv) {
		    		JSONObject dirOne = null;
		            if(row instanceof JSONObject) dirOne = (JSONObject) row;
		            else                          dirOne = (JSONObject) parser.parse(row.toString().trim());
		            
		            try {
		                String dPath = dirOne.get("path"     ).toString();
		                String dPrv  = dirOne.get("privilege").toString();
		                
		                if(pathParam.startsWith(dPath)) {
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
		    	
		    	File fileOne = new File(dest.getAbsolutePath() + File.separator + fileName);
		    	File destFil = new File(rootPath.getAbsolutePath() + File.separator + pathParam.replace("/", File.separator) + File.separator + fileOrig);
		    	
		    	if(destFil.exists()) {
		    		int dupx = 0;
		    		while(destFil.exists()) {
		    			dupx++;
		    			destFil = new File(rootPath.getAbsolutePath() + File.separator + pathParam.replace("/", File.separator) + File.separator + fileOrig + "." + dupx);
		    		}
		    	}
		    	
		    	if(fileOne.exists()) {
		    		fileOne.renameTo(destFil);
		    	} else {
		    		System.out.println("Upload complete but cannot move the file from temp directory to destination !");
		    		System.out.println("File : " + fileOne.getAbsolutePath());
		    		System.out.println("Dest : " + destFil.getAbsolutePath());
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
		String capt      = request.getParameter("captcha");
		String mode      = request.getParameter("mode");

		String code = (String) request.getSession().getAttribute("captcha_code");
		Long   time = (Long)   request.getSession().getAttribute("captcha_time");

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
			if(! code.equals(capt)) {
		        throw new RuntimeException("Wrong captcha code !");
		    }
			
			if(now - time.longValue() >= captchaLimitTime) {
			    code = "REFRESH";
			    request.getSession().setAttribute("captcha_code", code);
			}
			
		    if(code.equals("REFRESH")) {
		        throw new RuntimeException("Too old captcha code !");
		    }
		    
		    if(fileName == null || fileName.equals("")) {
		        throw new FileNotFoundException("File name is needed.");
		    }

		    file = new File(rootPath.getAbsolutePath() + File.separator + pathParam.replace("/", File.separator) + File.separator + fileName);
		    if(! file.exists()) {
		    	System.out.println("No File ! " + file.getAbsolutePath() + " <-- " + rootPath.getAbsolutePath() + File.separator + pathParam.replace("/", File.separator) + File.separator + fileName);
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
			// tx.printStackTrace();
			System.out.println("Exception message while sending file : " + tx.getMessage());
			response.reset();
			response.setContentType("text/html;charset=UTF-8");
			
			results = results.append("<pre>").append("Error : ").append(tx.getMessage()).append("</pre>");
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
	
	@SuppressWarnings("unchecked")
	public JSONObject remove(HttpServletRequest request) {
		JSONObject json = new JSONObject();
		String uIdType = "";
		JSONArray dirPrv = null;
		try {
		    String sessionJson = (String) request.getSession().getAttribute("fssession");
		    
		    if(sessionJson != null) {
		        JSONParser parser = new JSONParser();
		        JSONObject sessionMap = (JSONObject) parser.parse(sessionJson.trim());
		        
		        if(sessionMap != null) { if(sessionMap.get("id"    ) == null) sessionMap = null;         }
		        if(sessionMap != null) { if(sessionMap.get("idtype") == null) sessionMap = null;         }
		        if(sessionMap != null) { if(sessionMap.get("nick"  ) == null) sessionMap = null;         }
		        if(sessionMap != null) { if(sessionMap.get("idtype").equals("block")) sessionMap = null; } 
		        
		        if(sessionMap != null) {
		        	// uId     = sessionMap.get("id"    ).toString();
		        	uIdType = sessionMap.get("idtype").toString();
		        	
		        	Object oDirPrv = (Object) sessionMap.get("privileges");
		            if(oDirPrv != null) {
		                if(oDirPrv instanceof JSONArray) {
		                    dirPrv = (JSONArray) oDirPrv;
		                } else {
		                    dirPrv = (JSONArray) parser.parse(oDirPrv.toString().trim());
		                }
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
		        JSONParser parser = new JSONParser();
		        for(Object row : dirPrv) {
		            JSONObject dirOne = null;
		            if(row instanceof JSONObject) dirOne = (JSONObject) row;
		            else                          dirOne = (JSONObject) parser.parse(row.toString().trim());
		            
		            try {
		                String dPath = dirOne.get("path"     ).toString();
		                String dPrv  = dirOne.get("privilege").toString();
		                
		                if(pathParam.startsWith(dPath)) {
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
			    
			    File file = new File(rootPath.getAbsolutePath() + File.separator + pathParam.replace("/", File.separator) + File.separator + fileName);
			    if(! file.exists()) {
			        throw new FileNotFoundException("There is no file !");
			    }
			    if(file.isDirectory()) {
			        throw new FileNotFoundException("Cannot delete directory !");
			    }
			    
			    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			    File dest = new File(garbage.getAbsolutePath() + File.separator + dateFormat.format(new java.util.Date(System.currentTimeMillis())));
			    if(! dest.exists()) dest.mkdirs();

			    File fdest = new File(dest.getAbsolutePath() + File.separator + file.getName());
			    if(fdest.exists()) {
			    	int index = 0;
			    	while(fdest.exists()) {
			    		index++;
			    		fdest = new File(dest.getAbsolutePath() + File.separator + file.getName() + "." + index);
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
	
	@SuppressWarnings("unchecked")
	public JSONObject account(HttpServletRequest request) throws IOException {
		JSONObject sessionMap = null;
		JSONObject accountOne = null;
		boolean needInvalidate = false;
		try {
			String sessionJson = (String) request.getSession().getAttribute("fssession");
		    
		    if(sessionJson != null) {
		    	JSONParser parser = new JSONParser();
		    	sessionMap = (JSONObject) parser.parse(sessionJson.trim());
		    	
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

		JSONObject json = new JSONObject();
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
			
			File faJson = new File(fileConfigPath.getAbsolutePath() + File.separator + "accounts");
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
				
				System.out.println("Login requested ! " + id + " at " + now + " from " + request.getRemoteAddr());
				
				if(msg.equals("")) {
					File fileAcc = new File(faJson.getAbsolutePath() + File.separator + id + ".json");
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
				        
				        accountOne = (JSONObject) new JSONParser().parse(lineCollector.toString().trim());
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
			            
			            MessageDigest digest = MessageDigest.getInstance("SHA-256");
			            byte[] res = digest.digest((s1 + pw + s2 + salt + id + s3).getBytes(cs));
			            String pwInput = Base64.encodeBase64String(res);
			            
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
			                    				
			                    File fileAcc = new File(faJson.getAbsolutePath() + File.separator + id + ".json");
			                    fOut = new FileOutputStream(fileAcc);
			                    fOut.write(accountOne.toJSONString().getBytes(cs));
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
		                    
		                    File fileAcc = new File(faJson.getAbsolutePath() + File.separator + id + ".json");
		                    fOut = new FileOutputStream(fileAcc);
		                    fOut.write(accountOne.toJSONString().getBytes(cs));
		                    fOut.close(); fOut = null;
		                    
		                    accChanging = false;
			        	}
			        	
			        	JSONObject accountJsonNew = new JSONObject();
			        	accountJsonNew.putAll(accountOne);
			        	accountJsonNew.remove("pw");
			        	
			        	sessionMap = accountOne;
			        	request.getSession().setAttribute("fssession", accountJsonNew.toJSONString());
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
	public String getLanguage(HttpServletRequest request) {
    	String lang = (String) request.getSession().getAttribute("fslanguage");
    	if(lang == null) lang = "en";
    	return lang;
    }
}
