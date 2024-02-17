<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" import="java.io.*, java.util.*, java.security.*, org.json.simple.*, org.json.simple.parser.*, org.apache.commons.codec.binary.Base64" session="true"%><%@ include file="common.pront.jsp"%><%
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

String req = request.getParameter("req");
if(req == null) req = "status";
req = req.trim().toLowerCase();

JSONObject json = new JSONObject();
json.put("success", new Boolean(false));
json.put("message", "");

FileInputStream fIn = null;
Reader r1 = null, r2 = null;
FileOutputStream fOut = null;
try {
	String msg = "";
	
	if(! installed) {
		throw new RuntimeException("Please install first !");
	}
	
	if(fileConfigPath == null) {
		throw new RuntimeException("Please install first !");
	}
	
	File faJson = new File(fileConfigPath.getAbsolutePath() + File.separator + "accounts");
	if(! faJson.exists()) faJson.mkdirs();
	
	if(req.equals("status")) {
		json.put("success", new Boolean(true));
	}
	
	if(req.equals("logout")) {
	    sessionMap = null;
	    needInvalidate = true;
	    msg = "Log out complete";
	    System.out.println("Session log out from " + request.getRemoteAddr());
	    json.put("success", new Boolean(true));
	}
	
	if(req.equals("login")) {
		if(sessionMap != null) needInvalidate = true;
		sessionMap = null;
		
		if(noLogin) {
            throw new RuntimeException("No-Login Mode !");
        }
		
		String id = request.getParameter("id");
		String pw = request.getParameter("pw");
		
		if(id == null) id = "";
		if(pw == null) pw = "";
		
		id = id.trim(); pw = pw.trim();
		
		if(id.equals("")) msg = "Please input ID !";
		
		if(id.contains("'") || id.contains("\"") || id.contains("/") || id.contains("\\") || id.contains(File.separator) || id.contains(".") || id.contains(" ") || id.contains("\n") || id.contains("\t")) throw new RuntimeException("ID can only contains alphabets and numbers !");
		if(msg.equals("")) { if(pw.equals("")) msg = "Please input Password !"; }
		
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
	                msg = "Cannot login now ! Please try later.";
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
	                    		if(loops >= 10000L) throw new RuntimeException("The server is busy. Please try later.");
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
                            if(loops >= 10000L) throw new RuntimeException("The server is busy. Please try later.");
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

response.reset();
response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
%><%=json.toJSONString().trim()%>