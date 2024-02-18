<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type='text/javascript'>
$(function(){
	var footer = $('.fs_footer');
	var aLink  = footer.find('.a_footer');
	aLink.on('click', function() {
		var theme = $('body').attr('data-theme');
		if(theme == null || typeof(theme) == 'undefined') theme = '';
		window.open("<%=request.getContextPath()%>/jsp/fsabout.jsp?theme=" + theme, "pop_about", "width=500,height=600,scrollbars=no,status=no,location=no,toolbar=no");
	});
});
</script>
<div class='fs_footer footer container show-grid full'>
    <div class='row'>
        <div class='col-sm-12 align_center'>
            <a href='#' class='a_footer'>FS - Copyright 2024 HJOW (Heo Jin Won)</a>
        </div>
    </div>
</div>