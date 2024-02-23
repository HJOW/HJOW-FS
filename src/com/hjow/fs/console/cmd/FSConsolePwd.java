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

public class FSConsolePwd implements FSConsoleCommand {
	private static final long serialVersionUID = -1371251238129639475L;

	@Override
	public String getName() {
		return "pwd";
	}

	@Override
	public String getShortName() {
		return null;
	}

	@Override
	public Object run(FSConsole console, Map<String, Object> sessionMap, File root, String parameter) throws Throwable {
		return console.getPath();
	}

	@Override
	public String getHelp(String lang, boolean detail) {
		return "";
	}
}
