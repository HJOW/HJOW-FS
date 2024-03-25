<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, hjow.common.util.*"%><%
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

request.setAttribute("fsmodern", new Boolean(BrowserInfo.detectSupportES6(BrowserInfo.byUserAgent(request.getHeader("User-Agent")))));

String theme = request.getParameter("theme");

if(theme == null) theme = "";
theme = FSUtils.removeSpecials(theme);
%>
<jsp:include page="common.header.libs.jsp"></jsp:include>
<script type='text/javascript'>
$(function() {
    var lang = FSUtil.applyLanguage(bodys);
    
    var browser = FSUtil.detectBrowser();
    if(browser.nm == 'ie' && browser.ver < 8) {
        if(lang == 'ko') alert('이 웹 브라우저에서는 FS 를 원활히 사용할 수 없습니다.');
        else             alert('FS does not support this web browser !');
    }
    
    var bodys = $('body');
    if(bodys.is('.popup')) bodys.removeClass('dark');
    bodys.removeAttr('data-theme');
    
    var theme = "<%=theme%>";
    if(theme != '') {
        bodys.addClass(theme);
        bodys.attr('data-theme', theme);
    } else if(! bodys.is('.popup')) {
        if(FSUtil.detectDark()) {
            theme = 'dark';
            bodys.addClass(theme);
            bodys.attr('data-theme', theme);
        }
    }
    
    var captLogin = $('.if_captcha_l');
    if(captLogin != null && typeof(captLogin) != 'undefined' && captLogin.length >= 1) {
        captLogin.attr('src', "<%=request.getContextPath()%>/jsp/fs/fscaptin.jsp?key=fsl&randomize=true&scale=1&theme=" + theme + FSUtil.addTokenParameterString());
    }
    
    FSUtil.ajax({
        url    : "<%=request.getContextPath()%>/jsp/fs/fsproc.jsp",
        data   : { req : 'language', language : lang, force : 'false', praction : 'account' },
        method : "POST",
        dataType : "json",
        success : function(data) {
            if(data) {
                if(data.needrefresh) location.reload();
            }
        }
    });
});
</script>