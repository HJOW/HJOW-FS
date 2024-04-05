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
 * Modern FS page with React
 */

class FSContext extends FSBasic {
    pages = {}
    state = {
        logined : false,
        idtype : 'G',
        nick : 'Guest',
        noanonymous : false
    }
    constructor() { super(); }
    forceUpdate() {
        const selfs = this;
        return new Promise((resolve, reject) => {
            const res = resolve;
            let promises = [];
            for(let key in selfs.pages) {
                if(selfs.pages[key]) {
                    promises.push(new Promise((resolve, reject) => {
                        selfs.pages[key].forceUpdate(() => { resolve(); });
                    }));
                }
            }
            Promise.all(promises).then(() => { res(); });
        });
    }
}
const FSCTX = new FSContext();
 
class FSRoot extends React.Component {
    state = {
        path : '',
        excepts : '',
        allsearching : false,
        allsclist : [],
        files : [],
        success : false,
        privilege : 'view',
        session : {}
    }
    constructor(props) {
        super(props);
        FSCTX.pages['root'] = this;
    }
    componentDidMount() {
        this.refresh();
    }
    refresh() {
        const selfs = this;
        return new Promise((resolve, reject) => {
            // $('.fs_root').find('.img_icon').remove();
            selfs.trans().then((data) => { selfs.reloads(data).then(() => { selfs.applyFSChanges(); resolve(data); }); });
        });
    }
    trans() {
        FSUtil.setContextPath(FSCTX.ctxPath);
        return FSUtil.ajaxx({
            data   : $('#form_fs').serialize(),
            method : "POST",
            dataType : "json"
        });
    }
    reloads(data) {
        const selfs = this;
        return new Promise((resolve, reject) => {
            selfs.state.path      = data.path;
            selfs.state.files     = FSUtil.concatArray(data.directories, data.files);
            selfs.state.success   = data.success;
            selfs.state.privilege = data.privilege;
            selfs.state.session   = data.session;
    
            if(selfs.state.path != '') {
                let newArr = [];
                newArr.push({
                    type : 'ctrl',
                    name : '[뒤로 가기]',
                    action : 'back'
                });
                selfs.state.files = FSUtil.concatArray(newArr, selfs.state.files);
            }
    
            console.log(selfs.state);
            selfs.forceUpdate(() => { resolve(); });
        });
    }
    applyFSChanges() {
        const selfs = this;
        this.fIconize();
        FSUtil.applyLanguage();
        if(this.state.privilege == 'edit') {
            FSUtil.applyDragAndDrop($('.fs_root'), FSCTX.ctxPath, selfs.state.path);
        } else {
            $('.fs_root').find('.filednd').each(function() {
                var area = $(this);
                area.off('drop');
                area.off('dragover');
                area.off('dragenter');
                area.off('dragleave');
                area.removeClass('filedndin');
            });
        }
    }
    onClickCtrl(action) {
        const selfs = this;
        if(action.action == 'back') {
            const lists = this.state.path.split('/');
            let   newPath = '';
            for(let ldx = 0; ldx < lists.length - 1; ldx++) {
                if(ldx >= 1) newPath += '/';
                newPath += lists[ldx];
            }
    
            this.state.path = newPath;
            this.setState({ path : newPath }, () => { selfs.refresh(); });
        }
    }
    onClickDir(dir) {
        const selfs = this;
        this.setState({
            path : selfs.state.path + '/' + dir.name
        }, () => {
            console.log('reload');
            selfs.refresh();
        });
    }
    onClickFile(file) {
        let theme = '';
        if($('body').is('.dark')) theme='dark';
        if(FSCTX.useCaptchaDown) {
            const dia = document.getElementById('fs_pop_captdown');
            dia.style = "width : " + (FSCTX.captSizes.width + 300) + "px; height : " + (FSCTX.captSizes.height + 320) + "px; position: fixed; top: 100px; left : 100px;";
            const iframes = dia.getElementsByTagName('iframe')[0];
            iframes.src = FSCTX.ctxPath + '/jsp/fs/fscaptdown.jsp?popin=true&theme=' + theme + '&path=' + encodeURIComponent(this.state.path) + "&filename=" + encodeURIComponent(file.name) + FSUtil.addTokenParameterString();
            iframes.style = "width: 100%; overflow-y: hidden; height : " + (FSCTX.captSizes.height + 320 - 90) + "px";
            
            dia.show();
            $(dia).draggable();
            $(dia).resizable({
                resize : function(e, ui) {
                    $(this).find('iframe').width( ui.size.width  - 10);
                    $(this).find('iframe').height(ui.size.height - 20);
                }
            });
        } else {
            location.href = FSCTX.ctxPath + '/jsp/fs/' + 'fsdown.jsp?path=' + encodeURIComponent(this.state.path) + "&filename=" + encodeURIComponent(file.name);
        }
    }
    onClickDelete(file) {
        const selfs = this;
        if(file.type == 'dir') {
            let confirmMsg = 'Really? Do you want to delete this directory?';
            if(FSUtil.detectLanguage() == 'ko') confirmMsg = '이 폴더를 정말 삭제하시겠습니까?';
            
            if(confirm(confirmMsg)) {
                FSUtil.ajax({
                    data : {
                        path : file.value,
                        dels : 'dir',
                        praction : 'remove'
                    },
                    method : 'POST',
                    dataType : 'JSON',
                    success : function(data) {
                        if(! data.success) alert(data.message);
                        selfs.refresh();
                    }
                });
            }
        } else {
            let confirmMsg = 'Really? Do you want to delete this file?';
            if(FSUtil.detectLanguage() == 'ko') confirmMsg = '이 파일을 정말 삭제하시겠습니까?';
            
            if(confirm(confirmMsg)) {
                FSUtil.ajax({
                    data : {
                        path : selfs.state.path,
                        name : file.name,
                        dels : file.type,
                        praction : 'remove'
                    },
                    method : 'POST',
                    dataType : 'JSON',
                    success : function(data) {
                        if(! data.success) alert(data.message);
                        selfs.refresh();
                    }
                });
            }
        }
    }
    onClickConsole() {
        const dia = document.getElementById('fs_pop_console');

        let theme = '';
        if($('body').is('.dark')) theme='dark';

        let popularWidth, popularHeight;
        popularWidth  = window.innerWidth  - 120;
        popularHeight = 550;
        if(popularWidth  < 780) popularWidth  = 780;
        if(popularHeight < 550) popularHeight = 550;

        dia.style = "width : " + popularWidth + "px; height : " + popularHeight + "px; position: fixed; top: 100px; left : 100px; overflow: hidden";
        const iframes = dia.getElementsByTagName('iframe')[0];
        iframes.src = FSCTX.ctxPath + '/jsp/fs/fsconsolepop.jsp?popin=true&theme=' + theme + FSUtil.addTokenParameterString();
        iframes.style = "width: 100%; overflow-y: scroll; height : " + (popularHeight - 70) + "px";
        
        $(iframes).on('load', function() {
            const ct = $(this).contents();
            ct.find('.tf_terminal_console').focus();

            setTimeout(() => {
                ct.find('.mainelement').height($(iframes).height() - 140);
            }, 1000);
        });
        $('.ui-dialog-titlebar-close').text('X');

        dia.show();
        $(dia).draggable();
        $(dia).resizable({
            resize : function(e, ui) {
                $(this).find('iframe').width( ui.size.width  - 10);
                $(this).find('iframe').height(ui.size.height - 20);
                const ct = $(this).find('iframe').contents();
                ct.find('.mainelement').height(ui.size.height - 20 - 140);
            }
        });
    }
    onClickClassic() {
        let popularWidth, popularHeight;
        popularWidth  = window.innerWidth  - 120;
        popularHeight = window.innerHeight - 150;
        if(popularWidth  < 780) popularWidth  = 780;
        if(popularHeight < 550) popularHeight = 550;
        window.open( FSUtil.ctx + '/jsp/fs/fsx.jsp', 'FSClassic', 'width=' + popularWidth + ',height=' + popularHeight + ',toolbar=yes,status=no,location=no,resizable=yes' );
    }
    onClickNewDir() {
        const selfs = this;

        let msg = 'Please input the name for new folder. (No dot, /, quotes, <>, ?, &)';
        if(FSUtil.detectLanguage() == 'ko') msg = '생성할 폴더 이름을 입력해 주세요. (마침표, /, 따옴표, <>, ?, & 를 넣을 수 없습니다.)';
        
        let dirName = prompt(msg, '');
        
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
        
        FSUtil.ajax({
            data : {
                path : selfs.state.path,
                name : dirName,
                praction : 'mkdir'
            },
            method : 'POST',
            dataType : 'JSON',
            success : function(data) {
                if(! data.success) alert(data.message);
                selfs.refresh();
            }
        });
    }
    onClickConfig() {
        const dia = document.getElementById('fs_pop_admin');

        let theme = '';
        if($('body').is('.dark')) theme='dark';

        let popularWidth, popularHeight;
        popularWidth  = window.innerWidth  - 120;
        popularHeight = 550;
        if(popularWidth  < 780) popularWidth  = 780;
        if(popularHeight < 550) popularHeight = 550;

        dia.style = "width : " + popularWidth + "px; height : " + popularHeight + "px; position: fixed; top: 100px; left : 100px; overflow: hidden";
        const iframes = dia.getElementsByTagName('iframe')[0];
        iframes.src = FSCTX.ctxPath + '/jsp/fs/fsadmin.jsp?popin=true&theme=' + theme + FSUtil.addTokenParameterString();
        iframes.style = "width: 100%; overflow-y: scroll; height : " + (popularHeight - 70) + "px";
        
        $(iframes).on('load', function() {
            var ct = $(this).contents();
            ct.find('.tf_terminal_console').focus();
        });
        $('.ui-dialog-titlebar-close').text('X');

        dia.show();
        $(dia).draggable();
        $(dia).resizable({
            resize : function(e, ui) {
                $(this).find('iframe').width( ui.size.width  - 10);
                $(this).find('iframe').height(ui.size.height - 20);
            }
        });
    }
    onClickAllSrchRes(fileOne) {
        const selfs = this;
        const keyw   = document.getElementById('inp_allsearch').value;
        const splits = fileOne.split('/');
        let   newPath = '';

        for(let ldx = 0; ldx < splits.length - 1; ldx++) {
            if(ldx != 0) newPath += '/';
            newPath += splits[ldx];
        }

        document.getElementById('inp_allsearch').value = '';
        this.setState({
            path : newPath,
            allsearching : false,
            allsclist : []
        }, () => {
            document.getElementById('inp_search').value = keyw;
            selfs.refresh();
        });
    }
    fIconize() {
        const selfs = this;
        const fsRoot = $('.fs_root');
        const form     = fsRoot.find('.form_fs');
        const tables   = fsRoot.find('.fs_table_list');
        const listRoot = tables.find('.fs_list');
        const pathDisp = fsRoot.find('.path');
        listRoot.find('.tr_dir.no_icon').each(function() {
            const tdIcon = $(this).find('.td_mark_dir');
            const imgTag = tdIcon.find('img');
            imgTag.attr('src', FSUtil.ctx + '/css/images/dir.ico');
            imgTag.attr('alt', 'Directory');
        });
        listRoot.find('.tr_file.no_icon').each(function() {
            const tdIcon = $(this).find('.td_mark_file');
            const imgTag = tdIcon.find('img');
            imgTag.find('img').attr('src', FSCTX.ctxPath + '/css/images/files.png');
            imgTag.find('img').attr('alt', 'File');
        });
        
        if(! FSCTX.useIcon) return;
        
        let iconizeWorkSize = 10;
        let iconizeIndex = 0;
        let iconizeArray = [];
        let breaks = false;
        let bkColor = {r : 255, g : 255, b : 255};
        if($('body').is('.dark'))   bkColor = {r : 59, g : 59, b : 59};
        else if(fsRoot.is('.dark')) bkColor = {r : 59, g : 59, b : 59};
        
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
                let filelist = '';
                let fdx=0;
                for(fdx=0; fdx<iconizeArray.length; fdx++) {
                    if(filelist != '') filelist += ',';
                    filelist += iconizeArray[fdx];
                }
                let workingArray = iconizeArray;
                iconizeArray = [];
                FSUtil.ajax({
                    data : { path : selfs.state.path, files : filelist, br : bkColor.r, bg : bkColor.g, bb : bkColor.b, praction : 'fileicon' },
                    method : 'POST',
                    dataType : 'json',
                    success : function(data) {
                        if(data.success) {
                            // Apply received icon
                            $.each(data.data, function(dFileName, dImage) {
                                listRoot.find('.tr_file.no_icon').each(function() {
                                    if(! $(this).is('.no_icon')) return;
                                    
                                    const inFileNm = $(this).find('a.link_file').attr('data-name');
                                    if(inFileNm != dFileName) return;
                                    
                                    const inTdIcon = $(this).find('.td_mark_file');
                                    const imgTagIn = inTdIcon.find('img');
                                    imgTagIn.find('img').attr('src', dImage);
                                    imgTagIn.find('img').attr('alt', 'File');
                                    
                                    $(this).removeClass('no_icon');
                                    $(this).addClass('icon');
                                });
                            });
                        }
                    }, complete : function() {
                        selfs.fIconize();
                    }
                });
                breaks = true;
            }
        });
    }
    allsearch() {
        const selfs = this;
        const keyword = document.getElementById('inp_allsearch').value;

        if(FSUtil.isEmpty(keyword)) {
            selfs.setState({
                allsearching : false,
                allsclist : []
            }, () => {
                selfs.refresh();
            });
        } else {
            this.setState({
                allsearching : true,
                allsclist : ['...'],
                files : []
            }, () => {
                FSUtil.ajaxx({
                    data   : $('#form_allsearch').serialize(),
                    method : "POST",
                    dataType : "json"
                }).then((data) => {
                    if(data.list.length <= 0) {
                        selfs.setState({
                            allsearching : true,
                            allsclist : ['[EMPTY]']
                        });
                    } else {
                        selfs.setState({
                            allsearching : true,
                            allsclist : data.list
                        });
                    }
                });
            });
        }
        return false;
    }
    render() {
        const selfs = this;
        return (
            <div>
                <div className='fs_filelist fs_filelist_view container show-grid full'>
                    <div className='row fs_title'>
                        <div className='col-sm-8'>
                            <h2 className='fs_title'></h2>
                        </div>
                        <div className='col-sm-4 fs_allsearch align_right' style={{height: '60px'}}>
                            <form className='form_allsearch' id='form_allsearch' onSubmit={(e) => { e.preventDefault(); selfs.allsearch(); return false; }}>
                                <input type='hidden' name='path' className='hidden_path' value='' />
                                <input type='hidden' name='all'  className='hidden_conf' value='true' />
                                <input type='hidden' name='praction' value='listall' />
                                <input type='text'   className='inp_allsearch lang_attr_element reloading_readonly form-control' id='inp_allsearch' name='keyword' placeholder="전체 디렉토리 검색" data-lang-target='placeholder' data-lang-en='Search whole directories' style={{ width: '200px' }}/>
                                <input type='submit' className='btn_allsearch lang_attr_element reloading_disabled btnx btn btn-default' value='검색' data-lang-target='value' data-lang-en='Search'/>
                            </form>
                        </div>
                    </div>
                    <form className='form_fs' id='form_fs' onSubmit={(e) => {
                        e.preventDefault();
                        selfs.refresh();
                        return false;
                    }}>
                        <input type='hidden' name='path' className='hidden_path' value={this.state.path} />
                        <input type='hidden' name='excepts' className='hidden_excepts' value={this.state.excepts} />
                        <input type='hidden' name='praction' value='list' />
                        <div className='row fs_directory'>
                            <div className='col-sm-9'>
                                <h4 className='path_title'>
                                    <span className='lang_element' data-lang-en='Current Directory : '>현재 디렉토리 : </span><span className='path'>{'/' + selfs.state.path}</span>
                                </h4>
                            </div>
                            <div className='col-sm-3 align_right'>
                                {
                                    selfs.state.privilege == 'edit' ? (
                                        <span>
                                            <input type='button' className='btn_mkdir  btnx btn btn-default privilege_element lang_attr_element' value='새 폴더' data-lang-target='value' data-lang-en='New Folder' onClick={() => { selfs.onClickNewDir(); }}/>
                                        </span>
                                    ) : null
                                }
                                {
                                    FSCTX.state.idtype == 'A' ? (
                                        <input type='button' className='btn_config btnx btn btn-default privilege_element only_admin lang_attr_element' value='설정' data-lang-target='value' data-lang-en='Config' onClick={() => { selfs.onClickConfig(); }}/>
                                    ) : null
                                }
                                <input type='button' className='btn_console btnx btn btn-default lang_attr_element' value='콘솔' data-lang-target='value' data-lang-en='Console' accessKey="t" onClick={() => { selfs.onClickConsole(); }}/>
                                <input type='button' className='btn_classic btnx btn btn-default lang_attr_element' value='클래식' data-lang-target='value' data-lang-en='Classic' onClick={() => { selfs.onClickClassic(); }}/>
                            </div>
                        </div>
                        {
                            selfs.state.allsearching ? (
                                <div>
                                    <div className='row fs_allsrchcncl'>
                                        <div className='col-sm-12'>
                                            <input type='button' className='btn_cancel_allsearch btnx btn btn-default lang_attr_element full' id='btn_cancel_allsearch' value='전체 검색 종료' data-lang-target='value' data-lang-en='End All-Searching' onClick={() => {
                                                document.getElementById('inp_allsearch'       ).value = '';
                                                document.getElementById('btn_cancel_allsearch').value = '';
                                                selfs.allsearch();
                                                return false;
                                            }}/>
                                        </div>
                                    </div>
                                    <div className='row fs_root'>
                                        <div className='col-sm-12'>
                                            <table className="table table-hover full fs_table_list">
                                                <colgroup>
                                                    <col/>
                                                </colgroup>
                                                <tbody className='fs_list'>
                                                    {
                                                        selfs.state.allsclist.map((fileOne, index) => {
                                                            return (
                                                                <tr key={index} className={"element tr_file tr_file_" + index}>
                                                                    <td className="td_allsearch ellipsis">
                                                                        {
                                                                            fileOne == '[EMPTY]' ? (
                                                                                '' + fileOne
                                                                            ) : (
                                                                                <a href='#' data-file={fileOne} onClick={() => { selfs.onClickAllSrchRes(fileOne); }}>{fileOne}</a>
                                                                            )
                                                                        }
                                                                    </td>
                                                                </tr>
                                                            )
                                                        })
                                                    }
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            ) : (
                                <div>
                                    <div className='row fs_search'>
                                        <div className='col-sm-10'>
                                            <input type='text' className='inp_search full lang_attr_element reloading_readonly form-control' id='inp_search' name='keyword' placeholder="디렉토리 내 검색" data-lang-target='placeholder' data-lang-en='Search in current directory' />
                                        </div>
                                        <div className='col-sm-2'>
                                            <input type='submit' className='btn_search full lang_attr_element reloading_disabled btnx btn btn-default' value='검색' data-lang-target='value' data-lang-en='Search' />
                                        </div>
                                    </div>
                                    <div className='row fs_root'>
                                        <div className='col-sm-12'>
                                            <table className="table table-hover full fs_table_list">
                                                <colgroup>
                                                    <col style={{width: '50px'}} />
                                                    <col />
                                                    <col style={{width: '100px'}} />
                                                    <col style={{width: '80px'}} className='col_controls' />
                                                </colgroup>
                                                <tbody className='fs_list'>
                                                    {
                                                        selfs.state.files.map((fileOne, index) => {
                                                            if(fileOne.type == 'dir') {
                                                                return (
                                                                    <tr key={index} className={"element tr_dir tr_dir_" + index + " no_icon"}>
                                                                        <td className='td_mark_dir'><img style={{width: '20px', height: '20px'}} className='img_icon' src={FSUtil.ctx + '/css/images/dir.ico'}/></td>
                                                                        <td colSpan="2"><a href='#' className='link_dir ellipsi binded_click' data-path={fileOne.name} onClick={() => { this.onClickDir(fileOne); }}>{fileOne.name}</a></td>
                                                                        <td className='td_buttons'>
                                                                            {
                                                                                (fileOne.elements <= 0 && selfs.state.privilege == 'edit') ? (
                                                                                    <input type='button' className='btn_delete btnx btn btn-default' value='X' onClick={ () => { selfs.onClickDelete(fileOne); } }/>
                                                                                ) : null
                                                                            }
                                                                        </td>
                                                                    </tr>
                                                                )
                                                            } else if(fileOne.type == 'ctrl') {
                                                                return (
                                                                    <tr key={index} className={"element element_special back"}>
                                                                        <td colSpan='4'><a href='#' className='link_back lang_element' data-lang-en='[BACK]' onClick={() => { this.onClickCtrl(fileOne); }}>{fileOne.name}</a></td>
                                                                    </tr>
                                                                )
                                                            } else {
                                                                return (
                                                                    <tr key={index} className='element tr_file no_icon'>
                                                                        <td className='td_mark_file'><img style={{width: '20px', height: '20px'}} className='img_icon' src={FSUtil.ctx + '/css/images/files.png'}/></td>
                                                                        <td className="filednd">
                                                                            <div className="div_td_file_a">
                                                                                <a href='#' className="link_file" data-path={ this.state.path } data-name={fileOne.name} onClick={() => { this.onClickFile(fileOne); }}>{fileOne.name}</a>
                                                                            </div>
                                                                            {
                                                                                fileOne.previewing ? (
                                                                                    <div className='div_td_file_preview full'>
                                                                                        {
                                                                                            fileOne.previewType == '1' ? (
                                                                                                <img    className='img_preview    preview_element full' style={{'maxHeight' : (((window.innerHeight / 2) < 200 ? 200 : (window.innerHeight / 2)) + 'px'), 'overflow-y' : 'scroll'}} src={ FSUtil.ctx + "/jsp/fs/fsdown.jsp?path=" + encodeURIComponent(selfs.state.path) + "&filename=" + encodeURIComponent(fileOne.name) + "&mode=VIEW" + FSUtil.addTokenParameterString() }/>
                                                                                            ) : fileOne.previewType == '2' ? (
                                                                                                <video  className='video_preview  preview_element full' style={{'maxHeight' : (((window.innerHeight / 2) < 200 ? 200 : (window.innerHeight / 2)) + 'px'), 'overflow-y' : 'scroll'}} src={ FSUtil.ctx + "/jsp/fs/fsdown.jsp?path=" + encodeURIComponent(selfs.state.path) + "&filename=" + encodeURIComponent(fileOne.name) + "&mode=VIEW" + FSUtil.addTokenParameterString() }/>
                                                                                            ) : fileOne.previewType == '3' ? (
                                                                                                <audio  className='audio_preview  preview_element full' style={{'maxHeight' : (((window.innerHeight / 2) < 200 ? 200 : (window.innerHeight / 2)) + 'px'), 'overflow-y' : 'scroll'}} src={ FSUtil.ctx + "/jsp/fs/fsdown.jsp?path=" + encodeURIComponent(selfs.state.path) + "&filename=" + encodeURIComponent(fileOne.name) + "&mode=VIEW" + FSUtil.addTokenParameterString() }/>
                                                                                            ) : fileOne.previewType == '4' ? (
                                                                                                <iframe className='iframe_preview preview_element full' style={{'maxHeight' : (((window.innerHeight / 2) < 200 ? 200 : (window.innerHeight / 2)) + 'px'), 'overflow-y' : 'scroll'}} src={ FSUtil.ctx + "/jsp/fs/fsdown.jsp?path=" + encodeURIComponent(selfs.state.path) + "&filename=" + encodeURIComponent(fileOne.name) + "&mode=VIEW" + FSUtil.addTokenParameterString() }></iframe>
                                                                                            ) : null
                                                                                        }
                                                                                    </div>
                                                                                ) : null
                                                                            }
                                                                        </td>
                                                                        <td className='td_file_size filednd' style={{textAlign: 'right'}}>{fileOne.size}</td>
                                                                        <td className='td_buttons' style={{textAlign: 'right'}}>
                                                                            <span>
                                                                            {
                                                                                (fileOne.previewType >= 0 && (! fileOne.over_prev)) ? (
                                                                                    fileOne.previewing ? (
                                                                                        <input type='button' className='btn_preview btnx btn btn-default not_opened' value='▲' onClick={ () => { fileOne.previewing = false; selfs.forceUpdate(); }}/>
                                                                                    ) : (
                                                                                        <input type='button' className='btn_preview btnx btn btn-default not_opened' value='▼' onClick={ () => { fileOne.previewing = true; selfs.forceUpdate(); }}/>
                                                                                    )
                                                                                ) : null
                                                                            }
                                                                            </span>
                                                                            <span>
                                                                            {
                                                                                (selfs.state.privilege == 'edit') ? (
                                                                                    <input type='button' className='btn_delete btnx btn btn-default' value='X' onClick={ () => { selfs.onClickDelete(fileOne); } }/>
                                                                                ) : null
                                                                            }
                                                                            </span>
                                                                        </td>
                                                                    </tr>
                                                                )
                                                            }
                                                        })
                                                    }
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            )
                        }
                    </form>
                </div>
                <div className='fs_filelist fs_filelist_anonymous full invisible'>
                    
                </div>
                <div className='fs_hform invisible_wh'>
                    <form className='form_hidden' target='_blank'></form>
                </div>
                <div className='fs_pops invisible_wh'>
                    <dialog className='fs_pop_captdown fs_pop_in full' id='fs_pop_captdown'><div className='div_dialog_header'><span className='span_dialog_title'>Dialog</span><button className="btn_dialog_close" onClick={() => { document.getElementById('fs_pop_captdown').close(); }}>X</button></div><iframe></iframe></dialog>
                    <dialog className='fs_pop_upload   fs_pop_in full' id='fs_pop_upload'  ><div className='div_dialog_header'><span className='span_dialog_title'>Dialog</span><button className="btn_dialog_close" onClick={() => { document.getElementById('fs_pop_upload').close();   }}>X</button></div><iframe></iframe></dialog>
                    <dialog className='fs_pop_console  fs_pop_in full' id='fs_pop_console' ><div className='div_dialog_header'><span className='span_dialog_title'>Dialog</span><button className="btn_dialog_close" onClick={() => { document.getElementById('fs_pop_console').close();  }}>X</button></div><iframe></iframe></dialog>
                    <dialog className='fs_pop_admin    fs_pop_in full' id='fs_pop_admin'   ><div className='div_dialog_header'><span className='span_dialog_title'>Dialog</span><button className="btn_dialog_close" onClick={() => { document.getElementById('fs_pop_admin').close();    }}>X</button></div><iframe></iframe></dialog>
                </div>
            </div>
        )
    }
}

