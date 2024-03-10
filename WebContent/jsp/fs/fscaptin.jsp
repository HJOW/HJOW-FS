<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.util.*, java.io.*, java.awt.*, java.awt.image.*, javax.imageio.*, hjow.common.util.DataUtil" %><%@ include file="common.pront.jsp"%><%
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
String key   = request.getParameter("key");
String theme = request.getParameter("theme");
String randm = request.getParameter("randomize");
String scale = request.getParameter("scale");
String ctype = request.getParameter("captype");

String code  = (String) request.getSession().getAttribute(key + "_captcha_code");
Long   time  = (Long)   request.getSession().getAttribute(key + "_captcha_time");

if(randm != null) {
    boolean randomize = DataUtil.parseBoolean(randm);
    if(randomize) {
        int randomNo  = (int) Math.round(1000000 + Math.random() * 1000000 + Math.random() * 10000 + Math.random() * 100);
        String strRan = String.valueOf(randomNo).substring(0, 7);
        
        code = strRan;
        time = new Long(now);

        request.getSession().setAttribute(key + "_captcha_code", code);
        request.getSession().setAttribute(key + "_captcha_time", time);
    }
}

if(scale == null) scale = "1.0";
if(time == null) time = new Long(0L);

if(ctype == null) ctype = "image";
ctype = ctype.trim().toLowerCase();

String captRes = null;

if(ctype.equals("text")) {
	captRes = fsc.createTextCaptcha(request, key, code, time.longValue());
} else {
	captRes = fsc.createCaptchaBase64(request, key, code, time.longValue(), Double.parseDouble(scale), theme);
}

boolean captDarkMode  = false;
if(theme != null) {
    if(theme.equals("dark")) captDarkMode =  true;
}
%>
<!DOCTYPE html>
<html>
<head>
    <jsp:include page="./common.header.jsp"></jsp:include>
    <script type='text/javascript'>
    function iRefresh() {
        location.reload();
    }
    $(function() {
        setTimeout(function() { location.reload(); }, <%= fsc.getCaptchaLimitTime() %>);
        if(<%=captDarkMode%>) { $('body').css('background-color', '#3b3b3b'); $('textarea').css('background-color', '#3b3b3b'); $('textarea').css('color', '#C9C9C9'); }
    });
    </script>
</head>
<body>
<% if(ctype.equals("text")) { %>
    <textarea class='full' style='width: 100%; min-height: 100px; overflow-x: scroll; overflow-y: hidden; font-size: 6px;' readonly><%=captRes%></textarea>
<% } else { %>
    <img src="data:image/jpeg;base64,<%=captRes%>"/>
<% } %>
</body>
</html>