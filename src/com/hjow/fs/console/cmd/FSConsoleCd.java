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
import java.io.FileNotFoundException;

import com.hjow.fs.console.FSConsole;
import com.hjow.fs.console.FSConsoleResult;

public class FSConsoleCd implements FSConsoleCommand {
	@Override
	public String getName() {
		return "cd";
	}

	@Override
	public String getShortName() {
		return null;
	}

	@Override
	public Object run(FSConsole console, File root, String parameter) throws Throwable {
		String pathCalc = root.getCanonicalPath() + File.separator + console.getPath() + File.separator + parameter;
		File   fileCalc = new File(pathCalc);
		
		pathCalc = fileCalc.getCanonicalPath();
		
		if(! fileCalc.exists()     ) throw new FileNotFoundException("No such a directory !");
		if(! fileCalc.isDirectory()) throw new FileNotFoundException("No such a directory !");
		if(! pathCalc.startsWith(root.getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
		if(pathCalc.startsWith(new File(root.getCanonicalPath() + File.separator + ".garbage").getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
		if(pathCalc.startsWith(new File(root.getCanonicalPath() + File.separator + ".upload" ).getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
		
		pathCalc = pathCalc.replace(root.getCanonicalPath(), "");
		
		FSConsoleResult rs = new FSConsoleResult();
		rs.setNulll(true);
		rs.setDisplay(null);
		rs.setPath(pathCalc);
		rs.setSuccess(true);
		return rs;
	}

	@Override
	public void dispose() {
		
	}
}
