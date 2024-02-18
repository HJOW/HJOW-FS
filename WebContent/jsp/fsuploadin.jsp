<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.util.*, java.io.*, java.util.*, java.text.*, com.oreilly.servlet.*, com.oreilly.servlet.multipart.*, org.json.simple.*, org.json.simple.parser.*" session="true" %><%@ include file="common.pront.jsp"%><%
String uId = "", uIdType = "", msg = "";
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
            uId     = sessionMap.get("id"    ).toString();
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
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title><%= title %></title>
<jsp:include page="./common.header.jsp"></jsp:include>
</head>
<body>
    <div class='container show-grid full'>
	    <div class='row'><div class='col-sm-12'><%= msg %></div></div>
	    <div class='row'><div class='col-sm-12'><input type='button' value='Close' onclick="window.close();"/></div></div>
    </div>
</body>
</html>