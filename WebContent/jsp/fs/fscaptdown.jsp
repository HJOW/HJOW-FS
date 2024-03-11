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
String ctxPathCapt = request.getContextPath();

String pathParam = request.getParameter("path");
String fileName  = request.getParameter("filename");
String theme     = request.getParameter("theme");

if(pathParam == null) pathParam = "";
if(fileName  == null) fileName  = "";
if(theme     == null) theme     = "";

pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
fileName  = FSUtils.removeSpecials(fileName, false, true, true, true, false).replace("..", "").replace("?", "").replace("&", "").trim();
theme     = FSUtils.removeSpecials(theme).replace("?", "").replace("&", "").trim();

int randomNo  = (int) Math.round(1000000 + Math.random() * 1000000 + Math.random() * 10000 + Math.random() * 100);
String strRan = String.valueOf(randomNo).substring(0, 7);

fsc.setSessionObject(request, "fsd_captcha_code", strRan);
fsc.setSessionObject(request, "fsd_captcha_time", new Long(now));

String tokenID  = request.getHeader("fsid");
String tokenVal = request.getHeader("fstoken");

if(tokenID == null || tokenVal == null) {
    if(request.getParameter("fstoken_id") != null && request.getParameter("fstoken_val") != null) {
        tokenID  = request.getParameter("fstoken_id");
        tokenVal = request.getParameter("fstoken_val");
    }
}

if(tokenID != null && tokenVal != null) {
	tokenID  = FSUtils.removeSpecials(tokenID).replace("?", "").replace("&", "").trim();
	tokenVal = FSUtils.removeSpecials(tokenVal).replace("?", "").replace("&", "").trim();
}

%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title><%= fsc.getTitle() %></title>
<jsp:include page="./common.header.jsp"></jsp:include>
<script type='text/javascript'>
$(function() {
    $('.btn_re').on('click', function() {
        location.reload();
    });

    var form  = $('.form');
    var selMn = form.find('.sel_mode');
    var btnDn = form.find('.btn_dn');
    var ifrIn = $('.iframe_captchain');
    var theme = '<%=theme%>';
    var ctxPath = '<%=ctxPathCapt%>';

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
    
    selMn.on('change', function() {
        var currVal = $(this).val();
        var target = '';
        if(currVal == 'VIEW') {
            target = '_target';
        } else {
            target = '_self';
        }
        form.attr('target', target);
    });
    selMn.addClass('binded_change');

    $('.p_filename').text(fileName);
    $('.p_filename').attr('title', fileName);
    
    ifrIn.attr('src', ctxPath + '/jsp/fs/fscaptin.jsp?key=fsd&theme=' + theme + FSUtil.addTokenParameterString());
    
    <% if(tokenID != null && tokenVal != null) { %>
    form.append("<input type='hidden' name='fstoken_id'  value='<%=tokenID %>'/>");
    form.append("<input type='hidden' name='fstoken_val' value='<%=tokenVal%>'/>");
    <% } %>
    
    <% if(fsc.isCaptchaDownloadOn()) { %>
    $('.inp_captcha_d').focus();
    <% } else { %>
    $('.div_captcha_download').addClass('invisible');
    <% } %>
});
</script>
</head>
<body class='popup noscroll'>
    <div class='fs_capt fs_div container show-grid full noscroll'>
        <div class='row'>
            <div class='col-sm-12'>
                <h3 class='lang_element' data-lang-en='Download'>다운로드</h3>
            </div>
        </div>
        <div class='row div_captcha_download'>
            <div class='col-sm-12'>
                <p class='lang_element' data-lang-en='For download...'></p>
                <p class='p_filename ellipsis'></p>
                <p class='lang_element' data-lang-en=''>파일 다운로드를 위해 인증 코드 입력</p>
            </div>
        </div>
        <div class='row div_captcha_download'>
            <div class='col-sm-12 align_center'>
                <iframe class='iframe_captchain' style='width: <%=fsc.getCaptchaWidth() + 10%>px; height: <%=fsc.getCaptchaHeight() + 10%>px;' ></iframe>
            </div>
        </div>
        <form action='fsdown.jsp' method='POST' class='form' target='_self'>
            <input type='hidden' name='path'     class='hid_path' value='<%=pathParam%>'/>
            <input type='hidden' name='filename' class='hid_name' value='<%=fileName%>'/>
            <div class='row div_captcha_download'>
                <div class='col-sm-6 align_center'>
                    <input type='text'   class='inp_captcha_d lang_attr_element' name='captcha' placeholder='위의 코드 입력' data-lang-target='placeholder' data-lang-en='Input the code above'/>
                    <input type='button' class='btn_re btnx lang_attr_element' value='새로고침' data-lang-target='value' data-lang-en='Refresh' accesskey='r'/>
                </div>
            </div>
            <div class='row padding_top_10 padding_bottom_10'>
                <div class='col-sm-12 align_right'>
                    <select name='mode' class='sel_mode'>
                        <option value='DOWNLOAD' class='lang_element' data-lang-en='DOWNLOAD' selected>다운로드</option>
                    </select>
                    <input type='submit' class='btn_dn btnx lang_attr_element' value='실행' data-lang-target='value' data-lang-en='Now !' accesskey='d'/>
                </div>
            </div>
        </form>
        <div class='row margin_bottom_20'>
            <div class='col-sm-12'>
            </div>
        </div>
    </div>
</body>
</html>