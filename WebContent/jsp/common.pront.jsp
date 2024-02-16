<%@ page language="java" import="java.io.*, java.util.*, org.json.simple.*, org.json.simple.parser.* "%><%!
// Configs
JSONObject conf = new JSONObject();
volatile long    confReads    = 0L;
volatile boolean confChanging = false;
volatile boolean accChanging  = false;
boolean installed = false;
%><%
long now = System.currentTimeMillis();
String cs = "UTF-8";

// Installation Status
File fileConfigPath = null;

// Storage Root Directory
String storPath  = "/fsdir/storage/";

long   refreshConfGap = 4000L;

// Download limit size (KB)
long   limitSize = 10 * 1024 * 1024;

// Perform downloading buffer size (bytes)
int  bufferSize   = 1024 * 10;

// Perform downloading thread's sleeping gap (milliseconds) - Downloading speed faster when this value is become lower.
long sleepGap     = 100;

// Perform downloading thread's sleeping cycle (times) - Downloading speed faster when this value is become lower.
int  sleepRoutine = 100;

int  captchaWidth     = 250;
int  captchaHeight    = 100;
int  captchaFontSize  = 80;
long captchaLimitTime = 1000 * 60 * 5;
boolean captDarkMode  = false;

// Login Policy
boolean noLogin = false;
int loginFailCountLimit = 10;
int loginFailOverMinute = 10;

String salt = "fs";

/*********************** Don't edit below ! ****************************/
// Set Global Variables
File rootPath  = null;
String ctxPath = request.getContextPath();

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
		
	    propIn = this.getClass().getResourceAsStream("/test.properties");
	    if(propIn != null) {
	    	Properties propTest = new Properties();
	        propTest.load(propIn);
	        // Check test.properties
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
	                
	                // Get configuration directory from test.properties
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
	            // Close test.properties
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
	        System.out.println("No test.properties !");
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
	if(conf.get("UseAccount") != null) {
		noLogin = (! Boolean.parseBoolean(conf.get("UseAccount").toString().trim()));
	}
	if(conf.get("Salt") != null) {
		salt = conf.get("Salt").toString().trim();
	}
}

if(noLogin) {
	Object sessionMap = request.getSession().getAttribute("fssession");
	if(sessionMap != null) request.getSession().removeAttribute("fssession");
}
%>