<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.*, org.json.simple.*, org.json.simple.parser.*" session="true" %><%@ include file="common.pront.jsp"%><%
String pathParam = request.getParameter("path");
if(pathParam == null) pathParam = "";
pathParam = pathParam.trim();
if(pathParam.equals("/")) pathParam = "";
pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);

String keyword = request.getParameter("keyword");
if(keyword == null) keyword = "";
keyword = keyword.replace("'", "").replace("\"", "").replace("<", "").replace(">", "").trim();

JSONObject json = fsc.list(request, pathParam, keyword);

response.reset();
response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
%><%=json.toJSONString().trim()%>