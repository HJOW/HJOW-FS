package com.hjow.fs.console;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.hjow.fs.console.cmd.FSConsoleCat;
import com.hjow.fs.console.cmd.FSConsoleCd;
import com.hjow.fs.console.cmd.FSConsoleCommand;
import com.hjow.fs.console.cmd.FSConsoleDown;
import com.hjow.fs.console.cmd.FSConsoleFind;
import com.hjow.fs.console.cmd.FSConsoleFirst;
import com.hjow.fs.console.cmd.FSConsoleHelp;
import com.hjow.fs.console.cmd.FSConsoleLs;
import com.hjow.fs.console.cmd.FSConsoleNow;
import com.hjow.fs.console.cmd.FSConsolePwd;
import com.hjow.fs.console.cmd.FSConsoleWho;

public class FSConsole implements Serializable {
	private static final long serialVersionUID = 7995402708882449267L;
	private static List<Class<? extends FSConsoleCommand>> commands = new ArrayList<Class<? extends FSConsoleCommand>>();
	private static File rootPath = null;
	
	public static void init(File rootPath) {
		init(rootPath, null);
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized void init(File rootPath, List<String> commandClasses) {
		if(! (FSConsole.rootPath != null && FSConsole.rootPath.equals(rootPath))) {
			FSConsole.rootPath = rootPath;
			if(! commands.contains(FSConsoleCat.class  )) commands.add(FSConsoleCat.class  );
			if(! commands.contains(FSConsoleCd.class   )) commands.add(FSConsoleCd.class   );
			if(! commands.contains(FSConsoleDown.class )) commands.add(FSConsoleDown.class );
			if(! commands.contains(FSConsoleFirst.class)) commands.add(FSConsoleFirst.class);
			if(! commands.contains(FSConsoleHelp.class )) commands.add(FSConsoleHelp.class );
			if(! commands.contains(FSConsoleLs.class   )) commands.add(FSConsoleLs.class   );
			if(! commands.contains(FSConsoleNow.class  )) commands.add(FSConsoleNow.class  );
			if(! commands.contains(FSConsolePwd.class  )) commands.add(FSConsolePwd.class  );
			if(! commands.contains(FSConsoleWho.class  )) commands.add(FSConsoleWho.class  );
			if(! commands.contains(FSConsoleFind.class )) commands.add(FSConsoleFind.class );
		}
		
		if(commandClasses != null) {
			for(String c : commandClasses) {
				try {
					Class<? extends FSConsoleCommand> classObj = (Class<? extends FSConsoleCommand>) Class.forName(c);
					commands.add(classObj);
				} catch(ClassNotFoundException e) {
					System.out.println("Cannot found " + c + " - " + e.getMessage());
				}
			}
		}
	}
	public static FSConsole getInstance() {
		FSConsole c = new FSConsole();
		return c;
	}
	
    protected String path;
    protected List<FSConsoleCommand> cmds = new ArrayList<FSConsoleCommand>();
    
    private FSConsole() {
    	for(Class<? extends FSConsoleCommand> c : commands) {
			try { cmds.add(c.newInstance()); } catch(Throwable t) { System.out.println("Fail to initialize console command " + c + " - Error : " + t.getMessage()); }
		}
    	Collections.sort(cmds, new Comparator<FSConsoleCommand>() {
			@Override
			public int compare(FSConsoleCommand o1, FSConsoleCommand o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
    }
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public List<FSConsoleCommand> getCommands() {
		return cmds;
	}
	public FSConsoleResult run(Map<String, Object> sessionMap, String command) {
		System.out.println("Console called by " + sessionMap.get("id") + " - " + command + " at " + System.currentTimeMillis());
		
		FSConsoleResult res = null;
		
		List<String> lines = new ArrayList<String>();
		StringTokenizer lineTokenizer1 = new StringTokenizer(command, "\n");
		while(lineTokenizer1.hasMoreTokens()) {
			StringTokenizer lineTokenizer2 = new StringTokenizer(lineTokenizer1.nextToken().trim(), ";");
			while(lineTokenizer2.hasMoreTokens()) {
				lines.add(lineTokenizer2.nextToken().trim());
			}
		}
		lineTokenizer1 = null;
		command = null;
		
		List<FSConsoleResult> multipleRes = new ArrayList<FSConsoleResult>();
		
		try {
			for(String lineOne : lines) {
				StringBuilder parameter = new StringBuilder("");
				StringTokenizer spaceTokenizer = new StringTokenizer(lineOne, " ");
				String commandName = spaceTokenizer.nextToken();
				while(spaceTokenizer.hasMoreTokens()) {
					parameter = parameter.append(spaceTokenizer.nextToken()).append(" ");
				}
				
				System.out.println("LINE : " + lineOne);
				System.out.println("CMD : " + commandName);
				System.out.println("PARAM : " + parameter.toString());
				
				lineOne = null;
				spaceTokenizer = null;
				
				FSConsoleCommand commandOne = null;
				for(FSConsoleCommand c : cmds) {
					if(c.getName().equalsIgnoreCase(commandName)) {
						commandOne = c;
						break;
					}
				}
				if(commandOne == null) {
					for(FSConsoleCommand c : cmds) {
						if(c.getShortName() == null) continue;
						if(c.getShortName().equalsIgnoreCase(commandName)) {
							commandOne = c;
							break;
						}
					}
				}
				
				if(commandOne == null) throw new NullPointerException("Cannot found correct command.");
				
				Object result = commandOne.run(this, sessionMap, rootPath, parameter.toString().trim());
				if(result instanceof FSConsoleResult) {
					res = (FSConsoleResult) result;
				} else {
					res = new FSConsoleResult();
					if(result == null) {
						res.setNulll(true);
						res.setDisplay("");
					} else {
						res.setNulll(false);
						res.setDisplay(String.valueOf(result));
					}
					res.setPath(path);
					res.setSuccess(true);
				}
				multipleRes.add(res);
			}
		} catch(Throwable t) {
			if(! (t instanceof RuntimeException)) t.printStackTrace();
			
			res = new FSConsoleResult();
			res.setNulll(false);
			res.setDisplay("Error : " + t.getMessage());
			res.setPath(path);
			res.setSuccess(false);
			multipleRes.add(res);
		}
		
		if(multipleRes.size() == 1) return multipleRes.get(0);
		else return new FSConsoleMultipleResult(multipleRes);
	}
}
