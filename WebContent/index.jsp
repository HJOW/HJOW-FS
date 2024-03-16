<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title>File Storage</title>

<!-- This should be in head tag. (includes css, js embedding tags) -->
<jsp:include page="./jsp/fs/common.header.jsp"></jsp:include>

<!-- Not necessary script now. -->
<script type='text/javascript'>
$(function() {
	if(FSUtil.detectSupportES6()) {
		// TODO : move to modern UI page
		// location.href = './html/modern.html';
	}
});
</script>
</head>
<body>
    <!-- Login Bar Location. -->
    <jsp:include page="./jsp/fs/fsaccountbar.jsp"></jsp:include>
    
    <!-- File Lists Location. -->
    <jsp:include page="./jsp/fs/fs.jsp"></jsp:include>
</body>
</html>