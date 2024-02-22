<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/javascript">
$(function() {
	var ctxPathIn    = "<%=request.getContextPath()%>";
	var formTerminal = $('.form_fs_terminal');
    var taTermDisp   = formTerminal.find('.ta_terminal');
    var inpTermPath  = formTerminal.find('.tf_terminal_path');
    var inpTermCons  = formTerminal.find('.tf_terminal_console');
    
    formTerminal.on('submit', function() {
        inpTermCons.prop('readonly', true);
        taTermDisp.val(taTermDisp.val() + '\n' + '>> ' + inpTermCons.val());
        $.ajax({
            url    : ctxPathIn + "/jsp/fsconsolein.jsp",
            data   : formTerminal.serialize(),
            method : "POST",
            dataType : "json",
            success : function(data) {
                inpTermPath.val(data.path);
                if(! data.displaynull) taTermDisp.val(taTermDisp.val() + '\n' + data.display);
            }, error : function(jqXHR, textStatus, errorThrown) {
            	taTermDisp.val(taTermDisp.val() + '\n' + 'Error ! ' + textStatus + '\n    ' + errorThrown);
            }, complete : function() {
            	taTermDisp.scrollTop(taTermDisp[0].scrollHeight);
            	inpTermCons.prop('readonly', false);
                inpTermCons.val('');
                inpTermCons.focus();
            }
        });
    });
});
</script>
<form class='form_fs_terminal' onsubmit='return false;'>
    <div class='row'>
        <div class='col-sm-12'><input type='text' class='tf_terminal_path full' name='path' value='/' style='border: 0' readonly/></div>
    </div>
    <div class='row'>
        <div class='col-sm-12'><textarea class='ta_terminal full' style='min-height: 390px;' readonly></textarea></div>
    </div>
    <div class='row'>
        <div class='col-sm-12 align_left'>
            <input type='text' name='command' class='tf_terminal_console' style='min-width: 500px;'/>
            <input type='submit' value='>'/>
        </div>
    </div>
</form>