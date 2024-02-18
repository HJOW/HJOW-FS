<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.*, java.text.*, com.oreilly.servlet.*, com.oreilly.servlet.multipart.*, org.json.simple.*, org.json.simple.parser.*" session="true" %><%@ include file="common.pront.jsp"%><%
String msg = fsc.upload(request);
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title><%= fsc.title %></title>
<jsp:include page="./common.header.jsp"></jsp:include>
</head>
<body>
    <div class='container show-grid full'>
	    <div class='row'><div class='col-sm-12'><%= msg %></div></div>
	    <div class='row'><div class='col-sm-12'><input type='button' value='Close' onclick="window.close();"/></div></div>
    </div>
</body>
</html>