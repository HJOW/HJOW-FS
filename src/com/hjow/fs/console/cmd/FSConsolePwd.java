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

public class FSConsolePwd implements FSBundledConsoleCommand {
	private static final long serialVersionUID = 8206351811941412312L;

	@Override
	public String getName() {
		return "pwd";
	}

	@Override
	public String getShortName() {
		return null;
	}

	@Override
	public Object run(FSControl ctrl, FSConsole console, Map<String, Object> sessionMap, File root, String parameter) throws Throwable {
		return console.getPath();
	}

	@Override
	public String getHelp(String lang, boolean detail) {
		StringBuilder res = new StringBuilder("");
		if(detail) {
			if(lang.equals("ko")) {
				res = res.append(" * pwd").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    현재 작업 대상으로 선택된 디렉토리 경로를 봅니다.                   ").append("\n");
				res = res.append("    매개변수가 필요하지 않습니다.                                       ").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append(" * 예").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    pwd                                                                 ").append("\n");
				res = res.append("                                                                        ").append("\n");
			} else {
				res = res.append(" * pwd").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    Show the target directory's path.                                   ").append("\n");
				res = res.append("    No parameter needs.                                                 ").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append(" * example").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    pwd                                                                 ").append("\n");
				res = res.append("                                                                        ").append("\n");
			}
		} else {
			if(lang.equals("ko")) {
				res = res.append("현재 작업 대상으로 선택된 디렉토리 경로를 봅니다.").append("\n");
			} else {
				res = res.append("Show the target directory's path.").append("\n");
			}
		}
		return res.toString().trim();
	}
}
