<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="hjow.common.util.*, com.hjow.fs.*"%><%
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
<% if(FSControl.useModern(request)) { %>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/bootstrap.theme.dark.css"/>
<% } %>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/fonts.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/fs.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/fsdark.css"/>
<% if(! FSControl.useModern(request)) { %>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/video-js.css"/>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/video.js'></script>
<!--[if lt IE 9]>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/html5shiv-printshiv.min.js'></script>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/videojs-ie8.min.js'></script>
<![endif]-->
<script type='text/javascript' src='<%= ctxPathCmm %>/js/polyfill.min.js'></script>
<% } %>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/jquery-1.12.4.js'></script>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/jquery-ui.js'></script>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/bootstrap.js'></script>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/fscommon.js'></script>
<% if(FSControl.useModern(request)) { %>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/modern/fs.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/modern/fsdark.css"/>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/fscommones6.js'></script>
<script type="text/javascript" src="<%=ctxPathCmm%>/js/modern/react.development.js"></script>
<script type="text/javascript" src="<%=ctxPathCmm%>/js/modern/react-dom.development.js"></script>
<script type="text/javascript" src="<%=ctxPathCmm%>/js/modern/babel.min.js"></script>
<% FSControl fscHdr = FSControl.getInstance(); %>
<script type="text/javascript">
function FSBasic() {
    this.ctxPath        = "<%=fscHdr.getContextPath()%>";
    this.useIcon        = <%=fscHdr.isReadFileIconOn() ? "true" : "false"%>;
    this.useCaptchaDown = <%=fscHdr.isCaptchaDownloadOn() ? "true" : "false"%>;
    this.noAnonymous    = <%=fscHdr.isNoAnonymous() ? "true" : "false"%>;
    this.loginedFirst   = <%=(fscHdr.getSessionUserId(request) != null) ? "true" : "false"%>;
    this.captSizes      = {
        width  : <%= fscHdr.getCaptchaWidth()  %>,
        height : <%= fscHdr.getCaptchaHeight() %>
    };
    this.version = {
    	server : [ <%= FSControl.VERSION[0] %>, <%= FSControl.VERSION[1] %>, <%= FSControl.VERSION[2] %>, <%= FSControl.VERSION[3] %> ],
    	client : FSUtil.version
    };
    this.getTheme = function getTheme() {
    	if($('body').is('.dark'))   return 'dark';
        else if($('.fs_root').is('.dark')) return 'dark';
    	return 'light';
    };
}
</script>
<script type="text/babel" data-type="module" data-presets="es2015,react" src="<%=ctxPathCmm%>/js/modern/fs.js"></script>
<% } %>