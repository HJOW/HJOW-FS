<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title>JSON TEST</title>
<script type='text/javascript' src='../../js/jquery-1.12.4.js'></script>
<script type='text/javascript'>
/* https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Global_Objects/String/trim */
if(!String.prototype.trim) {
    String.prototype.trim = function() {
        return this.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, '');
    };
}
$(function() {
	$('#btn').on('click', function() {
		try {
			var inpVal = $('#input').val();
	        inpVal = String(inpVal).trim();
	        console.log('Input is...');
	        console.log(inpVal);
	        
	        var parsed = JSON.parse(inpVal);
	        console.log('Parsed object is...');
	        console.log(parsed);
	        $('#result').val(JSON.stringify(parsed));
		} catch(e) {
			console.log(e);
			$('#result').val('Error : ' + e);
		}
	});
});
</script>
</head>
<body>
    <div>
        <textarea style='width: 100%; min-height: 500px;' id='input'>
{}
        </textarea>
    </div>
    <div>
        <input type='button' id='btn' class='btnx' value='run'/>
    </div>
    <div>
        <textarea style='width: 100%; min-height: 500px;' id='result' readonly></textarea>
    </div>
</body>
</html>