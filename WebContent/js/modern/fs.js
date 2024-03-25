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
    constructor(props) {
        super(props);
        console.log(props);
    }
    render() { 
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
                    <form className='form_fs' onSubmit={() => {return false}}>
                        <input type='hidden' name='path' className='hidden_path' value='' />
                        <input type='hidden' name='excepts' className='hidden_excepts' value='' />
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
                                        <col style={{width: '50px'}} className='col_controls' />
                                    </colgroup>
                                    <tbody className='fs_list'>
            
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
                    <div className='fs_pop_captdown fs_pop_in full'><iframe></iframe></div>
                    <div className='fs_pop_upload   fs_pop_in full'><iframe></iframe></div>
                    <div className='fs_pop_console  fs_pop_in full'><iframe></iframe></div>
                    <div className='fs_pop_admin    fs_pop_in full'><iframe></iframe></div>
                </div>
            </div>
        )
    }
}

class FSAccountBar extends React.Component {
    constructor(props) {
        super(props);
        console.log(props);
    }
    render() { 
        return (
            <div>
                <div className='container valign_middle full'>
                    <form onSubmit={() => {return false}} className='form_fs_login'>
                    <input type='hidden' name='req'      value='status' className='inp_req'/>
                    <input type='hidden' name='praction' value='account'/>
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
                        <div className='div_captcha_login d_inline_block valign_middle' style={{ height: '60px' }}>
                            <iframe className='if_captcha_l valign_middle'></iframe>
                        </div>
                        <div className='div_captcha_login d_inline_block valign_middle padding_top_10' style={{'marginLeft': '10px', height : '60px', 'textAlign' : 'left'}}>
                            <input type='text' className='inp_captcha_l inp_login_element lang_attr_element valign_middle' name='captcha' placeholder='옆의 코드 입력' data-lang-target='placeholder' data-lang-en='Input the code left'/>
                        </div>
                        <div className='d_inline_block valign_middle' style={{width: '100px', height : '60px'}}>
                            <input type='submit' value='로그인' className='lang_attr_element btnx' data-lang-target='value' data-lang-en='LOGIN' style={{height : '50px'}}/>
                        </div>
                    </div>
                    <div className='row login_element logined padding_top_10'>
                        <div className='col-sm-12'>
                            <span className='lang_element' data-lang-en='Welcome, '></span><span className='span_type'></span> <span className='span_nick'></span><span className='lang_element' data-lang-en=''> 님 환영합니다.</span> 
                            <input type='button' value='로그아웃' className='btn_logout btnx lang_attr_element' data-lang-target='value' data-lang-en='LOGOUT'/>
                        </div>
                    </div>
                    </form>
                </div>
            </div>
        )
    }
}


/*
class FSRoot extends React.Component {
    render() { return <div><h1>FSRoot {this.props.text}</h1></div> }
}

class FSAccountBar extends React.Component {
    render() { return <div><h1>FSAccountBar {this.props.text}</h1></div> }
}
*/