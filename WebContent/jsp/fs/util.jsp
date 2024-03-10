<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ include file="common.pront.jsp"%><%--
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
 --%>
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
            var ctxPath = "<%=fsc.getContextPath()%>";
            var formObj = $('.form_util_sha_str');
            var inpRes  = formObj.find('.inp_util_sha_str_res');

            formObj.on('submit', function() {
                inpRes.val('');
                FSUtil.ajax({
                    url : ctxPath + "/jsp/fs/util/sha_str_in.jsp",
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
                    <input type='submit' value='조회' class='btnx'/>
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