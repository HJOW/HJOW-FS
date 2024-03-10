<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.FSControl"%><%
FSControl fscx = FSControl.getInstance();
%>
<script type="text/javascript">
$(function() {
    var ctxPathIn    = "<%=request.getContextPath()%>";
    var formTerminal = $('.form_fs_terminal');
    var taTermDisp   = formTerminal.find('.ta_terminal');
    var inpTermPath  = formTerminal.find('.tf_terminal_path');
    var inpTermCons  = formTerminal.find('.tf_terminal_console');
    
    var captSizes = {};
    captSizes['width' ] = <%= fscx.getCaptchaWidth()  %>;
    captSizes['height'] = <%= fscx.getCaptchaHeight() %>;
    var useCaptchaDown = <%=fscx.isCaptchaDownloadOn() ? "true" : "false"%>;
    
    var popRoot = $('.fs_pops');
    var pops = {};
    
    pops['captdown'] = {};
    pops['captdown'].iframe = popRoot.find('.fs_pop_captdown').find('iframe');
    pops['captdown'].dialog = popRoot.find('.fs_pop_captdown').dialog({ autoOpen : false, title : 'Downloads', width : captSizes['width'] + 300, height : captSizes['height'] + 320, resize : function(event, ui) { pops['captdown'].iframe.height(ui.size.height - 90);  } });
    pops['captdown'].open   = function() {
        var d   = pops['captdown'].dialog;
        var ifr = pops['captdown'].iframe;
        ifr.css('width', '100%');
        ifr.css('overflow-y', 'hidden');
        ifr.height(captSizes['height'] + 320 - 90);
        ifr.attr('scrolling', 'no');
        ifr.attr('frameborder', '0');
        
        ifr.on('load', function() {
            var ct = ifr.contents();
            var cForm = ct.find('form');
            cForm.on('submit', function() {
                pops['captdown'].close();
            });
        });
        $('.ui-dialog-titlebar-close').text('X');
        d.dialog('open');
    };
    pops['captdown'].close = function() {
        var d = pops['captdown'].dialog;
        d.dialog('close');
    };
    
    function fRun(displayInput, callback) {
        inpTermCons.prop('readonly', true);
        if(displayInput) taTermDisp.val(taTermDisp.val() + '\n' + '>> ' + inpTermCons.val());
        FSUtil.ajax({
            url    : ctxPathIn + "/jsp/fs/fsconsolein.jsp",
            data   : formTerminal.serialize(),
            method : "POST",
            dataType : "json",
            success : function(data) {
                inpTermPath.val(data.path);
                if(! data.displaynull) taTermDisp.val(taTermDisp.val() + '\n' + data.display);
                
                if(data.downloadaccept) {
                	if(useCaptchaDown) {
                		pops['captdown'].iframe.attr('src', ctxPathIn + '/jsp/fs/' + 'fscaptdown.jsp?popin=true&path=' + encodeURIComponent(data.path) + "&filename=" + encodeURIComponent(data.downloadfile));
                        pops['captdown'].open();
                	} else {
                		location.href = ctxPathIn + '/jsp/fs/' + 'fscaptdown.jsp?popin=true&path=' + encodeURIComponent(data.path) + "&filename=" + encodeURIComponent(data.downloadfile);
                	}
                }
            }, error : function(jqXHR, textStatus, errorThrown) {
                taTermDisp.val(taTermDisp.val() + '\n' + 'Error ! ' + textStatus + '\n    ' + errorThrown);
                FSUtil.log('Error ! ' + textStatus);
                FSUtil.log(errorThrown);
            }, complete : function() {
                taTermDisp.scrollTop(taTermDisp[0].scrollHeight);
                inpTermCons.prop('readonly', false);
                inpTermCons.val('');
                inpTermCons.focus();
                if(typeof(callback) == 'function') callback();
            }
        });
    }
    
    formTerminal.on('submit', function() {
        fRun(true);
    });
    
    fRun(false);
});
</script>
<form class='form_fs_terminal' onsubmit='return false;'>
    <div class='align_left'>
        <input type='text' class='tf_terminal_path full' name='path' value='/' style='border: 0' readonly/>
    </div>
    <div class='align_left'>
        <textarea class='ta_terminal full' style='min-height: 390px;' readonly></textarea>
    </div>
    <div class='align_left'>
        <input type='text' name='command' class='tf_terminal_console' style='min-width: 500px;' value='first'/>
        <input type='submit' value='>' class='btnx'/>
    </div>
</form>

<div class='fs_pops invisible_wh'>
    <div class='fs_pop_captdown fs_pop_in full'><iframe></iframe></div>
</div>