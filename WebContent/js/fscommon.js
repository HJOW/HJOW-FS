/**
 * File Storage Common Scripts
 */
 
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
    this.version = [];
    this.version.push(0);
    this.version.push(2);
    this.version.push(4);
    this.version.push(37);

    this.ctx = '';
    this.setContextPath = function(ctxPath) {
        this.ctx = ctxPath;
    }
    
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
        if(typeof(window.navigator.language)  != 'undefined') {
            var langOne = String(window.navigator.language);
            if(langOne.length == 2) return langOne;
            return langOne.substring(0, 2);
        }
        if(typeof(window.navigator.languages) != 'undefined') {
            for(var idx=0; idx<window.navigator.languages.length; idx++) {
                var langOne = window.navigator.languages[idx];
                if(langOne.length == 2) return langOne;
                if(langOne.length == 5) return langOne.substring(0, 2);
            }
        }
        return 'en';
    };

    this.parseFloatFirstBlock = function parseFloatFirstBlock(str) {
        try {
            var splits = String(str).split('.');
            if(splits.length <= 1) return parseInt(splits[0]);
            return parseFloat(splits[0] + '.' + splits[1]);
        } catch(e) {
            FSUtil.log('Error : ' + e);
            return -1;
        }
    };

    this.concatArray = function concatArray() {
        if(arguments == null) return [];
        if(typeof(arguments) == 'undefined') return [];
        if(arguments.length <= 0) return [];
        var newArr = [];
        for(var idx=0; idx<arguments.length; idx++) {
            var arrOne = arguments[idx];
            if($.isArray(arrOne)) {
                for(var ddx=0; ddx<arrOne.length; ddx++) {
                    if($.isArray(arrOne[ddx])) {
                        newArr = FSUtil.concatArray(newArr, arrOne[ddx]);
                    } else {
                        newArr.push(arrOne[ddx]);
                    }
                }
            } else {
                newArr.push(arrOne);
            }
        }
        return newArr;
    };
    
    function FSStorage(storKey) {
        this.storageKey = storKey;
        this.getRaw = function() {
            return eval(this.storageKey + 'Storage');
        }
        this.put = function(k, v) {
            this.getRaw().setItem(k, v);
        };
        this.get = function(k) {
            return this.getRaw().getItem(k);
        };
        this.remove = function(k) {
            this.getRaw().removeItem(k);
        };
        this.clear = function() {
            this.getRaw().clear();
        };
    };
    
    function FSStorageCollection() {
        this.local   = new FSStorage('local');
        this.session = new FSStorage('session');
        this.detect  = function detectStorage() {
            try {
                var locals = eval('localStorage');
                if(locals == null || typeof(locals) == 'undefined') {
                    return false;
                }
                if(typeof(locals.setItem) == 'undefined' || typeof(locals.getItem) == 'undefined' || typeof(locals.clear) == 'undefined' ) {
                    return false;
                }
                var sessions = eval('sessionStorage');
                if(sessions == null || typeof(sessions) == 'undefined') {
                    return false;
                }
                if(typeof(sessions.setItem) == 'undefined' || typeof(sessions.getItem) == 'undefined' || typeof(sessions.clear) == 'undefined' ) {
                    return false;
                }
                return true;
            } catch(e) { return false; }
        };
    };
    
    this.storage = new FSStorageCollection();
    this.detectStorage = function detectStorage() { return this.storage.detect() };
    
    this.detectBrowser = function detectBrowser() {
        var agent  = String(window.navigator.userAgent);
        var splits = agent.split(' ');
        var idx=0;
        
        // Detect Microsoft Internet Explorer
        for(idx=0; idx<splits.length; idx++) {
            var blockOne = splits[idx];
            var splitIn  = blockOne.split('/');
            if(splitIn.length != 2) continue;
            if(splitIn[0] == 'Trident') {
                var res = {};
                res.name = 'Microsoft Internet Explorer';
                try {
                    var version = Math.round(parseFloat(String(splitIn[1])));
                    version += 4;

                    res.nm      = 'ie';
                    res.ver     = version;
                    res.version = version + '';
                    
                    // Check Compatible Mode
                    var compatible = false;
                    var idx2 = 0;
                    for(idx2=0; idx2<splits.length; idx2++) {
                        var blockTwo = splits[idx2];
                        if(blockTwo.indexOf('compatible;') >= 0) { compatible = true; break; }
                    }
                    if(compatible) {
                        res.compatible = {};
                        res.compatible.name = res.name;
                        res.compatible.nm   = res.nm;
                        res.compatible.ver  = -1;
                        
                        var msieIdx = -1; 
                        for(idx2=0; idx2<splits.length; idx2++) {
                            var blockTwo = splits[idx2];
                            if(blockTwo.indexOf('MSIE') >= 0) { msieIdx = idx2; break; }
                        }
                        if(msieIdx >= 0 && splits.length > msieIdx + 1) {
                            var blockTwo = splits[msieIdx + 1];
                            res.compatible.ver = FSUtil.parseFloatFirstBlock(blockTwo);
                        }
                        res.compatible.version = res.compatible.ver + '';
                    } else {
                        res.compatible = null;
                    }
                    
                    res.agent = agent;
                    return res;
                } catch(e) {
                    return { name : res.name, nm : 'ie', version : 'Unknown', ver : -1, agent : agent };
                }
            }
        }
        
        // Detect Opera
        for(idx=0; idx<splits.length; idx++) {
            var blockOne = splits[idx];
            var splitIn  = blockOne.split('/');
            if(splitIn.length != 2) continue;
            if(splitIn[0] == 'OPR') return {
                name : 'Opera',
                nm   : 'opera',
                version  : splitIn[1],
                ver      : FSUtil.parseFloatFirstBlock(splitIn[1]),
                agent    : agent
            };
            if(splitIn[0] == 'opera') return {
                name : 'Opera',
                nm   : 'opera',
                version  : splitIn[1],
                ver      : FSUtil.parseFloatFirstBlock(splitIn[1]),
                agent    : agent
            };
        }
        
        // Detect Samsung Internet
        for(idx=0; idx<splits.length; idx++) {
            var blockOne = splits[idx];
            var splitIn  = blockOne.split('/');
            if(splitIn.length != 2) continue;
            if(splitIn[0] == 'SamsungBrowser') return {
                name : 'Samsung Browser',
                nm   : 'samsung',
                version  : splitIn[1],
                ver      : FSUtil.parseFloatFirstBlock(splitIn[1]),
                agent    : agent
            };
        }
        
        // Detect Brave
        for(idx=0; idx<splits.length; idx++) {
            var blockOne = splits[idx];
            var splitIn  = blockOne.split('/');
            if(splitIn.length != 2) continue;
            if(splitIn[0] == 'Brave') return {
                name : 'Brave',
                nm   : 'brave',
                version  : splitIn[1],
                ver      : FSUtil.parseFloatFirstBlock(splitIn[1]),
                agent    : agent
            };
        }
        
        // Detect Microsoft Edge
        for(idx=0; idx<splits.length; idx++) {
            var blockOne = splits[idx];
            var splitIn  = blockOne.split('/');
            if(splitIn.length != 2) continue;
            if(splitIn[0] == 'Edge') return {
                name : 'Microsoft Edge',
                nm   : 'edge',
                version  : splitIn[1],
                ver      : FSUtil.parseFloatFirstBlock(splitIn[1]),
                agent    : agent
            };
            if(splitIn[0] == 'Edg') return {
                name : 'Microsoft Edge',
                nm   : 'edge',
                version  : splitIn[1],
                ver      : FSUtil.parseFloatFirstBlock(splitIn[1]),
                agent    : agent
            };
        }
        
        // Detect Google Chrome
        for(idx=0; idx<splits.length; idx++) {
            var blockOne = splits[idx];
            var splitIn  = blockOne.split('/');
            if(splitIn.length != 2) continue;
            if(splitIn[0] == 'Chrome') return {
                name : 'Google Chrome',
                nm   : 'chrome',
                version  : splitIn[1],
                ver      : FSUtil.parseFloatFirstBlock(splitIn[1]),
                agent    : agent
            };
        }
        
        // Detect Safari
        for(idx=0; idx<splits.length; idx++) {
            var blockOne = splits[idx];
            var splitIn  = blockOne.split('/');
            if(splitIn.length != 2) continue;
            if(splitIn[0] == 'Safari') return {
                name : 'Apple Safari',
                nm   : 'safari',
                version  : splitIn[1],
                ver      : FSUtil.parseFloatFirstBlock(splitIn[1]),
                agent    : agent
            };
        }

        // Detect Mypal
        for(idx=0; idx<splits.length; idx++) {
            var blockOne = splits[idx];
            var splitIn  = blockOne.split('/');
            if(splitIn.length != 2) continue;
            if(splitIn[0] == 'Mypal') return {
                name : 'Mypal',
                nm   : 'mypal',
                version  : splitIn[1],
                ver      : FSUtil.parseFloatFirstBlock(splitIn[1]),
                agent    : agent
            };
        }
        
        // Detect Firefox
        for(idx=0; idx<splits.length; idx++) {
            var blockOne = splits[idx];
            var splitIn  = blockOne.split('/');
            if(splitIn.length != 2) continue;
            if(splitIn[0] == 'Firefox') return {
                name    : 'Mozilla Firefox',
                nm      : 'firefox',
                version : splitIn[1],
                ver     : FSUtil.parseFloatFirstBlock(splitIn[1]),
                agent   : agent
            };
        }
        
        return { name : 'Unknown', version : 'Unknown', ver : -1, agent : agent };
    };
    
    this.detectSupportES6 = function detectSupportES6() {
        var browser = FSUtil.detectBrowser();
        if(browser.nm != 'ie') {
            if(browser.nm == 'safari'  && browser.ver >= 10) return true;
            if(browser.nm == 'opera'   && browser.ver >= 38) return true;
            if(browser.nm == 'chrome'  && browser.ver >= 51) return true;
            if(browser.nm == 'edge'    && browser.ver >= 79) return true;
            if(browser.nm == 'samsung' && browser.ver >= 79) return true;
            if(browser.nm == 'mypal'   && browser.ver >= 68) return true;
            if(browser.ver >= 100) return true;
        }
        return false;
    };
    
    this.getDefaultAjaxUrl = function getDefaultAjaxUrl() {
        return this.ctx + "/jsp/fs/fsproc.jsp";
    };
    
    this.ajax = function ajax(ajaxParamJson) {
        var obj = ajaxParamJson;
        if(typeof(obj) == 'string') obj = JSON.parse(obj);
        
        if(this.storage.detect()) {
            var tkId  = this.storage.session.get('fsid'   );
            var tkVal = this.storage.session.get('fstoken');
            
            if(tkId != null && tkVal != null && typeof(tkId) != 'undefined' && typeof(tkVal) != 'undefined') {
                var beforFunc = null;
                if(typeof(obj.beforeSend) == 'function') beforFunc = obj.beforeSend;
                obj.beforeSend = function(xhr) {
                    xhr.setRequestHeader('fsid'   , tkId );
                    xhr.setRequestHeader('fstoken', tkVal);
                    if(beforFunc != null) beforFunc(xhr);
                }
            }
        }
        
        if(typeof(obj['url']) == 'undefined' || obj['url'] == null) obj.url = this.getDefaultAjaxUrl();
        
        $.ajax(obj);
    };

    this.ajaxx = function ajaxx(ajaxParamJson) {
        return new Promise(function(resolve, reject) {
            var newObj = {};
            $.each(ajaxParamJson, function(k, v) { newObj[k] = v; });

            var oldSuccess  = newObj.success;
            var oldError    = newObj.error;
            var oldComplete = newObj.complete;

            if(typeof(oldSuccess ) != 'function') { oldSuccess  = function(data) {}; }
            if(typeof(oldError   ) != 'function') { oldError    = function(jqXHR, textStatus, errorThrown) {}; }
            if(typeof(oldComplete) != 'function') { oldComplete = function() {}; }

            var resolved = false;

            newObj.success = function(data) {
                oldSuccess(data);
                if(resolved) return;
                resolved = true;
                resolve(data);
            };

            newObj.error = function(jqXHR, textStatus, errorThrown) {
                oldError(jqXHR, textStatus, errorThrown);
                if(resolved) return;
                resolved = true;
                reject('Error : ' + textStatus + '\n' + errorThrown);
            };

            newObj.complete = function() {
                if(resolved) return;
                reject('AJAX completed without response');
            };

            FSUtil.ajax(newObj);
        });
    };
    
    this.addTokenParameterString = function addTokenParameterString() {
        var tkId  = this.storage.session.get('fsid'   );
        var tkVal = this.storage.session.get('fstoken');
        
        if(tkId != null && tkVal != null && typeof(tkId) != 'undefined' && typeof(tkVal) != 'undefined') {
            return '&fstoken_id=' + encodeURIComponent(tkId) + '&fstoken_val=' + encodeURIComponent(tkVal);
        }
        return '';
    };
    
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
                
                var hiddFormHtml = "<form class='form_fs_dnd' action='" + ctxPath + "/jsp/fs/fsuploadin.jsp" + "' method='POST' enctype='multipart/form-data'>";
                hiddFormHtml += "</form>";
                hiddenPlace.append(hiddFormHtml);
                
                var formObj = hiddenPlace.find('.form_fs_dnd');
                var formData = new FormData(formObj[0]);
                formData.append("path", path);
                
                for(var idx=0; idx<files.length; idx++) {
                    var fileOne = files[idx];
                    
                    formData.append('file' + idx, fileOne);
                }
                
                FSUtil.ajax({
                    url : ctxPath + '/jsp/fs/fsuploadin.jsp',
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

var FSUtil  = new FSUtilClass();