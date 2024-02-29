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
import java.util.List;
import java.util.Map;

import com.hjow.fs.FSControl;
import com.hjow.fs.FSUtils;
import com.hjow.fs.console.FSConsole;

public class FSConsoleFind implements FSBundledConsoleCommand {
	private static final long serialVersionUID = 1139052487161349847L;

	@Override
	public String getName() {
		return "find";
	}

	@Override
	public String getShortName() {
		return "grep";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object run(FSControl ctrl, FSConsole console, Map<String, Object> sessionMap, File root, String parameter) throws Throwable {
		List<String> list = FSUtils.find(root, console.getPath(), parameter, FSControl.getInstance().getLimitSize());
		StringBuilder res = new StringBuilder("");
		
		List<String> hiddens = (List<String>) sessionMap.get("hiddendirs");
		if(hiddens == null) hiddens = new ArrayList<String>();
		boolean skip = false;
		
		for(String s : list) {
			skip = false;
			for(String h : hiddens) {
				if(s.startsWith(h)) {
					skip = true;
					break;
				}
			}
			if(! skip) res = res.append(s);
		}
		return res.toString().trim().replace("\\", "/");
	}

	@Override
	public String getHelp(String lang, boolean detail) {
		StringBuilder res = new StringBuilder("");
		if(detail) {
			if(lang.equals("ko")) {
				res = res.append(" * find").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    검색어로 파일 이름을 검색합니다.                                    ").append("\n");
				res = res.append("    매개변수로 검색어를 입력해야 합니다.                                ").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append(" * 예").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    find Test                                                           ").append("\n");
				res = res.append("                                                                        ").append("\n");
			} else {
				res = res.append(" * grep").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    Search file name.                                                   ").append("\n");
				res = res.append("    Parameter needs as a keyword.                                       ").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append(" * example").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    grep Test                                                           ").append("\n");
				res = res.append("                                                                        ").append("\n");
			}
		} else {
			if(lang.equals("ko")) {
				res = res.append("검색어로 파일을 검색합니다.").append("\n");
			} else {
				res = res.append("Search file name.").append("\n");
			}
		}
		return res.toString().trim();
	}
}
