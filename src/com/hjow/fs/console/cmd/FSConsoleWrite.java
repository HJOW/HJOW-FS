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
import com.hjow.fs.FSUtils;
import com.hjow.fs.console.FSConsole;
import com.hjow.fs.console.FSConsoleResult;

import hjow.common.util.FileUtil;

public class FSConsoleWrite implements FSBundledConsoleCommand {
	private static final long serialVersionUID = 8233184892522920302L;

	@Override
    public String getName() {
        return "write";
    }

    @Override
    public String getShortName() {
        return "wrt";
    }

    @Override
    public String getHelp(String lang, boolean detail) {
        StringBuilder res = new StringBuilder("");
        if(detail) {
            if(lang.equals("ko")) {
                res = res.append(" * write").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    매개변수로 텍스트 내용을 받습니다.                                  ").append("\n");
                res = res.append("    --file 옵션으로 파일명을 받습니다.                                  ").append("\n");
                res = res.append("    파일을 입력받은 내용으로 덮어 씁니다. 없으면 새로 생성합니다.       ").append("\n");
                res = res.append("    관리자 또는 해당 디렉토리에 권한이 있어야 합니다.                   ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * 예").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    write 'Hello World' --file Test1.txt                                ").append("\n");
                res = res.append("                                                                        ").append("\n");
            } else {
                res = res.append(" * write").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    Need a parameter as a new text content of file.                     ").append("\n");
                res = res.append("    '--file' option as a file's name.                                   ").append("\n");
                res = res.append("    Create, or overwrite a file.                                        ").append("\n");
                res = res.append("    Need administrator or edit privilege.                               ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * example").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    write 'Hello World' --file Test1.txt                                ").append("\n");
                res = res.append("                                                                        ").append("\n");
            }
        } else {
            if(lang.equals("ko")) {
                res = res.append("파일을 생성하거나 내용을 덮어 씁니다.").append("\n");
            } else {
                res = res.append("Create new file or overwrite.").append("\n");
            }
        }
        return res.toString().trim();
    }

    @Override
    public Object run(FSControl ctrl, FSConsole console, Map<String, Object> sessionMap, File root, String parameter, Map<String, String> options) throws Throwable {
        if(ctrl.isReadOnly()) throw new RuntimeException("Blocked. FS is read-only mode.");
        
        String fileName = options.get("file");
        if(fileName == null) fileName = options.get("f");
        if(fileName == null) throw new FileNotFoundException("'--file' optional parameter is necessary.");
        if(! FSUtils.canBeFileName(fileName)) throw new RuntimeException("Illegal character on file's name - " + fileName);
        
        String pathCalc = root.getCanonicalPath() + File.separator + console.getPath() + File.separator + fileName;
        File   fileCalc = new File(pathCalc);
        
        pathCalc = fileCalc.getCanonicalPath();
        
        if(! fileCalc.exists()   ) throw new FileNotFoundException("No such a file !");
        if(! pathCalc.startsWith(root.getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
        
        if(! FSConsole.hasEditPriv(ctrl, sessionMap, root, fileCalc)) {
            throw new RuntimeException("No privileges");
        }
        
        String charset = ctrl.getCharset();
        if(options.get("c"      ) != null) charset = options.get("c");
        if(options.get("cs"     ) != null) charset = options.get("cs");
        if(options.get("charset") != null) charset = options.get("charset");
        
        FileUtil.writeString(fileCalc, charset, parameter);
        
        FSConsoleResult rs = new FSConsoleResult();
        rs.setDisplay("");
        rs.setNulll(true);
        rs.setPath(console.getPath());
        rs.setSuccess(true);
        rs.setClosepopup(false);
        return rs;
    }
}
