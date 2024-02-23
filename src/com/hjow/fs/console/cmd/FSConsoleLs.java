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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.hjow.fs.FSUtils;
import com.hjow.fs.console.FSConsole;

public class FSConsoleLs implements FSConsoleCommand {
	private static final long serialVersionUID = 2838576926510433810L;

	@Override
	public String getName() {
		return "ls";
	}

	@Override
	public String getShortName() {
		return null;
	}

	@Override
	public Object run(FSConsole console, Map<String, Object> sessionMap, File root, String parameter) throws Throwable {
		String pathCalc = root.getCanonicalPath() + File.separator + console.getPath();
		File   fileCalc = new File(pathCalc);
		
		StringBuilder res = new StringBuilder("");
		res = res.append("-TYPE-\t-SIZE-\t\t-NAME-").append("\n");
		
		List<File> listTwo = new ArrayList<File>();
		File[] listOne = fileCalc.listFiles();
		for(File f : listOne) {
			listTwo.add(f);
		}
		listOne = null;
		
		Collections.sort(listTwo, new Comparator<File>() {
	    	@Override
	    	public int compare(File o1, File o2) {
	    		if(o1.isDirectory() && (! o2.isDirectory())) return -1;
	    		if((! o1.isDirectory()) && o2.isDirectory()) return 1;
	    		return (o1.getName().compareTo(o2.getName()));
	    	}
	    });
		
		for(File f : listTwo) {
			String name = f.getName();
			if(name.startsWith(".")) continue;
			
			if(f.isDirectory()) {
				res = res.append("DIR").append("\t\t\t").append(name).append("").append("\n");
			} else {
				res = res.append("FILE").append("\t").append(FSUtils.getFileSize(f)).append("\t\t").append(name).append("\n");
			}
		}
		
		return res.toString();
	}

	@Override
	public String getHelp(String lang, boolean detail) {
		StringBuilder res = new StringBuilder("");
		if(detail) {
			if(lang.equals("ko")) {
				res = res.append(" * ls").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    현재 디렉토리의 파일과 디렉토리 목록을 출력합니다.                  ").append("\n");
				res = res.append("    매개변수가 필요 없습니다.                                           ").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append(" * 예").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    ls                                                                  ").append("\n");
				res = res.append("                                                                        ").append("\n");
			} else {
				res = res.append(" * ls").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    Print all of files and directories in this directory.               ").append("\n");
				res = res.append("    No parameter needs.                                                 ").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append(" * example").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    ls                                                                  ").append("\n");
				res = res.append("                                                                        ").append("\n");
			}
		} else {
			if(lang.equals("ko")) {
				res = res.append("현재 디렉토리 내 파일과 디렉토리 목록을 볼 수 있습니다.").append("\n");
			} else {
				res = res.append("Print all of files and directories in this directory.").append("\n");
			}
		}
		return res.toString().trim();
	}
}
