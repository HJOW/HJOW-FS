<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*"%><%
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

String theme      = request.getParameter("theme");

if(theme == null) theme = "";
theme = FSUtils.removeSpecials(theme);
%>
<jsp:include page="common.header.libs.jsp"></jsp:include>
<script type='text/javascript'>
$(function() {
    var bodys = $('body');
    if(bodys.is('.popup')) bodys.removeClass('dark');
    bodys.removeAttr('data-theme');
    
    var theme = "<%=theme%>";
    if(theme != '') {
        bodys.addClass(theme);
        bodys.attr('data-theme', theme);
    } else if(! bodys.is('.popup')) {
        if(FSUtil.detectDark()) {
        	bodys.addClass('dark');
        	bodys.attr('data-theme', 'dark');
        }
    }
    
    var lang = FSUtil.applyLanguage(bodys);
    
    $.ajax({
        url    : "<%=request.getContextPath()%>/jsp/login.jsp",
        data   : { req : 'language', language : lang, force : 'false' },
        method : "POST",
        dataType : "json",
        success : function(data) { }
    });
    
    FSUtil.log(FSUtil.detectBrowser());
});
</script>