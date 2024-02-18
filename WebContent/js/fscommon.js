/**
 * File Storage Common Scripts
 */
 
/**
   Polyfills for classic browsers
*/ 

/* https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Global_Objects/String/startsWith */
if(!String.prototype.startsWith) {
    String.prototype.startsWith = function(search, pos) {
        return this.substr(!pos || pos < 0 ? 0 : +pos, search.length) === search;
    };
}

/* https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Global_Objects/String/endsWith */
if(!String.prototype.endsWith) {
    String.prototype.endsWith = function(searchString, position){
        var subjectString = this.toString();
        if(typeof position !== 'number' || !isFinite(position) || Math.floor(position) !== position || position > subjectString.length) {
            position = subjectString.length;
        }
        position -= searchString.length;
        var lastIndex = subjectString.indexOf(searchString, position);
        return lastIndex !== -1 && lastIndex === position;
    };
}

/* https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Global_Objects/String/trim */
if(!String.prototype.trim) {
    String.prototype.trim = function() {
        return this.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, '');
    };
}

/* https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Global_Objects/Array/isArray */
if (!Array.isArray) {
    Array.isArray = function(arg) {
        return Object.prototype.toString.call(arg) === '[object Array]';
    };
}

/**
   Common Utils

*/
 
function FSUtilClass() {
	this.log = function(logContent) {
    	try {
    		try {
        		console.log(logContent);
        	} catch(e) { window.console.log(logContent); }
    	} catch(e) {}
	}
	
    this.isEmpty = function isEmpty(obj) {
        if(obj == null) return true;
        if(typeof(obj) == 'undefined') return true;
        if($.isArray(obj)) return (obj.length == 0);
        
        var str = String(obj).trim();
        if(str == '') return true;
        return false;
    }
    
    this.replace = function replace(originalString, targetString, replacement) {
    	return String(originalString).split(targetString).join(replacement);
    }
    
    this.map = function map(patternedString, parameterJson) {
    	if(parameterJson == null || typeof(parameterJson) == 'undefined') return patternedString;
    	if(typeof(parameterJson) == 'string') parameterJson = JSON.parse(parameterJson);
    	var res = String(patternedString);
        $.each(parameterJson, function(k, v) {
        	res = FSUtil.replace(res, '[%' + k + '%]', v);
        });
        return res;
    }
    
    this.openPopupHtml = function openPopupHtml(htmls, title, spec) {
        if(spec  == null || typeof(spec ) == 'undefined') spec  = "scrollbars=no,status=no,location=no,toolbar=no";
        if(title == null || typeof(title) == 'undefined') title = "pop_" + Math.random();
        
        var pop = window.open("", String(title), spec);
        FSUtil.log(htmls);
        pop.document.write(String(htmls));
        return pop;
    }
    
    this.detectDark = function detectDark() {
    	try {
    		return eval("window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches");
    	} catch(e) {}
    	return false;
    }
    
    this.detectLanguage = function detectLanguage() {
        if(typeof(window.navigator.language)  != 'undefined') return window.navigator.language;
        if(typeof(window.navigator.languages) != 'undefined') {
            for(var idx=0; idx<window.navigator.languages.length; idx++) {
                var langOne = window.navigator.languages[idx];
                if(langOne.length == 2) return langOne;
                if(langOne.length == 5) return langOne.substring(0, 2);
            }
        }
        return 'en';
    }
    
    this.applyLanguage = function applyLanguage(range) {
        if(typeof(range) == 'undefined') range = $('body');
        else range = $(range);
        
        var lang = FSUtil.detectLanguage();
        if(lang != 'ko') {
            range.find('.lang_element').each(function() {
                var elementOne = $(this);
                var attrEn  = elementOne.attr('data-lang-en');
                var attrLoc = elementOne.attr('data-lang-' + lang);
                
                if(     typeof(attrLoc) != 'undefined') elementOne.text(attrLoc);
                else if(typeof(attrEn ) != 'undefined') elementOne.text(attrEn);
            });
            
            range.find('.lang_attr_element').each(function() {
                var elementOne = $(this);
                var attrTar = elementOne.attr('data-lang-target');
                var attrEn  = elementOne.attr('data-lang-en');
                var attrLoc = elementOne.attr('data-lang-' + lang);
                
                if(typeof(attrTar) == 'undefined') return;
                
                if(     typeof(attrLoc) != 'undefined') elementOne.attr(attrTar, attrLoc);
                else if(typeof(attrEn ) != 'undefined') elementOne.attr(attrTar, attrEn);
            });
        }
        return lang;
    }
    
    this.applyDragAndDrop = function applyDragAndDrop(range, ctxPath, path) {
        if(typeof(range) == 'undefined') range = $('body');
        else range = $(range);
        
        var hiddenPlace = range.find('.fs_filedndacts');
        if(hiddenPlace == null || typeof(hiddenPlace) == 'undefined' || hiddenPlace.length <= 0) {
            range.append("<div class='fs_filedndacts'></div>");
            hiddenPlace = range.find('.fs_filedndacts');
        }
        
        range.find('.filednd').each(function() {
            var area = $(this);
            area.off('drop');
            area.off('dragover');
            area.off('dragenter');
            area.off('dragleave');
            area.removeClass('filedndin');
           
            area.on('drop', function(e) {
                e.preventDefault();
                var dataTrans = e.originalEvent.dataTransfer;
                if(typeof(dataTrans      ) == 'undefined') return;
                if(typeof(dataTrans.files) == 'undefined') return;
                
                var files = dataTrans.files;
                if(files.length <= 0) return;
                
                hiddenPlace.empty();
                
                var hiddFormHtml = "<form class='form_fs_dnd' action='" + ctxPath + "/jsp/fsuploadin.jsp" + "' method='POST' enctype='multipart/form-data'>";
                hiddFormHtml += "</form>";
                hiddenPlace.append(hiddFormHtml);
                
                var formObj = hiddenPlace.find('.form_fs_dnd');
                var formData = new FormData(formObj[0]);
                formData.append("path", path);
                
                for(var idx=0; idx<files.length; idx++) {
                    var fileOne = files[idx];
                    
                    formData.append('file' + idx, fileOne);
                }
                
                $.ajax({
                    url : ctxPath + '/jsp/fsuploadin.jsp',
                    data : formData,
                    method : 'POST',
                    enctype : 'multipart/form-data',
                    processData : false,
                    contentType : false,
                    loader : false,
                    cache : false,
                    dataType : 'html',
                    success : function(dataHtml) {
                        FSUtil.openPopupHtml(dataHtml, null, 'width=400,height=300,scrollbars=no,status=no,location=no,toolbar=no');
                        var formFList = $('.form_fs');
                        if(formFList.length >= 1) formFList.trigger('submit');
                    }, error : function(jqXHR, textStatus, errorThrown) {
                        alert('Error : ' + textStatus);
                    }
                });
            });
            
            area.on('dragover', function(e) {
                e.stopPropagation();
                e.preventDefault();
                $(this).addClass('filedndin');
            });
            
            area.on('dragenter', function(e) {
                e.stopPropagation();
                e.preventDefault();
                $(this).addClass('filedndin');
            });
            
            area.on('dragleave', function(e) {
                e.stopPropagation();
                e.preventDefault();
                $(this).removeClass('filedndin');
            });
        });
    }
}

var FSUtil = new FSUtilClass();