<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.io.*, java.util.* " session="true" %><%@ include file="common.pront.jsp"%><%
if(! installed) {
	%><div class='fs_accbar container show-grid full invisible'>Not installed</div><%
} else if(! noLogin) {
	String sessionJson = (String) request.getSession().getAttribute("fssession");
%>
<div class='fs_accbar container show-grid full'>
	<script type='text/javascript'>
	$(function() {
		var ctxPath = "<%=ctxPath%>";
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
	     <div class='col-sm-10 container show-grid'>
	         <div class='row'>
	             <div class='col-sm-2' style='width:100px'>ID</div>
	             <div class='col-sm-10'><input type='text' name='id' class='full'/></div>
	         </div>
	         <div class='row'>
	             <div class='col-sm-2' style='width:100px'>Password</div>
	             <div class='col-sm-10'><input type='password' name='pw' class='full'/></div>
	         </div>
	     </div>
	     <div class='col-sm-2'>
	         <div class='col-sm-12'><input type='submit' value='LOGIN' class='full' style='height:50px;'/></div>
	     </div>
	</div>
	<div class='row login_element logined'>
	    <div class='col-sm-12'>
	        <span class='span_type'></span> <span class='span_nick'></span> 님 환영합니다. 
	        <input type='button' value='LOGOUT' class='btn_logout'/>
	    </div>
	</div>
	</form>
</div>
<% } %>