<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.*, org.json.simple.*, org.json.simple.parser.* "%><%
FSControl fsc = FSControl.getInstance();
long now = System.currentTimeMillis();
FSControl.init(request);
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
if(! fsc.installed) {
	%>
<body>
    <div class='fs_root container show-grid full fs_install'>
        <script type="text/javascript">
        $(function() {
        	var bodys = $('body');
            if(bodys.is('.popup')) bodys.removeClass('dark');
            
        	var ctxPath = "<%=fsc.ctxPath%>";
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
                $.ajax({
                	url    : ctxPath + "/jsp/installin.jsp",
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
                <div class='col-sm-2'><h2>FS Installation</h2></div>
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
                        <div class='col-sm-2 lang_element' style='width:150px' data-lang-en='Accounts'>계정</div>
                        <div class='col-sm-10'><label><input type='checkbox' name='useaccount' class='chk_account' value="true"/><span class='lang_element' data-lang-en='Use Accounts'>계정 기능 사용</span></label></div>
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
		        </div>
		        <div class='col-sm-2'>
		            <div class='col-sm-12'><input type='submit' value='설치' class='full lang_attr_element' style='height:50px;' data-lang-target='value' data-lang-en='Install'/></div>
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
        location.href = "<%=fsc.ctxPath%>/index.jsp";
    });
    </script>
    <a href="<%=fsc.ctxPath%>/index.jsp" class='lang_element' data-lang-en='[Home]'>[홈]</a>
</body>
</html>
	<%
}
%>