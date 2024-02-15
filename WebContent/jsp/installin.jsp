<%@ page language="java" pageEncoding="UTF-8" import="java.io.*, java.util.*, java.security.*, org.json.simple.*, org.json.simple.parser.*, org.apache.commons.codec.binary.Base64 "%><%@ include file="common.pront.jsp"%><%
JSONObject json = new JSONObject();
json.put("success", new Boolean(false));
json.put("message", "");

if(! installed) {
	Properties   propTest = new Properties();
    InputStream  propIn   = null;
	OutputStream fileOut  = null;
	try {
		String passwords = request.getParameter("pw");
		if(passwords == null) throw new RuntimeException("Please input Password !");
		
		propIn = this.getClass().getResourceAsStream("/test.properties");
        if(propIn == null) throw new FileNotFoundException("No test.properties found at ./WEB-INF/classes/");
        
        propTest = new Properties();
        propTest.load(propIn);
        
        propIn.close(); propIn = null;
        
        // Check test.properties
        String tx1 = propTest.getProperty("FS");
        String tx2 = propTest.getProperty("RD");
        String tx3 = propTest.getProperty("PW");
        String s1  = propTest.getProperty("S1");
        String s2  = propTest.getProperty("S2");
        String s3  = propTest.getProperty("S3");
        
        propTest.clear();
        propTest = null;
        
        if(tx1 == null || tx2 == null || tx3 == null || s1 == null || s2 == null || s3 == null) {
        	throw new FileNotFoundException("No correct test.properties found at ./WEB-INF/classes/ ! Please check values !");
        } else if(! (tx1.trim().equals("FileStorage") && tx2.trim().equals("SetConfigPathBelow"))) {
        	throw new FileNotFoundException("No correct test.properties found at ./WEB-INF/classes/ ! Please check values !");
        }
        
		String roots = request.getParameter("rootdir");
		if(roots == null) throw new RuntimeException("Please input the Root Directory !");
		
		if(! passwords.trim().equals(tx3.trim())) {
			throw new RuntimeException("Wrong installation password !");
		}
		
		rootPath = new File(roots);
		if(! rootPath.exists()) rootPath.mkdirs();
		
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

response.reset();
response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
%><%=json.toJSONString()%>