<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" import="java.io.*, java.util.*, org.json.simple.*"%><%@ include file="common.pront.jsp"%><%
String pathParam = request.getParameter("path");
if(pathParam == null) pathParam = "";
pathParam = pathParam.trim();
if(pathParam.equals("/")) pathParam = "";
pathParam = pathParam.replace(".", "").replace("'", "").replace("\"", "").replace("\\", "/").trim(); // 상대경로 방지를 위해 . 기호는 반드시 제거 !
if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);

String keyword = request.getParameter("keyword");
if(keyword == null) keyword = "";
keyword = keyword.replace("'", "").replace("\"", "").trim();

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
    if(! keyword.equals("")) { if(! name.contains(keyword)) continue; }
    
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
response.reset();
response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
%><%=json.toJSONString().trim()%>