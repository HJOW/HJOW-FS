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

public class FSConsoleFirst implements FSBundledConsoleCommand {
	private static final long serialVersionUID = 3062038114191457928L;

	@Override
	public String getName() {
		return "first";
	}

	@Override
	public String getShortName() {
		return null;
	}

	@Override
	public Object run(FSControl ctrl, FSConsole console, Map<String, Object> sessionMap, File root, String parameter) throws Throwable {
		StringBuilder res = new StringBuilder("");
		res = res.append("FS Console");
		res = res.append("Version ").append(FSControl.VERSION[0] + "." + FSControl.VERSION[1] + "." + FSControl.VERSION[2]).append(" (Build ").append(FSControl.VERSION[3] + ")");
		return res;
	}

	@Override
	public String getHelp(String lang, boolean detail) {
		if(lang.equals("ko")) return "콘솔을 처음 열었을 때 출력되는 메시지를 다시 볼 수 있습니다.";
		else return "Re-print messages you saw first when you opened a console.";
	}
}