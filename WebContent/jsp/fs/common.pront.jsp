<%@ page language="java" import="com.hjow.fs.*, java.io.*, java.util.*, hjow.common.json.*, com.hjow.fs.etc.* "%><%!
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
FSControl fsc = FSControl.getInstance();
%><%
long now = System.currentTimeMillis();
FSControl.init(request.getContextPath());
fsc = FSControl.getInstance();
if(fsc.getRealPath() == null) fsc.setRealPath(request);
%>