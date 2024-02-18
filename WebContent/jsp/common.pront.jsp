<%@ page language="java" import="com.hjow.fs.*, java.io.*, java.util.*, org.json.simple.*, org.json.simple.parser.* "%><%!
FSControl fsc = FSControl.getInstance();
%><%
long now = System.currentTimeMillis();
FSControl.init(request);
fsc = FSControl.getInstance();
%>