<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, hjow.common.util.*, java.io.*, java.util.* " session="true"%><%
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
if(FSUtils.getAttribute(request, "fsmodern") == null) request.setAttribute("fsmodern", new Boolean(BrowserInfo.detectSupportES6(BrowserInfo.byUserAgent(request.getHeader("User-Agent")))));
if(DataUtil.parseBoolean(FSUtils.getAttribute(request, "fsmodern"))) {
	// fsmodern.jsp fsclassic.jsp
	%><jsp:include page="fsclassic.jsp"></jsp:include><%
} else {
	%><jsp:include page="fsclassic.jsp"></jsp:include><%
}
%>