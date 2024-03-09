<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/javascript">
$(function() {
    var ctxPathIn    = "<%=request.getContextPath()%>";
    var formTerminal = $('.form_fs_terminal');
    var taTermDisp   = formTerminal.find('.ta_terminal');
    var inpTermPath  = formTerminal.find('.tf_terminal_path');
    var inpTermCons  = formTerminal.find('.tf_terminal_console');
    
    function fRun(displayInput, callback) {
        inpTermCons.prop('readonly', true);
        if(displayInput) taTermDisp.val(taTermDisp.val() + '\n' + '>> ' + inpTermCons.val());
        $.ajax({
            url    : ctxPathIn + "/jsp/fs/fsconsolein.jsp",
            data   : formTerminal.serialize(),
            method : "POST",
            dataType : "json",
            success : function(data) {
                inpTermPath.val(data.path);
                if(! data.displaynull) taTermDisp.val(taTermDisp.val() + '\n' + data.display);
                
                if(data.downloadaccept) {
                    window.open(ctxPathIn + '/jsp/fs/' + 'fsdown.jsp?path=' + encodeURIComponent(data.path) + "&filename=" + encodeURIComponent(data.downloadfile), 'cdownload', 'width=300,height=200,toolbar=no,status=no,location=no');
                }
                if(data.closepopup) {
                    window.close();
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