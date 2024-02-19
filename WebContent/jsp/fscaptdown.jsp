<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.* "%><%@ include file="common.pront.jsp"%><%
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
request.setCharacterEncoding("UTF-8");
String clients = request.getHeader("User-Agent");

String pathParam = request.getParameter("path");
String fileName  = request.getParameter("filename");
String speed     = request.getParameter("speed");
String theme     = request.getParameter("theme");

if(pathParam == null) pathParam = "";
if(fileName  == null) fileName  = "";
if(speed     == null) speed     = "";
if(theme     == null) theme     = "";

pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
fileName  = FSUtils.removeSpecials(fileName, false, true, true, true, false).replace("?", "").replace("&", "").trim();
speed     = FSUtils.removeSpecials(speed   ).replace("?", "").replace("&", "").trim();
theme     = FSUtils.removeSpecials(theme   ).replace("?", "").replace("&", "").trim();

int randomNo  = (int) Math.round(1000000 + Math.random() * 1000000 + Math.random() * 10000 + Math.random() * 100);
String strRan = String.valueOf(randomNo).substring(0, 7);

request.getSession().setAttribute("fsd_captcha_code", strRan);
request.getSession().setAttribute("fsd_captcha_time", new Long(now));
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title><%= fsc.title %></title>
<jsp:include page="./common.header.jsp"></jsp:include>
<script type='text/javascript'>
$(function() {
    $('.btn_re').on('click', function() {
        location.reload();
    });

    var form  = $('.form');
    var selMn = form.find('.sel_mode');
    var btnDn = form.find('.btn_dn');

    var fileName = form.find('.hid_name').val();
    var ext      = '';

    var spl = fileName.split('.');
    if(spl.length >= 2) {
    	for(var idx=0; idx<spl.length; idx++) {
            ext = spl[idx];
        }
    }
    ext = String(ext).toLowerCase();

    selMn.empty();
    if(ext == 'jpg' || ext == 'jpeg' || ext == 'png' || ext == 'gif' || ext == 'pdf') {
    	selMn.append("<option value='DOWNLOAD' class='lang_element' data-lang-en='DOWNLOAD'>다운로드</option>");
    	selMn.append("<option value='VIEW'     class='lang_element' data-lang-en='VIEW'    >보기</option>");
    } else {
        selMn.append("<option value='DOWNLOAD' class='lang_element' data-lang-en='DOWNLOAD'>다운로드</option>");
    }
    selMn.val('DOWNLOAD');

    $('.p_filename').text(fileName);
    $('.p_filename').attr('title', fileName);
    $('.inp_captcha').focus();
});
</script>
</head>
<body class='popup'>
    <div class='fs_capt container show-grid full'>
        <div class='row'>
            <div class='col-sm-12'>
                <h3 class='lang_element' data-lang-en='Captcha Authentication'>Captcha 인증</h3>
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12'>
                <p class='lang_element' data-lang-en='For download...'></p>
                <p class='p_filename ellipsis'></p>
                <p class='lang_element' data-lang-en=''>파일 다운로드를 위해 인증 코드 입력</p>
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12 align_center'>
                <iframe style='width: <%=fsc.captchaWidth + 10%>px; height: <%=fsc.captchaHeight + 10%>px;' src='fscaptin.jsp?key=fsd&theme=<%=theme%>'></iframe>
            </div>
        </div>
        <form action='fsdown.jsp' method='POST' class='form' target='_blank'>
            <input type='hidden' name='path'     class='hid_path' value='<%=pathParam%>'/>
            <input type='hidden' name='filename' class='hid_name' value='<%=fileName%>'/>
            <input type='hidden' name='speed'    class='hid_sped' value='<%=speed%>'/>
            <div class='row'>
                <div class='col-sm-6 align_center'>
	                <input type='text'   class='inp_captcha lang_attr_element' name='captcha' placeholder='위의 코드 입력' data-lang-target='placeholder' data-lang-en='Input the code above'/>
	                <input type='button' class='btn_re lang_attr_element' value='새로고침' data-lang-target='value' data-lang-en='Refresh'/>
                </div>
            </div>
            <div class='row'>
                <div class='col-sm-12 align_right'>
                    <select name='mode' class='sel_mode'>
                        <option value='DOWNLOAD' class='lang_element' data-lang-en='DOWNLOAD' selected>다운로드</option>
                    </select>
                    <input type='submit' class='btn_dn lang_attr_element' value='실행' data-lang-target='value' data-lang-en='Now !'/>
                </div>
            </div>
        </form>
    </div>
</body>
</html>