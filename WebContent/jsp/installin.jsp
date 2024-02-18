<%@ page language="java" pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.*, java.security.*, org.json.simple.*, org.json.simple.parser.*, org.apache.commons.codec.binary.Base64 "%><%@ include file="common.pront.jsp"%><%
JSONObject json = fsc.install(request);

response.reset();
response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
%><%=json.toJSONString()%>