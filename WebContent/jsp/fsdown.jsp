<%@ page language="java" import="com.hjow.fs.*, java.util.*, java.net.*, java.io.*" %><%@ include file="common.pront.jsp"%><%
String msg = fsc.download(request, response);
if(msg != null) out.println(msg);
%>