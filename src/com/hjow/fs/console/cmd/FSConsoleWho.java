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

import com.hjow.fs.console.FSConsole;

public class FSConsoleWho implements FSConsoleCommand {
	private static final long serialVersionUID = -3508005018737279193L;

	@Override
	public String getName() {
		return "whoami";
	}

	@Override
	public String getShortName() {
		return "who";
	}

	@Override
	public Object run(FSConsole console, Map<String, Object> sessionMap, File root, String parameter) throws Throwable {
		StringBuilder res = new StringBuilder("");
		res = res.append("Session Information").append("\n");
		res = res.append("id  ").append(" : ").append(sessionMap.get("id"    )).append("\n");
		res = res.append("type").append(" : ").append(sessionMap.get("idtype")).append("\n");
		res = res.append("nick").append(" : ").append(sessionMap.get("nick"  )).append("\n");
		return res.toString();
	}

	@Override
	public String getHelp(String lang, boolean detail) {
		StringBuilder res = new StringBuilder("");
		if(detail) {
			if(lang.equals("ko")) {
				res = res.append(" * who").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    현재 로그인한 세션 정보를 조회합니다.                               ").append("\n");
				res = res.append("    매개변수가 필요 없습니다.                                           ").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append(" * 예").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    ls                                                                  ").append("\n");
				res = res.append("                                                                        ").append("\n");
			} else {
				res = res.append(" * who").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    Print informations about current session.                           ").append("\n");
				res = res.append("    No parameter needs.                                                 ").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append(" * example").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    who                                                                  ").append("\n");
				res = res.append("                                                                        ").append("\n");
			}
		} else {
			if(lang.equals("ko")) {
				res = res.append("현재 로그인한 세션 정보를 조회합니다.").append("\n");
			} else {
				res = res.append("Print informations about current session.").append("\n");
			}
		}
		return res.toString().trim();
	}
}
