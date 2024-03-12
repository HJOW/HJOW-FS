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
import java.text.SimpleDateFormat;
import java.util.Map;

import com.hjow.fs.FSControl;
import com.hjow.fs.console.FSConsole;
import com.hjow.fs.console.FSConsoleResult;

public class FSConsoleDel implements FSBundledConsoleCommand {
	private static final long serialVersionUID = -2635992384395978063L;

	@Override
	public String getName() {
		return "delete";
	}

	@Override
	public String getShortName() {
		return "del";
	}

	@Override
	public String getHelp(String lang, boolean detail) {
		StringBuilder res = new StringBuilder("");
        if(detail) {
            if(lang.equals("ko")) {
                res = res.append(" * delete").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    매개변수로 파일명을 받습니다.                                       ").append("\n");
                res = res.append("    파일 또는 디렉토리를 삭제합니다.                                    ").append("\n");
                res = res.append("    관리자 또는 해당 디렉토리에 권한이 있어야 합니다.                   ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * 예").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    del Test1.txt                                                       ").append("\n");
                res = res.append("                                                                        ").append("\n");
            } else {
                res = res.append(" * delete").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    Need one parameter which is a file's name.                          ").append("\n");
                res = res.append("    Delete file or directory.                                           ").append("\n");
                res = res.append("    Need administrator or edit privilege.                               ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * example").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    del Test1.txt                                                       ").append("\n");
                res = res.append("                                                                        ").append("\n");
            }
        } else {
            if(lang.equals("ko")) {
                res = res.append("파일을 삭제합니다.").append("\n");
            } else {
                res = res.append("Delete file.").append("\n");
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
        
        if(! fileCalc.exists()   ) throw new FileNotFoundException("No such a file !");
        if(! pathCalc.startsWith(root.getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
        
        if(! FSConsole.hasEditPriv(ctrl, sessionMap, root, fileCalc)) {
        	throw new RuntimeException("No privileges");
        }
        
        File garbage = ctrl.getGarbage();
        if(garbage == null) garbage = new File(root.getCanonicalPath() + File.separator + ".garbage");
        if(! garbage.exists()) garbage.mkdirs();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        File dest = new File(garbage.getCanonicalPath() + File.separator + dateFormat.format(new java.util.Date(System.currentTimeMillis())));
        if(! dest.exists()) dest.mkdirs();

        File fdest = new File(dest.getCanonicalPath() + File.separator + fileCalc.getName());
        if(fdest.exists()) {
            int index = 0;
            while(fdest.exists()) {
                index++;
                fdest = new File(dest.getCanonicalPath() + File.separator + fileCalc.getName() + "." + index);
            }
        }
        fileCalc.renameTo(fdest);
        
        FSConsoleResult rs = new FSConsoleResult();
        rs.setDisplay("");
        rs.setNulll(true);
        rs.setPath(console.getPath());
        rs.setSuccess(true);
        rs.setClosepopup(false);
        return rs;
	}

}
