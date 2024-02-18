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
    
    this.detectDark = function detectDark() {
    	try {
    		return eval("window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches");
    	} catch(e) {}
    	return false;
    }
    
    this.detectLanguage = function detectLanguage() {
        // return 'en';
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
    }
}

var FSUtil = new FSUtilClass();