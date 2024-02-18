<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.util.*, java.io.*, java.awt.*, java.awt.image.*, javax.imageio.*,org.apache.commons.codec.binary.Base64" %><%@ include file="common.pront.jsp"%><%
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