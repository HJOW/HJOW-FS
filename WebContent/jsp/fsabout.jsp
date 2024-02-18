<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*"%><%@ include file="common.pront.jsp"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title><%= fsc.title %></title>
<jsp:include page="./common.header.jsp"></jsp:include>
<script type="text/javascript">
$(function() {
    var ctxPath = "<%=fsc.ctxPath%>";
    var taAbout = $('.ta_about');

    $('.btn_close').on('click', function() {
    	window.close();
    });
    
    taAbout.height(window.outerHeight - 200);
    $(window).on('resize', function() {
    	taAbout.height(window.outerHeight - 200);
    });
});
</script>
</head>
<body class='popup'>
    <div class='container show-grid full'>
        <div class='row'>
            <div class='col-sm-12'>
                <h4>About FS</h4>
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12'>
                <jsp:include page="fslicense.jsp"></jsp:include>
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12 align_right'>
                <input type='button' value='닫기' class='btn_close lang_attr_element' data-lang-target='value' data-lang-en='Close'/>
            </div>
        </div>
    </div>
</body>
</html>