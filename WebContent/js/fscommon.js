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
    this.isEmpty = function isEmpty(obj) {
        if(obj == null) return true;
        if(typeof(obj) == 'undefined') return true;
        if($.isArray(obj)) return (obj.length == 0);
        
        var str = String(obj).trim();
        if(str == '') return true;
        return false;
    }
    
    this.map = function(patternedString, parameterJson) {
        
    }
}

var FSUtil = new FSUtilClass();