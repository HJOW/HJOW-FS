<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.util.*, java.io.*, java.util.* " isErrorPage="true" %><%
String excMsg = "";
if(exception != null) excMsg = exception.getMessage();
if(excMsg    == null) excMsg = "";
excMsg = excMsg.replace("<", "&lt;").replace(">", "&gt;");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title>Error</title>
<jsp:include page="./common.header.jsp"></jsp:include>
</head>
<body>
    <div class='exception'>
        <h4>Error</h4>
        <pre><%= excMsg %></pre>
        <a href="../index.jsp">[Home]</a>
    </div>
</body>
</html>