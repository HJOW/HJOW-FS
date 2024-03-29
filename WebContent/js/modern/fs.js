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
 
class FSRoot extends React.Component {
    state = {
        path : '',
        excepts : '',
        dirs : [],
        files : [],
        success : false,
        privilege : [],
        session : {}
    }
    constructor(props) {
        super(props);
        console.log(props);
    }
    componentDidMount() {
        this.refresh();
    }
    refresh() {
        const selfs = this;
        return new Promise((resolve, reject) => {
            selfs.trans().then((data) => { selfs.reloads(data).then(() => { FSUtil.applyLanguage(); resolve(data); }); });
        });
    }
    trans() {
        FSUtil.setContextPath(this.props.basic.ctxPath);
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
            selfs.state.dirs      = data.directories;
            selfs.state.files     = data.files;
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
                selfs.state.dirs = FSUtil.concatArray(newArr, selfs.state.dirs);
            }
    
            console.log(selfs.state);
            selfs.forceUpdate(() => { resolve(); selfs.fIconize(); });
        });
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
        if(this.props.basic.useCaptchaDown) {
            const dia = document.getElementById('fs_pop_captdown');
            dia.style = "width : " + (this.props.basic.captSizes.width + 300) + "px; height : " + (this.props.basic.captSizes.height + 320) + "px; position: fixed; top: 100px; left : 100px;";
            const iframes = dia.getElementsByTagName('iframe')[0];
            iframes.src = this.props.basic.ctxPath + '/jsp/fs/fscaptdown.jsp?popin=true&theme=' + theme + '&path=' + encodeURIComponent(this.state.path) + "&filename=" + encodeURIComponent(file.name) + FSUtil.addTokenParameterString();
            iframes.style = "width: 100%; overflow-y: hidden; height : " + (this.props.basic.captSizes.height + 320 - 90) + "px";
            
            dia.show();
        } else {
            location.href = this.props.basic.ctxPath + '/jsp/fs/' + 'fsdown.jsp?path=' + encodeURIComponent(this.state.path) + "&filename=" + encodeURIComponent(file.name);
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
    fIconize() {
        const selfs = this;
        const fsRoot = $('.fs_root');
        const form     = fsRoot.find('.form_fs');
        const tables   = fsRoot.find('.fs_table_list');
        const listRoot = tables.find('.fs_list');
        const pathDisp = fsRoot.find('.path');
        listRoot.find('.tr_dir.no_icon').each(function() {
            const tdIcon = $(this).find('.td_mark_dir');
            tdIcon.empty();
            tdIcon.append("<img style='width: 20px; height: 20px;'/>");
            tdIcon.find('img').attr('src', FSUtil.ctx + '/css/images/dir.ico');
            tdIcon.find('img').attr('alt', 'Directory');
        });
        listRoot.find('.tr_file.no_icon').each(function() {
            const tdIcon = $(this).find('.td_mark_file');
            tdIcon.empty();
            tdIcon.append("<img style='width: 20px; height: 20px;'/>");
            tdIcon.find('img').attr('src', selfs.props.basic.ctxPath + '/css/images/files.png');
            tdIcon.find('img').attr('alt', 'File');
        });
        
        if(! selfs.props.basic.useIcon) return;
        
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
                                    
                                    var inFileNm = $(this).find('a.link_file').attr('data-name');
                                    if(inFileNm != dFileName) return;
                                    
                                    var inTdIcon = $(this).find('.td_mark_file');
                                    inTdIcon.empty();
                                    inTdIcon.append("<img style='width: 20px; height: 20px;'/>");
                                    inTdIcon.find('img').attr('src', dImage);
                                    inTdIcon.find('img').attr('alt', 'File');
                                    
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
    render() {
        const selfs = this;
        return (
            <div>
                <div className='fs_filelist fs_filelist_view container show-grid full'>
                    <div className='row fs_title'>
                        <div className='col-sm-8'>
                            <h2 className='fs_title'></h2>
                        </div>
                        <div className='col-sm-4 fs_allsearch align_right'>
                            <form className='form_allsearch' onSubmit={() => {return false}}>
                                <input type='hidden' name='path' className='hidden_path' value='' />
                                <input type='hidden' name='all'  className='hidden_conf' value='true' />
                                <input type='text'   className='inp_allsearch lang_attr_element reloading_readonly'      name='keyword' placeholder="전체 디렉토리 검색" data-lang-target='placeholder' data-lang-en='Search whole directories' style={{ width: '200px' }}/>
                                <input type='submit' className='btn_allsearch lang_attr_element reloading_disabled btnx' value='검색' data-lang-target='value' data-lang-en='Search' />
                            </form>
                        </div>
                    </div>
                    <form className='form_fs' id='form_fs' onSubmit={() => {
                        selfs.refresh();
                        return false;
                    }}>
                        <input type='hidden' name='path' className='hidden_path' value={this.state.path} />
                        <input type='hidden' name='excepts' className='hidden_excepts' value={this.state.excepts} />
                        <input type='hidden' name='praction' value='list' />
                        <div className='row fs_directory'>
                            <div className='col-sm-10'>
                                <h4 className='path_title'>
                                    <span className='lang_element' data-lang-en='Current Directory : '>현재
                                        디렉토리 : </span><span className='path'></span>
                                </h4>
                            </div>
                            <div className='col-sm-2 align_right'>
                                <input type='button' className='btn_upload btnx privilege_element invisible lang_attr_element' value='업로드' data-lang-target='value' data-lang-en='Upload' />
                                <input type='button' className='btn_mkdir  btnx privilege_element invisible lang_attr_element' value='새 폴더' data-lang-target='value' data-lang-en='New Folder' />
                                <input type='button' className='btn_config btnx privilege_element only_admin invisible lang_attr_element' value='설정' data-lang-target='value' data-lang-en='Config' />
                                <input type='button' className='btn_console btnx lang_attr_element' value='콘솔' data-lang-target='value' data-lang-en='Console' accessKey="t" />
                            </div>
                        </div>
                        <div className='row fs_search'>
                            <div className='col-sm-10'>
                                <input type='text' className='inp_search full lang_attr_element reloading_readonly' name='keyword' placeholder="디렉토리 내 검색" data-lang-target='placeholder' data-lang-en='Search in current directory' />
                            </div>
                            <div className='col-sm-2'>
                                <input type='submit' className='btn_search full lang_attr_element reloading_disabled btnx' value='검색' data-lang-target='value' data-lang-en='Search' />
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
                                            FSUtil.concatArray(selfs.state.dirs, selfs.state.files).map((fileOne, index) => {
                                                if(fileOne.type == 'dir') {
                                                    return (
                                                        <tr key={index} className={"element tr_dir tr_dir_" + index + " no_icon"}>
                                                            <td className='td_mark_dir'><img style={{width: '20px', height: '20px'}} src={FSUtil.ctx + '/css/images/dir.ico'}/></td>
                                                            <td colSpan="2"><a href='#' className='link_dir ellipsi binded_click' data-path={fileOne.name} onClick={() => { this.onClickDir(fileOne); }}>{fileOne.name}</a></td>
                                                            <td className='td_buttons'>
                                                                {
                                                                    (fileOne.elements <= 0 && selfs.state.privilege) ? (
                                                                        <input type='button' className='btn_delete btnx' value='X' onClick={ () => { selfs.onClickDelete(fileOne); } }/>
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
                                                            <td className='td_mark_file'><img style={{width: '20px', height: '20px'}} src={FSUtil.ctx + '/css/images/files.png'}/></td>
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
                                                                            <input type='button' className='btn_preview btnx not_opened' value='▲' onClick={ () => { fileOne.previewing = false; selfs.forceUpdate(); }}/>
                                                                        ) : (
                                                                            <input type='button' className='btn_preview btnx not_opened' value='▼' onClick={ () => { fileOne.previewing = true; selfs.forceUpdate(); }}/>
                                                                        )
                                                                    ) : null
                                                                }
                                                                </span>
                                                                <span>
                                                                {
                                                                    (selfs.state.privilege == 'edit') ? (
                                                                        <input type='button' className='btn_delete btnx' value='X' onClick={ () => { selfs.onClickDelete(fileOne); } }/>
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
                    </form>
                </div>
                <div className='fs_filelist fs_filelist_anonymous full invisible'>
                    
                </div>
                <div className='fs_hform invisible_wh'>
                    <form className='form_hidden' target='_blank'></form>
                </div>
                <div className='fs_pops invisible_wh'>
                    <dialog className='fs_pop_captdown fs_pop_in full' id='fs_pop_captdown'><div className='div_dialog_header'><button className="btn_dialog_close" onClick={() => { document.getElementById('fs_pop_captdown').close(); }}>X</button></div><iframe></iframe></dialog>
                    <dialog className='fs_pop_upload   fs_pop_in full' id='fs_pop_upload'  ><div className='div_dialog_header'><button className="btn_dialog_close" onClick={() => { document.getElementById('fs_pop_upload').close();   }}>X</button></div><iframe></iframe></dialog>
                    <dialog className='fs_pop_console  fs_pop_in full' id='fs_pop_console' ><div className='div_dialog_header'><button className="btn_dialog_close" onClick={() => { document.getElementById('fs_pop_console').close();  }}>X</button></div><iframe></iframe></dialog>
                    <dialog className='fs_pop_admin    fs_pop_in full' id='fs_pop_admin'   ><div className='div_dialog_header'><button className="btn_dialog_close" onClick={() => { document.getElementById('fs_pop_admin').close();    }}>X</button></div><iframe></iframe></dialog>
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
        console.log(props);
    }
    componentDidMount() {
        this.trans('status').then((data) => { this.reloads(data, true); });
    }
    trans(req) {
        FSUtil.setContextPath(this.props.basic.ctxPath);
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
            
            selfs.forceUpdate(() => { resolve(); });
        });
    }
    async login() {
        const res = await this.trans('login');
        await this.reloads(res, true);
        document.getElementById('form_fs').submit();
    }
    async logout() {
        const res = await this.trans('logout');
        await this.reloads(res, true);
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
                    <form onSubmit={() => { selfs.login(); return false; }} className='form_fs_login' id='form_fs_login'>
                    <input type='hidden' name='praction' value='account'/>
                    {
                        this.state.logined ? 
                            (
                                <div className='row login_element logined padding_top_10'>
                                    <div className='col-sm-12'>
                                        <span className='lang_element' data-lang-en='Welcome, '></span><span className='span_type'></span> <span className='span_nick'>{this.state.nick}</span><span className='lang_element' data-lang-en=''> 님 환영합니다.</span> 
                                        <input type='button' value='로그아웃' className='btn_logout btnx lang_attr_element' data-lang-target='value' data-lang-en='LOGOUT' onClick={() => { selfs.logout(); }}/>
                                    </div>
                                </div>                                 
                            )
                        :
                            (
                                <div className='row login_element not_logined padding_top_10'>
                                    <div className='container show-grid d_inline_block valign_middle' style={{ width: '270px', height: '60px' }}>
                                        <div className='row'>
                                            <div className='col-xs-12'>
                                                <span style={{display: 'inline-block', width: '80px'}}>ID</span><input type='text' name='id' className='inp_login_element' style={{width: '150px'}}/>
                                            </div>
                                        </div>
                                        <div className='row'>
                                            <div className='col-xs-12'>
                                                <span style={{display: 'inline-block', width: '80px'}} className='lang_element' data-lang-en='Password'>암호</span><input type='password' name='pw' className='inp_login_element' style={{width: '150px'}}/>
                                            </div>
                                        </div>
                                    </div>
                                    <div className='div_captcha_login d_inline_block valign_middle' style={{ width: (this.props.basic.captSizes.width + 10) + 'px', height: '60px' }}>
                                        <iframe className='if_captcha_l valign_middle' style={{width: (this.props.basic.captSizes.width + 5) + 'px', height : (this.props.basic.captSizes.height + 5) + 'px', border: 0}} src={this.props.basic.ctxPath + '/jsp/fs/fscaptin.jsp?key=fsl&scale=1&randomize=true&theme=' + this.props.basic.getTheme() + FSUtil.addTokenParameterString()}></iframe>
                                    </div>
                                    <div className='div_captcha_login d_inline_block valign_middle padding_top_10' style={{'marginLeft': '10px', height : '60px', 'textAlign' : 'left'}}>
                                        <input type='text' className='inp_captcha_l inp_login_element lang_attr_element valign_middle' name='captcha' placeholder='옆의 코드 입력' data-lang-target='placeholder' data-lang-en='Input the code left'/>
                                    </div>
                                    <div className='d_inline_block valign_middle' style={{width: '100px', height : '60px'}}>
                                        <input type='submit' value='로그인' className='lang_attr_element btnx' data-lang-target='value' data-lang-en='LOGIN' style={{height : '50px'}}/>
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