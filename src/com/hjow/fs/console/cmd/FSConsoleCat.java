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

import com.hjow.fs.FSControl;
import com.hjow.fs.FSUtils;
import com.hjow.fs.console.FSConsole;

public class FSConsoleCat implements FSBundledConsoleCommand {
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
    public Object run(FSControl ctrl, FSConsole console, Map<String, Object> sessionMap, File root, String parameter, Map<String, String> options) throws Throwable {
    	if(! FSUtils.canBeFileName(parameter)) throw new RuntimeException("Illegal character on file's name - " + parameter);
        
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
        
        String opt = options.get("v");
        if(opt == null) opt = options.get("var");
        if(opt == null) opt = options.get("variable");
        if(opt != null) {
        	String varName = opt;
        	opt = null;
        	
        	if(varName.contains(" ") || varName.contains("\t") || varName.contains("\n")) throw new RuntimeException("Variable name cannot contains spaces.");
        	if(varName.contains(".") || varName.contains("!")  || varName.contains("?" )) throw new RuntimeException("Illegal character on variable name - " + varName);
        	if(varName.contains("'") || varName.contains("\"") || varName.contains("(" )) throw new RuntimeException("Illegal character on variable name - " + varName);
        	if(varName.contains("[") || varName.contains("]")  || varName.contains(")" )) throw new RuntimeException("Illegal character on variable name - " + varName);
        	if(varName.contains("-") || varName.contains("+")  || varName.contains("=" )) throw new RuntimeException("Illegal character on variable name - " + varName);
        	
        	if(res.length() >= 3997) {
        		res.setLength(3996);
        		res = res.append("\n...");
        	}
        	
        	if(res.toString().getBytes("UTF-8").length >= 4000) {
        		int len = res.length();
        		while(res.toString().getBytes("UTF-8").length >= 3996) {
            		len--;
            		res.setLength(len);
            	}
        		res = res.append("\n...");
        	}
        	
        	String str = res.toString();
        	res.setLength(0);
        	
        	if(str.getBytes("UTF-8").length >= 4000) throw new RuntimeException("The content of variable can be 4000 byte or smaller.");
    		if(console.getVars().size() >= 200) {
    			if(! console.getVars().containsKey(varName)) throw new RuntimeException("Cannot create new variable.");
    		}
        	
        	console.getVars().put(varName, str);
        	return null;
        }
        
        return res.toString();
    }

    @Override
    public String getHelp(String lang, boolean detail) {
        StringBuilder res = new StringBuilder("");
        if(detail) {
            if(lang.equals("ko")) {
                res = res.append(" * cat").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    매개변수로 파일명을 받습니다.                                       ").append("\n");
                res = res.append("    파일을 최대 10 KB 까지 읽어 내용을 출력합니다.                      ").append("\n");
                res = res.append("    디렉토리는 읽을 수 없으며, UTF-8 인코딩으로 읽습니다.               ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * 예").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    cat Test1.txt                                                       ").append("\n");
                res = res.append("                                                                        ").append("\n");
            } else {
                res = res.append(" * cat").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    Need one parameter which is a file's name.                          ").append("\n");
                res = res.append("    Read file's content and print. (Max 10 KB)                          ").append("\n");
                res = res.append("    Cannot read directory. Read file as a UTF-8 text file.              ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * example").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    cat Test1.txt                                                       ").append("\n");
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
