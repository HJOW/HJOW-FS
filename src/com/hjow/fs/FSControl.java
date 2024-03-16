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
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import com.hjow.fs.console.FSConsole;
import com.hjow.fs.console.FSConsoleResult;
import com.hjow.fs.cttype.FSContentType;
import com.hjow.fs.lister.FSDefaultLister;
import com.hjow.fs.lister.FSFileLister;
import com.hjow.fs.lister.FSFileListingResult;
import com.hjow.fs.pack.FSControlEventHandler;
import com.hjow.fs.pack.FSPack;
import com.hjow.fs.pack.FSRequestHandler;
import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

import hjow.common.json.JsonArray;
import hjow.common.json.JsonCompatibleUtil;
import hjow.common.json.JsonObject;
import hjow.common.util.ClassUtil;
import hjow.common.util.DataUtil;
import hjow.common.util.FileUtil;
import hjow.common.util.GUIUtil;
import hjow.common.util.SecurityUtil;

public class FSControl {
    public static final int[] VERSION = {0, 1, 18, 25};
    
    private static FSControl instance = null;
    
    // Charset
    protected String cs = "UTF-8";

    // Title
    protected String title = "File Storage";

    // Installation Status
    protected File fileConfigPath = null;

    // Storage Root Directory
    protected String storPath  = "/fsdir/storage/";
    
    // Session remaining methods
    protected boolean useSession = true;
    protected boolean useToken   = true;

    // Reading configuration file time gap (milliseconds)
    protected long   refreshConfGap = 1000L * 300;
    
    // Read and display file icon
    protected boolean readFileIcon = true;

    // Download limit size (KB)
    protected long   limitSize = 10 * 1024 * 1024;
    // Previewing limit size (KB)
    protected long   limitPrev = 1  * 1024 * 1024;
    
    // Display limit count on one page
    protected int    limitCount = 1000;

    // Perform downloading buffer size (KB) - Downloading speed faster when this value is become higher.
    protected int  bufferSize   = 10;

    // Perform downloading thread's sleeping gap (milliseconds) - Downloading speed faster when this value is become lower.
    protected long sleepGap     = 50;

    // Perform downloading thread's sleeping cycle (times) - Downloading speed faster when this value is become higher.
    protected int  sleepRoutine = 10;

    // Captcha
    protected boolean captchaDownload = true, captchaLogin = true;
    protected int  captchaWidth     = 250;
    protected int  captchaHeight    = 40;
    protected int  captchaFontSize  = 30;
    protected int  captchaNoises    = 20;
    protected long captchaLimitTime = 1000 * 60 * 5;
    
    // Login Policy
    protected boolean noAnonymous = false;
    protected boolean noLogin     = false;
    protected boolean readOnly    = false;
    protected boolean allowSysCmd = false;
    protected int loginFailCountLimit = 10;
    protected int loginFailOverMinute = 10;
    protected int tokenLifeTime = 0; // Minutes  
    
    // Console Usage
    protected boolean noConsole = true;
    
    // JDBC
    protected boolean useJDBC = false;
    protected String dbType = null, jdbcClass = null, jdbcUrl = null, jdbcId = null, jdbcPw = null;
    
    // Logging
    protected transient String  logFileNm = "fs_[date]_[n].log";
    protected transient boolean logOnFile = false;
    protected transient boolean logOnJdbc = false;
    protected transient boolean logOnStd  = true;
    protected transient int     logLen    = 0;
    protected transient int     logLimit  = 4000;
    protected transient long    logLastDt = 0;
    protected transient Connection     logConn   = null;
    protected transient BufferedWriter logFileWr = null;
    
    // Other Temporary Fields
    protected transient JsonObject conf = new JsonObject();
    protected transient volatile long    confReads    = 0L;
    protected transient volatile long    confAccess   = 0L;
    protected transient volatile boolean confChanging = false;
    protected transient volatile boolean accChanging  = false;
    protected transient volatile boolean initializing = false;
    protected transient boolean installed = false;
    protected transient String salt = "fs";
    protected transient File rootPath  = null;
    protected transient File garbage   = null;
    protected transient File uploadd   = null;
    protected transient File logd      = null;
    protected transient String ctxPath  = "";
    protected transient String realPath = "";
    protected transient FSFileLister lister = new FSDefaultLister();
    protected transient List<FSPack> packs = new ArrayList<FSPack>();
    protected transient List<FSContentType> ftypes = new ArrayList<FSContentType>();
    
    protected FSControl() {
        ftypes.addAll(FSContentType.getDefaults());
    }
    
    public static FSControl getInstance() { return instance; }
    
    /** Initialize instances */
    public static void init(String contextPath) {
        init(contextPath, false);
    }
    
    /** Initialize instances */
    public static void init(HttpServletRequest request) {
        init(request.getContextPath());
    }
    
    /** Initialize instances */
    public static void init(HttpServletRequest request, boolean forceInit) {
        init(request.getContextPath(), forceInit);
    }
    
    /** Initialize instances */
    public static synchronized void init(String contextPath, boolean forceInit) {
        if(instance == null || forceInit) {
            if(instance != null) instance.dispose();
            
            Class<? extends FSControl> ctrlClass = getControlClass();
            try { instance = ctrlClass.newInstance(); } catch(Throwable t) { throw new RuntimeException("Cannot create fs control instance - (" + t.getClass().getSimpleName() + ") " + t.getMessage()); }
        }
        instance.initialize(contextPath);
    }
    
    /** Get FSControl class or alternatives (read CL property on fs.properties) */
    @SuppressWarnings("unchecked")
    protected static Class<? extends FSControl> getControlClass() {
        try {
            Properties propTest = getFSProperties();
            
            if(propTest != null) {
                String ctrlClass = propTest.getProperty("CL");
                if(ctrlClass != null) {
                    ctrlClass = ctrlClass.trim().toUpperCase();
                    if((! (ctrlClass.equals("DEFAULT") || ctrlClass.equals("")))) {
                        return (Class<? extends FSControl>) Class.forName(ctrlClass);
                    }
                }
            }
        } catch(Throwable t) {
            System.out.println("Cannot find or load fs control class - (" + t.getClass().getSimpleName() + ") " + t.getMessage());
        }
        return FSControl.class;
    }
    
    /** logging */
    public static void log(Object logContent, Class<?> froms) {
        if(instance == null) System.out.println(logContent);
        else getInstance().logIn(logContent, froms);
    }
    
    /** Clean FSControl instance */
    public static synchronized void disposeInstance() {
        FSControl c = instance;
        instance = null;
        if(c != null) c.dispose();
    }
    
    /** Load properties, configs, and DB tables when using JDBC */
    protected void initialize(String contextPath) {
        long now = System.currentTimeMillis();
        if(ctxPath == null) ctxPath = "";
        initializeIn(contextPath, now);
    }
    
    /** Load properties, configs, and DB tables when using JDBC */
    private void initializeIn(String contextPath, long now) {
    	if(ctxPath == null) ctxPath = "";

    	int preventInfLoop = 0;
    	while(initializing) {
    		try { Thread.sleep(100L); } catch(InterruptedException e) { break; }
    		preventInfLoop++;
    		if(preventInfLoop >= 10000) break;
    	}
    	
    	if(fileConfigPath == null || (! ctxPath.equals(contextPath))) {
    		initializeInSync(contextPath, now);
    		return;
    	}
    	if(now - confReads >= refreshConfGap) {
    		initializeInSync(contextPath, now);
    	} else if(Math.abs(now - confAccess) >= refreshConfGap / 10) {
    		initializeInSync(contextPath, now);
    	} else {
    		confAccess = now;
    	}
    }
    
