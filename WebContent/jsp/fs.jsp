<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.* " session="true"%><%@ include file="common.pront.jsp"%><%
if(! fsc.installed) {
	%>
	<script type="text/javascript">
	$(function() {
		location.href = "<%=fsc.ctxPath%>/jsp/install.jsp";
	});
	</script>
	<a href="<%=fsc.ctxPath%>/jsp/install.jsp" class='lang_element' data-lang-en='[Install]'>[설치]</a>
	<%
} else {
String pathParam = request.getParameter("path");
if(pathParam == null) pathParam = "";

// 상대경로 방지를 위해 . 기호  및 따옴표 문자는 반드시 제거 !
pathParam = pathParam.trim();
if(pathParam.equals("/")) pathParam = "";
pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();

if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);
%>
<script type='text/javascript'>
$(function() {
    var ctxPath = "<%=fsc.ctxPath%>";
    var captchaWidth  = parseInt("<%=fsc.captchaWidth  + 100%>");
    var captchaHeight = parseInt("<%=fsc.captchaHeight + 180%>");
	
    var form     = $('.form_fs');
    var tables   = $('.fs_table_list');
    var listRoot = tables.find('.fs_list');
    var pathDisp = $('.path');

    var inpPath   = form.find('.hidden_path');
    var inpSearch = form.find('.inp_search');
    var btnSearch = form.find('.btn_search');

    var btnUpload = form.find('.btn_upload');
    
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
                    listRoot.append("<tr class='element back'><td colspan='4'><a href='#' class='link_back lang_element' data-lang-en='[BACK]'>[뒤로 가기]</a></td></tr>");
                }

                if(arDirs.length == 0 && arFiles.length == 0) {
                	listRoot.append("<tr class='element empty'><td colspan='4' class='lang_element filednd' data-lang-en='Empty'>비어 있음</td></tr>");
                }

                var idx = 0;
                
                for(idx = 0; idx < arDirs.length; idx++) {
                    var lvalue = String(arDirs[idx].value);
                    var lname  = String(arDirs[idx].name);
                    
                    listRoot.append("<tr class='element tr_dir_" + idx + "'><td class='td_mark_dir'>[DIR]</td><td colspan='2'><a href='#' class='link_dir' data-path=''></a></td><td class='td_buttons'></td></tr>");
                    
                    var tr = listRoot.find('.tr_dir_' + idx);
                    var a  = tr.find('.link_dir');
                    
                    a.attr('data-path', lvalue);
                    a.text(lname);
                    a.addClass('ellipsis');

                    if(idType == 'A') {
                    	var tdBtns = tr.find('.td_buttons');
                    	tdBtns.css('text-align', 'right');
                    }
                }
                
                for(idx = 0; idx < arFiles.length; idx++) {
                    var lname  = String(arFiles[idx].name);
                    var lsize  = String(arFiles[idx].size);
                    
                    listRoot.append("<tr class='element tr_file_" + idx + "'><td colspan='2' class='filednd'><a href='#' class='link_file' data-path='' data-name=''></a></td><td class='td_file_size filednd'></td><td class='td_buttons'></td></tr>");
                    
                    var tr = listRoot.find('.tr_file_' + idx);
                    var a  = tr.find('.link_file');
                    
                    a.attr('data-path', $('.hidden_path').val());
                    a.attr('data-name', lname);
                    a.text(lname);
                    a.addClass('ellipsis');
                    tr.find('.td_file_size').text(lsize);
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
                            
                            var confirmMsg = 'Really? Do you want to delete this file?';
                            if(FSUtil.detectLanguage() == 'ko') confirmMsg = '이 파일을 정말 삭제하시겠습니까?';
                            
                            if(confirm(confirmMsg)) {
                                $.ajax({
                                    url  : ctxPath + '/jsp/fsremove.jsp',
                                    data : {
                                        path : delpath,
                                        name : delname
                                    },
                                    method : 'POST',
                                    dataType : 'JSON',
                                    success : function(data) {
                                        if(! data.success) alert(data.message);
                                        fReload();
                                    }
                                });
                            }
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
                        var theme = '';
                        if($('body').is('.dark')) theme='dark';
                        window.open(ctxPath + '/jsp/' + 'fscapt.jsp?theme=' + theme + '&path=' + encodeURIComponent($('.hidden_path').val()) + "&filename=" + encodeURIComponent($(this).attr('data-name')), 'captcha', popOpt);
                    });
                    aLink.addClass('binded-click');
                });

                $('.privilege_element').addClass('invisible');
                if(data.privilege == 'edit') {
                	$('.privilege_element').removeClass('invisible');
                	FSUtil.applyDragAndDrop($('body'), ctxPath, inpPath.val());
                } else {
                	$('body').find('.filednd').each(function() {
                		var area = $(this);
                        area.off('drop');
                        area.off('dragover');
                        area.off('dragenter');
                        area.off('dragleave');
                        area.removeClass('filedndin');
                	});
                }
                
                FSUtil.applyLanguage();
            }, error : function(jqXHR, textStatus, errorThrown) {
            	textStatus  = String(textStatus).replace(/[<>]+/g, '');
            	errorThrown = String(errorThrown).replace(/[<>]+/g, '');
            	listRoot.append("<tr class='element error'><td>ERROR ! " + textStatus + ", " + errorThrown + "</td></tr>");
            }
        });
    }

    form.on('submit', fReload)

    btnUpload.on('click', function() {
        var paths = inpPath.val();
        var popOpt = 'width=300,height=200,scrollbars=no,status=no,location=no,toolbar=no';
        var theme = '';
        if($('body').is('.dark')) theme='dark';
        window.open(ctxPath + '/jsp/fsupload.jsp?theme=' + theme + '&path=' + encodeURIComponent(paths), 'upload', popOpt);
    });

    fReload();
});
</script>
<div class='fs_root container show-grid full'>
	<form class='form_fs' onsubmit='return false;'>
	    <input type='hidden' name='path' class='hidden_path' value='<%= pathParam %>'/>
	    <div class='row fs_title'>
	        <div class='col-sm-12'>
	            <h2><%= fsc.title %></h2>
	        </div>
	    </div>
	    <div class='row fs_directory'>
	        <div class='col-sm-10'>
                <h4 class='path_title'><span class='lang_element' data-lang-en='Current Directory : '>현재 디렉토리 : </span><span class='path'></span></h4>
            </div>
            <div class='col-sm-2'>
                <input type='button' class='btn_upload privilege_element invisible lang_attr_element' value='업로드' data-lang-target='value' data-lang-en='Upload'/>
            </div>
	    </div>
	    <div class='row fs_search'>
	        <div class='col-sm-10'>
	            <input type='text'   class='inp_search full lang_attr_element' name='keyword' placeholder="디렉토리 내 검색" data-lang-target='placeholder' data-lang-en='Search in current directory'/>
	        </div>
	        <div class='col-sm-2'>
	            <input type='submit' class='btn_search full lang_attr_element' value='검색' data-lang-target='value' data-lang-en='Search'/>
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