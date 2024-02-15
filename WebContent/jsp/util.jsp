<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ include file="common.pront.jsp"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title>Utilities</title>
<jsp:include page="common.header.jsp"></jsp:include>
</head>
<body>
    <div class='fs_root container show-grid full'>
        <script type='text/javascript'>
        $(function() {
        	var ctxPath = "<%=ctxPath%>";
            var formObj = $('.form_util_sha_str');
            var inpRes  = formObj.find('.inp_util_sha_str_res');

            formObj.on('submit', function() {
            	inpRes.val('');
            	$.ajax({
                	url : ctxPath + "/jsp/util/sha_str_in.jsp",
                	data   : formObj.serialize(),
                    method : "POST",
                    dataType : "text",
                    success : function(data) {
                    	inpRes.val(data);
                    }, error : function(jqXHR, textStatus, errorThrown) {
                    	inpRes.val('ERROR : ' + textStatus);
                    }
                });
            });
        });
        </script>
        <form class='form_util_sha_str' onsubmit='return false'>
            <div>
	            <div class='col-sm-12'>
	                <h3>Hash String</h3>
	            </div>
	        </div>
	        <div class='row'>
	            <div class='col-sm-2'>
	                <select name='alg' class='full'>
	                    <option value='1'>SHA-1</option>
	                    <option value='256' selected>SHA-256</option>
	                    <option value='512'>SHA-512</option>
	                </select>
	            </div>
	            <div class='col-sm-8'>
	                <input type='text' name='content' class='full'/>
	            </div>
	            <div class='col-sm-2'>
	                <input type='submit' value='조회'/>
	            </div>
	        </div>
	        <div>
                <div class='col-sm-12'>
                    <input type='text' name='result' class='inp_util_sha_str_res full' readonly/>
                </div>
            </div>
        </form>
    </div>
</body>
</html>