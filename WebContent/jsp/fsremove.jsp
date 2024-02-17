<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" import="java.io.*, java.util.*, java.text.*, org.json.simple.*, org.json.simple.parser.*" session="true" %><%@ include file="common.pront.jsp"%><%
String uId = "", uIdType = "";
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
        }
    }
} catch(Throwable t) {
    t.printStackTrace();
}

JSONObject json = new JSONObject();
json.put("success", new Boolean(false));
json.put("message", "");

try {
	if(uIdType.equals("A")) {
		String pathParam = request.getParameter("path");
	    if(pathParam == null) pathParam = "";
	    pathParam = pathParam.trim();
	    if(pathParam.equals("/")) pathParam = "";
	    pathParam = pathParam.replace(".", "").replace("'", "").replace("\"", "").replace("\\", "/").trim(); // 상대경로 방지를 위해 . 기호는 반드시 제거 !
	    if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);

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

response.reset();
response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
%><%=json.toJSONString().trim()%>