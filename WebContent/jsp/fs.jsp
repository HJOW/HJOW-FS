<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.io.*, java.util.* " session="true"%><%@ include file="common.pront.jsp"%><%
if(! installed) {
	%>
	<script type="text/javascript">
	$(function() {
		location.href = "<%=ctxPath%>/jsp/install.jsp";
	});
	</script>
	<a href="<%=ctxPath%>/jsp/install.jsp">[Install]</a>
	<%
} else {
String pathParam = request.getParameter("path");
if(pathParam == null) pathParam = "";

// 상대경로 방지를 위해 . 기호  및 따옴표 문자는 반드시 제거 !
pathParam = pathParam.trim();
if(pathParam.equals("/")) pathParam = "";
pathParam = pathParam.replace(".", "").replace("'", "").replace("\"", "").replace("\\", "/").trim();

if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);
%>
<script type='text/javascript'>
$(function() {
    var ctxPath = "<%=ctxPath%>";
    var captchaWidth  = parseInt("<%=captchaWidth  + 100%>");
    var captchaHeight = parseInt("<%=captchaHeight + 180%>");
	
    var form     = $('.form_fs');
    var tables   = $('.fs_table_list');
    var listRoot = tables.find('.fs_list');
    var pathDisp = $('.path');

    var inpPath   = form.find('.hidden_path');
    var inpSearch = form.find('.inp_search');
    var btnSearch = form.find('.btn_search');
    
    var arDirs  = [];
    var arFiles = [];

    listRoot.empty();
    pathDisp.text('');

    function fReload() {
        tables.find('.col_controls').css('width', '10px');
    	listRoot.find('binded-click').each(function() { $(this).off('click'); });
    	listRoot.empty();
        pathDisp.text('');
        
    	$.ajax({
            url    : ctxPath + "/jsp/fslist.jsp",
            data   : form.serialize(),
            method : "POST",
            dataType : "json",
            success : function(data) {
                arDirs  = data.directories;
                arFiles = data.files;

                listRoot.empty();
                pathDisp.text('');

                var idType = 'U';

                if(data.session != null && typeof(data.session) != 'undefined') {
                    if(data.session.id != null && typeof(data.session.id) != 'undefined' && data.session.idtype != null && typeof(data.session.idtype) != 'undefined') {
                    	idType = String(data.session.idtype);
                    }
                }

                if(idType == 'A') tables.find('.col_controls').css('width', '100px');
                
                if(! data.path == '') {
                    listRoot.append("<tr class='element back'><td colspan='4'><a href='#' class='link_back'>[BACK]</a></td></tr>");
                }

                if(arDirs.length == 0 && arFiles.length == 0) {
                	listRoot.append("<tr class='element empty'><td colspan='4'>Empty</td></tr>");
                }

                var idx = 0;
                
                for(idx = 0; idx < arDirs.length; idx++) {
                    var lvalue = String(arDirs[idx].value);
                    var lname  = String(arDirs[idx].name);
                    
                    listRoot.append("<tr class='element tr_dir_" + idx + "'><td>[DIR]</td><td colspan='2'><a href='#' class='link_dir' data-path=''></a></td><td class='td_buttons'></td></tr>");
                    
                    var tr = listRoot.find('.tr_dir_' + idx);
                    var a  = tr.find('.link_dir');
                    
                    a.attr('data-path', lvalue);
                    a.text(lname);

                    if(idType == 'A') {
                    	var tdBtns = tr.find('.td_buttons');
                    	tdBtns.css('text-align', 'right');
                    }
                }
                
                for(idx = 0; idx < arFiles.length; idx++) {
                	var lvalue = String(arFiles[idx].value);
                    var lname  = String(arFiles[idx].name);
                    var lsize  = String(arFiles[idx].size);
                    
                    listRoot.append("<tr class='element tr_file_" + idx + "'><td colspan='2'><a href='#' class='link_file' data-path='' data-name=''></a></td><td class='td_file_size'></td><td class='td_buttons'></td></tr>");
                    
                    var tr = listRoot.find('.tr_file_' + idx);
                    var a  = tr.find('.link_file');
                    
                    a.attr('data-path', lvalue);
                    a.attr('data-name', lname);
                    a.text(lname);
                    tr.find('.td_file_size').text('(' + lsize + ')');
                    tr.find('.td_file_size').css('text-align', 'right');

                    if(idType == 'A') {
                        var tdBtns = tr.find('.td_buttons');
                        tdBtns.css('text-align', 'right');
                        tdBtns.append("<input type='button' class='btn_delete' value='X'/>");

                        var btnDel = tdBtns.find('.btn_delete');
                        btnDel.attr('data-path', a.attr('data-path'));
                        btnDel.attr('data-name', a.attr('data-name'));

                        btnDel.on('click', function() {
                            var delpath = $(this).attr('data-path');
                            var delname = $(this).attr('data-name');
                            alert('Not supported yet.');
                            // TODO
                        });
                        btnDel.addClass('binded_click');
                    }
                }
                
                arDirs  = null;
                arFiles = null;

                pathDisp.text(data.dpath);
                inpSearch.val(data.keyword);

                $('.link_back').each(function() {
                    var aLink = $(this);
                    aLink.on('click', function() {
                        var lists = $('.hidden_path').val().split('/');
                        var newPath = '';
                        for(var ldx = 0; ldx < lists.length - 1; ldx++) {
                            if(ldx >= 1) newPath += '/';
                            newPath += lists[ldx];
                        }

                        inpPath.val(newPath);
                        fReload();
                    });
                    aLink.addClass('binded-click');
                });
                
                $('.link_dir').each(function() {
                    var aLink = $(this);
                    aLink.on('click', function() {
                    	inpPath.val($(this).attr('data-path'));
                    	fReload();
                    });
                    aLink.addClass('binded-click');
                });
                
                $('.link_file').each(function() {
                    var aLink = $(this);
                    aLink.on('click', function() {
                        var popOpt = 'width=' + (captchaWidth + 50) + "," + "height=" + (captchaHeight + 50);
                        popOpt += ',scrollbars=no,status=no,location=no,toolbar=no';
                        window.open(ctxPath + '/jsp/' + 'fscapt.jsp?path=' + encodeURIComponent($('.hidden_path').val()) + "&filename=" + encodeURIComponent($(this).attr('data-name')), 'captcha', popOpt);
                    });
                    aLink.addClass('binded-click');
                });
            }, error : function(jqXHR, textStatus, errorThrown) {
            	textStatus  = String(textStatus).replace(/[<>]+/g, '');
            	errorThrown = String(errorThrown).replace(/[<>]+/g, '');
            	listRoot.append("<tr class='element error'><td>ERROR ! " + textStatus + ", " + errorThrown + "</td></tr>");
            }
        });
    }

    btnSearch.on('click', fReload);
    btnSearch.addClass('binded-click');

    fReload();
});
</script>
<div class='fs_root container show-grid full'>
	<form class='form_fs' onsubmit='return false;'>
	    <input type='hidden' name='path' class='hidden_path' value='<%= pathParam %>'/>
	    <div class='row fs_search'>
	        <div class='col-sm-12'>
	            <h4 class='path_title'><span>현재 디렉토리 : </span><span class='path'></span></h4>
	        </div>
	        <div class='col-sm-10'>
	            <input type='text'   class='inp_search full' name='keyword' placeholder="디렉토리 내 검색"/>
	        </div>
	        <div class='col-sm-2'>
	            <input type='button' class='btn_search full' value='검색'/>
	        </div>
	    </div>
	    <div class='row fs_root'>
	        <div class='col-sm-12'>
		        <table class="table table-hover full fs_table_list">
		            <colgroup>
		                <col style='width:50px;'/>
		                <col/>
		                <col style='width:100px;'/>
		                <col style='width:10px;' class='col_controls'/>
		            </colgroup>
		            <tbody class='fs_list'>
		            
		            </tbody>
		        </table>
	        </div>
	    </div>
	</form>
</div>
<%
}
%>