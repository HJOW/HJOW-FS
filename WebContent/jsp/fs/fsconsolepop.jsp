<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*"%><%@ include file="common.pront.jsp"%><%
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
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title><%= fsc.getTitle() %></title>
<jsp:include page="./common.header.jsp"></jsp:include>
<script type='text/javascript'>
$(function() {
	$('.tf_terminal_console').focus();
});
</script>
</head>
<body class='popup'>
    <div class='fs_div fs_consolepop container show-grid full'>
        <jsp:include page="fsconsole.jsp"></jsp:include>
    </div>
</body>
</html>