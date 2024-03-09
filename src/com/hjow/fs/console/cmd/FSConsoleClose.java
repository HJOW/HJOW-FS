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
import java.util.Map;

import com.hjow.fs.FSControl;
import com.hjow.fs.console.FSConsole;
import com.hjow.fs.console.FSConsoleResult;

public class FSConsoleClose implements FSBundledConsoleCommand {
    private static final long serialVersionUID = -3808145571940246437L;

    @Override
    public String getName() {
        return "close";
    }

    @Override
    public String getShortName() {
        return null;
    }

    @Override
    public Object run(FSControl ctrl, FSConsole console, Map<String, Object> sessionMap, File root, String parameter, Map<String, String> options) throws Throwable {
        FSConsoleResult rs = new FSConsoleResult();
        rs.setDisplay("");
        rs.setNulll(false);
        rs.setPath(console.getPath());
        rs.setSuccess(true);
        rs.setClosepopup(true);
        return rs;
    }

    @Override
    public String getHelp(String lang, boolean detail) {
        StringBuilder res = new StringBuilder("");
        if(detail) {
            if(lang.equals("ko")) {
                res = res.append(" * close").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    콘솔 창을 닫습니다.                                                 ").append("\n");
                res = res.append("    매개변수가 필요 없습니다.                                           ").append("\n");
                res = res.append("    브라우저의 보안 설정에 따라 동작하지 않을 수도 있습니다.            ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * 예").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    close                                                               ").append("\n");
                res = res.append("                                                                        ").append("\n");
            } else {
                res = res.append(" * close").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    Close the console window.                                           ").append("\n");
                res = res.append("    No parameter needs.                                                 ").append("\n");
                res = res.append("    May be no effect on some browsers.                                  ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * example").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    close                                                               ").append("\n");
                res = res.append("                                                                        ").append("\n");
            }
        } else {
            if(lang.equals("ko")) {
                res = res.append("콘솔 창을 닫습니다.").append("\n");
            } else {
                res = res.append("Close the console window.").append("\n");
            }
        }
        return res.toString().trim();
    }
}
