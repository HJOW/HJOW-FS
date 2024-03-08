<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, hjow.common.util.*"%><%@ include file="common.pront.jsp"%><%
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
String pathParam = request.getParameter("path");
if(pathParam == null) pathParam = "";
pathParam = pathParam.trim();
if(pathParam.equals("/")) pathParam = "";
pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);

String sPopin = request.getParameter("popin");
if(sPopin == null) sPopin = "false";
boolean popin = DataUtil.parseBoolean(sPopin);

%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title><%= fsc.getTitle() %></title>
<jsp:include page="./common.header.jsp"></jsp:include>
<script type="text/javascript">
$(function() {
	var ctxPath = "<%=fsc.getContextPath()%>";
	var path    = "<%=pathParam%>";

	$('.hidden_path').val(path);
	$('.div_path').text('DIR : /' + path);
});
</script>
</head>
<body class='popup'>
    <form action="<%=fsc.getContextPath()%>/jsp/fs/fsuploadin.jsp" method="post" enctype="multipart/form-data">
    <input type='hidden' name='path'  class='hidden_path' />
    <input type='hidden' name='popin' class='hidden_popin' value='<%=popin%>'/>
    <div class='fs_div fs_uploadpop container show-grid full'>
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
                <input type='submit' value='업로드' class='lang_attr_element btnx' data-lang-target='value' data-lang-en='UPLOAD'/>
            </div>
        </div>
    </div>
    </form>
</body>
</html>