<%@ page language="java" import="com.hjow.fs.*, java.util.*, java.net.*, java.io.*" %><%@ include file="common.pront.jsp"%><%
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
out.clear();
pageContext.pushBody();
try { fsc.invokeCallEvent("before", "download", request); } catch(Throwable t) { t.printStackTrace(); }

fsc.download(request, response);

try { fsc.invokeCallEvent("after" , "download", request); } catch(Throwable t) { t.printStackTrace(); }
return;
%>