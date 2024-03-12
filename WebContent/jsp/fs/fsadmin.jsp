<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.*, hjow.common.json.* "%><%@ include file="common.pront.jsp"%><%
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
    <div class='fs_root fs_div full fs_admin'>
        <script type="text/javascript">
        $(function() {
            var selfId = "<%=fsc.getSessionUserId(request)%>";
            
            var bodys = $('body');
            if(bodys.is('.popup')) bodys.removeClass('dark');
            
            var ctxPath = "<%=fsc.getContextPath()%>";
            var form    = $('.form_fs_admin');
            var chkAc   = form.find('.chk_account');
            chkAc.on('change', function() {
                // Prevent changing (Also not changeable on server)
                if($(this).is(':checked')) $(this).prop('checked', false); 
                else                       $(this).prop('checked', true ); 
            });
            
            FSUtil.applyLanguage(bodys);
            
            var beforeAccountFeatEnabled = false;
            
            FSUtil.ajax({
                url    : ctxPath + "/jsp/fs/fslogin.jsp",
                data   : { req : 'status' },
                method : "POST",
                dataType : "json",
                success : function(acc) {
                    if(! acc.logined) location.href = '/';
                    if(acc.idtype != 'A') location.href = '/';
                    
                    FSUtil.ajax({
                        url    : ctxPath + "/jsp/fs/fsadminin.jsp",
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
                                
                                if(conf['LoginFailCountLimit']) {
                                    form.find("[name='loginfailcnt']").val(conf['LoginFailCountLimit']);
                                }
                                
                                if(conf['TokenLifeTime']) {
                                    form.find("[name='tokenlifetime']").val(conf['TokenLifeTime']);
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
                            
                            form.find("[name='limitsize']").val(conf['LimitDownloadSize']);
                            form.find("[name='limitprev']").val(conf['LimitPreviewSize']);
                            
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
                            if(conf['UseConsole']) {
                                form.find("[name='useconsole']").prop('checked', true);
                            } else {
                                form.find("[name='useconsole']").prop('checked', false);
                            }
                        }, error : function(jqXHR, textStatus, errorThrown) {
                            alert('Error : ' + textStatus + '\n' + errorThrown)
                        }, complete : function() {
                            form.find('.hidden_req').val('update');
                            form.on('submit', function() {
                                FSUtil.ajax({
                                    url    : ctxPath + "/jsp/fs/fsadminin.jsp",
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
                                FSUtil.ajax({
                                    url    : ctxPath + "/jsp/fs/fsadminin.jsp",
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
            
            var tabButtons = $('.flatbuttons');
            
            tabButtons.find('.admintab').each(function() {
                $(this).on('click', function() {
                    tabButtons.find('.admintab').removeClass('thick');
                    $(this).addClass('thick');
                    
                    $('.adminelement').addClass('invisible');
                    $('.adminelement_' + $(this).attr('data-target')).removeClass('invisible');
                });
            });
            
            var formUserSrch = $('.form_fs_user_search');
            var tbodyUsers   = $('.tbody_users');
            
            formUserSrch.on('submit', function() {
                tbodyUsers.find('.binded_click').off('click');
                tbodyUsers.empty();
                tbodyUsers.append("<tr><td colspan='5'>...</td></tr>");
                
                FSUtil.ajax({
                    url    : ctxPath + "/jsp/fs/fsadminin.jsp",
                    data   : formUserSrch.serialize(),
                    method : "POST",
                    dataType : "json",
                    success : function(data) {
                        tbodyUsers.empty();
                        if(! data.success) {
                            tbodyUsers.append("<tr><td colspan='5' class='td_empty'></td></tr>");
                            tbodyUsers.find('.td_empty').text(data.message);
                            return;
                        }
                        
                        for(var idx=0; idx<data.userlist.length; idx++) {
                            var rowOne = data.userlist[idx];
                            tbodyUsers.append("<tr class='tr_user tr_user_" + idx + "'><td class='td_no'></td><td class='td_id'></td><td class='td_nick'></td><td class='td_type'></td><td class='td_etc'></td></tr>");
                            
                            var tr = tbodyUsers.find('.tr_user_' + idx);
                            tr.find('.td_no').text(String(idx));
                            tr.find('.td_id').text(rowOne['id']);
                            tr.find('.td_nick').text(rowOne['nick']);
                            tr.find('.td_type').text(rowOne['idtype']);
                            
                            if(rowOne['id'] != selfId) {
                                tr.find('.td_etc').append("<input type='button' value='X' class='btn_delete btnx' data-id=''/>");
                                
                                var btnDel = tr.find('.btn_delete');
                                btnDel.attr('data-id', rowOne['id']);
                                btnDel.on('click', function() {
                                    var dId = $(this).attr('data-id');
                                    FSUtil.ajax({
                                        url    : ctxPath + "/jsp/fs/fsadminin.jsp",
                                        data   : {
                                            req : 'userdel', 
                                            id  : dId
                                        },
                                        method : "POST",
                                        dataType : "json",
                                        success : function(data) {
                                            if(! data.success) {
                                                alert(data.message);
                                            } else {
                                                alert('Success !');
                                                formUserSrch.trigger('submit');
                                            }
                                        }
                                    });
                                });
                                btnDel.addClass('binded_click');
                            }
                            
                            FSUtil.applyLanguage();
                        }
                    }
                });
            });
            
            var formUserCr = $('.form_fs_user_new');
            formUserCr.on('submit', function() {
                FSUtil.ajax({
                    url    : ctxPath + "/jsp/fs/fsadminin.jsp",
                    data   : formUserCr.serialize(),
                    method : "POST",
                    dataType : "json",
                    success : function(data) {
                        if(! data.success) {
                            alert(data.message);
                        } else {
                            alert('Success !');
                            formUserSrch.trigger('submit');
                        }
                    }
                });
            });
        });
        </script>
        <div class='container show-grid full'>
            <div class='row'>
                <div class='col-sm-12'><h2>FS Administration Center</h2></div>
            </div>
            <div class='row'>
                <div class='col-sm-12 flatbuttons'>
                    <a href='#' class='flatbutton admintab lang_element thick' data-lang-en='Config'   data-target='config'   style='margin-left: -5px; border-top: 0; border-left: 0;'>설정</a>
                    <a href='#' class='flatbutton admintab lang_element'       data-lang-en='Users'    data-target='users'    style='margin-left: -5px; border-top: 0; border-left: 0;'>사용자관리</a>
                    <a href='#' class='flatbutton admintab lang_element'       data-lang-en='Reset'    data-target='reset'    style='margin-left: -5px; border-top: 0; border-left: 0;'>초기화</a>
                </div>
            </div>
        </div>
        <div class='container show-grid adminelement adminelement_config full'>
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
                            <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Download Max (KB)'>다운로드 MAX (KB)</div>
                            <div class='col-sm-10'><input type='number' name='limitsize' class='full lang_attr_element' placeholder="한 번에 다운로드할 수 있는 최대 용량 (KB)" data-lang-target='placeholder' data-lang-en='Maximum size for download at once (KB)' title='Maximum size for download at once (KB)' value='10485760' step='1' min="1"/></div>
                        </div>
                        <div class='row'>
                            <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Preview Max (KB)'>미리보기 MAX (KB)</div>
                            <div class='col-sm-10'><input type='number' name='limitprev' class='full lang_attr_element' placeholder="미리보기 가능한 파일 최대 용량 (KB)" data-lang-target='placeholder' data-lang-en='Maximum size for preview at once (KB)' title='Maximum size for preview at once (KB)' value='1048576' step='1' min="1"/></div>
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
                            <div class='col-sm-10'>
                                <label><input type='checkbox' name='useaccount' class='chk_account' value="true"/><span class='lang_element' data-lang-en='Use Accounts'>계정 기능 사용</span></label>
                                <label class='onlyaccount invisible'><span class='lang_element margin_left_20' data-lang-en='Login Fails Limit'>로그인 실패 횟수 제한     </span><input type='number' name='loginfailcnt'  class='num_loginfailcnt'  value="10" step="1" min="0" max="100"/></label>
                                <label class='onlyaccount invisible'><span class='lang_element margin_left_20' data-lang-en='Token Lifetime (Minutes)'>토큰 유효시간 (분) </span><input type='number' name='tokenlifetime' class='num_tokenlifetime' value="10" step="1" min="0" max="100"/></label>
                            </div>
                        </div>
                        <div class='row'>
                            <div class='col-sm-12'>
                                <div><h4 class='lang_element' data-lang-en='Hidden Folders'>숨김 폴더</h4></div>
                                <div><textarea name='hiddendirs' class='full' style='min-height: 300px;'>[]</textarea></div>
                            </div>
                        </div>
                        <div class='row'>
                            <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='ETC'>기타</div>
                            <div class='col-sm-10'>
                                <span class='etcspan'>
                                    <label><input type='checkbox' name='readfileicon'  class='chk_read_icon'  value="true"/><span class='lang_element' data-lang-en="Read file's icon">파일 아이콘 읽기</span></label>
                                </span>
                                <span class='etcspan'>
                                    <label><input type='checkbox' name='useconsole'   class='chk_use_console'  value="true"/><span class='lang_element' data-lang-en="Use Console">콘솔 사용</span></label>
                                </span>
                            </div>
                        </div>
                        <div class='row'>
                            <div class='col-sm-12 align_center'>
                                <input type='submit' value='적용' class='full lang_attr_element btn_apply btnx' style='height:50px;' data-lang-target='value' data-lang-en='Apply'/>
                            </div>
                        </div>
                    </div>
                </div>
            </form>
        </div>
        <div class='container show-grid adminelement adminelement_users invisible full'>
            <div class='row'>
                <div class='col-sm-12'>
                    <div><h4>Users</h4></div>
                    <form class='form_fs_user_search' onsubmit='return false;'>
                        <input type='hidden' name='req' value='userlist' class='hidden_req'/>
                        <input type='text' name='keyword'/>
                        <input type='submit' value='검색' class='lang_attr_element btnx' data-lang-target='value' data-lang-en='Search'/>
                    </form>
                </div>
            </div>
            <div class='row'>
                <div class='col-sm-12'>
                    <table class='table table-hover full fs_table_list'>
                        <colgroup>
                            <col style='width:30px'/>
                            <col style='width:120px'/>
                            <col/>
                            <col style='width:50px'/>
                            <col style='width:120px'/>
                        </colgroup>
                        <thead>
                            <tr>
                                <th>No</th>
                                <th>ID</th>
                                <th>Nick</th>
                                <th>Type</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody class='tbody_users'>
                            <tr>
                                <td colspan='5' class='lang_element' data-lang-en='Please search first !'>검색 버튼을 클릭해 주세요.</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class='row'>
                <div class='col-sm-12'><hr/></div>
            </div>
            <div class='row'>
                <div class='col-sm-12'>
                    <div><h4>Creating User</h4></div>
                    <form class='form_fs_user_new' onsubmit='return false;'>
                        <input type='hidden' name='req' value='usercreate' class='hidden_req'/>
                        <div class='container show-grid'>
                            <div class='row'>
                                <div class='col-sm-1'>ID</div>
                                <div class='col-sm-5'><input type='text' name='id'/></div>
                                <div class='col-sm-1'>PW</div>
                                <div class='col-sm-5'><input type='password' name='pw'/></div>
                            </div>
                            <div class='row'>
                                <div class='col-sm-1'>Nick</div>
                                <div class='col-sm-5'><input type='text' name='nick'/></div>
                                <div class='col-sm-1'>Type</div>
                                <div class='col-sm-5'>
                                    <select name='idtype'>
                                        <option value='A'>Admin</option>
                                        <option value='U' selected>User</option>
                                        <option value='B'>Blocked</option>
                                    </select>
                                </div>
                            </div>
                            <div class='row'>
                                <div class='col-sm-12 align_right'>
                                    <input type='submit' value='등록' class='lang_attr_element btnx' data-lang-target='value' data-lang-en='Create'/>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <div class='container show-grid adminelement adminelement_reset invisible full'>
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
                                <input type='submit' value='초기화' class='full lang_attr_element btn_apply btnx' style='height:50px;' data-lang-target='value' data-lang-en='Reset'/>
                            </div>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </div>
    <jsp:include page="common.footer.jsp"></jsp:include>
</body>
</html>