<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ include file="common.pront.jsp"%><%
String pathParam = request.getParameter("path");
if(pathParam == null) pathParam = "";
pathParam = pathParam.trim();
if(pathParam.equals("/")) pathParam = "";
pathParam = pathParam.replace(".", "").replace("'", "").replace("\"", "").replace("\\", "/").trim(); // 상대경로 방지를 위해 . 기호는 반드시 제거 !
if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title>File Storage</title>
<jsp:include page="./common.header.jsp"></jsp:include>
<script type="text/javascript">
$(function() {
	var ctxPath = "<%=ctxPath%>";
	var path    = "<%=pathParam%>";

	$('.hidden_path').val(path);
	$('.div_path').text('DIR : /' + path);
});
</script>
</head>
<body>
    <form action="<%=ctxPath%>/jsp/fsuploadin.jsp" method="post" enctype="multipart/form-data">
    <input type='hidden' name='path' class='hidden_path'/>
    <div class='container show-grid full'>
        <div class='row'>
            <div class='col-sm-12'>
                <h4>Upload</h4>
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12 div_path'>
                
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12 dummy'>
            
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12'>
                <input type='file' name='file01'/>
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12 dummy'>
            
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12'>
                <input type='submit' value='UPLOAD'/>
            </div>
        </div>
    </div>
    </form>
</body>
</html>