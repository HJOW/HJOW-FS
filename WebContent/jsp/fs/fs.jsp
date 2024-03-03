<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="com.hjow.fs.*, java.io.*, java.util.* "
	session="true"%><%@ include file="common.pront.jsp"%>
<%
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
if (!fsc.isInstalled()) {
%>
<script type="text/javascript">
	$(function() {
		location.href = "<%=fsc.getContextPath()%>/jsp/fs/fsinstall.jsp";
	});
	</script>
<a href="<%=fsc.getContextPath()%>/jsp/fs/fsinstall.jsp"
	class='lang_element' data-lang-en='[Install]'>[설치]</a>
<%
} else {
String pathParam = request.getParameter("path");
if (pathParam == null)
	pathParam = "";

// 상대경로 방지를 위해 . 기호  및 따옴표 문자는 반드시 제거 !
pathParam = pathParam.trim();
if (pathParam.equals("/"))
	pathParam = "";
pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();

if (pathParam.startsWith("/"))
	pathParam = pathParam.substring(1);

boolean useConsole = (!fsc.isNoConsoleMode());
%>
<script type='text/javascript'>
$(function() {
    var ctxPath = "<%=fsc.getContextPath()%>";
    var useIcon        = <%=fsc.isReadFileIconOn() ? "true" : "false"%>;
    var useCaptchaDown = <%=fsc.isCaptchaDownloadOn() ? "true" : "false"%>;
    var captchaWidth   = parseInt("<%=fsc.getCaptchaWidth() + 100%>");
    var captchaHeight  = parseInt("<%=fsc.getCaptchaHeight() + 180%>");
    var noAnonymous    = <%=fsc.isNoAnonymous() ? "true" : "false"%>;
    var loginedFirst   = <%=(fsc.getSessionUserId(request) != null) ? "true" : "false"%>
    
    var form     = $('.form_fs');
    var tables   = $('.fs_table_list');
    var listRoot = tables.find('.fs_list');
    var pathDisp = $('.path');

    var inpPath   = form.find('.hidden_path');
    var inpExcep  = form.find('.hidden_excepts');
    var inpSearch = form.find('.inp_search');
    var btnSearch = form.find('.btn_search');

    var btnUpload = form.find('.btn_upload');
    var btnMkdir  = form.find('.btn_mkdir');
    var btnConfig = form.find('.btn_config');
    
    var arDirs  = [];
    var arFiles = [];
    
    var browserInfo = FSUtil.detectBrowser();

    listRoot.empty();
    pathDisp.text('');
    
    function fIconize() {
    	listRoot.find('.tr_dir.no_icon').each(function() {
    		var tdIcon = $(this).find('.td_mark_dir');
    		tdIcon.empty();
    		tdIcon.append("<img style='width: 20px; height: 20px;'/>");
    		tdIcon.find('img').attr('src', ctxPath + '/css/images/dir.ico');
    		tdIcon.find('img').attr('alt', 'Directory');
    	});
    	listRoot.find('.tr_file.no_icon').each(function() {
    		var tdIcon = $(this).find('.td_mark_file');
            tdIcon.empty();
            tdIcon.append("<img style='width: 20px; height: 20px;'/>");
            tdIcon.find('img').attr('src', ctxPath + '/css/images/files.png');
            tdIcon.find('img').attr('alt', 'File');
    	});
    	
    	if(! useIcon) return;
    	
    	var iconizeWorkSize = 10;
    	var iconizeIndex = 0;
        var iconizeArray = [];
        var breaks = false;
        var bkColor = {r : 255, g : 255, b : 255};
        if($('body').is('.dark')) bkColor = {r : 59, g : 59, b : 59};
        
        listRoot.find('.tr_file.no_icon').each(function() {
        	if(breaks) return;
        	
            var fileNm = $(this).find('a.link_file').attr('data-name');
            if(typeof(fileNm) != 'undefined') {
            	var tries = $(this).attr('data-try');
                
                if(tries == null || typeof(tries) == 'undefined' || tries == '') tries = 0;
                else tries = parseInt(String(tries).trim());
                
                tries++;
                $(this).attr('data-try', '' + tries);
                
                if(tries >= 3) return;
            	iconizeArray.push(fileNm);
            }
            
            if(iconizeArray.length >= iconizeWorkSize) {
            	var filelist = '';
            	var fdx=0;
            	for(fdx=0; fdx<iconizeArray.length; fdx++) {
            		if(filelist != '') filelist += ',';
            		filelist += iconizeArray[fdx];
            	}
            	var workingArray = iconizeArray;
            	iconizeArray = [];
                $.ajax({
                	url  : ctxPath + "/jsp/fs/fsfileicons.jsp",
                	data : { path : inpPath.val(), files : filelist, br : bkColor.r, bg : bkColor.g, bb : bkColor.b },
                	method : 'POST',
                	dataType : 'json',
                	success : function(data) {
                		if(data.success) {
                			// Apply received icon
                			$.each(data.data, function(dFileName, dImage) {
                				listRoot.find('.tr_file.no_icon').each(function() {
                					if(! $(this).is('.no_icon')) return;
                					
                					var inFileNm = $(this).find('a.link_file').attr('data-name');
                					if(inFileNm != dFileName) return;
                					
                					var inTdIcon = $(this).find('.td_mark_file');
                					inTdIcon.empty();
                					inTdIcon.append("<img style='width: 20px; height: 20px;'/>");
                					inTdIcon.find('img').attr('src', dImage);
                					inTdIcon.find('img').attr('alt', 'File');
                					
                					$(this).removeClass('no_icon');
                					$(this).addClass('icon');
                				});
                			});
                		}
                	}, complete : function() {
                		fIconize();
                	}
                });
                breaks = true;
            }
        });
    }

    function fReload(firsts) {
    	if(firsts) {
    		tables.find('.col_controls').css('width', '50px');
            listRoot.find('binded-click').each(function() { $(this).off('click'); });
            
            listRoot.empty();
            listRoot.append("<tr class='element element_special progressing'><td colspan='4'>...</td></tr>");
            pathDisp.text('');
            
            inpExcep.val('');
    	}
    	
    	var expVal = inpExcep.val();
        
    	$.ajax({
            url    : ctxPath + "/jsp/fs/fslist.jsp",
            data   : form.serialize(),
            method : "POST",
            dataType : "json",
            success : function(data) {
                arDirs  = data.directories;
                arFiles = data.files;

                if(firsts) {
                	listRoot.empty();
                    pathDisp.text('');
                } else {
                	listRoot.find('.progressing').remove();
                }

                var idType = 'U';

                if(data.session != null && typeof(data.session) != 'undefined') {
                    if(data.session.id != null && typeof(data.session.id) != 'undefined' && data.session.idtype != null && typeof(data.session.idtype) != 'undefined') {
                    	idType = String(data.session.idtype);
                    }
                }

                if(idType == 'A') tables.find('.col_controls').css('width', '100px');
                
                if(! data.path == '') {
                    listRoot.append("<tr class='element element_special back'><td colspan='4'><a href='#' class='link_back lang_element' data-lang-en='[BACK]'>[뒤로 가기]</a></td></tr>");
                }

                if(arDirs.length == 0 && arFiles.length == 0) {
                	listRoot.append("<tr class='element element_special empty'><td colspan='4' class='lang_element filednd' data-lang-en='Empty'>비어 있음</td></tr>");
                }

                var idx = 0;
                
                for(idx = 0; idx < arDirs.length; idx++) {
                    var lvalue = String(arDirs[idx].value);
                    var lname  = String(arDirs[idx].name);
                    
                    if(expVal != '') expVal += ',';
                    expVal += lname;
                    
                    listRoot.append("<tr class='element tr_dir tr_dir_" + idx + "'><td class='td_mark_dir'>[DIR]</td><td colspan='2'><a href='#' class='link_dir' data-path=''></a></td><td class='td_buttons'></td></tr>");
                    
                    var tr = listRoot.find('.tr_dir_' + idx);
                    var a  = tr.find('.link_dir');
                    
                    a.attr('data-path', lvalue);
                    a.text(lname);
                    a.addClass('ellipsis');
                    tr.addClass('no_icon');

                    if(idType == 'A') {
                    	var tdBtns = tr.find('.td_buttons');
                    	tdBtns.css('text-align', 'right');
                    	
                    	if(typeof(arDirs[idx].elements) != 'undefined') {
                    		if(arDirs[idx].elements <= 0) {
                    			tdBtns.append("<input type='button' class='btn_delete' value='X'/>");
                    			
                    			var btnDel = tdBtns.find('.btn_delete');
                                btnDel.attr('data-path', a.attr('data-path'));
                                btnDel.attr('data-name', a.attr('data-name'));

                                btnDel.on('click', function() {
                                    var delpath = $(this).attr('data-path');
                                    
                                    var confirmMsg = 'Really? Do you want to delete this directory?';
                                    if(FSUtil.detectLanguage() == 'ko') confirmMsg = '이 폴더를 정말 삭제하시겠습니까?';
                                    
                                    if(confirm(confirmMsg)) {
                                        $.ajax({
                                            url  : ctxPath + '/jsp/fs/fsremove.jsp',
                                            data : {
                                                path : delpath,
                                                dels : 'dir'
                                            },
                                            method : 'POST',
                                            dataType : 'JSON',
                                            success : function(data) {
                                                if(! data.success) alert(data.message);
                                                fReload(true);
                                            }
                                        });
                                    }
                                });
                                btnDel.addClass('binded_click');
                    		}
                    	}
                    }
                }
                
                for(idx = 0; idx < arFiles.length; idx++) {
                	var fileOne = arFiles[idx];
                	
                    var lname   = String(fileOne.name);
                    var lsize   = String(fileOne.size);
                    var cttype  = String(fileOne.contentType);
                    var prvtype = String(fileOne.previewType);
                    
                    if(expVal != '') expVal += ',';
                    expVal += lname;
                    
                    var trHtml = "<tr class='element tr_file tr_file_" + idx + "'>";
                    trHtml += "<td class='td_mark_file filednd'>[FILE]</td>";
                    trHtml += "<td class='filednd'>";
                    trHtml += "<div class='div_td_file_a'>";
                    trHtml += "<a href='#' class='link_file' data-path='' data-name=''></a>";
                    trHtml += "</div>";
                    trHtml += "<div class='div_td_file_preview full invisible'>";
                    trHtml += "</div>";
                    trHtml += "</td>";
                    trHtml += "<td class='td_file_size filednd'></td>";
                    trHtml += "<td class='td_buttons'></td>";
                    trHtml += "</tr>";
                    
                    listRoot.append(trHtml);
                    
                    var tr = listRoot.find('.tr_file_' + idx);
                    var a  = tr.find('.link_file');
                    
                    a.attr('data-path', inpPath.val());
                    a.attr('data-name', lname);
                    if(fileOne['over_down']) a.addClass('disabled');
                    a.text(lname);
                    a.addClass('ellipsis');
                    tr.attr('data-idx', '' + idx);
                    tr.addClass('no_icon');
                    tr.find('.td_file_size').text(lsize);
                    tr.find('.td_file_size').css('text-align', 'right');

                    var tdBtns = tr.find('.td_buttons');
                    tdBtns.css('text-align', 'right');
                    tdBtns.append("<input type='button' class='btn_preview not_opened invisible' value='▼'/>");
                    
                    var divPrev = tr.find('.div_td_file_preview');
                    var btnPreview = tdBtns.find('.btn_preview');
                    if(prvtype >= 0) {
                    	if(fileOne['over_prev']) {
                    		btnPreview.addClass('disabled');
                    		btnPreview.prop('disabled', true);
                    		btnPreview.attr('title', 'This file is too big. Cannot preview.');
                    	} else {
                    		btnPreview.attr('data-path', a.attr('data-path'));
                            btnPreview.attr('data-name', a.attr('data-name'));
                            btnPreview.attr('data-idx', '' + idx);
                            btnPreview.attr('data-prv', '' + prvtype);
                            
                            btnPreview.on('click', function() {
                                var idxIn = $(this).attr('data-idx');
                                var trIn  = listRoot.find('.tr_file_' + idxIn);
                                var divPrvIn = trIn.find('.div_td_file_preview');
                                var prvIn    = $(this).attr('data-prv');
                                
                                if($(this).is('.not_opened')) {
                                    $(this).removeClass('not_opened');
                                    $(this).attr('value', '△');
                                    
                                    divPrvIn.empty();
                                    
                                    // browserInfo.nm == 'ie' && browserInfo.ver < 9
                                    if(prvIn == '1') divPrvIn.append("<img class='img_preview preview_element'/>");
                                    if(prvIn == '2') divPrvIn.append("<video class='video_preview preview_element'/>");
                                    if(prvIn == '3') divPrvIn.append("<audio class='audio_preview preview_element'/>");
                                    if(prvIn == '4') divPrvIn.append("<iframe class='iframe_preview preview_element'></iframe>");
                                    
                                    var srcs = ctxPath + "/jsp/fs/fsdown.jsp?path=" + encodeURIComponent($(this).attr('data-path')) + "&filename=" + encodeURIComponent($(this).attr('data-name')) + "&mode=VIEW";
                                    var elem = divPrvIn.find('.preview_element');
                                    
                                    elem.css('height', 400);
                                    elem.attr('src', srcs);
                                    divPrvIn.removeClass('invisible');
                                } else {
                                    divPrvIn.addClass('invisible');
                                    $(this).addClass('not_opened');
                                    $(this).attr('value', '▼');
                                    divPrvIn.empty();
                                }
                                
                            });
                            btnPreview.addClass('binded_click');
                    	}
                    	btnPreview.removeClass('invisible');
                    }
                    
                    if(idType == 'A') {
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
                                    url  : ctxPath + '/jsp/fs/fsremove.jsp',
                                    data : {
                                        path : delpath,
                                        name : delname,
                                        dels : 'file'
                                    },
                                    method : 'POST',
                                    dataType : 'JSON',
                                    success : function(data) {
                                        if(! data.success) alert(data.message);
                                        fReload(true);
                                    }
                                });
                            }
                        });
                        btnDel.addClass('binded_click');
                    }
                }
                
                inpExcep.val(expVal);
                
                arDirs  = null;
                arFiles = null;

                pathDisp.text(data.dpath);
                inpSearch.val(data.keyword);

                $('.link_back').each(function() {
                    var aLink = $(this);
                    aLink.on('click', function() {
                        var lists = inpPath.val().split('/');
                        var newPath = '';
                        for(var ldx = 0; ldx < lists.length - 1; ldx++) {
                            if(ldx >= 1) newPath += '/';
                            newPath += lists[ldx];
                        }

                        inpPath.val(newPath);
                        fReload(true);
                    });
                    aLink.addClass('binded-click');
                });
                
                $('.link_dir').each(function() {
                    var aLink = $(this);
                    aLink.on('click', function() {
                    	inpPath.val($(this).attr('data-path'));
                    	fReload(true);
                    });
                    aLink.addClass('binded-click');
                });
                
                $('.link_file').each(function() {
                    var aLink = $(this);
                    aLink.on('click', function() {
                    	if($(this).is('.disabled')) return;
                        var popOpt = 'width=' + (captchaWidth + 150) + "," + "height=" + (captchaHeight + 50);
                        popOpt += ',scrollbars=no,status=no,location=no,toolbar=no';
                        var theme = '';
                        if($('body').is('.dark')) theme='dark';
                        if(useCaptchaDown) {
                        	window.open(ctxPath + '/jsp/fs/' + 'fscaptdown.jsp?theme=' + theme + '&path=' + encodeURIComponent(inpPath.val()) + "&filename=" + encodeURIComponent($(this).attr('data-name')), 'download', popOpt);
                        } else {
                        	location.href = ctxPath + '/jsp/fs/' + 'fsdown.jsp?path=' + encodeURIComponent(inpPath.val()) + "&filename=" + encodeURIComponent($(this).attr('data-name'));
                        }
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
                if(idType != 'A') {
                	$('.only_admin ').addClass('invisible');
                }
                
                if(typeof(data.skipped) != 'undefined') {
                    if(data.skipped >= 1) {
                        listRoot.append("<tr class='element element_special askmore'><td colspan='4'><a href='#' class='a_askmore lang_element' data-lang-en='More...'>더 조회하기...</a></td></tr>");
                        listRoot.find('.a_askmore').on('click', function() {
                        	listRoot.find('.askmore').find('.binded_click').off('click');
                        	listRoot.find('.askmore').remove();
                        	
                        	fReload(false);
                        });
                        listRoot.find('.a_askmore').addClass('binded_click');
                    }
                }
                
                FSUtil.applyLanguage();
                fIconize();
            }, error : function(jqXHR, textStatus, errorThrown) {
            	textStatus  = String(textStatus).replace(/[<>]+/g, '');
            	errorThrown = String(errorThrown).replace(/[<>]+/g, '');
            	listRoot.append("<tr class='element error'><td>ERROR ! " + textStatus + ", " + errorThrown + "</td></tr>");
            }
        });
    }

    form.on('submit', function() { fReload(true); });

    btnUpload.on('click', function() {
        var paths = inpPath.val();
        var popOpt = 'width=300,height=200,scrollbars=no,status=no,location=no,toolbar=no';
        var theme = '';
        if($('body').is('.dark')) theme='dark';
        window.open(ctxPath + '/jsp/fs/fsupload.jsp?theme=' + theme + '&path=' + encodeURIComponent(paths), 'upload', popOpt);
    });
    
    btnConfig.on('click', function() {
        var popOpt = 'width=780,height=550,scrollbars=yes,status=no,location=no,toolbar=no';
        var theme = '';
        if($('body').is('.dark')) theme='dark';
        window.open(ctxPath + '/jsp/fs/fsadmin.jsp?theme=' + theme, 'config', popOpt);
    });
    
    btnMkdir.on('click', function() {
    	var msg = 'Please input the name for new folder. (No dot, /, quotes, <>, ?, &)';
    	if(FSUtil.detectLanguage() == 'ko') msg = '생성할 폴더 이름을 입력해 주세요. (마침표, /, 따옴표, <>, ?, & 를 넣을 수 없습니다.)';
    	
    	var dirName = prompt(msg, '');
    	
    	if(dirName == null || typeof(dirName) == 'undefined') {
    		return;
    	}
    	dirName = dirName.trim();
    	if(dirName == '') return;
    	
    	if(dirName.indexOf('.') >= 0) {
    		msg = 'Wrong name !';
            if(FSUtil.detectLanguage() == 'ko') msg = '폴더 이름으로 적합하지 않습니다.';
            alert(msg);
            return;
    	}
    	
    	if(dirName.indexOf('/') >= 0 || dirName.indexOf('\\') >= 0) {
            msg = 'Wrong name !';
            if(FSUtil.detectLanguage() == 'ko') msg = '폴더 이름으로 적합하지 않습니다.';
            alert(msg);
            return;
        }
    	
    	if(dirName.indexOf("'") >= 0 || dirName.indexOf('"') >= 0) {
            msg = 'Wrong name !';
            if(FSUtil.detectLanguage() == 'ko') msg = '폴더 이름으로 적합하지 않습니다.';
            alert(msg);
            return;
        }
    	
    	if(dirName.indexOf("<") >= 0 || dirName.indexOf('>') >= 0) {
            msg = 'Wrong name !';
            if(FSUtil.detectLanguage() == 'ko') msg = '폴더 이름으로 적합하지 않습니다.';
            alert(msg);
            return;
        }
    	
    	if(dirName.indexOf("?") >= 0 || dirName.indexOf('&') >= 0) {
            msg = 'Wrong name !';
            if(FSUtil.detectLanguage() == 'ko') msg = '폴더 이름으로 적합하지 않습니다.';
            alert(msg);
            return;
        }
    	
    	$.ajax({
            url  : ctxPath + '/jsp/fs/fsmkdir.jsp',
            data : {
                path : inpPath.val(),
                name : dirName
            },
            method : 'POST',
            dataType : 'JSON',
            success : function(data) {
                if(! data.success) alert(data.message);
                fReload(true);
            }
        });
    });

    var fsFileList = $('.fs_filelist');
    if(noAnonymous) {
    	if(loginedFirst) {
    		fsFileList.addClass('invisible');
            $('.fs_filelist_view').removeClass('invisible');
    	} else {
    		fsFileList.addClass('invisible');
            $('.fs_filelist_anonymous').removeClass('invisible');
    	}
    } else {
    	fsFileList.addClass('invisible');
        $('.fs_filelist_view').removeClass('invisible');
    }
    
    <%if (useConsole) {%>
    var btnConsole = form.find('.btn_console');
    btnConsole.on('click', function() {
        var theme = '';
        if($('body').is('.dark')) theme='dark';
        window.open(ctxPath + '/jsp/fs/fsconsolepop.jsp?theme=' + theme, 'console', 'width=780,height=450,scrollbars=yes,status=no,location=no,toolbar=no');
    });
    <%}%>
    
    fReload(true);
});
</script>
<div class='fs_root'>
	<div class='fs_filelist fs_filelist_view container show-grid full'>
		<form class='form_fs' onsubmit='return false;'>
			<input type='hidden' name='path' class='hidden_path'
				value='<%=pathParam%>' /> <input type='hidden' name='excepts'
				class='hidden_excepts' value='' />
			<div class='row fs_title'>
				<div class='col-sm-12'>
					<h2><%=fsc.getTitle()%></h2>
				</div>
			</div>
			<div class='row fs_directory'>
				<div class='col-sm-10'>
					<h4 class='path_title'>
						<span class='lang_element' data-lang-en='Current Directory : '>현재
							디렉토리 : </span><span class='path'></span>
					</h4>
				</div>
				<div class='col-sm-2'>
					<input type='button'
						class='btn_upload  privilege_element invisible lang_attr_element'
						value='업로드' data-lang-target='value' data-lang-en='Upload' /> <input
						type='button'
						class='btn_mkdir   privilege_element invisible lang_attr_element'
						value='새 폴더' data-lang-target='value' data-lang-en='New Folder' />
					<input type='button'
						class='btn_config  privilege_element only_admin invisible lang_attr_element'
						value='설정' data-lang-target='value' data-lang-en='Config' />
					<%
					if (useConsole) {
					%>
					<input type='button' class='btn_console lang_attr_element'
						value='콘솔' data-lang-target='value' data-lang-en='Console'
						accesskey="t" />
					<%
					}
					%>
				</div>
			</div>
			<div class='row fs_search'>
				<div class='col-sm-10'>
					<input type='text' class='inp_search full lang_attr_element'
						name='keyword' placeholder="디렉토리 내 검색"
						data-lang-target='placeholder'
						data-lang-en='Search in current directory' />
				</div>
				<div class='col-sm-2'>
					<input type='submit' class='btn_search full lang_attr_element'
						value='검색' data-lang-target='value' data-lang-en='Search' />
				</div>
			</div>
			<div class='row fs_root'>
				<div class='col-sm-12'>
					<table class="table table-hover full fs_table_list">
						<colgroup>
							<col style='width: 50px;' />
							<col />
							<col style='width: 100px;' />
							<col style='width: 50px;' class='col_controls' />
						</colgroup>
						<tbody class='fs_list'>

						</tbody>
					</table>
				</div>
			</div>
		</form>
	</div>
	<div class='fs_filelist fs_filelist_anonymous full invisible'>
		<jsp:include page="fsanonymousblock.jsp"></jsp:include>
	</div>
</div>
<jsp:include page="common.footer.jsp"></jsp:include>
<%
}
%>