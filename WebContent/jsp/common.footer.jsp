<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.FSControl"%><%--
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
 --%>
<script type='text/javascript'>
$(function(){
	var footer = $('.fs_footer');
	var aLink  = footer.find('.a_footer');
	aLink.on('click', function() {
		var theme = $('body').attr('data-theme');
		if(theme == null || typeof(theme) == 'undefined') theme = '';
		window.open("<%=request.getContextPath()%>/jsp/fsabout.jsp?theme=" + theme, "pop_about", "width=580,height=600,scrollbars=no,status=no,location=no,toolbar=no");
	});
	var spanJs = footer.find('.span_js_version');
	spanJs.text(FSUtil.version[0] + '.' + FSUtil.version[1] + '.' + FSUtil.version[2]);
	spanJs.attr('title', 'Build ' + FSUtil.version[3]);
});
</script>
<div class='fs_footer footer container show-grid full'>
    <div class='row'>
        <div class='col-sm-12 align_center'>
            FS (
            <span class='span_version'>
                JS    <span class='span_js_version'></span>
                Class <span class='span_class_version' title="Build <%=FSControl.VERSION[3]%>"><%= FSControl.VERSION[0] %>.<%= FSControl.VERSION[1] %>.<%= FSControl.VERSION[2] %></span>
            </span>
            ) - 
            <a href='#' class='a_footer'>Copyright 2024 HJOW (Heo Jin Won)</a>
        </div>
    </div>
</div>