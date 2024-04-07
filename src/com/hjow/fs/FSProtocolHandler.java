/*
Copyright 2019 HJOW

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
package com.hjow.fs;

import java.awt.Color;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hjow.common.json.JsonObject;
import hjow.common.util.ClassUtil;
import hjow.common.util.DataUtil;

public class FSProtocolHandler implements Closeable {
    private static final FSProtocolHandler instances = new FSProtocolHandler();
    public static FSProtocolHandler getInstnace() { return instances; }
    private FSProtocolHandler() { }
    
    protected FSControl ctrl = FSControl.getInstance();
    
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        if(ctrl == null) ctrl = FSControl.getInstance();
        String pAction = "";
        try {
            pAction = request.getParameter("praction");
            if(pAction == null) pAction = request.getParameter("PRACTION");
            if(pAction == null) pAction = "";
            
            pAction = pAction.toLowerCase().trim();
            
            ctrl.invokeCallEvent("before",  pAction, request);
            
            if(pAction.equals("list")) {
                String pathParam = request.getParameter("path");
                if(pathParam == null) pathParam = "";
                pathParam = pathParam.trim();
                if(pathParam.equals("/")) pathParam = "";
                pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
                if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);

                String keyword = request.getParameter("keyword");
                if(keyword == null) keyword = "";
                keyword = keyword.replace("'", "").replace("\"", "").replace("<", "").replace(">", "").trim();

                String excepts = request.getParameter("excepts");
                if(excepts == null) excepts = "";
                
                JsonObject json = ctrl.list(request, pathParam, keyword, excepts);

                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(json.toJSON().getBytes("UTF-8"));
            } else if(pAction.equals("listall")) {
            	JsonObject json = ctrl.listAll(request);

                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(json.toJSON().getBytes("UTF-8"));
            } else if(pAction.equals("download") || pAction.equals("down")) {
                ctrl.download(request, response);
            } else if(pAction.equals("upload")) {
            	String msg = ctrl.upload(request);
            	
            	response.reset();
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(msg.getBytes("UTF-8"));
            } else if(pAction.equals("account")) {
                JsonObject json = ctrl.account(request);
                
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(json.toJSON().getBytes("UTF-8"));
            } else if(pAction.equals("console")) {
                JsonObject json = ctrl.console(request);
                
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(json.toJSON().getBytes("UTF-8"));
            } else if(pAction.equals("captcha")) {
                String key   = request.getParameter("key");
                String theme = request.getParameter("theme");
                String randm = request.getParameter("randomize");
                String scale = request.getParameter("scale");
                String ctype = request.getParameter("captype");

                String code  = (String) ctrl.getSessionObject(request, key + "_captcha_code");
                Long   time  = (Long)   ctrl.getSessionObject(request, key + "_captcha_time");

                if(randm != null) {
                    boolean randomize = DataUtil.parseBoolean(randm);
                    if(randomize) {
                        int randomNo  = (int) Math.round(1000000 + FSControl.random() * 1000000 + FSControl.random() * 10000 + FSControl.random() * 100);
                        String strRan = String.valueOf(randomNo).substring(0, 7);
                        
                        code = strRan;
                        time = new Long(System.currentTimeMillis());

                        ctrl.setSessionObject(request, key + "_captcha_code", code);
                        ctrl.setSessionObject(request, key + "_captcha_time", time);
                    }
                }

                if(scale == null) scale = "1.0";

                if(time == null) time = new Long(0L);
                
                if(ctype == null) ctype = "image";
                ctype = ctype.trim().toLowerCase();
                
                JsonObject json = new JsonObject();
                json.put("captype", ctype);
                
                String captRes = null;
                
                if(ctype.equals("text")) {
                    captRes = ctrl.createTextCaptcha(request, key, code, time.longValue());
                } else {
                    captRes = ctrl.createCaptchaBase64(request, key, code, time.longValue(), Double.parseDouble(scale), theme);
                }
                json.put("captcha", captRes);
                
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(json.toJSON().getBytes("UTF-8"));
            } else if(pAction.equals("chcaptcha")) {
            	String key = request.getParameter("key");
            	
            	JsonObject json = new JsonObject();
                json.put("avail", new Boolean(ctrl.isCaptchaCreated(request, key)));
            	
            	response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(json.toJSON().getBytes("UTF-8"));
            } else if(pAction.equals("fileicon")) {
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

            	if(ctrl.isReadFileIconOn()) {
            	    try {
            	        File dir = new File(ctrl.getRootPath().getAbsolutePath() + File.separator + pathParam);
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
            	        Map<File, String> iconMap = ctrl.getIcons(files, background);
            	        
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
            	response.getOutputStream().write(json.toJSON().getBytes("UTF-8"));
            } else if(pAction.equals("install")) {
                JsonObject json = ctrl.install(request);
                
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(json.toJSON().getBytes("UTF-8"));
            } else if(pAction.equals("admin")) {
                JsonObject json = ctrl.admin(request);
                
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(json.toJSON().getBytes("UTF-8"));
            } else if(pAction.equals("mkdir")) {
            	JsonObject json = ctrl.mkdir(request);
            	
            	response.reset();
            	response.setContentType("application/json");
            	response.setCharacterEncoding("UTF-8");
            	response.getOutputStream().write(json.toJSON().getBytes("UTF-8"));
            } else if(pAction.equals("remove")) {
                JsonObject json = ctrl.remove(request);
            	
            	response.reset();
            	response.setContentType("application/json");
            	response.setCharacterEncoding("UTF-8");
            	response.getOutputStream().write(json.toJSON().getBytes("UTF-8"));
            } else {
            	throw new RuntimeException("Unknown Action " + pAction);
            }
            
            ctrl.invokeCallEvent("after",  pAction, request);
        } catch(Throwable t) {
            FSControl.log("Exception when handling protocol - (" + t.getClass().getName() + ") " + t.getMessage(), this.getClass());
            try { ctrl.invokeCallEvent("exception", pAction, request); } catch(Throwable ignores) {}
            
            JsonObject json = new JsonObject();
            json.put("message", "Error : " + t.getMessage());
            json.put("success", new Boolean(false));
            
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try { response.getOutputStream().write(json.toJSON().getBytes("UTF-8")); } catch(Throwable ignores) {}
        }        
    }
    
    public static void disposeInstance() {
        ClassUtil.closeAll(getInstnace());
    }
    
    @Override
    public void close() throws IOException {
        ctrl = null;
    }
}
