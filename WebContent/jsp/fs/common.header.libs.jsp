<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="hjow.common.util.*"%><%
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

String ctxPathCmm = request.getContextPath();
%>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/jquery-ui.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/jquery-ui.structure.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/jquery-ui.theme.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/bootstrap.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/bootstrap-theme.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/fonts.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/video-js.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/fs.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/fsdark.css"/>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/video.js'></script>
<!--[if lt IE 9]>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/html5shiv-printshiv.min.js'></script>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/videojs-ie8.min.js'></script>
<![endif]-->
<script type='text/javascript' src='<%= ctxPathCmm %>/js/jquery-1.12.4.js'></script>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/jquery-ui.js'></script>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/bootstrap.js'></script>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/fscommon.js'></script>
<% if(BrowserInfo.detectSupportES6(BrowserInfo.byUserAgent(request.getHeader("User-Agent")))) { %>
<script type="text/javascript" src="<%=ctxPathCmm%>/js/modern/react.development.js"></script>
<script type="text/javascript" src="<%=ctxPathCmm%>/js/modern/react-dom.development.js"></script>
<script type="text/javascript" src="<%=ctxPathCmm%>/js/modern/babel.min.js"></script>
<script type="text/babel" data-type="module" src="<%=ctxPathCmm%>/js/modern/fs.js"></script>
<% } %>