class FSAccountBar extends React.Component {
    state = {
        logined : false,
        idtype : 'G',
        nick : 'Guest',
        noanonymous : false,
        req : 'status'
    }
    constructor(props) {
        super(props);
        FSCTX.pages['account'] = this;
    }
    componentDidMount() {
        this.trans('status').then((data) => { this.reloads(data, true); });
    }
    trans(req) {
        FSUtil.setContextPath(FSCTX.ctxPath);
        return FSUtil.ajaxx({
            data   : $('#form_fs_login').serialize() + '&req=' + req,
            method : "POST",
            dataType : "json",
        });
    }
    reloads(data, alerts) {
        const selfs = this;
        return new Promise((resolve, reject) => {
            if(alerts && (! data.success)) { alert(data.message); location.reload(); resolve(); return; };
            if(data.needrefresh) { location.reload(); resolve(); return; }
            if(data.token) {
                if(FSUtil.detectStorage()) {
                    FSUtil.storage.session.put("fsid"   , data.id   );
                    FSUtil.storage.session.put("fstoken", data.token);
                }
            }
    
            selfs.state.noanonymous = data.noanonymous;
            selfs.state.logined     = data.logined;
            if(selfs.state.logined) {
                selfs.state.idtype  = data.idtype;
                selfs.state.nick    = data.nick;
            }
            FSCTX.state = selfs.state;
            FSCTX.forceUpdate(() => { resolve(); });
        });
    }
    async refresh(req, alerts) {
        const res = await this.trans(req);
        await this.reloads(res, alerts);
        return res;
    }
    async login() {
        await this.refresh('login', true);
        document.getElementById('form_fs').submit();
    }
    async logout() {
        await this.refresh('logout', true);
        if(FSUtil.detectStorage()) {
            FSUtil.storage.session.remove("fsid"   );
            FSUtil.storage.session.remove("fstoken");
        }
        document.getElementById('form_fs').submit();
    }
    render() { 
        const selfs = this;
        return (
            <div>
                <div className='container valign_middle full'>
                    <form onSubmit={() => { selfs.login(); return false; }} className='form_fs_login' id='form_fs_login' method='POST'>
                    <input type='hidden' name='praction' value='account'/>
                    {
                        this.state.logined ? 
                            (
                                <div className='row login_element logined padding_top_10'>
                                    <div className='col-sm-12'>
                                        <span className='lang_element' data-lang-en='Welcome, '></span><span className='span_type'></span> <span className='span_nick'>{this.state.nick}</span><span className='lang_element' data-lang-en=''> 님 환영합니다.</span> 
                                        <input type='button' value='로그아웃' className='btn_logout btnx btn btn-default lang_attr_element' data-lang-target='value' data-lang-en='LOGOUT' onClick={() => { selfs.logout(); }}/>
                                    </div>
                                </div>                                 
                            )
                        :
                            (
                                <div className='row login_element not_logined padding_top_10'>
                                    <div className='container show-grid d_inline_block valign_middle' style={{ width: '320px', height: '80px' }}>
                                        <div className='row'>
                                            <div className='col-xs-12'>
                                                <span style={{display: 'inline-block', width: '80px'}}>ID</span><input type='text' name='id' className='inp_login_element form-control' style={{display: 'inline-block', width: '150px'}}/>
                                            </div>
                                        </div>
                                        <div className='row'>
                                            <div className='col-xs-12'>
                                                <span style={{display: 'inline-block', width: '80px'}} className='lang_element' data-lang-en='Password'>암호</span><input type='password' name='pw' className='inp_login_element form-control' style={{display: 'inline-block', width: '150px'}}/>
                                            </div>
                                        </div>
                                    </div>
                                    <div className='div_captcha_login d_inline_block valign_middle' style={{ width: (FSCTX.captSizes.width + 10) + 'px', height: '60px', paddingTop: '5px' }}>
                                        <iframe className='if_captcha_l valign_middle' style={{width: (FSCTX.captSizes.width + 5) + 'px', height : (FSCTX.captSizes.height + 5) + 'px', border: 0}} src={FSCTX.ctxPath + '/jsp/fs/fscaptin.jsp?key=fsl&scale=1&randomize=true&theme=' + FSCTX.getTheme() + FSUtil.addTokenParameterString()}></iframe>
                                    </div>
                                    <div className='div_captcha_login d_inline_block valign_middle padding_top_10' style={{'marginLeft': '10px', height : '60px', 'textAlign' : 'left'}}>
                                        <input type='text' className='inp_captcha_l inp_login_element lang_attr_element valign_middle form-control' name='captcha' placeholder='옆의 코드 입력' data-lang-target='placeholder' data-lang-en='Input the code left'/>
                                    </div>
                                    <div className='d_inline_block valign_middle' style={{width: '100px', height : '60px'}}>
                                        <input type='submit' value='로그인' className='lang_attr_element btnx btn btn-default' data-lang-target='value' data-lang-en='LOGIN' style={{height : '50px'}}/>
                                    </div>
                                </div>  
                            )
                    }
                    </form>
                </div>
            </div>
        )
    }
}