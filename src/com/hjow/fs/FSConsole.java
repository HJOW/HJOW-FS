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
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.hjow.fs.sctools.FSConsoleTool;

public class FSConsole implements Serializable {
	private static final long serialVersionUID = 7995402708882449267L;
	private static final ScriptEngineManager scman = new ScriptEngineManager();
	private static List<Class<? extends FSScriptTool>> toolClasses = new ArrayList<Class<? extends FSScriptTool>>();
	public static void init(File rootPath) {
		toolClasses.add(FSConsoleTool.class);
		
		for(Class<? extends FSScriptTool> c : toolClasses) {
			try {
				FSScriptTool tempInstance = c.newInstance();
				tempInstance.init(rootPath);
				tempInstance.dispose();
			} catch(Throwable ex) {
				System.out.println("Error when initialize " + c.getName());
				ex.printStackTrace();
			}
		}
	}
	public static FSConsole getInstance() {
		FSConsole c = new FSConsole();
		return c;
	}
	
    protected ScriptEngine engine;
    protected String path;
    private FSConsole() {
    	engine = scman.getEngineByName("JavaScript");
    }
	public ScriptEngine getEngine() {
		return engine;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public FSConsoleResult run(String id, String command) {
		System.out.println("Console called by " + id + " - " + command);
		
		engine.put("path", getPath());
		
		List<FSScriptTool> tools = new ArrayList<FSScriptTool>();
		for(Class<? extends FSScriptTool> c : toolClasses) {
			try {
				FSScriptTool tool = c.newInstance();
				tool.setConsole(this);
				tools.add(tool);
				engine.put(tool.getVariableName(), tool);
			} catch(Throwable ex) {
				System.out.println("Error when prepare " + c.getName());
				ex.printStackTrace();
			}
		}
		
		FSConsoleResult res =  new FSConsoleResult();
		res.setPath(getPath());
		try {
			if(detectReflection(command)) throw new RuntimeException("Illegal Command !");
			Object rs = engine.eval(command);
			res.setSuccess(true);
			
			if(rs == null) {
				res.setNulll(true);
			} else {
				if(rs instanceof FSConsoleResult) {
					FSConsoleResult preRes = (FSConsoleResult) rs;
					if(preRes.getPath() != null) {
						setPath(preRes.getPath());
					} else {
						preRes.setPath(getPath());
					}
					
					for(FSScriptTool t : tools) {
						t.dispose();
					}
					tools.clear();
					return preRes;
				}
				res.setDisplay(String.valueOf(rs));
			}
		} catch(Throwable t) {
			t.printStackTrace();
			res.setSuccess(false);
			res.setDisplay("Error : " + t.getMessage());
		}
		for(FSScriptTool t : tools) {
			t.dispose();
		}
		tools.clear();
		engine.put("tool", null);
		engine.put("t", null);
		return res;
	}
	private boolean detectReflection(String command) {
		if(command.indexOf("getClass") >= 0) {
			int getClassPos = 0;
			while(command.indexOf("getClass", getClassPos) >= 0) {
				getClassPos = command.indexOf("getClass", getClassPos);
				for(int prog = 8; getClassPos + prog < command.length() ; prog++) {
					char c = command.charAt(getClassPos + prog);
					if(c == '(') return true;
					if(c != ' ' && c != '\t' && c != '\n') {
						getClassPos++;
						break;
					}
				}
			}
		}
		return false;
	}
}
