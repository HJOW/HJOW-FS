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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Map;

import com.hjow.fs.console.FSConsole;

public class FSConsoleCat implements FSConsoleCommand {
	private static final long serialVersionUID = -167214053198674139L;

	@Override
	public String getName() {
		return "read";
	}

	@Override
	public String getShortName() {
		return "cat";
	}

	@Override
	public Object run(FSConsole console, Map<String, Object> sessionMap, File root, String parameter) throws Throwable {
		String pathCalc = root.getCanonicalPath() + File.separator + console.getPath() + File.separator + parameter;
		File   fileCalc = new File(pathCalc);
		
		pathCalc = fileCalc.getCanonicalPath();
		
		if(! fileCalc.exists()   ) throw new FileNotFoundException("No such a file !");
		if(fileCalc.isDirectory()) throw new FileNotFoundException("No such a file !");
		if(! pathCalc.startsWith(root.getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
		if(pathCalc.startsWith(new File(root.getCanonicalPath() + File.separator + ".garbage").getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
		if(pathCalc.startsWith(new File(root.getCanonicalPath() + File.separator + ".upload" ).getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
		
		StringBuilder res = new StringBuilder("");
		
		FileInputStream   fIn = null;
		InputStreamReader rd1 = null;
		Throwable caught = null;
		
		char[] buffer = new char[32];
		int r = 0, i = 0, bytes = 0;
		try {
			fIn = new FileInputStream(fileCalc);
			rd1 = new InputStreamReader(fIn, "UTF-8");
			
			while(true) {
				r = rd1.read(buffer);
				if(r < 0) break;
				for(i=0; i<r; i++) {
					res = res.append(buffer[i]);
				}
				bytes += r;
				if(bytes >= 1024 * 64) {
					res = res.append("\n...");
					break;
				}
			}
			
			rd1.close(); rd1 = null;
			fIn.close(); fIn = null;
		} catch(Throwable t) {
			caught = t;
		} finally {
			if(rd1 != null) rd1.close();
			if(fIn != null) fIn.close();
		}
		
		if(caught != null) throw caught;
		
		return res.toString();
	}

	@Override
	public String getHelp(String lang, boolean detail) {
		return "";
	}
}
