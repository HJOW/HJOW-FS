<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.*, java.text.*, com.oreilly.servlet.*, com.oreilly.servlet.multipart.*, hjow.common.json.*, hjow.common.util.*" session="true" %><%@ include file="common.pront.jsp"%><%
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

try { fsc.invokeCallEvent("before", "download", request); } catch(Throwable t) { t.printStackTrace(); }

String msg = fsc.upload(request);

try { fsc.invokeCallEvent("after" , "download", request); } catch(Throwable t) { t.printStackTrace(); }
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title><%= fsc.getTitle() %></title>
<jsp:include page="./common.header.jsp"></jsp:include>
</head>
<body>
    <div class='container show-grid full'>
        <div class='row'><div class='col-sm-12'><%= msg %></div></div>
    </div>
</body>
</html>