<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.io.*, java.util.* "%><%@ include file="common.pront.jsp"%><%
request.setCharacterEncoding("UTF-8");
String clients = request.getHeader("User-Agent");

String pathParam = request.getParameter("path");
String fileName  = request.getParameter("filename");
String speed     = request.getParameter("speed");

if(pathParam == null) pathParam = "";
if(fileName  == null) fileName  = "";
if(speed     == null) speed     = "";

pathParam = pathParam.replace("'", "").replace("\"", "").replace(".", "").trim();
fileName  = fileName.replace("'", "").replace("\"", "").trim();
speed     = speed.replace("'", "").replace("\"", "").trim();

int randomNo  = (int) Math.round(1000000 + Math.random() * 1000000 + Math.random() * 10000 + Math.random() * 100);
String strRan = String.valueOf(randomNo).substring(0, 7);

session.setAttribute("captcha_code", strRan);
session.setAttribute("captcha_time", new Long(now));
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title>File Storage</title>
<jsp:include page="./common.header.jsp"></jsp:include>
<script type='text/javascript'>
$(function() {
    $('.btn_re').on('click', function() {
        location.reload();
    });
    
    $('.inp_captcha').focus();
});
</script>
</head>
<body>
     <h3>Captcha Authentication</h3>
     <div>
         <p>For download <%= fileName %>...</p>
     </div>
     <div>
         <iframe style='width: <%=captchaWidth + 10%>px; height: <%=captchaHeight + 10%>px;' src='fscaptin.jsp'></iframe>
     </div>
     <div>
         <form action='fsdown.jsp' method='POST' class='form'>
             <input type='hidden' name='path'     value='<%=pathParam%>'/>
             <input type='hidden' name='filename' value='<%=fileName%>'/>
             <input type='hidden' name='speed'    value='<%=speed%>'/>
             <input type='text'   class='inp_captcha' name='captcha'/>
             <input type='submit' class='btn_dn' value='Download Now !'/>
             <input type='button' class='btn_re' value='Refresh'/>
         </form>
     </div>
</body>
</html>