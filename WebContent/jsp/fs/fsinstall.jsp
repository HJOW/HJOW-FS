<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.*, hjow.common.json.* "%><%
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
FSControl fsc = FSControl.getInstance();
long now = System.currentTimeMillis();
FSControl.init(request.getContextPath());
fsc = FSControl.getInstance();

String theme      = request.getParameter("theme");
if(theme == null) theme = "";
theme = FSUtils.removeSpecials(theme);
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title>FS Installation</title>
<jsp:include page="./common.header.libs.jsp"></jsp:include>
</head><%
if(! fsc.isInstalled()) {
%>
<body>
    <div class='fs_root fs_div container show-grid full fs_install'>
        <script type="text/javascript">
        $(function() {
            var bodys = $('body');
            if(bodys.is('.popup')) bodys.removeClass('dark');
            
            var ctxPath = "<%=fsc.getContextPath()%>";
            var form    = $('.form_fs_ins');
            var chkAc   = form.find('.chk_account');
            
            var theme = "<%=theme%>";
            if(theme != '') {
                bodys.addClass(theme);
            } else if(! bodys.is('.popup')) {
                if(FSUtil.detectDark()) {
                    bodys.addClass('dark');
                }
            }

            chkAc.on('change', function() {
                if($(this).is(':checked')) form.find('.onlyaccount').removeClass('invisible');
                else                       form.find('.onlyaccount').addClass('invisible');
            });
            
            FSUtil.applyLanguage(bodys);

            form.on('submit', function() {
                FSUtil.ajax({
                    url    : ctxPath + "/jsp/fs/fsinstallin.jsp",
                    data   : form.serialize(),
                    method : "POST",
                    dataType : "json",
                    success : function(data) {
                        alert(data.message);
                        if(data.success) {
                            location.href = ctxPath + "/index.jsp";
                        }
                    }
                });
            });
        });
        </script>
        <form class='form_fs_ins' onsubmit='return false;'>
            <div class='row'>
                <div class='col-sm-12'><h2>FS Installation</h2></div>
            </div>
            <div class='row'>
                <div class='col-sm-12'>
                    <jsp:include page="fslicense.jsp"></jsp:include>
                </div>
            </div>
            <div class='row'>
                <div class='col-sm-10 container show-grid'>
                    <div class='row'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Title'>타이틀</div>
                        <div class='col-sm-10'><input type='text' name='title' class='full' placeholder="Title" value="File Storage"/></div>
                    </div>
                    <div class='row'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Root Directory'>최상위 경로</div>
                        <div class='col-sm-10'><input type='text' name='rootdir' class='full lang_attr_element' placeholder="공유할 파일이 있는 최상위 경로를 입력" data-lang-target='placeholder' data-lang-en='Root Directory for sharing'/></div>
                    </div>
                    <div class='row'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Password'>암호</div>
                        <div class='col-sm-10'><input type='password' name='pw' class='full lang_attr_element' placeholder="fs.properties 에 있는 암호" data-lang-target='placeholder' data-lang-en='Password in fs.properties'/></div>
                    </div>
                    <div class='row'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Download Max Size (KB)'>다운로드 MAX (KB)</div>
                        <div class='col-sm-10'><input type='number' name='limitsize' class='full lang_attr_element' placeholder="한 번에 다운로드할 수 있는 최대 용량 (KB)" data-lang-target='placeholder' data-lang-en='Maximum size for download at once (KB)' title='Maximum size for download at once (KB)' value='10485760' step='1' min="1"/></div>
                    </div>
                    <div class='row'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Preview Max Size (KB)'>미리보기 MAX (KB)</div>
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
                    <div class='row onlyaccount invisible'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Admin ID'>관리자 ID</div>
                        <div class='col-sm-10'><input type='text' name='adminid' class='full lang_attr_element' placeholder="관리자 ID" data-lang-target='placeholder' data-lang-en='Administration user ID'/></div>
                    </div>
                    <div class='row onlyaccount invisible'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Admin Password'>관리자 계정 암호</div>
                        <div class='col-sm-10'><input type='password' name='adminpw' class='full lang_attr_element' placeholder="관리자 계정 암호" data-lang-target='placeholder' data-lang-en='Administration user Password'/></div>
                    </div>
                    <div class='row onlyaccount invisible'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Admin Nickname'>관리자 별명</div>
                        <div class='col-sm-10'><input type='text' name='adminnick' class='full lang_attr_element' placeholder="관리자 별명" data-lang-target='placeholder' data-lang-en='Administration user Nickname'/></div>
                    </div>
                    <div class='row onlyaccount invisible'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Salt'>Salt</div>
                        <div class='col-sm-10'><input type='password' name='salt' class='full' placeholder="Salt" value='fs'/></div>
                    </div>
                    <div class='row'>
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='ETC'>기타</div>
                        <div class='col-sm-10'>
                            <span class='etcspan'>
                                <label><input type='checkbox' name='readonlymode'  class='chk_read_only'  value="true"/><span class='lang_element' data-lang-en="Read-Only mode">읽기 전용 모드</span></label>
                            </span>
                            <span class='etcspan'>
                                <label><input type='checkbox' name='readfileicon'  class='chk_read_icon'  value="true"/><span class='lang_element' data-lang-en="Read file's icon">파일 아이콘 읽기</span></label>
                            </span>
                            <span class='etcspan'>
                                <label><input type='checkbox' name='useconsole'   class='chk_use_console'  value="true"/><span class='lang_element' data-lang-en="Use Console">콘솔 사용</span></label>
                            </span>
                        </div>
                    </div>
                </div>
                <div class='col-sm-2'>
                    <div class='col-sm-12'><input type='submit' value='설치' class='full lang_attr_element btnx' style='height:50px;' data-lang-target='value' data-lang-en='Install'/></div>
                </div>
            </div>
        </form>
    </div>
    <jsp:include page="common.footer.jsp"></jsp:include>
</body>
</html>
<%
} else {
%>
<body>
    <script type="text/javascript">
    $(function() {
        location.href = "<%=fsc.getContextPath()%>/index.jsp";
    });
    </script>
    <a href="<%=fsc.getContextPath()%>/index.jsp" class='lang_element' data-lang-en='[Home]'>[홈]</a>
</body>
</html>
<%
}
%>