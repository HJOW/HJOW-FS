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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Map;

import com.hjow.fs.FSControl;
import com.hjow.fs.console.FSConsole;
import com.hjow.fs.console.FSConsoleResult;

import hjow.common.util.ClassUtil;
import hjow.common.util.FileUtil;

public class FSConsoleCmd implements FSBundledConsoleCommand {
	private static final long serialVersionUID = 6745297979416310463L;
	@Override
    public String getName() {
        return "command";
    }

    @Override
    public String getShortName() {
        return "cmd";
    }

    @Override
    public String getHelp(String lang, boolean detail) {
        StringBuilder res = new StringBuilder("");
        if(detail) {
            if(lang.equals("ko")) {
                res = res.append(" * cmd").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    매개변수 내용으로 서버 OS 명령을 실행합니다.                        ").append("\n");
                res = res.append("    --file 옵션으로 파일명을 받으면 파일 내용을 명령으로 실행합니다.    ").append("\n");
                res = res.append("    FS 에 시스템 cmd (AllowSystemCommand) 옵션이 켜져 있어야 합니다.    ").append("\n");
                res = res.append("    관리자 또는 해당 디렉토리에 권한이 있어야 합니다.                   ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * 예").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    cmd 'java -version'                                                 ").append("\n");
                res = res.append("                                                                        ").append("\n");
            } else {
                res = res.append(" * cmd").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    Run OS command.                                                     ").append("\n");
                res = res.append("    Need 'AllowSystemCommand' option for FS.                            ").append("\n");
                res = res.append("    Need administrator or edit privilege.                               ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * example").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    cmd 'java -version'                                                 ").append("\n");
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

    protected Runtime rt = null;
    @Override
    public Object run(FSControl ctrl, FSConsole console, Map<String, Object> sessionMap, File root, String parameter, Map<String, String> options) throws Throwable {
        if(ctrl.isReadOnly()) throw new RuntimeException("Blocked. FS is read-only mode.");
        if(! ctrl.isAllowSysCmd()) throw new RuntimeException("Blocked. FS is not allowed for cmd.");
        if(! FSConsole.hasEditPriv(ctrl, sessionMap, root, new File(console.getPath()))) {
            throw new RuntimeException("No privileges");
        }
        
		if(rt == null) rt = Runtime.getRuntime();
		
		StringBuilder outputs = new StringBuilder("");
		
    	Process proc = null;
    	BufferedReader rd1, rd2;
    	rd1 = null;
    	rd2 = null;
    	Throwable caught = null;
		
		try {
			String scripts = parameter;
			
			String charsetInput   = ctrl.getCharset();
			String charsetConsole = console.getSysCmdCharset();
			
	        if(options.get("c"       ) != null) charsetInput = options.get("c");
	        if(options.get("cs"      ) != null) charsetInput = options.get("cs");
	        if(options.get("charset" ) != null) charsetInput = options.get("charset");
	        if(options.get("cc"      ) != null) charsetConsole = options.get("cc");
	        if(options.get("ccs"     ) != null) charsetConsole = options.get("ccs");
	        if(options.get("ccharset") != null) charsetConsole = options.get("ccharset");
			
			String opt = options.get("f");
			if(opt == null) opt = options.get("file");
			if(opt != null) {
				String fileName = options.get("file");
				
				String pathCalc = root.getCanonicalPath() + File.separator + console.getPath() + File.separator + fileName;
		        File   fileCalc = new File(pathCalc, charsetInput);
		        
		        pathCalc = fileCalc.getCanonicalPath();
		        
		        if(! fileCalc.exists()   ) throw new FileNotFoundException("No such a file !");
		        if(! pathCalc.startsWith(root.getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
		        
		        scripts = FileUtil.readString(fileCalc);
			}
			
			proc = rt.exec(scripts);
			rd1 = new BufferedReader(new InputStreamReader(proc.getInputStream(), charsetConsole));
			rd2 = new BufferedReader(new InputStreamReader(proc.getErrorStream(), charsetConsole));
			
			String l = null;
			while(true) {
				l = rd1.readLine();
				if(l == null) break;
				outputs = outputs.append("\n").append(l);
			}
			
			while(true) {
				l = rd2.readLine();
				if(l == null) break;
				outputs = outputs.append("\n").append(l);
			}
			
			ClassUtil.closeAll(rd2, rd1);
			rd2 = null;
			rd1 = null;
			proc.destroy();
			proc = null;
			
			String resStr = outputs.toString().trim();
			outputs.setLength(0);
			
			FSConsoleResult res = new FSConsoleResult();
	        if(resStr.equals("")) {
	            res.setNulll(true);
	            res.setDisplay("");
	        } else {
	            res.setNulll(false);
	            res.setDisplay(resStr);
	        }
	        res.setPath(console.getPath());
	        res.setSuccess(true);
		} catch(Throwable t) {
			caught = t;
		} finally {
			ClassUtil.closeAll(rd2, rd1);
			if(proc != null) proc.destroy();
		}
		if(caught != null) throw new RuntimeException(caught.getMessage(), caught);
		return null;
    }
}