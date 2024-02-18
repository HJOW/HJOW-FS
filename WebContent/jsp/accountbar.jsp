<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.* " session="true" %><%@ include file="common.pront.jsp"%><%
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
if(! fsc.installed) {
	%><div class='fs_accbar container show-grid full invisible'>Not installed</div><%
} else if(! fsc.noLogin) {
	String sessionJson = (String) request.getSession().getAttribute("fssession");
%>
<div class='fs_accbar container show-grid full'>
	<script type='text/javascript'>
	$(function() {
		var ctxPath = "<%=fsc.ctxPath%>";
	    var formObj = $('.form_fs_login');
	    var inpReq  = formObj.find('.inp_req');
	    var btnLogout = formObj.find('.btn_logout');
	    
	    var ser = formObj.serialize();

	    $('.login_element').addClass('invisible');
	    $('.login_element.not_logined').removeClass('invisible');

	    function fLogin() {
	    	inpReq.val('login');
	    	$.ajax({
	            url    : ctxPath + "/jsp/login.jsp",
	            data   : formObj.serialize(),
	            method : "POST",
	            dataType : "json",
	            success : function(data) {
	            	fRef(data, true);
	            }
	        });
		}

		function fRef(data, alerts) {
			if(alerts && (! data.success)) alert(data.message);
			$('.login_element').addClass('invisible');
			if(data.logined) {
				$('.login_element.logined').removeClass('invisible');
				formObj.find('.span_type').text('[' + data.idtype + ']');
				formObj.find('.span_nick').text(data.nick);
            } else {
                $('.login_element.not_logined').removeClass('invisible');
            }
			inpReq.val('status');

			var formFList = $('.form_fs');
			if(formFList.length >= 1) formFList.trigger('submit');
	    }

		formObj.on('submit', fLogin);

		inpReq.val('status');
	    $.ajax({
	    	url    : ctxPath + "/jsp/login.jsp",
            data   : formObj.serialize(),
            method : "POST",
            dataType : "json",
            success : function(data) {
            	fRef(data, false);
            }
		});

	    btnLogout.on('click', function() {
	    	inpReq.val('logout');
	        $.ajax({
	            url    : ctxPath + "/jsp/login.jsp",
	            data   : formObj.serialize(),
	            method : "POST",
	            dataType : "json",
	            success : function(data) {
	                fRef(data, true);
	            }
	        });
		});
	});
	</script>
	<form onsubmit='return false' class='form_fs_login'>
	<input type='hidden' name='req' value='status' class='inp_req'/>
	<div class='row login_element not_logined'>
	     <div class='col-sm-10 container show-grid padding_top_10'>
	         <div class='row'>
	             <div class='col-sm-2' style='width:100px'>ID</div>
	             <div class='col-sm-10'><input type='text' name='id' class='full'/></div>
	         </div>
	         <div class='row'>
	             <div class='col-sm-2 lang_element' style='width:100px' data-lang-en='Password'>암호</div>
	             <div class='col-sm-10'><input type='password' name='pw' class='full'/></div>
	         </div>
	     </div>
	     <div class='col-sm-2 padding_top_10'>
	         <div class='col-sm-12'><input type='submit' value='로그인' class='full lang_attr_element' style='height:50px;' data-lang-target='value' data-lang-en='LOGIN'/></div>
	     </div>
	</div>
	<div class='row login_element logined'>
	    <div class='col-sm-12'>
	        <span class='lang_element' data-lang-en='Welcome, '></span><span class='span_type'></span> <span class='span_nick'></span><span class='lang_element' data-lang-en=''> 님 환영합니다.</span> 
	        <input type='button' value='로그아웃' class='btn_logout lang_attr_element' data-lang-target='value' data-lang-en='LOGOUT'/>
	    </div>
	</div>
	</form>
</div>
<% } %>