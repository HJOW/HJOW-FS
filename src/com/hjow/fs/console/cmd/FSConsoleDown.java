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

public class FSConsoleDown implements FSBundledConsoleCommand {
    private static final long serialVersionUID = -435109197370300784L;

    @Override
    public String getName() {
        return "download";
    }

    @Override
    public String getShortName() {
        return "down";
    }

    @Override
    public Object run(FSControl ctrl, FSConsole console, Map<String, Object> sessionMap, File root, String parameter, Map<String, String> options) throws Throwable {
        String pathCalc = root.getCanonicalPath() + File.separator + console.getPath() + File.separator + parameter;
        File   fileCalc = new File(pathCalc);
        
        pathCalc = fileCalc.getCanonicalPath();
        
        if(! fileCalc.exists()   ) throw new FileNotFoundException("No such a file !");
        if(fileCalc.isDirectory()) throw new FileNotFoundException("No such a file !");
        if(! pathCalc.startsWith(root.getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
        if(pathCalc.startsWith(new File(root.getCanonicalPath() + File.separator + ".garbage").getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
        if(pathCalc.startsWith(new File(root.getCanonicalPath() + File.separator + ".upload" ).getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
        
        FSConsoleResult rs = new FSConsoleResult();
        rs.setDisplay("Downloading will be started soon...");
        rs.setNulll(false);
        rs.setPath(console.getPath());
        rs.setDownloadAccepted(fileCalc.getName());
        rs.setSuccess(true);
        
        return rs;
    }

    @Override
    public String getHelp(String lang, boolean detail) {
        StringBuilder res = new StringBuilder("");
        if(detail) {
            if(lang.equals("ko")) {
                res = res.append(" * download").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    매개변수로 파일명을 받습니다.                                       ").append("\n");
                res = res.append("    파일 다운로드를 요청합니다. 성공 시 다운로드 창이 나타납니다.       ").append("\n");
                res = res.append("    디렉토리는 다운로드할 수 없습니다.                                  ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * 예").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    download Test1.txt                                                  ").append("\n");
                res = res.append("                                                                        ").append("\n");
            } else {
                res = res.append(" * download").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    Need one parameter which is a file's name.                          ").append("\n");
                res = res.append("    Download file.                                                      ").append("\n");
                res = res.append("    Cannot download directories.                                        ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * example").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    download Test1.txt                                                       ").append("\n");
                res = res.append("                                                                        ").append("\n");
            }
        } else {
            if(lang.equals("ko")) {
                res = res.append("파일을 다운로드합니다.").append("\n");
            } else {
                res = res.append("Download file.").append("\n");
            }
        }
        return res.toString().trim();
    }
}
