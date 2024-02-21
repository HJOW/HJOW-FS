<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.*, org.json.simple.*, org.json.simple.parser.* "%><%@ include file="common.pront.jsp"%><%
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
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title>FS Installation</title>
<jsp:include page="./common.header.jsp"></jsp:include>
</head>
<body>
    <div class='fs_root container show-grid full fs_admin'>
        <script type="text/javascript">
        $(function() {
        	var bodys = $('body');
            if(bodys.is('.popup')) bodys.removeClass('dark');
            
        	var ctxPath = "<%=fsc.ctxPath%>";
            var form    = $('.form_fs_admin');
            var chkAc   = form.find('.chk_account');
            chkAc.on('change', function() {
                if($(this).is(':checked')) form.find('.onlyaccount').removeClass('invisible');
                else                       form.find('.onlyaccount').addClass('invisible');
            });
            
            FSUtil.applyLanguage(bodys);
            
            var beforeAccountFeatEnabled = false;
            
            $.ajax({
                url    : ctxPath + "/jsp/fslogin.jsp",
                data   : { req : 'status' },
                method : "POST",
                dataType : "json",
                success : function(acc) {
                	if(! acc.logined) location.href = '/';
                	if(acc.idtype != 'A') location.href = '/';
                	
                	$.ajax({
                		url    : ctxPath + "/jsp/fsadminin.jsp",
                        data   : { req : 'read' },
                        method : "POST",
                        dataType : "json",
                        success : function(data) {
                        	if(! data.success) {
                        		alert(data.message);
                        		location.href = '/';
                        		return;
                        	}
                        	
                        	var conf = data.config;
                        	form.find("[name='title']").val(conf['Title']);
                        	
                        	if(conf['UseAccount']) {
                        		beforeAccountFeatEnabled = true;
                        		form.find("[name='useaccount']").prop('checked', true);
                        		form.find('.onlyaccount').removeClass('invisible');
                        		
                        		if(conf['UseCaptchaLogin']) {
                                    form.find("[name='usecaptchalogin']").prop('checked', true);
                                } else {
                                	form.find("[name='usecaptchalogin']").prop('checked', false);
                                }
                        	} else {
                        		beforeAccountFeatEnabled = false;
                        		form.find("[name='useaccount']").prop('checked', false);
                        		form.find("[name='useaccount']").prop('disabled', true);
                        		form.find('.onlyaccount').addClass('invisible');
                        		form.find("[name='usecaptchalogin']").prop('checked', false);
                        	}
                        	form.find("[name='rootdir']").val('...');
                        	form.find("[name='rootdir']").prop('disabled', true);
                        	
                        	var hddirs = conf['HiddenDirs'];
                        	if(typeof(hddirs) != 'string') hddirs = JSON.stringify(hddirs);
                        	if(typeof(conf['sHiddenDirs']) != 'undefined') hddirs = conf['sHiddenDirs'];
                        	form.find("[name='hiddendirs']").val(hddirs);
                        	
                        	form.find("[name='limitsize']").val(conf['LimitUploadSize']);
                        	
                        	if(conf['UseCaptchaDown']) {
                        		form.find("[name='usecaptchadown']").prop('checked', true);
                        	} else {
                        		form.find("[name='usecaptchadown']").prop('checked', false);
                        	}
                        	
                        	if(conf['ReadFileIcon']) {
                                form.find("[name='readfileicon']").prop('checked', true);
                            } else {
                                form.find("[name='readfileicon']").prop('checked', false);
                            }
                        }, error : function(jqXHR, textStatus, errorThrown) {
                        	alert('Error : ' + textStatus + '\n' + errorThrown)
                        }, complete : function() {
                        	form.find('.hidden_req').val('update');
                        	form.on('submit', function() {
                                $.ajax({
                                    url    : ctxPath + "/jsp/fsadminin.jsp",
                                    data   : form.serialize(),
                                    method : "POST",
                                    dataType : "json",
                                    success : function(data) {
                                        alert(data.message);
                                    }
                                });
                            });
                        	
                        	var formReset = $('.form_fs_reset');
                        	formReset.on('submit', function() {
                                $.ajax({
                                    url    : ctxPath + "/jsp/fsadminin.jsp",
                                    data   : formReset.serialize(),
                                    method : "POST",
                                    dataType : "json",
                                    success : function(data) {
                                        alert(data.message);
                                        if(data.reset) window.close();
                                    }
                                });
                            });
                        }
                	});
                }
            });
        });
        </script>
        <div class='row'>
            <div class='col-sm-12'><h2>FS Administration Center</h2></div>
        </div>
        <form class='form_fs_admin' onsubmit='return false;'>
            <input type='hidden' name='req' value='status' class='hidden_req'/>
            <div class='row'>
                <div class='col-sm-12'><h3 class='lang_element' data-lang-en='Configuration'>설정</h3></div>
            </div>
            <div class='row'>
		        <div class='col-sm-12 container show-grid'>
		            <div class='row'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Title'>타이틀</div>
                        <div class='col-sm-10'><input type='text' name='title' class='full' placeholder="Title" value="File Storage"/></div>
                    </div>
		            <div class='row'>
		                <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Root Directory'>최상위 경로</div>
		                <div class='col-sm-10'><input type='text' name='rootdir' class='full lang_attr_element' placeholder="공유할 파일이 있는 최상위 경로를 입력" data-lang-target='placeholder' data-lang-en='Root Directory for sharing'/></div>
		            </div>
		            <div class='row'>
		                <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Limit Size (KB)'>용량 제한 (KB)</div>
                        <div class='col-sm-10'><input type='number' name='limitsize' class='full lang_attr_element' placeholder="한 번에 업로드할 수 있는 최대 용량 (KB)" data-lang-target='placeholder' data-lang-en='Maximum size for upload at once (KB)' title='Maximum size for upload at once (KB)' value='10485760'/></div>
		            </div>
		            <div class='row'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Output Count'>출력 최대 갯수</div>
                        <div class='col-sm-10'><input type='number' name='limitcount' class='full lang_attr_element' placeholder="파일 목록 한번 불러올 때 가져올 최대 갯수" data-lang-target='placeholder' data-lang-en='Maximum count of files on single networking' title='Maximum count of files on single networking' value='1000'/></div>
                    </div>
		            <div class='row'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Captcha'>캡차</div>
                        <div class='col-sm-10'>
                            <span>
                                <label><input type='checkbox' name='usecaptchadown'  class='chk_captcha_down'  value="true"/><span class='lang_element' data-lang-en='Ask on download'>다운로드 시 요구</span></label>
                            </span>
                            <span class='onlyaccount invisible'>
                                <label><input type='checkbox' name='usecaptchalogin' class='chk_captcha_login' value="true"/><span class='lang_element' data-lang-en='Ask on login'   >로그인 시 요구</span></label>
                            </span>
                        </div>
                    </div>
		            <div class='row'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Accounts'>계정</div>
                        <div class='col-sm-10'><label><input type='checkbox' name='useaccount' class='chk_account' value="true"/><span class='lang_element' data-lang-en='Use Accounts'>계정 기능 사용</span></label></div>
                    </div>
                    <div class='row'>
                        <div class='col-sm-12'>
                            <div><h4 class='lang_element' data-lang-en='Hidden Folders'>숨김 폴더</h4></div>
                            <div><textarea name='hiddendirs' class='full'>[]</textarea></div>
                        </div>
                    </div>
                    <div class='row'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='ETC'>기타</div>
                        <div class='col-sm-10'>
                            <span>
                                <label><input type='checkbox' name='readfileicon'  class='chk_read_icon'  value="true"/><span class='lang_element' data-lang-en="Read file's icon">파일 아이콘 읽기</span></label>
                            </span>
                        </div>
                    </div>
                    <div class='row'>
		                <div class='col-sm-12 align_center'>
		                    <input type='submit' value='적용' class='full lang_attr_element btn_apply' style='height:50px;' data-lang-target='value' data-lang-en='Apply'/>
		                </div>
		            </div>
		        </div>
		    </div>
        </form>
        <form class='form_fs_reset' onsubmit='return false;'>
            <input type='hidden' name='req' value='reset' class='hidden_req'/>
            <div class='row'>
                <div class='col-sm-12'><h3 class='lang_element' data-lang-en='Reset'>초기화</h3></div>
            </div>
            <div class='row'>
                <div class='col-sm-12 container show-grid'>
                    <div class='row'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Password'>암호</div>
                        <div class='col-sm-10'><input type='password' name='pw' class='full lang_attr_element' placeholder="fs.properties 에 있는 암호" data-lang-target='placeholder' data-lang-en='Password in fs.properties'/></div>
                    </div>
                    <div class='row'>
                        <div class='col-sm-12 align_center'>
                            <input type='submit' value='초기화' class='full lang_attr_element btn_apply' style='height:50px;' data-lang-target='value' data-lang-en='Reset'/>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </div>
    <jsp:include page="common.footer.jsp"></jsp:include>
</body>
</html>