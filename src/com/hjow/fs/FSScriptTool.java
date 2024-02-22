package com.hjow.fs;
/*
Copyright 2024 HJOW (Heo Jin Won)

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

import java.io.File;
import java.io.Serializable;

public abstract class FSScriptTool implements Serializable {
	private static final long serialVersionUID = -768709662832694881L;
	protected static File rootPath = null;
	protected FSConsole console = null;
	
	synchronized void init(File rtPath) {
		if(rootPath != null) return;
		rootPath = rtPath;
	}
	
	FSConsole getConsole() {
		return console;
	}

	void setConsole(FSConsole console) {
		this.console = console;
	}
	
	public abstract String getVariableName();
	
	public void dispose() {
		this.console = null;
	}
}
