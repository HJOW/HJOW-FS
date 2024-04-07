<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.* " session="true"%><%@ include file="common.pront.jsp"%><%
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
<div id='fs_root' class='fs_root fs_div'></div>
<script>
$(function() {
	$('body').addClass('modern').addClass('dark');
});
</script>
<script type="text/babel" data-presets="es2015,react" data-plugins="transform-es2015-modules-umd">
if(! <%= fsc.isInstalled() %>) {
    location.href = "<%= fsc.getContextPath() %>/jsp/fs/fsinstall.jsp";
} else {
    ReactDOM.createRoot(document.getElementById('fs_root')).render(<FSRoot basic={new FSBasic()}/>);
}
</script>
<div class='full invisible' id='fs_filelist_anonymous_source'>
    <jsp:include page="fsanonymousblock.jsp"></jsp:include>
</div>
<jsp:include page="common.footer.jsp"></jsp:include>