<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.util.*, java.io.*, java.util.*, org.json.simple.*, org.json.simple.parser.*" session="true" %><%@ include file="common.pront.jsp"%><%
String pathParam = request.getParameter("path");
if(pathParam == null) pathParam = "";
pathParam = pathParam.trim();
if(pathParam.equals("/")) pathParam = "";
pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);

String keyword = request.getParameter("keyword");
if(keyword == null) keyword = "";
keyword = keyword.replace("'", "").replace("\"", "").replace("<", "").replace(">", "").trim();

File dir = new File(rootPath.getAbsolutePath() + File.separator + pathParam);

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
        if(f.length() / 1024 >= limitSize) continue;
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
    
	String linkDisp = f.getAbsolutePath().replace(rootPath.getAbsolutePath(), "").replace("\\", "/").replace("'", "").replace("\"", "");
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

response.reset();
response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
%><%=json.toJSONString().trim()%>