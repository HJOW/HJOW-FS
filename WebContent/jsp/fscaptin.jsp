<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.util.*, java.io.*, java.awt.*, java.awt.image.*, javax.imageio.*,org.apache.commons.codec.binary.Base64" %><%@ include file="common.pront.jsp"%><%
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
String code  = (String) request.getSession().getAttribute("captcha_code");
Long   time  = (Long)   request.getSession().getAttribute("captcha_time");
String theme = request.getParameter("theme");

if(time == null) time = new Long(0L);
String bs64str = fsc.createCaptchaBase64(request, code, time.longValue(), theme);

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
    $(function() {
        setTimeout(function() { location.reload(); }, <%= fsc.captchaLimitTime %>);
        if(<%=captDarkMode%>) { $('body').css('background-color', 'black'); }
    });
    </script>
</head>
<body>
    <img src="data:image/jpeg;base64,<%=bs64str%>"/>
</body>
</html>