    /** Load properties, configs, and DB tables when using JDBC */
    private synchronized void initializeInSync(String contextPath, long now) {
    	initializing = true;
    	
        Connection        conn     = null;
        PreparedStatement pstmt    = null;
        ResultSet         rs       = null;
        InputStream       propIn   = null;
        OutputStream      fileOut  = null;
        Reader            rd1      = null;
        Reader            rd2      = null;
        Throwable         catched  = null;
        
        String pPacks = "";
        if(ctxPath == null) ctxPath = "";
        
        try {
            confReads  = now;
            confAccess = now;
            
            // Set Global Variables
            ctxPath = contextPath;
            
            logIn("Trying to reload configs at " + System.currentTimeMillis());
            
            // Check installation and trying to find configuration file
            String s1 = "";
            String s2 = "";
            String s3 = "";
            
            Properties propTest = getFSProperties();
            
            if(propTest != null) {
                // Check fs.properties
                String t1 = propTest.getProperty("FS");
                String t2 = propTest.getProperty("RD");
                String t3 = propTest.getProperty("PW");
                s1 = propTest.getProperty("S1");
                s2 = propTest.getProperty("S2");
                s3 = propTest.getProperty("S3");
                if(t1 != null && t2 != null && t3 != null && s1 != null && s2 != null && s3 != null) {
                    if(t1.trim().equals("FileStorage") && t2.trim().equals("SetConfigPathBelow")) {
                        logIn("Configuration Found !");
                        
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
                                cf = cf.replace("\\", File.separator);
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
                            
                            try { Class.forName(jdbcClass); } catch(ClassNotFoundException e) { logIn("No jdbc driver found - " + jdbcClass); useJDBC = false; } 
                        }
                    }
                }
                
                pPacks = propTest.getProperty("PK");
                if(pPacks == null) pPacks = "";
                pPacks = pPacks.trim();
                
                propTest.clear();
                propTest = null;
                propIn = null;
                
                // Read configs
                if(useJDBC) {
                    if(conn == null) {
                        Class.forName(jdbcClass);
                        conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);                                
                    }
                    
                    pstmt = conn.prepareStatement("SELECT JSONCONFIG FROM FS_CONFIG");
                    rs    = pstmt.executeQuery();
                    
                    String confJson = null;
                    while(rs.next()) {
                        confJson = rs.getString("JSONCONFIG");
                    }
                    
                    rs.close(); rs = null;
                    pstmt.close(); pstmt = null;
                    conn.close(); conn = null;
                    
                    conf = (JsonObject) JsonCompatibleUtil.parseJson(confJson);
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
                    
                    conf = (JsonObject) JsonCompatibleUtil.parseJson(lineCollection.toString().trim());
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
                    
                    File ftJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "tokens");
                    if(! ftJson.exists()) {
                        ftJson.mkdirs();
                    }
                }
            }
            if(conf    == null) { try { conf = new JsonObject(); } catch(Throwable txc) {} }
            propIn = null;
            
            // Applying Configs
            if(conf.get("Installed") != null) {
                installed = DataUtil.parseBoolean(conf.get("Installed").toString().trim());
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
                logd = new File(fileConfigPath.getCanonicalPath() + File.separator + ".logs");
                if(! logd.exists()) logd.mkdirs();
                applyConfigs();
             }
            
            // Searching FSPack declared from config (This config value is array filled with FSPack class names)
            List<String> packList = new ArrayList<String>();
            if(conf.get("Packs") != null) {
                JsonArray arr = null;
                if(conf.get("Packs") instanceof JsonArray) arr = (JsonArray) conf.get("Packs");
                else arr = (JsonArray) JsonCompatibleUtil.parseJson(conf.get("Packs").toString().trim());
                for(Object a : arr) {
                    String packClass = a.toString().trim();
                    if(! packList.contains(packClass)) packList.add(packClass);
                }
            } else {
                conf.put("Packs", new JsonArray());
            }
            
            // Searching FSPack declared from fs.properties (This prop value is array filled with FSPack class names)
            if(! pPacks.equals("")) {
                StringTokenizer colonTokenizer = new StringTokenizer(pPacks, ",");
                while(colonTokenizer.hasMoreTokens()) {
                    String packClass = colonTokenizer.nextToken().trim();
                    if(! packList.contains(packClass)) packList.add(packClass);
                }
            }
            
            // Trying to create FSPack instances
            for(String packClass : packList) {
                try {
                    Class<?> pc = Class.forName(packClass);
                    FSPack pack = (FSPack) pc.newInstance();
                    if(! pack.isAvail(VERSION)) continue;
                    if(packs.contains(pack)) continue;
                    pack.init(this);
                    packs.add(pack);
                    
                    if(pack instanceof FSFileLister) this.lister = (FSFileLister) pack;
                    
                    List<FSContentType> typesBundled = pack.getContentTypes();
                    if(typesBundled != null) {
                        for(FSContentType t : typesBundled) {
                            if(! ftypes.contains(t)) ftypes.add(t);
                        }
                    }
                } catch(Throwable tc) {
                    logIn("Exception when loading Pack " + packClass + " - (" + tc.getClass().getName() + ") " + tc.getMessage());
                }
            }
            packList.clear();
            packList = null;
            
            // Load commands from FSPack
            List<String> cmdList = new ArrayList<String>();
            for(FSPack p : packs) {
                List<String> l = p.getCommandClasses();
                for(String c : l) {
                    if(! cmdList.contains(c)) cmdList.add(c);
                }
            }
            
            FSConsole.init(rootPath, cmdList);
        } catch(Throwable thx) {
            thx.printStackTrace();
            fileConfigPath = null;
            catched = thx;
        } finally {
            if(rd2     != null) { try { rd2.close();             } catch(Throwable txc) {} }
            if(rd1     != null) { try { rd1.close();             } catch(Throwable txc) {} }
            if(propIn  != null) { try { propIn.close();          } catch(Throwable txc) {} }
            if(fileOut != null) { try { fileOut.close();         } catch(Throwable txc) {} }
            if(rs      != null) { try { rs.close();              } catch(Throwable txc) {} }
            if(pstmt   != null) { try { pstmt.close();           } catch(Throwable txc) {} }
            if(conn    != null) { try { conn.close();            } catch(Throwable txc) {} }
            if(conf    == null) { try { conf = new JsonObject(); } catch(Throwable txc) {} }
            initializing = false;
        }
        
        if(catched != null) throw new RuntimeException(catched.getMessage(), catched);
    }
    
    /** Called from fsinstall.jsp, which is called by installation page by ajax. */
    public JsonObject install(HttpServletRequest request) throws Exception {
    	initialize(request.getContextPath());
    	
        JsonObject json = processHandler("install", request);
        if(json != null) return json;
        json = new JsonObject();
        
        if(! installed) {
            Properties   propTest = null;
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
                
                propTest = getFSProperties();
                
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
                
                roots = roots.replace("\\", "/");
                
                // Check Installation Password
                if(! passwords.equals(tx3.trim())) {
                    if(! SecurityUtil.hash(passwords, "SHA-256").equals(tx3.trim())) {
                        throw new RuntimeException("Wrong installation password !");
                    }
                }
                
                // Check JDBC
                if(useJDBC) {
                    if(conn == null) {
                        Class.forName(jdbcClass);
                        conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                    }
                    
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
                        
                        pstmt = conn.prepareStatement("SELECT USERID, USERPW, USERNICK, USERTYPE, FAILCNT, FAILTIME, PRIVILEGES FROM FS_USER");
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
                    
                    // Check FS_TOKEN
                    
                    try {
                        // Check FS_USER exist
                        pstmt = conn.prepareStatement("SELECT COUNT(*) AS CNT FROM FS_TOKEN");
                        rs = pstmt.executeQuery();
                        
                        while(rs.next()) {
                            rs.getInt("CNT");
                        }
                        
                        rs.close(); rs = null;
                        pstmt.close(); pstmt = null;
                        
                        pstmt = conn.prepareStatement("SELECT USERID, TOKEN, CRTIME, ENTIME, CONTENT FROM FS_TOKEN");
                        rs = pstmt.executeQuery();
                        
                        rs.close(); rs = null;
                        pstmt.close(); pstmt = null;
                    } catch(SQLException e) {
                        if(dbType.equals("oracle")) pstmt = conn.prepareStatement("CREATE TABLE FS_TOKEN ( TOKEN VARCHAR2(1024) PRIMARY KEY, USERID VARCHAR2(40), CRTIME NUMBER(20) , ENTIME NUMBER(20) , CONTENT VARCHAR2(4000) )");
                        else                        pstmt = conn.prepareStatement("CREATE TABLE FS_TOKEN ( TOKEN VARCHAR(1024)  PRIMARY KEY, USERID VARCHAR(40) , CRTIME NUMERIC(20), ENTIME NUMERIC(20), CONTENT VARCHAR(4000) )");
                        pstmt.execute();
                        conn.commit();
                        pstmt.close(); pstmt = null;
                    }
                    
                    // Check FS_LOG
                    
                    try {
                        // Check FS_LOG exist
                        pstmt = conn.prepareStatement("SELECT COUNT(*) AS CNT FROM FS_LOG");
                        rs = pstmt.executeQuery();
                        
                        while(rs.next()) {
                            rs.getInt("CNT");
                        }
                        
                        rs.close(); rs = null;
                        pstmt.close(); pstmt = null;
                        
                        pstmt = conn.prepareStatement("SELECT LOGNO, LOGCONTENT, LOGCLASS, LOGDATE FROM FS_LOG");
                        rs = pstmt.executeQuery();
                        
                        rs.close(); rs = null;
                        pstmt.close(); pstmt = null;
                    } catch(SQLException e) {
                        if(dbType.equals("oracle")) pstmt = conn.prepareStatement("CREATE TABLE FS_LOG ( LOGNO NUMBER(10)  PRIMARY KEY, LOGCONTENT VARCHAR2(4000), LOGCLASS VARCHAR2(100), LOGDATE NUMBER(20) )");
                        else                        pstmt = conn.prepareStatement("CREATE TABLE FS_LOG ( LOGNO NUMERIC(10) PRIMARY KEY, LOGCONTENT VARCHAR(4000) , LOGCLASS VARCHAR(100) , LOGDATE NUMERIC(20) )");
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
                
                uploadd = new File(rootPath.getCanonicalPath() + File.separator + ".upload");
                if(! uploadd.exists()) uploadd.mkdirs();
                
                logd = new File(fileConfigPath.getCanonicalPath() + File.separator + ".logs");
                if(! logd.exists()) logd.mkdirs();
                
                String sMaxSize = request.getParameter("limitsize");
                if(sMaxSize == null) sMaxSize = "" + (1024 * 1024);
                Long.parseLong(sMaxSize); // Checking valid number
                
                String sMaxPrev = request.getParameter("limitprev");
                if(sMaxPrev == null) sMaxPrev = "" + (1024 * 1024);
                Long.parseLong(sMaxPrev); // Checking valid number
                
                String sMaxCount = request.getParameter("limitcount");
                if(sMaxCount == null) sMaxCount = "1000";
                Integer.parseInt(sMaxCount); // Checking valid number
                
                String sLoginfailcnt  = "10";
                String sTokenlifetime =  "0"; 
                if(! noLogin) {
                    sLoginfailcnt = request.getParameter("loginfailcnt");
                    if(sLoginfailcnt == null) sLoginfailcnt = "10";
                    Integer.parseInt(sLoginfailcnt); // Checking valid number
                    
                    sTokenlifetime = request.getParameter("tokenlifetime");
                    if(sTokenlifetime == null) sTokenlifetime = "10";
                    Integer.parseInt(sTokenlifetime); // Checking valid number
                }
                
                String sUseCaptchaDown  = request.getParameter("usecaptchadown");
                String sUseCaptchaLogin = request.getParameter("usecaptchalogin");
                String sReadFileIcon    = request.getParameter("readfileicon");
                String sUseConsole      = request.getParameter("useconsole");
                String sUseSession      = request.getParameter("usesession");
                String sReadOnly        = request.getParameter("readonlymode");
                
                boolean useCaptchaDown  = false;
                boolean useCaptchaLogin = false;
                boolean useReadFileIcon = false;
                boolean useConsole      = false;
                boolean useSession      = false;
                boolean rdonly          = false;
                
                if(sUseCaptchaDown  != null) useCaptchaDown  = DataUtil.parseBoolean(sUseCaptchaDown.trim());
                if(sUseCaptchaLogin != null) useCaptchaLogin = DataUtil.parseBoolean(sUseCaptchaLogin.trim());
                if(sReadFileIcon    != null) useReadFileIcon = DataUtil.parseBoolean(sReadFileIcon.trim());
                if(sUseConsole      != null) useConsole      = DataUtil.parseBoolean(sUseConsole.trim());
                if(sUseSession      != null) useSession      = DataUtil.parseBoolean(sUseSession.trim());
                if(sReadOnly        != null) rdonly          = DataUtil.parseBoolean(sReadOnly.trim());
                
                String sUseAccounts = request.getParameter("useaccount");
                if(sUseAccounts != null) {
                    noLogin = (! DataUtil.parseBoolean(sUseAccounts.trim()));
                    if(! noLogin) {
                        if(sUseCaptchaLogin != null) useCaptchaLogin = DataUtil.parseBoolean(sUseCaptchaLogin.trim());
                        
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
                        
                        if(adminId.equalsIgnoreCase("guest") || adminId.startsWith("GUEST")) {
                            noLogin = true;
                            throw new RuntimeException("ID cannot be 'guest' !");
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
                            
                            File ftJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "tokens");
                            if(! ftJson.exists()) ftJson.mkdirs();
                            
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
                
                conf.put("Title"               , titles);
                conf.put("sHiddenDirs"         , sHiddenDir);
                conf.put("HiddenDirs"          , new JsonArray());
                conf.put("Path"                , rootPath.getCanonicalPath());
                conf.put("UseAccount"          , new Boolean(! noLogin));
                conf.put("UseCaptchaDown"      , new Boolean(useCaptchaDown));
                conf.put("UseCaptchaLogin"     , new Boolean(useCaptchaLogin));
                conf.put("UseConsole"          , new Boolean(useConsole));
                conf.put("UseSession"          , new Boolean(useSession));
                conf.put("ReadOnly"            , new Boolean(rdonly));
                conf.put("LoginFailCountLimit" , new Integer(sLoginfailcnt ));
                conf.put("TokenLifeTime"       , new Integer(sTokenlifetime));
                conf.put("LimitDownloadSize"   , sMaxSize);
                conf.put("LimitPreviewSize"    , sMaxPrev);
                conf.put("LimitFilesSinglePage", sMaxCount);
                conf.put("ReadFileIcon"        , new Boolean(useReadFileIcon));
                conf.put("S1", s1);
                conf.put("S2", s2);
                conf.put("S3", s3);
                conf.put("Salt", salt);
                conf.put("Installed", new Boolean(true));
                
                applyConfigs();
                applyModifiedConfig("Installation Program");
                
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
                if(fileOut != null) { try { fileOut.close(); } catch(Throwable txc) {} }
                if(rs      != null) { try { rs.close();      } catch(Throwable txc) {} }
                if(pstmt   != null) { try { pstmt.close();   } catch(Throwable txc) {} }
                if(conn    != null) { try { conn.close();    } catch(Throwable txc) {} }
            }
        }
        return json;
    }
    
    /** Called from fsadmin.jsp, which is called by administration page by ajax. */
    public JsonObject admin(HttpServletRequest request) throws Exception {
    	initialize(request.getContextPath());
    	
        JsonObject json = processHandler("admin", request);
        if(json != null) return json;
        json = new JsonObject();
        
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
            
            sessionMap = getSessionFSObject(request);
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
            
            propTest = getFSProperties();
            
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
            
            if(useJDBC) {
                if(conn == null) {
                    Class.forName(jdbcClass);
                    conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                }
                
                pstmt = conn.prepareStatement("SELECT JSONCONFIG FROM FS_CONFIG");
                rs = pstmt.executeQuery();
                String content = "{}";
                
                while(rs.next()) {
                    content = rs.getString("JSONCONFIG");
                }
                
                rs.close(); rs = null;
                pstmt.close(); pstmt = null;
                conn.close(); conn = null;
                
                jsonConfig = (JsonObject) JsonCompatibleUtil.parseJson(content.trim());
            } else {
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
                
                jsonConfig = (JsonObject) JsonCompatibleUtil.parseJson(lineCollection.toString().trim());
            }
            
            json.put("message", "");
            
            if(req.equalsIgnoreCase("update")) {
                if(readOnly) throw new RuntimeException("Blocked. FS is read-only mode.");
                
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
                Object hiddenDirs = JsonCompatibleUtil.parseJson(FSUtils.removeLineComments(sHiddenDirs, '#').trim()); // Checking valid JSON
                if(! (hiddenDirs instanceof JsonArray)) throw new RuntimeException("'Hidden Folders' Should be a JSON array.");
                
                String sMaxSize = request.getParameter("limitsize");
                if(sMaxSize == null) sMaxSize = "" + (1024 * 1024);
                Long.parseLong(sMaxSize); // Checking valid number
                
                String sMaxPrev = request.getParameter("limitprev");
                if(sMaxPrev == null) sMaxPrev = "" + (1024 * 1024);
                Long.parseLong(sMaxPrev); // Checking valid number
                
                String sMaxCount = request.getParameter("limitcount");
                if(sMaxCount == null) sMaxCount = "1000";
                Integer.parseInt(sMaxCount); // Checking valid number
                
                String sLoginfailcnt  = null;
                String sTokenlifetime = null;
                if(! noLogin) {
                    sLoginfailcnt = request.getParameter("loginfailcnt");
                    if(sLoginfailcnt == null) sLoginfailcnt = "10";
                    Integer.parseInt(sLoginfailcnt); // Checking valid number
                    
                    sTokenlifetime = request.getParameter("tokenlifetime");
                    if(sTokenlifetime == null) sTokenlifetime = "10";
                    Integer.parseInt(sTokenlifetime); // Checking valid number
                } else {
                	sLoginfailcnt  = "10";
                	sTokenlifetime = "0";
                }
                
                String sUseCaptchaDown  = request.getParameter("usecaptchadown");
                String sUseCaptchaLogin = request.getParameter("usecaptchalogin");
                String sReadFileIcon    = request.getParameter("readfileicon");
                String sUseConsole      = request.getParameter("useconsole");
                
                boolean useCaptchaDown  = false;
                boolean useCaptchaLogin = false;
                boolean useReadFileIcon = false;
                boolean useConsole      = false;
                
                if(sUseCaptchaDown  != null) useCaptchaDown  = DataUtil.parseBoolean(sUseCaptchaDown.trim());
                if(sUseCaptchaLogin != null) useCaptchaLogin = DataUtil.parseBoolean(sUseCaptchaLogin.trim());
                if(sReadFileIcon    != null) useReadFileIcon = DataUtil.parseBoolean(sReadFileIcon.trim());
                if(sUseConsole      != null) useConsole      = DataUtil.parseBoolean(sUseConsole.trim());
                
                conf.put("Title", titles);
                conf.put("sHiddenDirs", sHiddenDirs);
                conf.put("HiddenDirs", hiddenDirs);
                conf.put("UseCaptchaDown" , new Boolean(useCaptchaDown));
                if(! noLogin) {
                    conf.put("UseCaptchaLogin", new Boolean(useCaptchaLogin));
                    if(sLoginfailcnt  != null) conf.put("LoginFailCountLimit", new Integer(sLoginfailcnt ));
                    if(sTokenlifetime != null) conf.put("TokenLifeTime"      , new Integer(sTokenlifetime));
                }
                conf.put("LimitDownloadSize", sMaxSize);
                conf.put("LimitPreviewSize", sMaxPrev);
                conf.put("LimitFilesSinglePage", sMaxCount);
                conf.put("ReadFileIcon", new Boolean(useReadFileIcon));
                conf.put("UseConsole", new Boolean(useConsole));
                conf.put("Installed", new Boolean(true));
                
                jsonConfig.clear();
                jsonConfig = (JsonObject) conf.cloneObject();
                
                logIn("Configuration Updating requested by " + sessionMap.get("id") + " when " + System.currentTimeMillis());
                
                applyConfigs();
                applyModifiedConfig(sessionMap.get("id").toString());
                
                logIn("Configuration Updated by " + sessionMap.get("id") + " when " + System.currentTimeMillis());
                
                json.put("message", "Update Success !");
            } else if(req.equalsIgnoreCase("reset")) {
                if(readOnly) throw new RuntimeException("Blocked. FS is read-only mode.");
                
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
                
                logIn("Reset requested by " + sessionMap.get("id") + " when " + System.currentTimeMillis());
                
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
                                    if(g.isDirectory()) {
                                        File[] gchildren = g.listFiles();
                                        for(File gg : gchildren) {
                                            if(gg.isDirectory()) continue;
                                            gg.delete();
                                        }
                                    }
                                    g.delete();
                                }
                                c.delete();
                            } else {
                                c.delete();
                            }
                        }
                        f.delete();
                    } else {
                        f.delete();
                    }
                }
                
                if(useJDBC) {
                    if(conn == null) {
                        Class.forName(jdbcClass);
                        conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                    }
                    
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
                
                logIn("Reset completed.");
                json.put("reset", new Boolean(true));
                json.put("message", "Reset Success !");
                request.getSession().invalidate();
            } else if(req.equalsIgnoreCase("userlist")) {
                String keyword = request.getParameter("keyword");
                if(keyword == null) keyword = "";
                keyword = keyword.trim();
                
                JsonArray arr = new JsonArray();
                
                if(useJDBC) {
                    if(conn == null) {
                        Class.forName(jdbcClass);
                        conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                    }
                    
                    String sql = "SELECT USERID, USERPW, USERNICK, USERTYPE, FAILCNT, FAILTIME, PRIVILEGES FROM FS_USER";
                    if(! keyword.equals("")) sql += " WHERE USERID LIKE ? OR USERNICK LIKE ?";
                    sql += " ORDER BY USERID";
                    
                    pstmt = conn.prepareStatement(sql);
                    if(! keyword.equals("")) {
                        keyword = "%" + keyword + "%";
                        pstmt.setString(1, keyword);
                        pstmt.setString(2, keyword);
                    }
                    
                    rs = pstmt.executeQuery();
                    
                    while(rs.next()) {
                        JsonObject accountOneTmp = new JsonObject();
                        accountOneTmp.put("id"       , rs.getString("USERID"));
                        accountOneTmp.put("idtype"   , rs.getString("USERTYPE"));
                        accountOneTmp.put("nick"     , rs.getString("USERNICK"));
                        accountOneTmp.put("fail_cnt" , rs.getLong("FAILCNT"));
                        accountOneTmp.put("fail_time", rs.getLong("FAILTIME"));
                        arr.add(accountOneTmp);
                    }
                    
                    rs.close(); rs = null;
                    pstmt.close(); pstmt = null;
                } else {
                    File faJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "accounts");
                    if(! faJson.exists()) faJson.mkdirs();
                    
                    File ftJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "tokens");
                    if(! ftJson.exists()) ftJson.mkdirs();
                    
                    File fileAcc = new File(faJson.getCanonicalPath());
                    File[] files = fileAcc.listFiles();
                    for(File f : files) {
                        if(f.exists()) {
                            JsonObject accountOneTmp = new JsonObject();
                            StringBuilder lineCollector = new StringBuilder("");
                            String lx;
                            
                            FileInputStream fIn = null;
                            InputStreamReader r1 = null;
                            BufferedReader r2 = null;
                            try {
                                fIn = new FileInputStream(f);
                                r1 = new InputStreamReader(fIn, cs);
                                r2 = new BufferedReader(r1);
                                while(true) {
                                    lx = ((BufferedReader) r2).readLine();
                                    if(lx == null) break;
                                    lineCollector = lineCollector.append("\n").append(lx);
                                }
                                r2.close(); r2 = null;
                                r1.close(); r1 = null;
                                fIn.close(); fIn = null;
                                
                                accountOneTmp = (JsonObject) JsonCompatibleUtil.parseJson(lineCollector.toString().trim());
                                lineCollector.setLength(0);
                                lineCollector = null;
                                
                                String id   = accountOneTmp.get("id").toString();
                                String nick = accountOneTmp.get("nick").toString();
                                
                                if(! (id.contains(keyword) || nick.contains(keyword))) continue;
                                
                                accountOneTmp.remove("pw");
                                arr.add(accountOneTmp);
                            } catch(Throwable t) {
                                t.printStackTrace();
                            } finally {
                                if(r2  != null) { try { r2.close();  } catch(Throwable txc) {} }
                                if(r1  != null) { try { r1.close();  } catch(Throwable txc) {} }
                                if(fIn != null) { try { fIn.close(); } catch(Throwable txc) {} }
                            }
                        }
                    }
                }
                
                json.put("userlist" , arr);
            } else if(req.equalsIgnoreCase("usercreate")) {
                if(readOnly) throw new RuntimeException("Blocked. FS is read-only mode.");
                
                String cid     = request.getParameter("id");
                String cpw     = request.getParameter("pw");
                String cnick   = request.getParameter("nick");
                String cidtype = request.getParameter("idtype");
                
                if(cid     == null) cid = "";
                if(cpw     == null) cpw = "";
                if(cnick   == null) cnick = "";
                if(cidtype == null) cidtype = "";
                
                cid     = cid.trim();
                cpw     = cpw.trim();
                cnick   = cnick.trim();
                cidtype = cidtype.trim();
                
                if(lang.equals("ko")) {
                    if(cid.equals("")    ) throw new RuntimeException("ID 를 입력해 주세요.");
                    if(cpw.equals("")    ) throw new RuntimeException("PW 를 입력해 주세요.");
                    if(cnick.equals("")  ) throw new RuntimeException("Nick 을 입력해 주세요.");
                    if(cidtype.equals("")) throw new RuntimeException("Type 을 선택해 주세요.");
                } else {
                    if(cid.equals("")    ) throw new RuntimeException("Please input ID !");
                    if(cpw.equals("")    ) throw new RuntimeException("Please input PW !");
                    if(cnick.equals("")  ) throw new RuntimeException("Please input Nick !");
                    if(cidtype.equals("")) throw new RuntimeException("Please select Type !");
                }
                
                File faJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "accounts");
                File ftJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "tokens");
                
                // Check ID Duplication
                if(useJDBC) {
                    if(conn == null) {
                        Class.forName(jdbcClass);
                        conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                    }
                    pstmt = conn.prepareStatement("SELECT COUNT(*) AS CNT FROM FS_USER WHERE USERID = ?");
                    pstmt.setString(1, cid);
                    int counts = 0;
                    
                    rs = pstmt.executeQuery();
                    while(rs.next()) {
                        counts = rs.getInt("CNT");
                    }
                    
                    rs.close(); rs = null;
                    pstmt.close(); pstmt = null;
                    
                    if(counts >= 1) {
                        if(lang.equals("ko")) throw new RuntimeException("이 ID를 사용할 수 없습니다.");
                        else                  throw new RuntimeException("Cannot use these ID.");
                    }
                } else {
                    if(! faJson.exists()) faJson.mkdirs();
                    if(! ftJson.exists()) ftJson.mkdirs();
                    
                    File fileAcc = new File(faJson.getCanonicalPath());
                    File[] files = fileAcc.listFiles();
                    for(File f : files) {
                        if(f.exists()) {
                            if(f.getName().equals(cid + ".json") || f.getName().equals(cid + ".JSON")) {
                                if(lang.equals("ko")) throw new RuntimeException("이 ID를 사용할 수 없습니다.");
                                else                  throw new RuntimeException("Cannot use these ID.");
                            }
                            
                            JsonObject accountOneTmp = new JsonObject();
                            StringBuilder lineCollector = new StringBuilder("");
                            String lx;
                            
                            FileInputStream fIn = null;
                            InputStreamReader r1 = null;
                            BufferedReader r2 = null;
                            try {
                                fIn = new FileInputStream(f);
                                r1 = new InputStreamReader(fIn, cs);
                                r2 = new BufferedReader(r1);
                                while(true) {
                                    lx = ((BufferedReader) r2).readLine();
                                    if(lx == null) break;
                                    lineCollector = lineCollector.append("\n").append(lx);
                                }
                                r2.close();  r2  = null;
                                r1.close();  r1  = null;
                                fIn.close(); fIn = null;
                                
                                accountOneTmp = (JsonObject) JsonCompatibleUtil.parseJson(lineCollector.toString().trim());
                                lineCollector.setLength(0);
                                lineCollector = null;
                                
                                String fid   = accountOneTmp.get("id").toString();
                                if(fid.equals(cid)) {
                                    if(lang.equals("ko")) throw new RuntimeException("이 ID를 사용할 수 없습니다.");
                                    else                  throw new RuntimeException("Cannot use these ID.");
                                }
                            } catch(Throwable t) {
                                t.printStackTrace();
                            } finally {
                                if(r2  != null) { try { r2.close();  } catch(Throwable txc) {} }
                                if(r1  != null) { try { r1.close();  } catch(Throwable txc) {} }
                                if(fIn != null) { try { fIn.close(); } catch(Throwable txc) {} }
                            }
                        }
                    }
                }
                
                if(cid.equalsIgnoreCase("guest")) {
                    if(lang.equals("ko")) throw new RuntimeException("이 ID를 사용할 수 없습니다.");
                    else                  throw new RuntimeException("Cannot use these ID.");
                }
                
                if(cid.contains("'") || cid.contains("\"") || cid.contains("/") || cid.contains("\\") || cid.contains(File.separator) || cid.contains(".") || cid.contains(" ") || cid.contains("\n") || cid.contains("\t")) {
                    if(lang.equals("ko")) throw new RuntimeException("ID 로는 알파벳과 숫자만 사용할 수 있습니다.");
                    else throw new RuntimeException("ID can only contains alphabets and numbers !");
                }
                
                JsonObject newAcc = new JsonObject();
                newAcc.put("id"    , cid);
                newAcc.put("idtype", cidtype.toUpperCase());
                newAcc.put("nick"  , cnick);
                newAcc.put("fail_cnt", "0");
                newAcc.put("fail_time", "0");
                
                JsonArray prvgroup = new JsonArray();
                prvgroup.add("user");
                if(cidtype.equalsIgnoreCase("A")) prvgroup.add("admin");
                newAcc.put("privgroup", prvgroup);
                newAcc.put("pw", SecurityUtil.hash(s1 + cpw + s2 + salt + cid + s3, "SHA-256"));
                
                // Register
                if(useJDBC) {
                    if(conn == null) {
                        Class.forName(jdbcClass);
                        conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                    }
                    
                    pstmt = conn.prepareStatement("INSERT INTO FS_USER (USERID, USERPW, USERNICK, USERTYPE, FAILCNT , FAILTIME, PRIVGROUP, PRIVILEGES) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                    pstmt.setString(1, cid);
                    pstmt.setString(2, newAcc.get("pw").toString());
                    pstmt.setString(3, cnick);
                    pstmt.setString(4, newAcc.get("idtype").toString());
                    pstmt.setInt(5, 0);
                    pstmt.setInt(6, 0);
                    if(cidtype.equalsIgnoreCase("A")) pstmt.setString(7, "[\"user\", \"admin\"]");
                    else                              pstmt.setString(7, "[\"user\"]");
                    pstmt.setString(8, "[]");
                    
                    pstmt.executeUpdate();
                    conn.commit();
                    
                    pstmt.close(); pstmt = null;
                } else {
                    if(! faJson.exists()) faJson.mkdirs();
                    if(! ftJson.exists()) ftJson.mkdirs();
                    
                    File fileAcc = new File(faJson.getCanonicalPath() + File.separator + cid + ".json");
                    fileAcc.getCanonicalPath(); // Check valid
                    fileOut = new FileOutputStream(fileAcc);
                    fileOut.write(newAcc.toJSON().getBytes(cs));
                    fileOut.close(); fileOut = null;
                }
            } else if(req.equalsIgnoreCase("userdel")) {
                if(readOnly) throw new RuntimeException("Blocked. FS is read-only mode.");
                
                String dId = request.getParameter("id");
                
                if(dId == null) dId = "";
                if(dId.equals("")) {
                    throw new RuntimeException("Invalid parameter. Please refresh the page !");
                }
                
                if(dId.contains("'") || dId.contains("\"") || dId.contains("/") || dId.contains("\\") || dId.contains(File.separator) || dId.contains(".") || dId.contains(" ") || dId.contains("\n") || dId.contains("\t")) {
                    if(lang.equals("ko")) throw new RuntimeException("ID 로는 알파벳과 숫자만 사용할 수 있습니다.");
                    else throw new RuntimeException("ID can only contains alphabets and numbers !");
                }
                
                File faJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "accounts");
                File ftJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "tokens");
                
                if(useJDBC) {
                    if(conn == null) {
                        Class.forName(jdbcClass);
                        conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                    }
                    
                    pstmt = conn.prepareStatement("SELECT COUNT(*) AS CNT FROM FS_USER WHERE USERID = ?");
                    pstmt.setString(1, dId);
                    
                    rs = pstmt.executeQuery();
                    
                    int counts = 0;
                    while(rs.next()) {
                        counts = rs.getInt("CNT");
                    }
                    
                    rs.close(); rs = null;
                    pstmt.close(); pstmt = null;
                    
                    if(counts <= 0) {
                        if(lang.equals("ko")) throw new RuntimeException("해당 ID의 사용자가 없습니다. 다시 검색 후 이용해 주세요.");
                        else                  throw new RuntimeException("There are no user using that ID. Please re-search.");
                    }
                    
                    pstmt = conn.prepareStatement("DELETE FROM FS_TOKEN WHERE USERID = ?");
                    pstmt.setString(1, dId);
                    pstmt.executeUpdate();
                    
                    conn.commit();
                    pstmt.close(); pstmt = null;
                    
                    pstmt = conn.prepareStatement("DELETE FROM FS_USER WHERE USERID = ?");
                    pstmt.setString(1, dId);
                    pstmt.executeUpdate();
                    
                    conn.commit();
                    pstmt.close(); pstmt = null;
                } else {
                    File fTarget = new File(faJson.getCanonicalPath() + File.separator + dId + ".json");
                    if(! fTarget.exists()) {
                        if(lang.equals("ko")) throw new RuntimeException("해당 ID의 사용자가 없습니다. 다시 검색 후 이용해 주세요.");
                        else                  throw new RuntimeException("There are no user using that ID. Please re-search.");
                    }
                    fTarget.delete();
                    
                    File dirTokens = new File(ftJson.getCanonicalFile() + File.separator + dId);
                    if(dirTokens.exists() && dirTokens.isDirectory()) {
                        File[] children = dirTokens.listFiles();
                        for(File f : children) {
                        	if(f.isDirectory()) {
                        		File[] gchildren = f.listFiles();
                        		for(File gf : gchildren) {
                        			if(gf.isDirectory()) continue;
                        			gf.delete();
                        		}
                        	}
                            f.delete();
                        }
                        dirTokens.delete();
                    }
                }
            } else if(req.equalsIgnoreCase("gblist")) {
            	garbage = new File(rootPath.getCanonicalPath() + File.separator + ".garbage");
                if(! garbage.exists()) garbage.mkdirs();
                
                JsonArray dates = new JsonArray();
                File[] lists = garbage.listFiles();
                for(File f : lists) {
                	if(f.isDirectory()) {
                		File[] children = f.listFiles();
                		for(File child : children) {
                			if(child.isDirectory()) {
                				File[] gchildren = child.listFiles();
                				for(File gchild : gchildren) {
                					if(gchild.isDirectory()) continue;
                					dates.add(gchild.getCanonicalPath().replace(garbage.getCanonicalPath(), ""));
                				}
                			}
                			dates.add(child.getCanonicalPath().replace(garbage.getCanonicalPath(), ""));
                		}
                	}
                	dates.add(f.getCanonicalPath().replace(garbage.getCanonicalPath(), ""));
                }
                json.put("garbages", dates);
            } else if(req.equalsIgnoreCase("cleangb")) {
            	garbage = new File(rootPath.getCanonicalPath() + File.separator + ".garbage");
                if(! garbage.exists()) garbage.mkdirs();
                
                File[] lists = garbage.listFiles();
                for(File f : lists) {
                	if(f.isDirectory()) {
                		File[] children = f.listFiles();
                		for(File child : children) {
                			if(child.isDirectory()) {
                				File[] gchildren = child.listFiles();
                				for(File gchild : gchildren) {
                					if(gchild.isDirectory()) continue;
                					gchild.delete();
                				}
                			}
                			child.delete();
                		}
                	}
                	f.delete();
                }
            }
            
            if(! req.equalsIgnoreCase("reset")) {
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
            if(rs      != null) { try { rs.close();       } catch(Throwable tx) {} }
            if(pstmt   != null) { try { pstmt.close();    } catch(Throwable tx) {} }
            if(conn    != null) { try { conn.close();     } catch(Throwable tx) {} }
            if(fileOut != null) { try { fileOut.close();  } catch(Throwable tx) {} }
            if(propIn  != null) { try { propIn.close();   } catch(Throwable tx) {} }
        }
        
        return json;
    }
    
    /** Called from fsconsole.jsp, which is called by console page by ajax. */
    public JsonObject console(HttpServletRequest request) throws Exception {
        JsonObject json = processHandler("console", request);
        if(json != null) return json;
        json = new JsonObject();
                
        JsonObject sessionMap = null;
        String lang = "en";
        
        try {
            if(noConsole) {
                if(lang.equals("ko")) throw new RuntimeException("콘솔 기능이 비활성화되어 있습니다.");
                else                  throw new RuntimeException("The console feature is disabled.");
            }
            
            sessionMap = getSessionFSObject(request);
            lang       = getLanguage(request);
            
            json.put("message", "");
            
            FSConsole console = (FSConsole) getSessionObject(request, "fsscen");
            if(console == null) {
                console = FSConsole.getInstance();
                setSessionObject(request, "fsscen", console);
            }
            
            String path, command;
            path    = request.getParameter("path");
            command = request.getParameter("command");
            
            if(path    == null) throw new RuntimeException("Wrong parameter. Please refresh the page !");
            if(command == null) command = "";
            
            console.setPath(path);
            
            // Session infos for console
            Map<String, Object> sessionNewMap = new HashMap<String, Object>();
            if(sessionMap == null) {
                sessionNewMap.put("id"        , "guest");
                sessionNewMap.put("nick"      , "GUEST");
                sessionNewMap.put("idtype"    , "G");
                sessionNewMap.put("privileges", new JsonArray());
                sessionNewMap.put("privgroup" , new JsonArray());
            } else {
                sessionNewMap.put("id"        , sessionMap.get("id"));
                sessionNewMap.put("idtype"    , sessionMap.get("idtype"));
                sessionNewMap.put("nick"      , sessionMap.get("nick"));
                sessionNewMap.put("privileges", sessionMap.get("privileges"));
                sessionNewMap.put("privgroup" , sessionMap.get("privgroup"));
            }
            
            sessionNewMap.put("lang", lang);
            
            // Getting hidden directories
            Object oHiddenDir = conf.get("HiddenDirs");
            List<String> hiddenDirList = new ArrayList<String>();
            
            String idtype = sessionNewMap.get("idtype").toString();
            
            if(oHiddenDir != null) {
                JsonArray hiddenDir = null;
                if(oHiddenDir instanceof JsonArray) hiddenDir = (JsonArray) oHiddenDir;
                else                                hiddenDir = (JsonArray) JsonCompatibleUtil.parseJson(oHiddenDir.toString().trim());
                oHiddenDir = null;
                
                if(idtype.equalsIgnoreCase("A")) {
                    hiddenDirList.clear();
                } else {
                    if(hiddenDir != null) {
                        for(Object obj : hiddenDir) {
                            if(obj == null) continue;
                            hiddenDirList.add(obj.toString().trim());
                        }
                    }
                    
                    JsonArray dirPrv = null;
                    Object oDirPrv = (Object) sessionNewMap.get("privileges");
                    if(oDirPrv != null) {
                        dirPrv = null;
                        if(oDirPrv instanceof JsonArray) {
                            dirPrv = (JsonArray) oDirPrv;
                        } else {
                            dirPrv = (JsonArray) JsonCompatibleUtil.parseJson(oDirPrv.toString().trim());
                        }
                    }
                    
                    if(dirPrv != null) {
                        for(Object row : dirPrv) {
                            JsonObject dirOne = null;
                            if(row instanceof JsonObject) dirOne = (JsonObject) row;
                            else                          dirOne = (JsonObject) JsonCompatibleUtil.parseJson(row.toString().trim());
                            
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
                                logIn("Wrong account configuration - " + t.getMessage());
                            }
                        }
                    }
                }
            }
            
            sessionNewMap.put("hiddendirs", hiddenDirList);
            
            // RUN
            FSConsoleResult rs = console.run(this, sessionNewMap, command);
            
            // If result marked as a logout, clean session.
            if(rs.isLogout()) removeSessionObject(request, "fssession");
            
            if(rs.isSavetoken()) {
            	setSessionObject(request, "fsscen", console);
            }
            
            String rsPath = rs.getPath();
            if(rsPath != null) rsPath = rsPath.replace("\\", "/");
            
            // Make result JSON
            json.put("success", new Boolean(true));
            json.put("path"   , rsPath);
            json.put("display", rs.getDisplay());
            json.put("displaynull", new Boolean(rs.isNulll()));
            json.put("logout", new Boolean(rs.isLogout()));
            json.put("closepopup", new Boolean(rs.isClosepopup()));
            
            if(rs.getDownloadAccepted() != null) {
                json.put("downloadaccept", new Boolean(true));
                json.put("downloadfile"  , rs.getDownloadAccepted());
                setSessionObject(request, "fsd_captcha_code", "SKIP");
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
    
    /** Called from fs.jsp, which is called by file list page by ajax. */
    public JsonObject list(HttpServletRequest request, String pPath, String pKeyword, String pExcept) {
    	initialize(request.getContextPath());
        
        JsonObject json = processHandler("list", request);
        if(json != null) return json;
        json = new JsonObject();
        
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
            
            FSFileListingResult flist = lister.list(this, dir, keyword, excepts);
            excepts.clear();
            excepts = null;
            
            json.put("skipped" , new Integer(flist.getSkippedCount()));
            json.put("excepted", new Integer(flist.getExceptsCount()));
            
            Collections.sort(flist.getDirs());
            Collections.sort(flist.getFiles());
            
            String pathDisp = pathParam; // 화면 출력용
            if(pathDisp.startsWith("//")) pathDisp = pathDisp.substring(1);
            if(! pathDisp.startsWith("/")) pathDisp = "/" + pathDisp;
            pathParam = pathParam.replace("\\", "/");
            
            json.put("type", "list");
            json.put("keyword", keyword);
            json.put("path"   , pathParam);
            json.put("dpath"  , pathDisp);

            jsonSess = getSessionFSObject(request);
            String idtype = getSessionUserType(jsonSess);
            List<String> hiddenDirList = getHiddensOnCurrentUser(jsonSess, pathParam, false);
            if(hiddenDirList == null) hiddenDirList = new ArrayList<String>();
            
            if(idtype.equalsIgnoreCase("A")) {
                if(readOnly) json.put("privilege", "view");
                else         json.put("privilege", "edit");
            } else {
                String prv = null;
                JsonArray dirPrv = getSessionDirectoryPrivileges(jsonSess);   
                for(Object row : dirPrv) {
                    JsonObject dirOne = null;
                    if(row instanceof JsonObject) dirOne = (JsonObject) row;
                    else                          dirOne = (JsonObject) JsonCompatibleUtil.parseJson(row.toString().trim());
                    
                    try {
                        String dPath = dirOne.get("path"     ).toString();
                        String dPrv  = dirOne.get("privilege").toString();
                        
                        if(pathParam.startsWith(dPath) || ("/" + pathParam).startsWith(dPath)) {
                            prv = dPrv.toLowerCase();
                            break;
                        }
                    } catch(Throwable t) {
                        logIn("Wrong account configuration - " + t.getMessage());
                    }
                }
                
                if(prv == null) {
                    prv = "view";
                    for(String h : hiddenDirList) {
                        if(pathParam.startsWith(h))         prv = "none";
                        if(("/" + pathParam).startsWith(h)) prv = "none";
                    }
                }
                
                if(prv.equals("none")) {
                    json.put("directories", new JsonArray());
                    json.put("files", new JsonArray());
                    if(json.get("privilege") == null) json.put("privilege", "view");
                    json.put("success", new Boolean(true));
                    return json;
                }
                
                if(readOnly && prv.equals("edit")) prv = "view";
                json.put("privilege", prv);
            }
            
            JsonArray dirs = new JsonArray();
            for(File f : flist.getDirs()) {
                String name = f.getName();
                // if(! keyword.equals("")) { if(! name.toLowerCase().contains(keyword.toLowerCase())) continue; }
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
            StringTokenizer dotTokenizer = null;
            for(File f : flist.getFiles()) {
                String name = f.getName();
                if(! keyword.equals("")) { if(! name.toLowerCase().contains(keyword.toLowerCase())) continue; }
                
                String linkDisp = name.replace("\"", "'");
                
                JsonObject fileOne = new JsonObject();
                
                long fSize = f.length();
                
                fileOne.put("type", "file");
                fileOne.put("name", linkDisp);
                fileOne.put("size", FSUtils.getFileSize(fSize));
                fileOne.put("over_down", new Boolean(fSize / 1024 > limitSize));
                fileOne.put("over_prev", new Boolean(fSize / 1024 > limitPrev));
                
                if(limitPrev > 0) {
                    // Get Extension of file
                    dotTokenizer = new StringTokenizer(name, ".");
                    String ext = "";
                    if(dotTokenizer.countTokens() >= 2) {
                        while(dotTokenizer.hasMoreTokens()) {
                            ext = dotTokenizer.nextToken();
                        }
                    }
                    ext = ext.trim().toLowerCase();
                    dotTokenizer = null;
                    
                    // Get Content Types
                    FSContentType thisType = null;
                    for(FSContentType ty : ftypes) {
                        if(ty.getExtension().equals(ext)) {
                            thisType = ty;
                            break;
                        }
                    }
                    ext = null;
                    if(thisType == null) {
                        fileOne.put("contentType", "application/octet-stream");
                        fileOne.put("previewType", FSContentType.PREVIEW_TYPE_NONE);
                    } else {
                        fileOne.put("contentType", thisType.getContentType());
                        fileOne.put("previewType", new Integer(thisType.getPreviewType()));
                    }
                } else {
                    fileOne.put("contentType", "application/octet-stream");
                    fileOne.put("previewType", FSContentType.PREVIEW_TYPE_NONE);
                }
                
                files.add(fileOne);
            }
            json.put("files", files);
            if(json.get("privilege") == null) json.put("privilege", "view");
            json.put("success", new Boolean(true));
        } catch(Throwable t) {
            t.printStackTrace();
            if(jsonSess != null) jsonSess.clear();
            json.put("success", new Boolean(false));
            if(t instanceof RuntimeException) {
                json.put("message", t.getMessage());
            } else {
                json.put("message", "Error : " + t.getMessage());
            }
        }
        json.put("session", jsonSess);
        
        return json;
    }
    
    /** Called from fs.jsp, which is called by file list page by ajax. */
    public JsonObject listAll(HttpServletRequest request) {
        JsonObject json = processHandler("listAll", request);
        if(json != null) return json;
        json = new JsonObject();
        
        String pathParam = request.getParameter("path");
        if(pathParam == null) pathParam = "";
        pathParam = pathParam.trim();
        if(pathParam.equals("/")) pathParam = "";
        pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
        if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);

        String keyword = request.getParameter("keyword");
        if(keyword == null) keyword = "";
        keyword = keyword.replace("'", "").replace("\"", "").replace("<", "").replace(">", "").trim();
        
        JsonObject jsonSess = getSessionFSObject(request);
        String     lang     = getLanguage(request);
        
        if(keyword.equals("")) {
            json.put("success", new Boolean(false));
            if(lang.equals("ko")) json.put("message", "전체 디렉토리 검색 기능을 이용하려면 검색어가 반드시 필요합니다.");
            else                  json.put("message", "Please input the keyword to search whole path !");
            return json;
        }
        
        if(keyword.length() <= 1) {
            json.put("success", new Boolean(false));
            if(lang.equals("ko")) json.put("message", "전체 디렉토리 검색 기능을 이용하려면 검색어가 2자 이상 필요합니다.");
            else                  json.put("message", "Please input the keyword more than 2 letters to search whole path !");
            return json;
        }
        
        List<String> hiddenDirList = getHiddensOnCurrentUser(jsonSess, pathParam, false);
        if(hiddenDirList == null) hiddenDirList = new ArrayList<String>();
        
        final List<String> hiddenDirListF = hiddenDirList;
        hiddenDirList = null;
        
        try {
            File dir = new File(rootPath.getCanonicalPath() + File.separator + pathParam);
            List<String> res = FSUtils.find(dir, pathParam, keyword, limitSize, new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    String name = pathname.getName();
                    
                    if(name.equals(".garbage")) return false;
                    if(name.equals(".upload" )) return false;
                    
                    String linkDisp = null;
                    try {
                        linkDisp = pathname.getCanonicalPath().replace(getRootPath().getCanonicalPath(), "").replace("\\", "/").replace("'", "").replace("\"", "");
                        if(pathname.isDirectory() && linkDisp.indexOf(".") == 0) return false;
                        linkDisp = linkDisp.replace("\\", "/");
                        if(linkDisp.indexOf("/") == 0) linkDisp = linkDisp.substring(1);
                        
                        for(String h : hiddenDirListF) {
                            if(linkDisp.startsWith(h        )) return false;
                            if(("/" + linkDisp).startsWith(h)) return false;
                        }
                    } catch(IOException ex) {
                        log("Exception on searching all directory at " + name + " - (" + ex.getClass().getName()+ ")" + ex.getMessage(), FSControl.class);
                        return false;
                    }
                    return true;
                }
            });
            
            JsonArray arr = new JsonArray();
            for(String r : res) {
                r = r.replace("\\", "/");
                arr.add(r); 
            }
            res.clear();
            
            json.put("list", arr);
            json.put("success", new Boolean(true));
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
    
    /** Called from fs.jsp, which is called by file list page by ajax. */
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
    
    /** Called from fscaptin.jsp, which is called by captcha page by iframe. */
    public String createCaptchaBase64(HttpServletRequest request, String key, String code, long time, double scale, String theme) {
        // initialize(request.getContextPath());
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
                setSessionObject(request, key + "_captcha_code", code);
            }

            return FSUtils.createImageCaptchaBase64(code, captchaWidth, captchaHeight, captchaNoises, captchaFontSize, captDarkMode, null);
        } catch(Throwable t) {
            t.printStackTrace();
            return "Error : " + t.getMessage();
        }
    }
    
    /** Called from fscaptin.jsp, which is called by captcha page by iframe. */
    public String createTextCaptcha(HttpServletRequest request, String key, String code, long time) {
        try {
            if(code == null) {
                code = "REFRESH";
            }
            
            long now = System.currentTimeMillis();

            if(now - time >= captchaLimitTime) {
                code = "REFRESH";
                setSessionObject(request, key + "_captcha_code", code);
            }

            return GUIUtil.createTextCaptcha(code);
        } catch(Throwable t) {
            t.printStackTrace();
            return "Error : " + t.getMessage();
        }
    }
    
    /** Called from fsupload.jsp, which is called by uploading popup. */
    public String upload(HttpServletRequest request) {
        // initialize(request.getContextPath());
        
        String uIdType = "", msg = "";
        JsonArray dirPrv = null;
        try {
            if(readOnly) throw new RuntimeException("Blocked. FS is read-only mode.");
            
            JsonObject sessionMap = getSessionFSObject(request);
            
            if(sessionMap != null) {
                // uId     = sessionMap.get("id"    ).toString();
                uIdType = sessionMap.get("idtype").toString();
                
                Object oDirPrv = (Object) sessionMap.get("privileges");
                if(oDirPrv != null) {
                    if(oDirPrv instanceof JsonArray) {
                        dirPrv = (JsonArray) oDirPrv;
                    } else {
                        dirPrv = (JsonArray) JsonCompatibleUtil.parseJson(oDirPrv.toString().trim());
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
                    else                          dirOne = (JsonObject) JsonCompatibleUtil.parseJson(row.toString().trim());
                    
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
                        logIn("Wrong account configuration - " + t.getMessage());
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
                    logIn("Upload complete but cannot move the file from temp directory to destination !");
                    logIn("File : " + fileOne.getCanonicalPath());
                    logIn("Dest : " + destFil.getCanonicalPath());
                    logIn("COS File System Name : " + fileName);
                    logIn("COS Original Name : " + fileOrig);
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
    
    private int downloadSerial = 0;
    public void download(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        request.setCharacterEncoding("UTF-8");
        
        long now = System.currentTimeMillis();
        
        if(this.downloadSerial >= Integer.MAX_VALUE - 10) this.downloadSerial = 0;
        this.downloadSerial++;
        int downloadSerial = this.downloadSerial;
        
        StringBuilder results = new StringBuilder("");
        
        String clients = request.getHeader("User-Agent");

        String pathParam = request.getParameter("path");
        String fileName  = request.getParameter("filename");
        String mode      = request.getParameter("mode");

        String capt      = request.getParameter("captcha");
        String code      = (String) getSessionObject(request, "fsd_captcha_code");
        Long   time      = (Long)   getSessionObject(request, "fsd_captcha_time");

        if(code == null) code = "REFRESH";
        if(capt == null) capt = "";
        if(mode == null) mode = "DOWNLOAD";
        mode = mode.trim().toUpperCase();

        if(pathParam == null) pathParam = "";
        pathParam = pathParam.trim();
        if(pathParam.equals("/")) pathParam = "";
        pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
        if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);
        
        JsonObject sessions = getSessionFSObject(request);
        int speed = getSessionUserSpeedConst(sessions);
        String lang  = getLanguage(request);
        
        if(lang == null) lang = "en";
        
        int sleepRoutine = this.sleepRoutine;
        sleepRoutine = (int) Math.round(sleepRoutine * speed);
        
        boolean captchaSkipped = false;
        boolean viewMode = (mode.equals("VIEWER") || mode.equals("VIEW"));

        FileInputStream fIn = null;
        OutputStream outputs = null;
        File file = null;
        int dBufferSize = this.bufferSize * 1024;
        byte[] buffers = new byte[dBufferSize];
        
        try {
        	if(! FSUtils.canBeFileName(fileName)) throw new RuntimeException("Illegal character on file's name - " + fileName);
        	
            if(captchaDownload) {
                if(! viewMode) {
                    if(code.equals("SKIP")) {
                        capt = code;
                        time = new Long(now);
                    }
                    
                    if(! code.equals(capt)) {
                        throw new RuntimeException("Wrong captcha code !");
                    }
                    
                    if(now - time.longValue() >= captchaLimitTime) {
                        code = "REFRESH";
                        setSessionObject(request, "fsd_captcha_code", code);
                    }
                    
                    if(code.equals("REFRESH")) {
                        throw new RuntimeException("Too old captcha code !");
                    }
                    
                    if(code.equals("SKIP")) {
                        code = "REFRESH";
                        setSessionObject(request, "fsd_captcha_code", code);
                    }
                } else {
                    captchaSkipped = true;
                }
            }
            
            List<String> hiddenDirList = getHiddensOnCurrentUser(sessions, pathParam, false);
            if(hiddenDirList == null) hiddenDirList = new ArrayList<String>();
            
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
                logIn("No File ! " + file.getCanonicalPath() + " <-- " + rootPath.getCanonicalPath() + File.separator + pathParam.replace("/", File.separator) + File.separator + fileName);
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
            } else if(viewMode) {
                // Get Extension of file
                StringTokenizer dotTokenizer = new StringTokenizer(fileName, ".");
                String ext = "";
                if(dotTokenizer.countTokens() >= 2) {
                    while(dotTokenizer.hasMoreTokens()) {
                        ext = dotTokenizer.nextToken();
                    }
                }
                ext = ext.trim().toLowerCase();
                
                FSContentType cttype = null;
                for(FSContentType ty : ftypes) {
                    if(ty.getExtension().equals(ext)) {
                        cttype = ty;
                        break;
                    }
                }
                
                if(cttype == null) {
                    contentType = "application/octet-stream";
                } else {
                    contentType = cttype.getContentType();
                }
                
                if(! contentType.equals("application/octet-stream")) contentDisp = "inline";
            } else {
                contentDisp = "attachment";
                contentType = "application/octet-stream";
            }
            
            if(viewMode) {
                if(contentType.equals("application/octet-stream")) {
                    throw new RuntimeException("Cannot view this file without download. Unavailable content type. Try download this.");
                }
                if(captchaSkipped && (dataLength / 1024 >= limitPrev)) {
                    throw new RuntimeException("Cannot view this file without download. Too big. Try download this.");
                }
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
            
            logIn("Download requested, " + file.getName() + " at " + System.currentTimeMillis() + " from " + request.getRemoteAddr() + ", serial : " + downloadSerial);
            
            int sleepCnt = sleepRoutine;
            fIn = new FileInputStream(file);
            while(true) {
                int readLengths = fIn.read(buffers, 0, dBufferSize);
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
            fIn     = null;
            outputs = null;
            logIn("Download end, " + file.getName() + " at " + System.currentTimeMillis() + " from " + request.getRemoteAddr() + ", serial : " + downloadSerial);
            return;
        } catch(Throwable tx) {
            try {
                if(((tx instanceof java.net.SocketException) || (tx.getCause() != null && (tx.getCause() instanceof java.net.SocketException)))) {
                    logIn("SocketException - " + tx.getMessage() + "... May be, downloading cancelled by client. Serial : " + downloadSerial);
                } else {
                    logIn("Exception message while sending file : (" + tx.getClass().getName() + ") " + tx.getMessage() + "... Serial : " + downloadSerial);
                    
                    response.reset();
                    response.setContentType("text/html;charset=UTF-8");
                    
                    results = results.append("<pre style='background-color: gray; color: black; padding: 5px 5px 5px 5px;'>").append("Error : ").append(tx.getMessage()).append("</pre>");
                    if(outputs == null) outputs = response.getOutputStream();
                    outputs.write(results.toString().getBytes(cs));
                }
            } catch(Throwable tIn) { logIn("Exception when processing main exception on performing download - (" + tIn.getClass().getName() + ") " + tIn.getMessage()); }
        } finally {
            if(fIn     != null) { try { fIn.close();       } catch(Throwable te) {}}
            if(outputs != null) { try { outputs.close();   } catch(Throwable te) {}}
        }
    }
    
    public JsonObject mkdir(HttpServletRequest request) {
        JsonObject json = processHandler("mkdir", request);
        if(json != null) return json;
        json = new JsonObject();
        
        String uIdType = "";
        JsonArray dirPrv = null;
        try {
            JsonObject sessionMap = getSessionFSObject(request);
            
            if(sessionMap != null) {
                // uId     = sessionMap.get("id"    ).toString();
                uIdType = sessionMap.get("idtype").toString();
                
                Object oDirPrv = (Object) sessionMap.get("privileges");
                if(oDirPrv != null) {
                    if(oDirPrv instanceof JsonArray) {
                        dirPrv = (JsonArray) oDirPrv;
                    } else {
                        dirPrv = (JsonArray) JsonCompatibleUtil.parseJson(oDirPrv.toString().trim());
                    }
                }
            }
        } catch(Throwable t) {
            t.printStackTrace();
        }

        json.put("success", new Boolean(false));
        json.put("message", "");
        
        try {
        	if(readOnly) throw new RuntimeException("Blocked. FS is read-only mode.");
        	
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
                    else                          dirOne = (JsonObject) JsonCompatibleUtil.parseJson(row.toString().trim());
                    
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
                        logIn("Wrong account configuration - " + t.getMessage());
                    }
                }
            }
            if(dirPrv != null) dirPrv.clear();
            
            if(hasPriv) {
                String dirName = request.getParameter("name");
                
                if(! FSUtils.canBeFileName(dirName)) throw new RuntimeException("Illegal character on file's name - " + dirName);
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
        JsonObject json = processHandler("remove", request);
        if(json != null) return json;
        json = new JsonObject();
        
        String uIdType = "";
        JsonArray dirPrv = null;
        try {
            JsonObject sessionMap = getSessionFSObject(request);
            
            if(sessionMap != null) {
                // uId     = sessionMap.get("id"    ).toString();
                uIdType = sessionMap.get("idtype").toString();
                
                Object oDirPrv = (Object) sessionMap.get("privileges");
                if(oDirPrv != null) {
                    if(oDirPrv instanceof JsonArray) {
                        dirPrv = (JsonArray) oDirPrv;
                    } else {
                        dirPrv = (JsonArray) JsonCompatibleUtil.parseJson(oDirPrv.toString().trim());
                    }
                }
            }
        } catch(Throwable t) {
            t.printStackTrace();
        }

        json.put("success", new Boolean(false));
        json.put("message", "");

        try {
        	if(readOnly) throw new RuntimeException("Blocked. FS is read-only mode.");
        	
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
                    else                          dirOne = (JsonObject) JsonCompatibleUtil.parseJson(row.toString().trim());
                    
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
                        logIn("Wrong account configuration - " + t.getMessage());
                    }
                }
            }
            if(dirPrv != null) dirPrv.clear();
            
            if(hasPriv) {
                String fileName = request.getParameter("name");
                File file;
                
                if(fileName != null) {
                	if(! FSUtils.canBeFileName(fileName)) throw new RuntimeException("Illegal character on file's name - " + fileName);
                }
                
                if(delType.equals("dir")) file = new File(rootPath.getCanonicalPath() + File.separator + pathParam.replace("/", File.separator));
                else                      file = new File(rootPath.getCanonicalPath() + File.separator + pathParam.replace("/", File.separator) + File.separator + fileName);
                
                if(! file.exists()) {
                    logIn("There is no file ! " + file.getCanonicalPath());
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
        JsonObject json = processHandler("account", request);
        if(json != null) return json;
        json = new JsonObject();
        
        JsonObject sessionMap = null;
        JsonObject accountOne = null;
        boolean needInvalidate = false;
        try {
            String sessionJson = (String) getSessionObject(request, "fssession");
            
            if(sessionJson != null) {
                sessionMap = (JsonObject) JsonCompatibleUtil.parseJson(sessionJson.trim());
                
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

        json.put("success", new Boolean(false));
        json.put("message", "");
        
        String lang = getLanguage(request);

        FileInputStream fIn = null;
        Reader r1 = null, r2 = null;
        FileOutputStream fOut = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
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
            
            File ftJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "tokens");
            if(! ftJson.exists()) ftJson.mkdirs();
            
            String tokenID  = null;
            String tokenVal = null;
            
            if(useToken && tokenLifeTime >= 1) {
                tokenID  = request.getHeader("fsid");
                tokenVal = request.getHeader("fstoken");
                
                if(tokenID == null || tokenVal == null) {
                    if(request.getParameter("fstoken_id") != null && request.getParameter("fstoken_val") != null) {
                        tokenID  = request.getParameter("fstoken_id");
                        tokenVal = request.getParameter("fstoken_val");
                    }
                }
                
                if(! isTokenAvail(tokenID, tokenVal)) {
                    tokenID  = null;
                    tokenVal = null;
                }
                
                if(tokenID == null && tokenVal == null) {
                    if(sessionMap == null) {
                        tokenID  = "GUEST" + SecurityUtil.hash(request.getRemoteHost() + "_" + request.getRemoteAddr(), "SHA-256").substring(0, 15);
                    } else {
                        tokenID = sessionMap.get("id").toString();
                    }
                    tokenVal = FSUtils.createToken(tokenID, String.valueOf(conf.get("S1")), String.valueOf(conf.get("S2")), String.valueOf(conf.get("S3")));
                    createToken(tokenID, tokenVal, now + (1000L * 60 * tokenLifeTime), null);
                }
                
                if(tokenID != null && tokenVal != null) {
                    json.put("id"     , tokenID);
                    json.put("token"  , tokenVal);
                }
            }
            
            if(req.equalsIgnoreCase("status")) {
                json.put("success", new Boolean(true));
            }
            
            if(req.equalsIgnoreCase("language")) {
                String tlng = request.getParameter("language");
                String frce = request.getParameter("force");
                if(tlng == null) tlng = "";
                tlng = FSUtils.removeSpecials(tlng);
                
                if(frce == null) frce = "false";
                
                boolean applys = false;
                if(getSessionObject(request, "fslanguage") != null) {
                    if(DataUtil.parseBoolean(frce)) {
                        applys = true;
                    }
                } else {
                    applys = true;
                }
                
                if(applys) {
                    if(tlng.equals("")) {
                        removeSessionObject(request, "fslanguage");
                    } else {
                        setSessionObject(request, "fslanguage", tlng);
                    }
                }
                
                json.put("success", new Boolean(true));
            }
            
            if(req.equalsIgnoreCase("logout")) {
            	String outId = "UNKNOWN";
            	if(sessionMap != null) {
            		if(sessionMap.get("id") != null) outId = sessionMap.get("id").toString();
            	}
            	
                sessionMap = null;
                needInvalidate = true;
                
                if(tokenID != null && tokenVal != null) {
                	if(outId.equals("UNKNOWN")) outId = tokenID;
                	
                    // Delete Token
                    File ftId = new File(ftJson.getCanonicalPath() + File.separator + tokenID);
                    if(ftId.exists() && ftId.isDirectory()) {
                        File[] fTokens = ftId.listFiles();
                        for(File f : fTokens) {
                            try {
                                String t = FileUtil.readString(f, "UTF-8");
                                JsonObject tJson = (JsonObject) JsonCompatibleUtil.parseJson(t);
                                String tokenOne = String.valueOf(tJson.get("token"));
                                if(tokenOne.equals(tokenVal)) {
                                    f.delete();
                                }
                            } catch(Throwable tToken) {
                                log("Exception when deleting token - (" + tToken.getClass().getName() + ") " + tToken.getMessage(), this.getClass());
                            }
                        }
                    }
                    
                    if(useJDBC) {
                        try {
                            if(conn == null) {
                                Class.forName(jdbcClass);
                                conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                            }
                            
                            pstmt = conn.prepareStatement("DELETE FROM FS_TOKEN WHERE TOKEN = ?");
                            pstmt.setString(1, tokenVal);
                            
                            pstmt.executeUpdate();
                            conn.commit();
                            
                            pstmt.close(); pstmt = null;
                            conn.close(); conn = null;
                        } catch(Throwable tToken) {
                            log("Exception when deleting token - (" + tToken.getClass().getName() + ") " + tToken.getMessage(), this.getClass());
                        } finally {
                            ClassUtil.closeAll(pstmt, conn);
                        }
                    }
                }
                
                if(lang.equals("ko")) msg = "로그아웃 되었습니다."; 
                else                  msg = "Log out complete";
                logIn("[LOGOUT][SUCCESS] " + outId + " at " + now + " from " + request.getRemoteAddr());
                json.put("success", new Boolean(true));
            }
            
            if(req.equalsIgnoreCase("login")) {
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
                
                if(id.equalsIgnoreCase("guest")) {
                    if(lang.equals("ko")) msg = "ID는 guest 일 수 없습니다.";
                    else                  msg = "ID cannot be 'guest' !";
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
                    String ccode = (String) getSessionObject(request, "fsl_captcha_code");
                    Long   ctime = (Long)   getSessionObject(request, "fsl_captcha_time");
                    
                    if(ccode == null) ccode = "REFRESH";
                    if(ccapt == null) ccapt = "";
                    
                    if(! ccode.equals(ccapt)) {
                        if(lang.equals("ko")) throw new RuntimeException("Wrong captcha code !");
                        else                  throw new RuntimeException("코드를 올바르게 입력해 주세요.");
                    }
                    
                    if(now - ctime.longValue() >= captchaLimitTime) {
                        ccode = "REFRESH";
                        setSessionObject(request, "fsl_captcha_code", ccode);
                    }
                    
                    if(ccode.equals("REFRESH")) {
                        if(lang.equals("ko")) throw new RuntimeException("Too old captcha code !");
                        else                  throw new RuntimeException("코드가 오래되었습니다. 새로 고침 후 이용해 주세요.");
                    }
                }
                
                logIn("Login requested ! " + id + " at " + now + " from " + request.getRemoteAddr());
                
                if(msg.equals("")) {
                    if(useJDBC) {
                        if(conn == null) {
                            Class.forName(jdbcClass);
                            conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                        }
                        
                        pstmt = conn.prepareStatement("SELECT USERID, USERPW, USERNICK, USERTYPE, FAILCNT, FAILTIME, PRIVILEGES FROM FS_USER WHERE USERID = ?");
                        pstmt.setString(1, id);
                        
                        rs = pstmt.executeQuery();
                        JsonObject accountOneTmp = new JsonObject();
                        
                        while(rs.next()) {
                            accountOneTmp.put("id"       , rs.getString("USERID"));
                            accountOneTmp.put("idtype"   , rs.getString("USERTYPE"));
                            accountOneTmp.put("nick"     , rs.getString("USERNICK"));
                            accountOneTmp.put("fail_cnt" , rs.getLong("FAILCNT"));
                            accountOneTmp.put("fail_time", rs.getLong("FAILTIME"));
                        }
                        
                        rs.close(); rs = null;
                        pstmt.close(); pstmt = null;
                        
                        accountOne = accountOneTmp;
                    } else {
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
                            
                            accountOne = (JsonObject) JsonCompatibleUtil.parseJson(lineCollector.toString().trim());
                            lineCollector.setLength(0);
                            lineCollector = null;
                        }
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
                                                
                                if(useJDBC) {
                                    pstmt = conn.prepareStatement("UPDATE FS_USER SET FAILCNT = ?, FAILTIME = ? WHERE USERID = ?");
                                    pstmt.setLong(1, failCnt);
                                    pstmt.setLong(2, failTime);
                                    pstmt.setString(3, id);
                                    
                                    pstmt.executeUpdate();
                                    conn.commit();
                                    pstmt.close();
                                } else {
                                    File fileAcc = new File(faJson.getCanonicalPath() + File.separator + id + ".json");
                                    fOut = new FileOutputStream(fileAcc);
                                    fOut.write(accountOne.toJSON().getBytes(cs));
                                    fOut.close(); fOut = null;
                                }
                                
                                accChanging = false;
                                msg = "Wrong password !";
                            }
                        }
                    }
                    
                    if(msg.equals("")) {
                        // Success to login
                    	log("[LOGIN][ACCEPTED] " + accountOne.get("id") + " at " + now + " from " + request.getRemoteAddr(), this.getClass());
                    	
                        if(failCnt >= 1) {
                            // Reset Fail Count
                            failCnt = 0;
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
                            
                            if(useJDBC) {
                                pstmt = conn.prepareStatement("UPDATE FS_USER SET FAILCNT = ?, FAILTIME = ? WHERE USERID = ?");
                                pstmt.setLong(1, failCnt);
                                pstmt.setLong(2, failTime);
                                pstmt.setString(3, id);
                                
                                pstmt.executeUpdate();
                                conn.commit();
                                pstmt.close();
                            } else {
                                File fileAcc = new File(faJson.getCanonicalPath() + File.separator + id + ".json");
                                fOut = new FileOutputStream(fileAcc);
                                fOut.write(accountOne.toJSON().getBytes(cs));
                                fOut.close(); fOut = null;
                            }
                            accChanging = false;
                        }
                        sessionMap = accountOne;
                        
                        // Set session and create token
                        
                        JsonObject accountJsonNew = new JsonObject();
                        accountJsonNew.put("id"    , accountOne.get("id"));
                        accountJsonNew.put("idtype", accountOne.get("idtype"));
                        accountJsonNew.put("nick"  , accountOne.get("nick"));
                        
                        String nId = accountJsonNew.get("id").toString();
                        setSessionObject(request, "fssession", accountJsonNew.toJSON());
                        
                        if(noLogin) tokenLifeTime = 0;
                        if(tokenLifeTime > 0) {
                            // Check before token (may be a guest token)
                            if(tokenID != null && tokenVal != null) {
                                if(useJDBC) {
                                    pstmt = conn.prepareStatement("DELETE FROM FS_TOKEN WHERE TOKEN = ? AND USERID = ?");
                                    pstmt.setString(1, tokenVal);
                                    pstmt.setString(2, tokenID);
                                    pstmt.executeUpdate();
                                    conn.commit();
                                    
                                    pstmt.close();
                                    pstmt = null;
                                } else {
                                    File dirTokens = new File(ftJson.getCanonicalFile() + File.separator + tokenID);
                                    if(dirTokens.exists()) {
                                        File[] lists = dirTokens.listFiles();
                                        for(File f : lists) {
                                            if(f.isDirectory()) {
                                            	File[] children = f.listFiles();
                                            	for(File c : children) {
                                            		if(c.isDirectory()) {
                                            			File[] gchildren = c.listFiles();
                                            			for(File gc : gchildren) {
                                            				if(gc.isDirectory()) continue;
                                            				gc.delete();
                                            			}
                                            		}
                                            		c.delete();
                                            	}
                                            }
                                            f.delete();
                                        }
                                    }
                                }
                            }
                            
                            // Create new token
                            String newToken = FSUtils.createToken(nId, String.valueOf(conf.get("S1")), String.valueOf(conf.get("S2")), String.valueOf(conf.get("S3")));
                            
                            JsonObject tokenSessionObj = new JsonObject();
                            tokenSessionObj.put("fssession", accountJsonNew.toJSON());
                            accountJsonNew = null;
                            
                            // Save token
                            createToken(nId, newToken, now + (1000L * 60 * tokenLifeTime), tokenSessionObj);
                            tokenSessionObj = null;
                            
                            tokenID  = nId;
                            tokenVal = newToken;
                            json.put("token", newToken);
                        }
                        
                        logIn("[LOGIN][SUCCESS] " + id);
                        needInvalidate = false;
                        json.put("success", new Boolean(true));
                    } else {
                    	log("[LOGIN][FAIL] " + accountOne.get("id") + " at " + now + " from " + request.getRemoteAddr() + " - " + msg, this.getClass());
                    }
                }
            }
            
            json.put("type", "sessionstatus");
            
            if(req.equalsIgnoreCase("logout")) {
                sessionMap = null;
                needInvalidate = true;
            }
            
            json.put("logined", new Boolean((sessionMap != null)));
            
            if(sessionMap != null) {
                json.put("id"    , sessionMap.get("id"  ));
                json.put("idtype", sessionMap.get("idtype"));
                json.put("nick"  , sessionMap.get("nick"));
            }
            
            if(getSessionObject(request, "fslanguage") != null) json.put("language", getSessionObject(request, "fslanguage"));
            json.put("spd", new Double(calculateSpeed(getSessionUserSpeedConst(sessionMap))));
            json.put("noanonymous", new Boolean(noAnonymous));
            json.put("readonlymode", new Boolean(readOnly));
            json.put("message", msg);
        } catch(Throwable t) {
            json.put("success", new Boolean(false));
            json.put("message", "Error : " + t.getMessage());
            if(! (t instanceof RuntimeException)) t.printStackTrace();
        } finally {
            if(needInvalidate) {
                request.getSession().invalidate();
                logIn("Session Invalidated");
                json.put("invalidated", new Boolean(true));
            }
            if(rs    != null) { try { rs.close();     } catch(Throwable tx) {}}
            if(pstmt != null) { try { pstmt.close();  } catch(Throwable tx) {}}
            if(conn  != null) { try { conn.close();   } catch(Throwable tx) {}}
            if(r2    != null) { try { r2.close();     } catch(Throwable tx) {}}
            if(r1    != null) { try { r1.close();     } catch(Throwable tx) {}}
            if(fIn   != null) { try { fIn.close();    } catch(Throwable tx) {}}
            if(fOut  != null) { try { fOut.close();   } catch(Throwable tx) {}}
            fOut = null;
            accChanging = false;
        }
        return json;
    }
    
    /** Get session attribute keys */
    public Set<String> getSessionKeys(HttpServletRequest request) {
        HashSet<String> res = new HashSet<String>();
        Enumeration<String> sessKeys = request.getSession().getAttributeNames();
        while(sessKeys.hasMoreElements()) { res.add(sessKeys.nextElement()); }
        
        String tokenID  = null;
        String tokenVal = null;
        
        if((! noLogin) && tokenLifeTime >= 1) {
            tokenID  = request.getHeader("fsid");
            tokenVal = request.getHeader("fstoken");
            
            if(tokenID == null || tokenVal == null) {
                if(request.getParameter("fstoken_id") != null && request.getParameter("fstoken_val") != null) {
                    tokenID  = request.getParameter("fstoken_id");
                    tokenVal = request.getParameter("fstoken_val");
                }
            }
        }
        
        if(tokenID != null && tokenVal != null) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                JsonObject addiContent1 = null;
                JsonObject addiContent2 = null;
                if(useJDBC) {
                    // Get additional session attributes from FS_TOKEN
                    String addContents = null;
                    if(conn == null) {
                        Class.forName(jdbcClass);
                        conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                    }
                    pstmt = conn.prepareStatement("SELECT CONTENT FROM FS_TOKEN WHERE TOKEN = ? AND USERID = ?");
                    pstmt.setString(1, tokenVal);
                    pstmt.setString(2, tokenID);
                    rs = pstmt.executeQuery();
                    while(rs.next()) {
                        addContents = rs.getString("CONTENT");
                    }
                    rs.close(); rs = null;
                    pstmt.close(); pstmt = null;
                    
                    if(addContents != null) {
                        try {
                            addiContent1 = (JsonObject) JsonCompatibleUtil.parseJson(addContents);
                        } catch(Throwable tx) {
                            log("Cannot load session attributes from FS_TOKEN CONTENT column... " + tx.getMessage(), this.getClass());
                            addiContent1 = null;
                        }                                
                    }
                } else {
                    // Get additional session attributes
                    
                    File ftJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "tokens");
                    if(! ftJson.exists()) ftJson.mkdirs();
                    
                    File ftId = new File(ftJson.getCanonicalPath() + File.separator + tokenID);
                    if(! ftId.exists()) ftId.mkdirs();
                    
                    File[] fTokens = ftId.listFiles();
                    JsonObject jsonComp = null;
                    for(File f : fTokens) {
                        String t = FileUtil.readString(f, "UTF-8");
                        JsonObject tJson = (JsonObject) JsonCompatibleUtil.parseJson(t);
                        String tokenOne = String.valueOf(tJson.get("token"));
                        
                        Long.parseLong(String.valueOf(tJson.get("crtime"))); // Just Check
                        long entime = Long.parseLong(String.valueOf(tJson.get("entime"))); // Check expiration
                        
                        if(entime < System.currentTimeMillis()) { // expiration
                            f.delete();
                            continue;
                        }
                        
                        if(jsonComp != null) continue; // If another token is already accepted, skip.
                        if(! tokenOne.equals(tokenVal)) continue; // Check equals
                        
                        // Accept
                        addiContent1 = (JsonObject) JsonCompatibleUtil.parseJson(tJson.get("content"));
                        addiContent2 = (JsonObject) JsonCompatibleUtil.parseJson(tJson.get("blob"));
                        break;
                    }
                }
                if(addiContent1 != null) {
                    Set<String> keysIn = addiContent1.keySet();
                    res.addAll(keysIn);
                }
                if(addiContent2 != null) {
                    Set<String> keysIn = addiContent2.keySet();
                    res.addAll(keysIn);
                }
            } catch(Throwable t) {
                t.printStackTrace();
                log("Exception when access session - (" + t.getClass().getName() + ") " + t.getMessage(), getClass());
            } finally {
                ClassUtil.closeAll(rs, pstmt, conn);
            }
        }
        
        return res;
    }
    
    /** Get session attribute. */
    public Object getSessionObject(HttpServletRequest request, String key) {
        Object res = null;
        
        if(useSession) {
            res = getSessionObjectRaw(request.getSession(), key);
            if(res != null) return res;
        }
        
        if(useToken && tokenLifeTime >= 1) {
            String tokenID  = null;
            String tokenVal = null;
            
            tokenID  = request.getHeader("fsid");
            tokenVal = request.getHeader("fstoken");
            
            if(tokenID == null || tokenVal == null) {
                if(request.getParameter("fstoken_id") != null && request.getParameter("fstoken_val") != null) {
                    tokenID  = request.getParameter("fstoken_id");
                    tokenVal = request.getParameter("fstoken_val");
                }
            }
            
            if(tokenID != null && tokenVal != null) {
                Connection conn = null;
                PreparedStatement pstmt = null;
                ResultSet rs = null;
                try {
                    JsonObject addiContent1 = null;
                    JsonObject addiContent2 = null;
                    if(useJDBC) {
                        // Get additional session attributes from FS_TOKEN
                        String addContents = null;
                        if(conn == null) {
                            Class.forName(jdbcClass);
                            conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                        }
                        pstmt = conn.prepareStatement("SELECT CONTENT FROM FS_TOKEN WHERE TOKEN = ? AND USERID = ?");
                        pstmt.setString(1, tokenVal);
                        pstmt.setString(2, tokenID);
                        rs = pstmt.executeQuery();
                        while(rs.next()) {
                            addContents = rs.getString("CONTENT");
                        }
                        rs.close(); rs = null;
                        pstmt.close(); pstmt = null;
                        
                        if(addContents != null) {
                            try {
                                addiContent1 = (JsonObject) JsonCompatibleUtil.parseJson(addContents);
                            } catch(Throwable tx) {
                                log("Cannot load session attributes from FS_TOKEN CONTENT column... " + tx.getMessage(), this.getClass());
                                addiContent1 = null;
                            }                                
                        }
                    } else {
                        // Get additional session attributes
                        
                        File ftJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "tokens");
                        if(! ftJson.exists()) ftJson.mkdirs();
                        
                        File ftId = new File(ftJson.getCanonicalPath() + File.separator + tokenID);
                        if(! ftId.exists()) ftId.mkdirs();
                        
                        File[] fTokens = ftId.listFiles();
                        JsonObject jsonComp = null;
                        for(File f : fTokens) {
                        	if(f.isDirectory()) continue;
                        	String fName = f.getName();
                        	if(! fName.toLowerCase().endsWith(".json")) continue;
                        	
                            String t = FileUtil.readString(f, "UTF-8");
                            JsonObject tJson = (JsonObject) JsonCompatibleUtil.parseJson(t);
                            if(tJson == null) continue;
                            
                            String tokenOne = String.valueOf(tJson.get("token"));
                            
                            Long.parseLong(String.valueOf(tJson.get("crtime"))); // Just Check
                            long entime = Long.parseLong(String.valueOf(tJson.get("entime"))); // Check expiration
                            
                            if(entime < System.currentTimeMillis()) { // expiration
                                f.delete();
                                continue;
                            }
                            
                            if(jsonComp != null) continue; // If another token is already accepted, skip.
                            if(! tokenOne.equals(tokenVal)) continue; // Check equals
                            
                            // Accept
                            addiContent1 = (JsonObject) JsonCompatibleUtil.parseJson(tJson.get("content"));
                            addiContent2 = (JsonObject) JsonCompatibleUtil.parseJson(tJson.get("blob"));
                            break;
                        }
                    }
                    if(addiContent1 != null) {
                        res = addiContent1.get(key);
                        if(res != null) return res;
                    }
                    if(addiContent2 != null) {
                    	Object oFilePath = addiContent2.get(key);
                    	if(oFilePath != null) {
                    		File ftJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "tokens");
                            if(! ftJson.exists()) ftJson.mkdirs();
                            
                            File ftId = new File(ftJson.getCanonicalPath() + File.separator + tokenID);
                            if(! ftId.exists()) ftId.mkdirs();
                    		
                    		File fBlob = new File(ftId.getCanonicalPath() + File.separator + oFilePath.toString());
                    		if(fBlob.exists()) {
                    			FileInputStream   in1 = null;
                    			ObjectInputStream in2 = null;
                    			Throwable ccaught = null;
                    			try {
                    				in1 = new FileInputStream(fBlob);
                    				in2 = new ObjectInputStream(in1);
                    				res = in2.readObject();
                    			} catch(Throwable tIn) {
                    				ccaught = tIn;
                    			} finally {
                    				ClassUtil.closeAll(in2, in1);
                    			}
                    			if(ccaught != null) throw new RuntimeException(ccaught.getMessage(), ccaught);
                    			return  res;
                    		}
                    	}
                    }
                } catch(Throwable t) {
                    t.printStackTrace();
                    log("Exception when access session - (" + t.getClass().getName() + ") " + t.getMessage(), getClass());
                } finally {
                    ClassUtil.closeAll(rs, pstmt, conn);
                }
            }
        }
        
        return null;
    }
    
    @Deprecated
    public Object getSessionObjectRaw(HttpSession session, String key) {
        if(! useSession) return null;
        return session.getAttribute(key);
    }
    
    public void setSessionObject(HttpServletRequest request, String key, Object val) {
        setSessionObjectRaw(request.getSession(), key, val);
        
        if(useToken && tokenLifeTime >= 1) {
            String tokenID  = request.getHeader("fsid");
            String tokenVal = request.getHeader("fstoken");
            boolean tokenGuest = false;
            
            if(tokenID == null || tokenVal == null) {
                if(request.getParameter("fstoken_id") != null && request.getParameter("fstoken_val") != null) {
                    tokenID  = request.getParameter("fstoken_id");
                    tokenVal = request.getParameter("fstoken_val");
                }
            }
            
            if(tokenID != null && tokenVal != null) {
                Map<String, Object> updates = new HashMap<String, Object>();
                updates.put(key, val);
                updateToken(tokenID, tokenVal, System.currentTimeMillis() + (1000L * 60 * tokenLifeTime), updates, true, tokenGuest);
            }
        }
    }
    
    @Deprecated
    public void setSessionObjectRaw(HttpSession session, String key, Object val) {
        if(! useSession) return;
        session.setAttribute(key, val);
    }
    
    public void removeSessionObject(HttpServletRequest request, String key) {
        removeSessionObjectRaw(request.getSession(), key);
        
        if(useToken && (tokenLifeTime >= 1)) {
            String tokenID  = request.getHeader("fsid");
            String tokenVal = request.getHeader("fstoken");
            
            if(tokenID == null || tokenVal == null) {
                if(request.getParameter("fstoken_id") != null && request.getParameter("fstoken_val") != null) {
                    tokenID  = request.getParameter("fstoken_id");
                    tokenVal = request.getParameter("fstoken_val");
                }
            }
            
            if(tokenID != null && tokenVal != null) {
                Map<String, Object> updates = new HashMap<String, Object>();
                updates.put(key, null);
                updateToken(tokenID, tokenVal, System.currentTimeMillis() + (1000L * 60 * tokenLifeTime), updates, true, false);
            }
        }
    }
    
    @Deprecated
    public void removeSessionObjectRaw(HttpSession session, String key) {
        if(! useSession) return;
        session.removeAttribute(key);
    }
    
    /** Check session status (Not accepted, return guest session) */
    public JsonObject getSessionFSObject(HttpServletRequest request) {
        Throwable caught = null;
        JsonObject obj = null;
        try {
            String sessionJson = null;
            
            if(useToken && tokenLifeTime >= 1) {
                String tokenID  = request.getHeader("fsid");
                String tokenVal = request.getHeader("fstoken");
                
                if(tokenID == null || tokenVal == null) {
                    if(request.getParameter("fstoken_id") != null && request.getParameter("fstoken_val") != null) {
                        tokenID  = request.getParameter("fstoken_id");
                        tokenVal = request.getParameter("fstoken_val");
                    }
                }
                
                if(tokenID != null && tokenVal != null) {
                    obj = getTokenAvail(tokenID, tokenVal);
                    if(obj != null) return obj;
                }
            }
            
            sessionJson = (String) getSessionObject(request, "fssession");
            if(sessionJson != null) {
                sessionJson = sessionJson.trim();
                if(! sessionJson.equals("")) {
                    obj = (JsonObject) JsonCompatibleUtil.parseJson(sessionJson);
                    if(obj != null) { if(obj.get("id"    ) == null) obj = null;         }
                    if(obj != null) { if(obj.get("idtype") == null) obj = null;         }
                    if(obj != null) { if(obj.get("nick"  ) == null) obj = null;         }
                    if(obj != null) {
                        JsonObject jsonSess = getSessionFSObjectWhenAccepted(obj.get("id").toString());
                        obj = null;
                        return jsonSess;
                    }
                }
            }
        } catch(Throwable t) {
            caught = t;
        }
        if(caught != null) throw new RuntimeException(caught.getMessage(), caught);
        obj = new JsonObject();
        obj.put("id"        , "guest");
        obj.put("nick"      , "GUEST");
        obj.put("idtype"    , "G");
        obj.put("privileges", new JsonArray());
        obj.put("privgroup" , new JsonArray());
        return obj;
    }
    
    /** Check token status (Not accepted, return null) */
    public JsonObject getTokenAvail(String tokenID, String tokenVal) {
        if(! useToken) return null;
        if(tokenLifeTime <= 0) return null;
        
        if(isTokenAvail(tokenID, tokenVal)) {
            JsonObject jsonSess = getSessionFSObjectWhenAccepted(tokenID);
            if(jsonSess != null) {
                updateToken(tokenID, tokenVal, System.currentTimeMillis() + (1000L * 60 * tokenLifeTime), false);
                return jsonSess;
            }
        }
        return null;
    }
    
    /** Check token ID and value available (Just check that token has correct form.) */
    public boolean isTokenAvail(String tokenID, String tokenVal) {
        if(! useToken) return false;
        if(tokenLifeTime <= 0) return false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Throwable caught = null;
        try {
            if(tokenID != null && tokenVal != null) {
                if(useJDBC) {
                    Class.forName(jdbcClass);
                    conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                    
                    // Delete expired tokens
                    pstmt = conn.prepareStatement("DELETE FROM FS_TOKEN WHERE ENTIME < ?");
                    pstmt.setLong(1, System.currentTimeMillis());
                    
                    pstmt.executeUpdate();
                    conn.commit();
                    
                    pstmt.close(); pstmt = null;
                    
                    // Check token equals
                    pstmt = conn.prepareStatement("SELECT COUNT(*) AS CNT FROM FS_TOKEN WHERE TOKEN = ? AND USERID = ?");
                    pstmt.setString(1, tokenVal);
                    pstmt.setString(2, tokenID);
                    
                    rs = pstmt.executeQuery();
                    int c = 0;
                    while(rs.next()) {
                        c = rs.getInt("CNT");
                    }
                    rs.close(); rs = null;
                    pstmt.close(); pstmt = null;
                    
                    if(c >= 1) {
                        // Accept
                        return true;
                    } else {
                        conn.close(); conn = null;
                    }
                } else {
                    File ftJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "tokens");
                    if(! ftJson.exists()) ftJson.mkdirs();
                    
                    File ftId = new File(ftJson.getCanonicalPath() + File.separator + tokenID);
                    if(! ftId.exists()) ftId.mkdirs();
                    
                    File[] fTokens = ftId.listFiles();
                    JsonObject jsonComp = null;
                    for(File f : fTokens) {
                        try {
                        	if(f.isDirectory()) continue;
                        	String fName = f.getName();
                        	if(! fName.toLowerCase().endsWith(".json")) continue;
                        	
                            String t = FileUtil.readString(f, "UTF-8");
                            JsonObject tJson = (JsonObject) JsonCompatibleUtil.parseJson(t);
                            String tokenOne = String.valueOf(tJson.get("token"));
                            
                            Long.parseLong(String.valueOf(tJson.get("crtime"))); // Just Check
                            long entime = Long.parseLong(String.valueOf(tJson.get("entime"))); // Check expiration
                            
                            if(entime < System.currentTimeMillis()) { // expiration
                                f.delete();
                                continue;
                            }
                            
                            if(jsonComp != null) continue; // If another token is already accepted, skip.
                            if(! tokenOne.equals(tokenVal)) continue; // Check equals
                            
                            // Accept
                            return true;
                        } catch(Throwable tToken) {
                            log("Exception when checking token - (" + tToken.getClass().getName() + ") " + tToken.getMessage(), this.getClass());
                        }
                    }
                }
            }
        } catch(Throwable t) {
            caught = t;
        } finally {
            ClassUtil.closeAll(rs, pstmt, conn);
        }
        if(caught != null) throw new RuntimeException(caught.getMessage(), caught);
        return false;
    }
    
    /** Update token's end time. */
    public boolean updateToken(String tokenID, String tokenVal, long newEndTime, boolean checkAcceptedToken) {
        return updateToken(tokenID, tokenVal, newEndTime, null, checkAcceptedToken, false);
    }
    
    /** Update token's end time and session contents. */
    public boolean updateToken(String tokenID, String tokenVal, long newEndTime, Map<String, Object> content, boolean checkAcceptedToken, boolean guest) {
        if(! useToken) return false;
        if(tokenLifeTime <= 0) return false;
        
        if(checkAcceptedToken && (! guest)) {
            if(! isTokenAvail(tokenID, tokenVal)) return false;
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Throwable caught = null;
        try {
            if(useJDBC) {
                Class.forName(jdbcClass);
                conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                
                if(content == null) {
                    // Extend token's enddate
                    pstmt = conn.prepareStatement("UPDATE FS_TOKEN SET ENTIME = ? WHERE TOKEN = ? AND USERID = ?");
                    pstmt.setLong(1, newEndTime);
                    pstmt.setString(2, tokenVal);
                    pstmt.setString(3, tokenID);
                    pstmt.executeUpdate();
                    conn.commit();
                    
                    // Close
                    pstmt.close(); pstmt = null;
                    conn.close(); conn = null;
                } else {
                    // Get content
                    JsonObject jContent = null;
                    
                    pstmt = conn.prepareStatement("SELECT CONTENT FROM FS_TOKEN WHERE TOKEN = ? AND USERID = ?");
                    pstmt.setString(1, tokenVal);
                    pstmt.setString(2, tokenID);
                    rs = pstmt.executeQuery();
                    
                    String sContent = null;
                    while(rs.next()) {
                        sContent = rs.getString("CONTENT");
                    }
                    
                    rs.close(); rs = null;
                    pstmt.close(); pstmt = null;
                    
                    if(sContent != null) {
                        jContent = (JsonObject) JsonCompatibleUtil.parseJson(sContent.trim());
                        
                        Set<String> updateKeys = content.keySet();
                        for(String k : updateKeys) {
                            Object c = content.get(k);
                            if(c == null) {
                                jContent.remove(k);
                                continue;
                            }
                            if((c instanceof JsonObject) || (c instanceof JsonArray)) {
                                jContent.put(k, c);
                                continue;
                            }
                            if((c instanceof Integer) || (c instanceof Long) || (c instanceof Float) || (c instanceof Double) || (c instanceof Boolean)) {
                                jContent.put(k, c);
                                continue;
                            }
                            if(c instanceof CharSequence) {
                                jContent.put(k, c.toString());
                                continue;
                            }
                        }
                        
                        // Updates
                        pstmt = conn.prepareStatement("UPDATE FS_TOKEN SET ENTIME = ?, CONTENT = ? WHERE TOKEN = ? AND USERID = ?");
                        pstmt.setLong(1, newEndTime);
                        pstmt.setString(2, jContent.toJSON());
                        pstmt.setString(3, tokenVal);
                        pstmt.setString(4, tokenID);
                        pstmt.executeUpdate();
                        conn.commit();
                        
                        // Close
                        pstmt.close(); pstmt = null;
                        conn.close(); conn = null;
                    }
                    
                }
                
                return true;
            } else {
                File ftJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "tokens");
                if(! ftJson.exists()) ftJson.mkdirs();
                
                File ftId = new File(ftJson.getCanonicalPath() + File.separator + tokenID);
                if(! ftId.exists()) ftId.mkdirs();
                
                File[] fTokens = ftId.listFiles();
                
                fTokens = ftId.listFiles();
                
                for(File f : fTokens) {
                    try {
                    	if(f.isDirectory()) continue;
                    	String fName = f.getName();
                    	if(! fName.toLowerCase().endsWith(".json")) continue;
                    	
                        String t = FileUtil.readString(f, "UTF-8");
                        JsonObject tJson = (JsonObject) JsonCompatibleUtil.parseJson(t);
                        String tokenOne = String.valueOf(tJson.get("token"));
                        
                        Long.parseLong(String.valueOf(tJson.get("crtime"))); // Just Check
                        long entime = Long.parseLong(String.valueOf(tJson.get("entime"))); // Check expiration
                        
                        if(entime < System.currentTimeMillis()) { // expiration
                            f.delete();
                            continue;
                        }
                        
                        if(! tokenOne.equals(tokenVal)) continue; // Check equals
                        
                        // Accepts
                        
                        // Extend token's enddate
                        tJson.put("entime", String.valueOf(newEndTime));
                        
                        // Apply Content changes
                        if(content != null) {
                            Object oContent = tJson.get("content");
                            JsonObject jContent = (JsonObject) JsonCompatibleUtil.parseJson(oContent);
                            oContent = null;
                            
                            Object oBlob = tJson.get("blob");
                            JsonObject jBlob = null;
                            if(oBlob != null) jBlob = (JsonObject) JsonCompatibleUtil.parseJson(oBlob);
                            else              jBlob = new JsonObject();
                            oBlob = null;
                            
                            Set<String> updateKeys = content.keySet();
                            for(String k : updateKeys) {
                                Object c = content.get(k);
                                if(c == null) {
                                    jContent.remove(k);
                                    continue;
                                }
                                if((c instanceof JsonObject) || (c instanceof JsonArray)) {
                                    jContent.put(k, c);
                                    continue;
                                }
                                if((c instanceof Integer) || (c instanceof Long) || (c instanceof Float) || (c instanceof Double) || (c instanceof Boolean)) {
                                    jContent.put(k, c);
                                    continue;
                                }
                                if(c instanceof CharSequence) {
                                    jContent.put(k, c.toString());
                                    continue;
                                }
                                if(c instanceof Serializable) {
                                	File fblob = null;
                                	
                                	Object beforeNm = jBlob.get(k);
                                	if(beforeNm == null) {
                                		int no = 0;
                                    	fblob = new File(ftId.getCanonicalPath() + File.separator + "blob" + no + ".blob");
                                    	while(fblob.exists()) {
                                    		no++;
                                    		fblob = new File(ftId.getCanonicalPath() + File.separator + "blob" + no + ".blob");
                                    	}
                                	} else {
                                		fblob = new File(ftId.getCanonicalPath() + File.separator + beforeNm.toString());
                                	}
                                	
                                	jBlob.put(k, fblob.getName());
                                	
                                	FileOutputStream   out1 = null;
                                	ObjectOutputStream out2 = null;
                                	Throwable ccaught = null;
                                	try {
                                		out1 = new FileOutputStream(fblob);
                                		out2 = new ObjectOutputStream(out1);
                                		out2.writeObject(c);
                                	} catch(Throwable tIn) {
                                		ccaught = tIn;
                                	} finally {
                                		ClassUtil.closeAll(out2, out1);
                                	}
                                	if(ccaught != null) throw new RuntimeException(ccaught.getMessage(), ccaught);
                                }
                            }
                            
                            tJson.put("content", jContent);
                            tJson.put("blob"   , jBlob   );
                        }
                        
                        // Re-write
                        FileUtil.writeString(f, "UTF-8", tJson.toJSON());
                        return true;
                    } catch(Throwable tToken) {
                        log("Exception when checking token - (" + tToken.getClass().getName() + ") " + tToken.getMessage(), this.getClass());
                    }
                }
            }
        } catch(Throwable t) {
            caught = t;
        } finally {
            ClassUtil.closeAll(rs, pstmt, conn);
        }
        if(caught != null) throw new RuntimeException(caught.getMessage(), caught);
        return false;
    }
    
    /** Create token file or data */
    protected synchronized boolean createToken(String tokenID, String tokenVal, long newEndTime, Map<String, Object> content) throws IOException {
        File ftJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "tokens");
        if(! ftJson.exists()) ftJson.mkdirs();
        
        log("Creating tokens...", this.getClass());
        
        if(useJDBC) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                Class.forName(jdbcClass);
                conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                        
                // Check exists
                int c = 0;
                pstmt = conn.prepareStatement("SELECT COUNT(*) AS CNT FROM FS_TOKEN WHERE TOKEN = ? AND USERID = ?");
                rs = pstmt.executeQuery();
                while(rs.next()) {
                    c = rs.getInt("CNT");                
                }
                
                rs.close();
                rs = null;
                
                pstmt.close();
                pstmt = null;
                
                if(c <= 0) {
                    log("Token does not exists. Inserting one...", this.getClass());
                    // Create Token
                    long tCrTime = System.currentTimeMillis();
                    pstmt = conn.prepareStatement("INSERT INTO FS_TOKEN (TOKEN, USERID, CRTIME, ENTIME, CONTENT) VALUES (?, ?, ?, ?, ?)");
                    pstmt.setString(1, tokenVal);
                    pstmt.setString(2, tokenID);
                    pstmt.setLong(3, tCrTime);
                    pstmt.setLong(4, newEndTime);
                    if(content == null) pstmt.setString(5, "{}");
                    else                pstmt.setString(5, ((JsonObject) JsonCompatibleUtil.parseJson(content)).toJSON());
                    pstmt.executeUpdate();
                    conn.commit();
                    
                    pstmt.close();
                    pstmt = null;
                }
                return true;
            } catch(Throwable t) {
                log("Exception when creating guest token - (" + t.getClass().getName() + ") - " + t.getMessage(), getClass());
            } finally {
                ClassUtil.closeAll(rs, pstmt, conn);
            }
        } else {
            File ftId = new File(ftJson.getCanonicalPath() + File.separator + tokenID);
            if(! ftId.exists()) ftId.mkdirs();
            
            File[] fTokens = ftId.listFiles();
            
            // Check exists
            boolean exists = false;
            for(File f : fTokens) {
                try {
                    String t = FileUtil.readString(f, "UTF-8");
                    JsonObject tJson = (JsonObject) JsonCompatibleUtil.parseJson(t);
                    String tokenOne = String.valueOf(tJson.get("token"));
                    
                    Long.parseLong(String.valueOf(tJson.get("crtime"))); // Just Check
                    long entime = Long.parseLong(String.valueOf(tJson.get("entime"))); // Check expiration
                    
                    if(entime < System.currentTimeMillis()) { // expiration
                        f.delete();
                        continue;
                    }
                    
                    if(! tokenOne.equals(tokenVal)) continue; // Check equals
                    
                    exists = true;
                    break;
                } catch(Throwable tToken) {
                    log("Exception when checking token - (" + tToken.getClass().getName() + ") " + tToken.getMessage(), this.getClass());
                }
            }
            
            if(! exists) {
                log("Token does not exists. Writing one...", this.getClass());
                // Write new
                int fIndex = 0;
                File fToken = new File(ftId.getCanonicalPath() + File.separator + "t" + fIndex + ".json");
                while(fToken.exists()) {
                    fIndex++;
                    fToken = new File(ftId.getCanonicalPath() + File.separator + "t" + fIndex + ".json");
                }
                
                long tCrTime = System.currentTimeMillis();
                JsonObject jToken = new JsonObject();
                jToken.put("id"   , tokenID);
                jToken.put("token", tokenVal);
                jToken.put("crtime", String.valueOf(tCrTime));
                jToken.put("entime", String.valueOf(newEndTime));
                
                if(content == null) jToken.put("content", new JsonObject());
                else                jToken.put("content", (JsonObject) (JsonCompatibleUtil.parseJson(content)));
                
                FileUtil.writeString(fToken, "UTF-8", jToken.toJSON());
                return true;
            }
        }
        return false;
    }
    
    /** Get user's data excepts password. */
    @SuppressWarnings("unused")
    protected JsonObject getSessionFSObjectWhenAccepted(String userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        FileInputStream fIn = null;
        Reader r1 = null;
        Reader r2 = null;
        Throwable caught = null;
        
        try {
            JsonObject jsonSess = null;
            if(useJDBC) {
                if(conn == null) {
                    Class.forName(jdbcClass);
                    conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                }
                
                pstmt = conn.prepareStatement("SELECT USERID, USERPW, USERNICK, USERTYPE, FAILCNT, FAILTIME, PRIVILEGES FROM FS_USER WHERE USERID = ?");
                pstmt.setString(1, userId);
                
                rs = pstmt.executeQuery();
                jsonSess = new JsonObject();
                
                while(rs.next()) {
                    jsonSess.put("id"       , rs.getString("USERID"));
                    jsonSess.put("idtype"   , rs.getString("USERTYPE"));
                    jsonSess.put("nick"     , rs.getString("USERNICK"));
                    jsonSess.put("fail_cnt" , rs.getLong("FAILCNT"));
                    jsonSess.put("fail_time", rs.getLong("FAILTIME"));
                }
                
                rs.close(); rs = null;
                pstmt.close(); pstmt = null;
                conn.close(); conn = null;
            } else {
                File faJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "accounts");
                if(! faJson.exists()) faJson.mkdirs();
                
                File ftJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "tokens");
                if(! ftJson.exists()) ftJson.mkdirs();
                
                File fileAcc = new File(faJson.getCanonicalPath() + File.separator + userId + ".json");
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
                    
                    jsonSess = (JsonObject) JsonCompatibleUtil.parseJson(lineCollector.toString().trim());
                    lineCollector.setLength(0);
                    lineCollector = null;
                }
            }
            
            if(jsonSess != null) {
                jsonSess.remove("pw");
                if(jsonSess.get("privgroup") == null) jsonSess.put("privgroup", new JsonArray());
            }
            
            return jsonSess;
        } catch(Throwable t) {
            caught = t;
        } finally {
            ClassUtil.closeAll(rs, pstmt, conn);
        }
        if(caught != null) throw new RuntimeException(caught.getMessage(), caught);
        return null;
    }
    
    public String getSessionUserId(HttpServletRequest request) {
        JsonObject sess = getSessionFSObject(request);
        if(sess == null) return null;
        return getSessionUserId(sess);
    }
    
    public String getSessionUserId(JsonObject sess) {
        return sess.get("id").toString();
    }
    
    public String getSessionUserNick(HttpServletRequest request) {
        JsonObject sess = getSessionFSObject(request);
        if(sess == null) return null;
        return getSessionUserNick(sess);
    }
    
    public String getSessionUserNick(JsonObject sess) {
        return sess.get("nick").toString();
    }
    
    public String getSessionUserType(HttpServletRequest request) {
        JsonObject sess = getSessionFSObject(request);
        if(sess == null) return null;
        return getSessionUserType(sess);
    }
    
    public String getSessionUserType(JsonObject sess) {
        if(sess == null) return "G";
        if(sess.get("idtype") == null) return "G";
        return sess.get("idtype").toString().toUpperCase();
    }
    
    public int getSessionUserSpeedConst(HttpServletRequest request) {
        JsonObject sess = getSessionFSObject(request);
        if(sess == null) return 2;
        return getSessionUserSpeedConst(sess);
    }
    
    public int getSessionUserSpeedConst(JsonObject sess) {
        String sSpeed = null;
        if(sess == null) return 2;
        if(sess.get("speed") == null) {
            if(getSessionUserType(sess).equals("A")) sSpeed = "5";
            else sSpeed = "2";
        } else {
            sSpeed = sess.get("speed").toString().trim();
        }
        try { return Integer.parseInt(sSpeed); } catch(NumberFormatException e) { return 2; }
    }
    
    public double calculateSpeed(int speedConst) {
        double res = (double) bufferSize;
        if(sleepGap != 0) res = res * (1000.0 / sleepGap);
        else return -1;
        return res = res * speedConst;
    }
    
    public String getLanguage(HttpServletRequest request) {
        String lang = (String) getSessionObject(request, "fslanguage");
        return getLanguage(lang, getSessionFSObject(request));
    }
    
    public String getLanguage(String fslang, JsonObject sess) {
        String lang = fslang;
        if(sess != null) {
            if(sess.get("lang"    ) != null) lang = sess.get("lang"    ).toString();
            if(sess.get("language") != null) lang = sess.get("language").toString();
        }
        
        if(lang == null) lang = "en";
        lang = lang.trim().toLowerCase();
        if(lang.length() > 2) lang = lang.substring(0, 2);
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
    
    public long getLimitPreview() {
        return limitPrev;
    }

    public void setLimitPreview(long limitPrev) {
        this.limitPrev = limitPrev;
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

    public boolean isNoAnonymous() {
        return noAnonymous;
    }

    public void setNoAnonymous(boolean noAnonymous) {
        this.noAnonymous = noAnonymous;
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
    
    public int getTokenLifeTime() {
        return tokenLifeTime;
    }
    
    public void setTokenLifeTime(int tokenLifeTime) {
        this.tokenLifeTime = tokenLifeTime; 
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
    
    public boolean useSession() {
        return useSession;
    }

    public boolean useToken() {
        return useToken;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
    
    public boolean isAllowSysCmd() {
    	return allowSysCmd;
    }

    public String getRealPath() {
		return realPath;
	}

	public void setRealPath(ServletContext ctx) {
		this.realPath = ctx.getRealPath("/");
	}
	
	public void setRealPath(HttpServletRequest req) {
		setRealPath(req.getServletContext());
	}

	public synchronized void logIn(Object logContent) {
        logIn(logContent, FSControl.class);
    }
    
    public synchronized void logIn(Object logContent, Class<?> froms) {
        if(logOnStd) System.out.println(logContent);
        
        PreparedStatement pstmt = null;
        long now = System.currentTimeMillis();
        try {
            String classNm = "UNKNOWN";
            if(froms != null) classNm = froms.getClass().getSimpleName();
            
            if(logContent instanceof Throwable) logContent = DataUtil.stackTrace((Throwable) logContent);
            
            String strContent = "[" + classNm + "][" + now + "]" + String.valueOf(logContent);
            logContent = null;
            int sizes = strContent.getBytes("UTF-8").length;
            
            if(sizes > logLimit) {
                String prints = FSUtils.cutStringSizeByte(strContent, cs, logLimit); 
                String left   = strContent.substring(prints.length());
                
                logIn(prints, froms);
                logIn(left, froms);
                return;
            }
            
            if(logOnFile) {
                if(logFileWr != null) {
                    if(now - logLastDt >= 1000L * 60 * 60 || logLen + sizes >= logLimit) {
                        logFileWr.close();
                        logFileWr = null;
                        logLen = 0;
                    }
                }
                
                if(logFileWr == null) {
                    logd = new File(rootPath.getCanonicalPath() + File.separator + ".logs");
                    if(! logd.exists()) logd.mkdirs();
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                    int fileIndex = 0;
                    String fileDate = dateFormat.format(new Date(now));
                    
                    String lFileNm = logFileNm;
                    lFileNm = lFileNm.replace("[date]", fileDate);
                    lFileNm = lFileNm.replace("[n]", "" + fileIndex);
                    
                    File file = new File(logd.getCanonicalPath() + File.separator + lFileNm);
                    while(file.exists()) {
                        fileIndex++;
                        
                        lFileNm = logFileNm;
                        lFileNm = lFileNm.replace("[date]", fileDate);
                        lFileNm = lFileNm.replace("[n]", "" + fileIndex);
                        
                        file = new File(logd.getCanonicalPath() + File.separator + lFileNm);
                    }
                    
                    logFileWr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), cs));
                }
                
                logFileWr.write(strContent);
                logFileWr.newLine();
                logLen += sizes;
            }
            if(logOnJdbc) {
                if(now - logLastDt >= 1000L * 60 * 60) {
                    logConn.close();
                    logConn = null;
                }
                
                if(logConn == null) {
                    Class.forName(jdbcClass);
                    logConn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                }
                
                if(dbType.equals("oracle")) pstmt = logConn.prepareStatement("INSERT INTO FS_LOG (LOGNO, LOGCONTENT, LOGCLASS, LOGDATE) VALUES ( COALESCE(( SELECT MAX(LOGNO) FROM FS_LOG ), 0) + 1, ?, ?, ?  )");
                else                        pstmt = logConn.prepareStatement("INSERT INTO FS_LOG (LOGNO, LOGCONTENT, LOGCLASS, LOGDATE) VALUES ( COALESCE(( SELECT MAX(LOGNO) FROM FS_LOG ), 0) + 1, ?, ?, ?  )");
                
                pstmt.setString(1, strContent);
                pstmt.setString(2, classNm);
                pstmt.setLong(3, now);
                
                pstmt.executeQuery();
                logConn.commit();
                
                pstmt.close(); pstmt = null;
            }
        } catch(Throwable t) {
            t.printStackTrace();
            if(pstmt   != null) { try { pstmt.close();   } catch(Throwable tx) {} }
            if(logConn != null) { try { logConn.close(); } catch(Throwable tx) {} }
            pstmt   = null;
            logConn = null;
        } finally {
            if(pstmt != null) { try { pstmt.close(); } catch(Throwable tx) {} }
            logLastDt = now;
        }
    }
    
    /** Apply current configs on file (or DB when using JDBC) */
    public void applyModifiedConfig(String changer) {
        Connection        conn    = null;
        PreparedStatement pstmt   = null;
        FileOutputStream  fileOut = null;
        Throwable         caught  = null;
        try {
            if(useJDBC) {
                if(conn == null) {
                    Class.forName(jdbcClass);
                    conn = DriverManager.getConnection(jdbcUrl, jdbcId, jdbcPw);
                }
                
                pstmt = conn.prepareStatement("UPDATE FS_CONFIG SET JSONCONFIG = ?");
                pstmt.setString(1, conf.toJSON());
                pstmt.executeUpdate();
                conn.commit();
                pstmt.close(); pstmt = null;
                conn.close(); conn = null;
            } else {
                File fJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "config.json");
                fileOut = new FileOutputStream(fJson);
                fileOut.write(conf.toJSON().getBytes(cs));
                fileOut.close(); fileOut = null;
            }
            
            logIn("Configuration Updated by " + changer + " when " + System.currentTimeMillis());
        } catch(Throwable t) {
            t.printStackTrace();
            caught = t;
        } finally {
            if(pstmt    != null) { try { pstmt.close();    } catch(Throwable tx) {} }
            if(conn     != null) { try { conn.close();     } catch(Throwable tx) {} }
            if(fileOut  != null) { try { fileOut.close();  } catch(Throwable tx) {} }
        }
        if(caught != null) throw new RuntimeException(caught.getMessage(), caught);
    }
    
    public synchronized void applyConfigs() {
        if(conf.get("NoAnonymous") != null) {
            noAnonymous = DataUtil.parseBoolean(conf.get("NoAnonymous").toString().trim());
        } else {
            conf.put("NoAnonymous", new Boolean(noAnonymous));
        }
        if(conf.get("UseAccount") != null) {
            noLogin = (! DataUtil.parseBoolean(conf.get("UseAccount").toString().trim()));
        } else {
            conf.put("UseAccount", new Boolean(! noLogin));
        }
        if(conf.get("UseSession") != null) {
            useSession = DataUtil.parseBoolean(conf.get("UseSession").toString().trim());
        } else {
            conf.put("UseSession", new Boolean(useSession));
        }
        if(conf.get("UseToken") != null) {
            useToken = DataUtil.parseBoolean(conf.get("UseToken").toString().trim());
        } else {
            conf.put("UseToken", new Boolean(useToken));
        }
        if(conf.get("ReadOnly") != null) {
            readOnly = DataUtil.parseBoolean(conf.get("ReadOnly").toString().trim());
        } else {
            conf.put("ReadOnly", new Boolean(readOnly));
        }
        if(conf.get("AllowSystemCommand") != null) {
        	allowSysCmd = DataUtil.parseBoolean(conf.get("AllowSystemCommand").toString().trim());
        } else {
        	conf.put("AllowSystemCommand", new Boolean(allowSysCmd));
        }
        if(conf.get("UseConsole") != null) {
            noConsole = (! DataUtil.parseBoolean(conf.get("UseConsole").toString().trim()));
        } else {
            conf.put("UseConsole", new Boolean(! noConsole));
        }
        if(conf.get("UseCaptchaDown") != null) {
            captchaDownload = DataUtil.parseBoolean(conf.get("UseCaptchaDown").toString().trim());
        } else {
            conf.put("UseCaptchaDown", new Boolean(captchaDownload));
        }
        if(conf.get("UseCaptchaLogin") != null) {
            captchaLogin = DataUtil.parseBoolean(conf.get("UseCaptchaLogin").toString().trim());
        } else {
            conf.put("UseCaptchaLogin", new Boolean(captchaLogin));
        }
        if(conf.get("LimitDownloadSize") != null) {
            limitSize = Long.parseLong(conf.get("LimitDownloadSize").toString().trim());
            limitPrev = limitSize / 10;
        } else {
            conf.put("LimitDownloadSize", limitSize + "");
        }
        if(conf.get("LimitPreviewSize") != null) {
            limitPrev = Long.parseLong(conf.get("LimitPreviewSize").toString().trim());
        } else {
            conf.put("LimitPreviewSize", limitPrev + "");
        }
        if(conf.get("LimitFilesSinglePage") != null) {
            limitCount = Integer.parseInt(conf.get("LimitFilesSinglePage").toString().trim());
        } else {
            conf.put("LimitFilesSinglePage", new Integer(limitCount));
        }
        if(conf.get("BufferSize") != null) {
            bufferSize = Integer.parseInt(conf.get("BufferSize").toString().trim());
            if(bufferSize <= 1) bufferSize = 1;
        } else {
            conf.put("BufferSize", new Integer(bufferSize));
        }
        if(conf.get("SleepGap") != null) {
            sleepGap = Integer.parseInt(conf.get("SleepGap").toString().trim());
            if(sleepGap < 20) sleepGap = 20;
        }
        if(conf.get("SleepRoutine") != null) {
            sleepRoutine = Integer.parseInt(conf.get("SleepRoutine").toString().trim());
            if(sleepRoutine <= 1) sleepRoutine = 1;
        }
        if(conf.get("ReadFileIcon") != null) {
            readFileIcon = DataUtil.parseBoolean(conf.get("ReadFileIcon").toString().trim());
        } else {
            conf.put("ReadFileIcon", new Boolean(readFileIcon));
        }
        if(conf.get("Salt") != null) {
            salt = conf.get("Salt").toString().trim();
        }
        if(conf.get("LoginFailCountLimit") != null) {
            loginFailCountLimit = Integer.parseInt(conf.get("LoginFailCountLimit").toString().trim());
        }
        if(conf.get("LoginFailOverTime") != null) {
            loginFailOverMinute = Integer.parseInt(conf.get("LoginFailOverTime").toString().trim());
            if(loginFailOverMinute <= 0) loginFailOverMinute = 1;
        }
        if(conf.get("TokenLifeTime") != null) {
            tokenLifeTime = Integer.parseInt(conf.get("TokenLifeTime").toString().trim());
        }
        if(conf.get("Title") != null) {
            String tx = conf.get("Title").toString().trim();
            if(! tx.equals("")) title = tx;
        } else {
            conf.put("Title", title);
        }
        if(conf.get("Log") != null) {
            JsonObject confLog = null;
            if(conf.get("Log") instanceof JsonObject) confLog = (JsonObject) conf.get("Log");
            else confLog = (JsonObject) JsonObject.parseJson(conf.get("Log").toString());
            
            if(confLog.get("OnFile") != null) {
                logOnFile = DataUtil.parseBoolean(confLog.get("OnFile").toString().trim());
            }
            if(confLog.get("OnJdbc") != null) {
                logOnJdbc = DataUtil.parseBoolean(confLog.get("OnJdbc").toString().trim());
            }
            if(confLog.get("OnStdOut") != null) {
                logOnStd = DataUtil.parseBoolean(confLog.get("OnStdOut").toString().trim());
            }
        } else {
            JsonObject jsonSample = new JsonObject();
            jsonSample.put("OnStdOut", new Boolean(logOnStd));
            jsonSample.put("OnFile"  , new Boolean(logOnFile));
            conf.put("Log", jsonSample);
        }
        if(logFileWr != null) {
            try { logFileWr.close(); } catch(Throwable txc) {}
            logFileWr = null;
        }
        if(logConn != null) {
            try { logConn.close(); } catch(Throwable txc) {}
            logConn = null;
        }
    }
    
    protected static Properties getFSProperties() {
        Properties propTest = new Properties();
        InputStream propIn = null;
        Throwable caught = null;
        try {
            int fileCnt = 0;
            Properties temp = new Properties();
            
            propIn = FSControl.class.getResourceAsStream("/fsdefault.properties");
            if(propIn != null) {
                temp.load(propIn);
                propIn.close(); propIn = null;
                propTest.putAll(temp);
                temp.clear();
            }
            
            propIn = FSControl.class.getResourceAsStream("/fs.properties");
            if(propIn != null) {
                temp.load(propIn);
                propIn.close(); propIn = null;
                fileCnt++;
                propTest.putAll(temp);
                temp.clear();
            }
            
            propIn = FSControl.class.getResourceAsStream("/fs.xml");
            if(propIn != null) {
                temp.loadFromXML(propIn);
                propIn.close(); propIn = null;
                fileCnt++;
                propTest.putAll(temp);
                temp.clear();
            }
            
            if(fileCnt <= 0) throw new FileNotFoundException("There is no fs.properties or fs.xml !");
        } catch(Throwable t) {
            System.out.println("Exception when loading fs properties... (" + t.getClass().getName() + ") - " + t.getMessage());
            caught = t;
        } finally {
            if(propIn != null) { try { propIn.close(); } catch(Throwable ignores) {} }
        }
        if(caught != null) throw new RuntimeException(caught.getMessage(), caught);
        return propTest;
    }
    
    public JsonArray getSessionDirectoryPrivileges(JsonObject jsonSess) {
        String idtype = "G";
        JsonArray dirPrv = null;
        
        if(jsonSess != null) {
            if(jsonSess.get("idtype") != null) {
                idtype = jsonSess.get("idtype").toString();
                if(! idtype.equalsIgnoreCase("A")) {
                    Object oDirPrv = (Object) jsonSess.get("privileges");
                    if(oDirPrv != null) {
                        dirPrv = null;
                        if(oDirPrv instanceof JsonArray) {
                            dirPrv = (JsonArray) oDirPrv;
                        } else {
                            dirPrv = (JsonArray) JsonCompatibleUtil.parseJson(oDirPrv.toString().trim());
                        }
                        
                        return dirPrv;
                    }
                } else {
                    return null;
                }
                
                if(idtype.equalsIgnoreCase("B")) {
                    if(noAnonymous) throw new RuntimeException("No privilege");
                }
            } else {
                if(noAnonymous) throw new RuntimeException("No privilege");
            }
        } else {
            if(noAnonymous) throw new RuntimeException("No privilege");
        }
        if(dirPrv == null) dirPrv = new JsonArray();
        
        return dirPrv;
    }
    
    public List<String> getHiddensOnCurrentUser(JsonObject jsonSess, String path, boolean editpriv) {
        String idtype = "G";
        JsonArray dirPrv = getSessionDirectoryPrivileges(jsonSess);
        if(dirPrv == null) dirPrv = new JsonArray();
        
        Object oHiddenDir = conf.get("HiddenDirs");
        List<String> hiddenDirList = new ArrayList<String>();
        
        if(oHiddenDir != null) {
            JsonArray hiddenDir = null;
            if(oHiddenDir instanceof JsonArray) hiddenDir = (JsonArray) oHiddenDir;
            else                                hiddenDir = (JsonArray) JsonCompatibleUtil.parseJson(oHiddenDir.toString().trim());
            oHiddenDir = null;
            
            if(idtype.equalsIgnoreCase("A")) {
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
                    else                          dirOne = (JsonObject) JsonCompatibleUtil.parseJson(row.toString().trim());
                    
                    try {
                        String dPath = dirOne.get("path"     ).toString();
                        String dPrv  = dirOne.get("privilege").toString();
                        
                        int hdx=0;
                        while(hdx < hiddenDirList.size()) {
                            String hiddenDirOne = hiddenDirList.get(hdx);
                            if(hiddenDirOne.startsWith(dPath) || ("/" + hiddenDirOne).startsWith(dPath)) {
                                if(editpriv) {
                                    if(dPrv.equals("edit")) {
                                        hiddenDirList.remove(hdx);
                                        continue;
                                    }
                                } else {
                                    if(dPrv.equals("view") || dPrv.equals("edit")) {
                                        hiddenDirList.remove(hdx);
                                        continue;
                                    }
                                }
                            }
                            hdx++;
                        }
                    } catch(Throwable t) {
                        logIn("Wrong account configuration - " + t.getMessage());
                    }
                }
            }
        }
        
        return hiddenDirList;
    }
    
    /** Call action for FSPack alternative works for FSControl */
    public JsonObject processHandler(String method, HttpServletRequest req) {
        for(FSPack p : packs) {
            FSRequestHandler h = p.getHandler(method);
            if(h != null) {
                Map<String, String> params = new HashMap<String, String>();
                Enumeration<String> enums = req.getParameterNames();
                while(enums.hasMoreElements()) {
                    String pr = enums.nextElement();
                    params.put(pr, req.getParameter(pr));
                }
                return h.handle(this, params, getSessionFSObject(req));
            }
        }
        return null;
    }
    
    /** Call event handler */
    public void invokeCallEvent(String event, String action, HttpServletRequest req) throws Throwable {
    	for(FSPack pack : packs) {
    		List<FSControlEventHandler> list = pack.getEventHandlers();
    		if(list == null) continue;
    		for(FSControlEventHandler e : list) {
    			e.eventOccured(event, action, req);
    		}
    	}
    }
    
    /** Delete all tokens */
    public void removeAllTokens() throws IOException {
        File ftJson = new File(fileConfigPath.getCanonicalPath() + File.separator + "tokens");
        if(ftJson.exists()) {
            File[] children = ftJson.listFiles();
            for(File f : children) {
                if(f.isDirectory()) {
                    File[] gchildren = f.listFiles();
                    for(File gf : gchildren) {
                        if(gf.isDirectory()) {
                            File[] ggchildren = gf.listFiles();
                            for(File ggf : ggchildren) {
                                if(ggf.isDirectory()) {
                                    continue;
                                }
                                try { ggf.delete(); } catch(Throwable t) { t.printStackTrace(); }
                            }
                        }
                        try { gf.delete(); } catch(Throwable t) { t.printStackTrace(); }
                    }
                }
                try { f.delete(); } catch(Throwable t) { t.printStackTrace(); }
            }
        }
    }
    
    /** Dispose FSControl, all FSPacks */
    public synchronized void dispose() {
        log("FS Control starts to dispose instance...", this.getClass());
        try {
            log("    Clearing tokens...", this.getClass());
            removeAllTokens();
        } catch(Throwable tx) { tx.printStackTrace(); }
        try {
            log("    Disposing FS Packs...", this.getClass());
            for(FSPack p : packs) {
                try { p.dispose(this); } catch(Throwable t) { t.printStackTrace(); }
            }
            
            packs.clear();
        } catch(Throwable tx) { tx.printStackTrace(); }
        try {
            
            log("    Closing log streams...", this.getClass());
            
            if(logFileWr != null) { try {  logFileWr.close(); logFileWr = null; } catch(Throwable t) { t.printStackTrace(); } }
            if(logConn   != null) { try {  logConn.close();   logConn   = null; } catch(Throwable t) { t.printStackTrace(); } }
            conf.clear();
        } catch(Throwable tx) { tx.printStackTrace(); }
        System.out.println("FS Control instance is disposed.");
    }
}
