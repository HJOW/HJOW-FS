/*
Copyright 2019 HJOW

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
package com.hjow.fs.console.cmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import com.hjow.fs.FSControl;
import com.hjow.fs.console.FSConsole;
import com.hjow.fs.console.FSConsoleResult;

public class FSConsoleMkdir implements FSBundledConsoleCommand {
    private static final long serialVersionUID = -6829700197763805168L;

    @Override
    public String getName() {
        return "makedir";
    }

    @Override
    public String getShortName() {
        return "mkdir";
    }

    @Override
    public String getHelp(String lang, boolean detail) {
        StringBuilder res = new StringBuilder("");
        if(detail) {
            if(lang.equals("ko")) {
                res = res.append(" * mkdir").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    매개변수로 디렉토리명을 받습니다.                                   ").append("\n");
                res = res.append("    새 디렉토리를 생성합니다.                                           ").append("\n");
                res = res.append("    관리자 또는 현재 디렉토리에 권한이 있어야 합니다.                   ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * 예").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    mkdir t20240310                                                     ").append("\n");
                res = res.append("                                                                        ").append("\n");
            } else {
                res = res.append(" * mkdir").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    Need one parameter which is a new directory's name.                 ").append("\n");
                res = res.append("    Create new directory.                                               ").append("\n");
                res = res.append("    Need administrator or edit privilege.                               ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * example").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    mkdir t20240310                                                     ").append("\n");
                res = res.append("                                                                        ").append("\n");
            }
        } else {
            if(lang.equals("ko")) {
                res = res.append("디렉토리를 생성합니다.").append("\n");
            } else {
                res = res.append("Create new directory.").append("\n");
            }
        }
        return res.toString().trim();
    }

    @Override
    public Object run(FSControl ctrl, FSConsole console, Map<String, Object> sessionMap, File root, String parameter, Map<String, String> options) throws Throwable {
        if(ctrl.isReadOnly()) throw new RuntimeException("Blocked. FS is read-only mode.");
        
        String pathCalc = root.getCanonicalPath() + File.separator + console.getPath() + File.separator + parameter;
        File   fileCalc = new File(pathCalc);
        
        pathCalc = fileCalc.getCanonicalPath();
        
        if(fileCalc.exists()   ) throw new FileNotFoundException("Already exist !");
        if(! pathCalc.startsWith(root.getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
        
        if(! FSConsole.hasEditPriv(ctrl, sessionMap, root, fileCalc)) {
            throw new RuntimeException("No privileges");
        }
        
        fileCalc.mkdirs();
        
        FSConsoleResult rs = new FSConsoleResult();
        rs.setDisplay("");
        rs.setNulll(true);
        rs.setPath(console.getPath());
        rs.setSuccess(true);
        rs.setClosepopup(false);
        return rs;
    }

}
