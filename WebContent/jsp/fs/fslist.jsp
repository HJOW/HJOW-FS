<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.*, hjow.common.json.*" session="true" %><%@ include file="common.pront.jsp"%><%
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
String pathParam = request.getParameter("path");
if(pathParam == null) pathParam = "";
pathParam = pathParam.trim();
if(pathParam.equals("/")) pathParam = "";
pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);

String keyword = request.getParameter("keyword");
if(keyword == null) keyword = "";
keyword = keyword.replace("'", "").replace("\"", "").replace("<", "").replace(">", "").trim();

String excepts = request.getParameter("excepts");
if(excepts == null) excepts = "";

JsonObject json = fsc.list(request, pathParam, keyword, excepts);

response.reset();
response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
%><%=json.toJSON().trim()%>