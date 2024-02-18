<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*"%><%
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
});
</script>