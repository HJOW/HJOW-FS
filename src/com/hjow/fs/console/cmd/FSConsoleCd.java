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

public class FSConsoleCd implements FSBundledConsoleCommand {
	private static final long serialVersionUID = -6401676768399101638L;

	@Override
	public String getName() {
		return "cd";
	}

	@Override
	public String getShortName() {
		return null;
	}

	@Override
	public Object run(FSControl ctrl, FSConsole console, Map<String, Object> sessionMap, File root, String parameter) throws Throwable {
		String pathCalc = root.getCanonicalPath() + File.separator + console.getPath() + File.separator + parameter;
		File   fileCalc = new File(pathCalc);
		
		pathCalc = fileCalc.getCanonicalPath();
		
		if(! fileCalc.exists()     ) throw new FileNotFoundException("No such a directory !");
		if(! fileCalc.isDirectory()) throw new FileNotFoundException("No such a directory !");
		if(! pathCalc.startsWith(root.getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
		if(pathCalc.startsWith(new File(root.getCanonicalPath() + File.separator + ".garbage").getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
		if(pathCalc.startsWith(new File(root.getCanonicalPath() + File.separator + ".upload" ).getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
		
		pathCalc = pathCalc.replace(root.getCanonicalPath(), "");
		
		FSConsoleResult rs = new FSConsoleResult();
		rs.setNulll(true);
		rs.setDisplay(null);
		rs.setPath(pathCalc);
		rs.setSuccess(true);
		return rs;
	}

	@Override
	public String getHelp(String lang, boolean detail) {
		StringBuilder res = new StringBuilder("");
		if(detail) {
			if(lang.equals("ko")) {
				res = res.append(" * cd").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    작업할 디렉토리를 변경합니다.                                       ").append("\n");
				res = res.append("    매개변수로 디렉토리명을 받습니다.                                   ").append("\n");
				res = res.append("    상대경로 사용이 가능합니다.                                         ").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append(" * 예").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    cd Directory1                                                       ").append("\n");
				res = res.append("                                                                        ").append("\n");
			} else {
				res = res.append(" * cd").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    Go to another directory as a work target.                           ").append("\n");
				res = res.append("    Need parameter as a directory name.                                 ").append("\n");
				res = res.append("    You can use relative path.                                          ").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append(" * example").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    cd Directory1                                                       ").append("\n");
				res = res.append("                                                                        ").append("\n");
			}
		} else {
			if(lang.equals("ko")) {
				res = res.append("작업할 디렉토리를 변경합니다.").append("\n");
			} else {
				res = res.append("Go to another directory as a work target.").append("\n");
			}
		}
		return res.toString().trim();
	}
}
