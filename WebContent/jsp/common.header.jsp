<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*"%><%
String ctxPathCmm = request.getContextPath();
String theme      = request.getParameter("theme");

if(theme == null) theme = "";
theme = FSUtils.removeSpecials(theme);
%>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/jquery-ui.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/jquery-ui.structure.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/jquery-ui.theme.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/bootstrap.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/bootstrap-theme.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/fs.css"/>
<link rel="stylesheet" href="<%= ctxPathCmm %>/css/fsdark.css"/>
<!--[if lt IE 9]>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/html5shiv-printshiv.min.js'></script>
<![endif]-->
<script type='text/javascript' src='<%= ctxPathCmm %>/js/jquery-1.12.4.js'></script>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/jquery-ui.js'></script>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/bootstrap.js'></script>
<script type='text/javascript' src='<%= ctxPathCmm %>/js/fscommon.js'></script>
<script type='text/javascript'>
$(function() {
    var bodys = $('body');
    if(bodys.is('.popup')) bodys.removeClass('dark');
    
    var theme = "<%=theme%>";
    if(theme != '') {
        bodys.addClass(theme);
    } else if(! bodys.is('.popup')) {
        if(FSUtil.detectDark()) {
        	bodys.addClass('dark');
        }
    }
    
    FSUtil.applyLanguage(bodys);
});
</script>