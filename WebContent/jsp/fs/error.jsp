<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.* , java.net.*" isErrorPage="true" %><%
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
String excMsg = "";
try {
	if(exception != null) {
		excMsg = exception.getMessage();
		if(exception instanceof SocketException) {
			excMsg = "On error.jsp, " + excMsg;
			try { FSControl.log(excMsg, this.getClass()); } catch(Throwable ig) { System.out.println(excMsg); }
			return;
		}
		
		if(exception.getCause() != null) {
			Throwable causes = exception.getCause();
			excMsg = exception.getMessage();
			if(causes instanceof SocketException) {
	            excMsg = "On error.jsp, " + excMsg;
	            try { FSControl.log(excMsg, this.getClass()); } catch(Throwable ig) { System.out.println(excMsg); }
	            return;
	        }
		}
	}
	
	if(excMsg == null) excMsg = "";
	excMsg = excMsg.replace("<", "&lt;").replace(">", "&gt;");
	%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title>Error</title>
<jsp:include page="./common.header.jsp"></jsp:include>
</head>
<body>
    <div class='exception'>
        <h4>Error</h4>
        <pre><%= excMsg %></pre>
        <a href="../index.jsp">[Home]</a>
    </div>
</body>
</html>	
	<%
} catch(Throwable tx) {
	excMsg = "Exception on error.jsp - (" + tx.getClass().getName() + ") - " + tx.getMessage() + "\n";
	if(exception != null) excMsg += "    Caused exception - (" + exception.getClass().getName() + ") - " + exception.getMessage();
	try { FSControl.log(excMsg, this.getClass()); } catch(Throwable ig) { System.out.println(excMsg); }
}
%>
