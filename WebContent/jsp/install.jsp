<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.util.*, java.io.*, java.util.*, org.json.simple.*, org.json.simple.parser.* "%><%@ include file="common.pront.jsp"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title>FS Installation</title>
<jsp:include page="./common.header.jsp"></jsp:include>
</head><%
if(! installed) {
	%>
<body>
    <div class='fs_root container show-grid full fs_install'>
        <script type="text/javascript">
        $(function() {
        	var ctxPath = "<%=ctxPath%>";
            var form    = $('.form_fs_ins');
            var chkAc   = form.find('.chk_account');

            chkAc.on('change', function() {
            	if($(this).is(':checked')) form.find('.onlyaccount').removeClass('invisible');
            	else                       form.find('.onlyaccount').addClass('invisible');
            });

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
		        <div class='col-sm-10 container show-grid'>
		            <div class='row'>
                        <div class='col-sm-2' style='width:150px'>Title</div>
                        <div class='col-sm-10'><input type='text' name='title' class='full' placeholder="Title" value="File Storage"/></div>
                    </div>
		            <div class='row'>
		                <div class='col-sm-2' style='width:150px'>Root Directory</div>
		                <div class='col-sm-10'><input type='text' name='rootdir' class='full' placeholder="Root Directory for sharing"/></div>
		            </div>
		            <div class='row'>
		                <div class='col-sm-2' style='width:150px'>Password</div>
		                <div class='col-sm-10'><input type='password' name='pw' class='full' placeholder="Password in fs.properties"/></div>
		            </div>
		            <div class='row'>
                        <div class='col-sm-2' style='width:150px'>Accounts</div>
                        <div class='col-sm-10'><label><input type='checkbox' name='useaccount' class='chk_account' value="true"/>Use Accounts</label></div>
                    </div>
                    <div class='row onlyaccount invisible'>
                        <div class='col-sm-2' style='width:150px'>Admin ID</div>
                        <div class='col-sm-10'><input type='text' name='adminid' class='full' placeholder="Administration user ID"/></div>
                    </div>
                    <div class='row onlyaccount invisible'>
                        <div class='col-sm-2' style='width:150px'>Admin Password</div>
                        <div class='col-sm-10'><input type='password' name='adminpw' class='full' placeholder="Administration user Password"/></div>
                    </div>
                    <div class='row onlyaccount invisible'>
                        <div class='col-sm-2' style='width:150px'>Admin Nickname</div>
                        <div class='col-sm-10'><input type='text' name='adminnick' class='full' placeholder="Administration user Nickname"/></div>
                    </div>
                    <div class='row onlyaccount invisible'>
                        <div class='col-sm-2' style='width:150px'>Salt</div>
                        <div class='col-sm-10'><input type='password' name='salt' class='full' placeholder="Salt"/></div>
                    </div>
		        </div>
		        <div class='col-sm-2'>
		            <div class='col-sm-12'><input type='submit' value='Install' class='full' style='height:50px;'/></div>
		        </div>
		    </div>
        </form>
    </div>
</body>
</html>
	<%
} else {
	%>
<body>
    <script type="text/javascript">
    $(function() {
        location.href = "<%=ctxPath%>/index.jsp";
    });
    </script>
    <a href="<%=ctxPath%>/index.jsp">[Home]</a>
</body>
</html>
	<%
}
%>