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

import java.io.Closeable;
import java.io.IOException;

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
    	try {
        	String pAction = request.getParameter("praction");
        	if(pAction == null) pAction = request.getParameter("PRACTION");
        	if(pAction == null) return;
        	
        	pAction = pAction.toLowerCase().trim();
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
        	} else if(pAction.equals("download") || pAction.equals("down")) {
        		ctrl.download(request, response);
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
        		        int randomNo  = (int) Math.round(1000000 + Math.random() * 1000000 + Math.random() * 10000 + Math.random() * 100);
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
        	}
    	} catch(Throwable t) {
    		FSControl.log("Exception when handling protocol - (" + t.getClass().getName() + ") " + t.getMessage(), this.getClass());
    		
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
