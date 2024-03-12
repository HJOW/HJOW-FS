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
import java.util.Set;

import com.hjow.fs.FSControl;
import com.hjow.fs.console.FSConsole;

public class FSConsoleVar implements FSBundledConsoleCommand {
	private static final long serialVersionUID = -9151923332826078175L;

	@Override
    public String getName() {
        return "variable";
    }

    @Override
    public String getShortName() {
        return "var";
    }

    @Override
    public Object run(FSControl ctrl, FSConsole console, Map<String, Object> sessionMap, File root, String parameter, Map<String, String> options) throws Throwable {
    	String varName = parameter;
    	if(varName == null) return "";
    	if(varName.contains(" ") || varName.contains("\t") || varName.contains("\n")) throw new RuntimeException("Variable name cannot contains spaces.");
    	if(varName.contains(".") || varName.contains("!")  || varName.contains("?" )) throw new RuntimeException("Illegal character on variable name - " + varName);
    	if(varName.contains("'") || varName.contains("\"") || varName.contains("(" )) throw new RuntimeException("Illegal character on variable name - " + varName);
    	if(varName.contains("[") || varName.contains("]")  || varName.contains(")" )) throw new RuntimeException("Illegal character on variable name - " + varName);
    	if(varName.contains("-") || varName.contains("+")  || varName.contains("=" )) throw new RuntimeException("Illegal character on variable name - " + varName);
    	
    	String opt;
    	
    	opt = options.get("input");
    	if(opt == null) opt = options.get("i");
    	if(opt != null) {
    		if(opt.getBytes("UTF-8").length >= 4000) throw new RuntimeException("The content of variable can be 4000 byte or smaller.");
    		if(console.getVars().size() >= 200) {
    			if(! console.getVars().containsKey(varName)) throw new RuntimeException("Cannot create new variable.");
    		}
    		console.getVars().put(varName, opt);
    		return null;
    	}
    	
    	opt = options.get("list");
    	if(opt == null) opt = options.get("l");
    	if(opt != null) {
    		Set<String> keys = console.getVars().keySet();
    		StringBuilder res = new StringBuilder("");
    		for(String k : keys) {
    			res = res.append("\n").append(k);
    		}
    		return res.toString().trim();
    	}
    	
    	opt = options.get("remove");
    	if(opt == null) opt = options.get("r");
    	if(opt != null) {
    		console.getVars().remove(varName);
    		return null;
    	}
    	
    	return console.getVars().get(varName);
    }

    @Override
    public String getHelp(String lang, boolean detail) {
        StringBuilder res = new StringBuilder("");
        if(detail) {
            if(lang.equals("ko")) {
                res = res.append(" * var").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    변수의 값을 보거나 변수에 값을 넣습니다.                            ").append("\n");
                res = res.append("    매개변수로 변수의 이름을 지정합니다.                                ").append("\n");
                res = res.append("    -i 옵션으로 변수에 넣을 내용을 지정합니다.                          ").append("\n");
                res = res.append("    변수를 삭제하려면 -r 옵션을 사용합니다.                             ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * 예").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    var tempVar1                                                        ").append("\n");
                res = res.append("    var tempVar2 -i HelloWorld                                          ").append("\n");
                res = res.append("                                                                        ").append("\n");
            } else {
                res = res.append(" * var").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    See variable value, or modify it.                                   ").append("\n");
                res = res.append("    Set parameter as a variable name.                                   ").append("\n");
                res = res.append("    If you modify one, use -i option.                                   ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * var").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    var tempVar1                                                        ").append("\n");
                res = res.append("    var tempVar2 -i HelloWorld                                          ").append("\n");
                res = res.append("                                                                        ").append("\n");
            }
        } else {
            if(lang.equals("ko")) {
                res = res.append("텍스트 파일 내용을 보는 데 사용됩니다.").append("\n");
            } else {
                res = res.append("Read and see file's content as a text.").append("\n");
            }
        }
        return res.toString().trim();
    }
}
