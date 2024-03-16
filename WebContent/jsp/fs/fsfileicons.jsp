<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.*, java.awt.Color, hjow.common.json.*" session="true" %><%@ include file="common.pront.jsp"%><%
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
String pathParam = request.getParameter("path");
if(pathParam == null) pathParam = "";
pathParam = pathParam.trim();
if(pathParam.equals("/")) pathParam = "";
pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);

String targets = request.getParameter("files");
if(targets == null) targets = "";
targets = targets.replace("'", "").replace("\"", "");

String br = request.getParameter("br");
String bg = request.getParameter("bg");
String bb = request.getParameter("bb");

JsonObject json = new JsonObject();
json.put("success", new Boolean(false));
json.put("message", "");

System.out.println("DEPRECATED fsfileicons.jsp");

if(fsc.isReadFileIconOn()) {
    try {
        File dir = new File(fsc.getRootPath().getAbsolutePath() + File.separator + pathParam);
        List<File> files = new ArrayList<File>();

        StringTokenizer commaTokenizer = new StringTokenizer(targets, ",");
        int counts = 0;
        while(commaTokenizer.hasMoreTokens()) {
            String sFileOne = commaTokenizer.nextToken().trim();
            File   fileOne  = new File(dir.getAbsolutePath() + File.separator + sFileOne);
            
            if(! fileOne.exists()   ) continue;
            if(fileOne.isDirectory()) continue;
            
            if(counts >= 20) break;
            files.add(fileOne);
            counts++;
        }

        pathParam = null;
        targets = null;
        commaTokenizer = null;
        
        JsonObject jsonResults = new JsonObject();
        
        Color background = new Color(59, 59, 59);
        if(br != null && bg != null && bb != null) {
            int r, g, b;
            r = Integer.parseInt(br);
            g = Integer.parseInt(bg);
            b = Integer.parseInt(bb);
            if(r < 0) r = 0; if(r > 255) r = 255;
            if(g < 0) r = 0; if(g > 255) g = 255;
            if(b < 0) r = 0; if(b > 255) b = 255;
            background = new Color(r, g, b);
        } else {
            background = new Color(59, 59, 59);
        }
        Map<File, String> iconMap = fsc.getIcons(files, background);
        
        Set<File> keys = iconMap.keySet();
        for(File f : keys) {
            jsonResults.put(f.getName(), iconMap.get(f));
        }
        iconMap.clear();
        json.put("data", jsonResults);
        json.put("success", new Boolean(true));
        json.put("message", "");
    } catch(Throwable t) {
        t.printStackTrace();
        json.put("success", new Boolean(false));
        json.put("message", "Error : " + t.getMessage());
    }
} else {
    json.put("data", new JsonObject());
    json.put("success", new Boolean(true));
    json.put("message", "Icon service does not supported.");
}

response.reset();
response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
%><%=json.toJSON().trim()%>