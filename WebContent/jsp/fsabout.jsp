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
                <textarea class='ta_about full' readonly>
* FS License
                
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

* FS Sources

https://github.com/HJOW/HJOW-FS

* FS Dependencies

jQuery (https://jquery.com/)
jQuery UI (https://jquery.com/)
bootstrap (https://getbootstrap.com/)
Apache Common Codec (https://commons.apache.org/proper/commons-codec/)
json-simple (https://github.com/fangyidong/json-simple)
                </textarea>
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