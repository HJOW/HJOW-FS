<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*"%><%@ include file="common.pront.jsp"%><%
String pathParam = request.getParameter("path");
if(pathParam == null) pathParam = "";
pathParam = pathParam.trim();
if(pathParam.equals("/")) pathParam = "";
pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title><%= fsc.title %></title>
<jsp:include page="./common.header.jsp"></jsp:include>
<script type="text/javascript">
$(function() {
	var ctxPath = "<%=fsc.ctxPath%>";
	var path    = "<%=pathParam%>";

	$('.hidden_path').val(path);
	$('.div_path').text('DIR : /' + path);
});
</script>
</head>
<body class='popup'>
    <form action="<%=fsc.ctxPath%>/jsp/fsuploadin.jsp" method="post" enctype="multipart/form-data">
    <input type='hidden' name='path' class='hidden_path'/>
    <div class='container show-grid full'>
        <div class='row'>
            <div class='col-sm-12'>
                <h4 class='lang_element' data-lang-en='Upload'>업로드</h4>
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12 div_path'>
                
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12 dummy margin_bottom_10'>
            
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12'>
                <input type='file' name='file01'/>
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12 dummy margin_bottom_10'>
            
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12 align_right'>
                <input type='submit' value='업로드' class='lang_attr_element' data-lang-target='value' data-lang-en='UPLOAD'/>
            </div>
        </div>
    </div>
    </form>
</body>
</html>