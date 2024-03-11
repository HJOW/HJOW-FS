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
if(! fsc.isInstalled()) {
    %><div class='fs_accbar container show-grid full invisible'>Not installed</div><%
} else if(! fsc.isNoLoginMode()) {
    String sessionJson = (String) fsc.getSessionObject(request, "fssession");
%>
<div class='fs_accbar fs_div full'>
    <div class='container valign_middle full'>
        <script type='text/javascript'>
        $(function() {
            var ctxPath   = "<%=fsc.getContextPath()%>";
            var fsRoot    = $('.fs_root');
            var acRoot    = $('.fs_accbar');
            var formObj   = acRoot.find('.form_fs_login');
            var inpReq    = formObj.find('.inp_req');
            var btnLogout = formObj.find('.btn_logout');
            
            var ser = formObj.serialize();
        
            acRoot.find('.login_element').addClass('invisible');
            acRoot.find('.login_element.not_logined').removeClass('invisible');
            fsRoot.find('.login_element').addClass('invisible');
            fsRoot.find('.login_element.not_logined').removeClass('invisible');
        
            function fLogin() {
                inpReq.val('login');
                FSUtil.ajax({
                    url    : ctxPath + "/jsp/fs/fslogin.jsp",
                    data   : formObj.serialize(),
                    method : "POST",
                    dataType : "json",
                    success : function(data) {
                    	if(data.token) {
                    		if(FSUtil.detectStorage()) {
                    			FSUtil.storage.session.put("fsid"   , data.id   );
                    			FSUtil.storage.session.put("fstoken", data.token);
                    		}
                    	}
                        fRef(data, true);
                    }
                });
            }
        
            function fRef(data, alerts) {
                if(alerts && (! data.success)) alert(data.message);
                if(data.needrefresh) { location.reload(); return; }
                acRoot.find('.login_element').addClass('invisible');
                fsRoot.find('.login_element').addClass('invisible');
                if(data.logined) {
                    $('.login_element.logined').removeClass('invisible');
                    formObj.find('.span_type').text('[' + data.idtype + ']');
                    formObj.find('.span_nick').text(data.nick);
                } else {
                    acRoot.find('.login_element.not_logined').removeClass('invisible');
                    fsRoot.find('.login_element.not_logined').removeClass('invisible');
                }
                inpReq.val('status');
                
                var captLogin = acRoot.find('.if_captcha_l');
                if(captLogin != null && typeof(captLogin) != 'undefined' && captLogin.length >= 1) {
                    captLogin.attr('src', captLogin.attr('src') + '&not=' + Math.round(Math.random() * 100));
                }
                
                formObj.find('.inp_login_element').val('');
                
                var fsFileList = fsRoot.find('.fs_filelist');
                if(fsFileList != null && typeof(fsFileList) != 'undefined' && fsFileList.length >= 1) {
                    if(data.noanonymous) {
                        if(data.logined) {
                            fsFileList.addClass('invisible');
                            fsRoot.find('.fs_filelist_view').removeClass('invisible');
                        } else {
                            fsFileList.addClass('invisible');
                            fsRoot.find('.fs_filelist_anonymous').removeClass('invisible');
                        }
                    } else {
                        fsFileList.addClass('invisible');
                        fsRoot.find('.fs_filelist_view').removeClass('invisible');
                    }
                }
        
                var formFList = fsRoot.find('.form_fs');
                if(formFList.length >= 1) {
                    if(data.noanonymous && (! data.logined)) return;
                    formFList.trigger('submit');
                }
            }
        
            formObj.on('submit', fLogin);
        
            inpReq.val('status');
            FSUtil.ajax({
                url    : ctxPath + "/jsp/fs/fslogin.jsp",
                data   : formObj.serialize(),
                method : "POST",
                dataType : "json",
                success : function(data) {
                    fRef(data, false);
                }
            });
        
            btnLogout.on('click', function() {
                inpReq.val('logout');
                
                if(FSUtil.detectStorage()) {
                    FSUtil.storage.session.remove("fsid"   );
                    FSUtil.storage.session.remove("fstoken");
                }
                
                FSUtil.ajax({
                    url    : ctxPath + "/jsp/fs/fslogin.jsp",
                    data   : formObj.serialize(),
                    method : "POST",
                    dataType : "json",
                    success : function(data) {
                        fRef(data, true);
                    }
                });
            });
            
            <% if(! fsc.isCaptchaLoginOn()) { %>
            acRoot.find('.div_captcha_login').addClass('invisible');
            <% } %>
        });
        </script>
        <form onsubmit='return false' class='form_fs_login'>
        <input type='hidden' name='req' value='status' class='inp_req'/>
        <div class='row login_element not_logined padding_top_10'>
            <div class='container show-grid d_inline_block valign_middle' style='width:270px; height: 60px; '>
                <div class='row'>
                    <div class='col-xs-12'>
                        <span style='display: inline-block; width:80px'>ID</span><input type='text' name='id' class='inp_login_element' style='width: 150px;'/>
                    </div>
                </div>
                <div class='row'>
                    <div class='col-xs-12'>
                        <span style='display: inline-block; width:80px' class='lang_element' data-lang-en='Password'>암호</span><input type='password' name='pw' class='inp_login_element' style='width: 150px;'/>
                    </div>
                </div>
            </div>
            <div class='div_captcha_login d_inline_block valign_middle' style='width: <%=(fsc.getCaptchaWidth() + 10)%>px;  height: 60px;'>
                <iframe style='width: <%=(fsc.getCaptchaWidth() + 5)%>px; height: <%=(fsc.getCaptchaHeight() + 5)%>px; border:0;' class='if_captcha_l valign_middle' src='<%=fsc.getContextPath()%>/jsp/fs/fscaptin.jsp?key=fsl&scale=1&theme='></iframe>
            </div>
            <div class='div_captcha_login d_inline_block valign_middle padding_top_10' style='margin-left: 10px; width: <%=((fsc.getCaptchaWidth() / 2) + 10)%>px;  height: 60px; text-align: left;'>
                <input  style='width: <%=((fsc.getCaptchaWidth() / 2) + 5)%>px; height: <%=((fsc.getCaptchaHeight() / 2) + 5)%>px;' type='text' class='inp_captcha_l inp_login_element lang_attr_element valign_middle' name='captcha' placeholder='옆의 코드 입력' data-lang-target='placeholder' data-lang-en='Input the code left'/>
            </div>
            <div class='d_inline_block valign_middle' style='width:100px;  height: 60px;'>
                <input type='submit' value='로그인' class='lang_attr_element btnx' data-lang-target='value' data-lang-en='LOGIN' style='height: 50px;'/>
            </div>
        </div>
        <div class='row login_element logined padding_top_10'>
            <div class='col-sm-12'>
                <span class='lang_element' data-lang-en='Welcome, '></span><span class='span_type'></span> <span class='span_nick'></span><span class='lang_element' data-lang-en=''> 님 환영합니다.</span> 
                <input type='button' value='로그아웃' class='btn_logout btnx lang_attr_element' data-lang-target='value' data-lang-en='LOGOUT'/>
            </div>
        </div>
        </form>
    </div>
</div>
<% } %>