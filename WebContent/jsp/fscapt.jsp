<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.io.*, java.util.* "%><%@ include file="common.pront.jsp"%><%
request.setCharacterEncoding("UTF-8");
String clients = request.getHeader("User-Agent");

String pathParam = request.getParameter("path");
String fileName  = request.getParameter("filename");
String speed     = request.getParameter("speed");

if(pathParam == null) pathParam = "";
if(fileName  == null) fileName  = "";
if(speed     == null) speed     = "";

pathParam = pathParam.replace("'", "").replace("\"", "").replace(".", "").trim();
fileName  = fileName.replace("'", "").replace("\"", "").trim();
speed     = speed.replace("'", "").replace("\"", "").trim();

int randomNo  = (int) Math.round(1000000 + Math.random() * 1000000 + Math.random() * 10000 + Math.random() * 100);
String strRan = String.valueOf(randomNo).substring(0, 7);

request.getSession().setAttribute("captcha_code", strRan);
request.getSession().setAttribute("captcha_time", new Long(now));
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title><%= title %></title>
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
    	selMn.append("<option value='DOWNLOAD'>DOWNLOAD</option>");
    	selMn.append("<option value='VIEW'>VIEW</option>");
    } else {
        selMn.append("<option value='DOWNLOAD'>DOWNLOAD</option>");
    }
    selMn.val('DOWNLOAD');

    $('.p_filename').text(fileName);
    $('.inp_captcha').focus();

    console.log(ext);
});
</script>
</head>
<body class='popup'>
    <div class='fs_capt container show-grid full'>
        <div class='row'>
            <div class='col-sm-12'>
                <h3>Captcha Authentication</h3>
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12'>
                <p>For download</p>
                <p class='p_filename'></p>
            </div>
        </div>
        <div class='row'>
            <div class='col-sm-12 align_center'>
                <iframe style='width: <%=captchaWidth + 10%>px; height: <%=captchaHeight + 10%>px;' src='fscaptin.jsp'></iframe>
            </div>
        </div>
        <form action='fsdown.jsp' method='POST' class='form' target='_blank'>
            <input type='hidden' name='path'     class='hid_path' value='<%=pathParam%>'/>
            <input type='hidden' name='filename' class='hid_name' value='<%=fileName%>'/>
            <input type='hidden' name='speed'    class='hid_sped' value='<%=speed%>'/>
            <div class='row'>
                <div class='col-sm-6 align_center'>
	                <input type='text'   class='inp_captcha' name='captcha' placeholder='Input the code'/>
	                <input type='button' class='btn_re' value='Refresh'/>
                </div>
            </div>
            <div class='row'>
                <div class='col-sm-12 align_right'>
                    <select name='mode' class='sel_mode'>
                        <option value='DOWNLOAD' selected>DOWNLOAD</option>
                    </select>
                    <input type='submit' class='btn_dn' value='Now !'/>
                </div>
            </div>
        </form>
    </div>
</body>
</html>