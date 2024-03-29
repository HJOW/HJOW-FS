<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title>File Storage</title>

<!-- This should be in head tag. (includes css, js embedding tags) -->
<jsp:include page="./common.header.jsp"></jsp:include>

</head>
<body>
    <!-- Login Bar Location. -->
    <jsp:include page="./fsaccountbarclassic.jsp"></jsp:include>
    
    <!-- File Lists Location. -->
    <jsp:include page="./fsclassic.jsp"></jsp:include>
</body>
</html>