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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.hjow.fs.FSControl;
import com.hjow.fs.console.cmd.FSConsoleCat;
import com.hjow.fs.console.cmd.FSConsoleCd;
import com.hjow.fs.console.cmd.FSConsoleCmd;
import com.hjow.fs.console.cmd.FSConsoleCommand;
import com.hjow.fs.console.cmd.FSConsoleConfig;
import com.hjow.fs.console.cmd.FSConsoleDel;
import com.hjow.fs.console.cmd.FSConsoleDown;
import com.hjow.fs.console.cmd.FSConsoleFind;
import com.hjow.fs.console.cmd.FSConsoleFirst;
import com.hjow.fs.console.cmd.FSConsoleHelp;
import com.hjow.fs.console.cmd.FSConsoleJDBC;
import com.hjow.fs.console.cmd.FSConsoleLs;
import com.hjow.fs.console.cmd.FSConsoleMkdir;
import com.hjow.fs.console.cmd.FSConsoleNow;
import com.hjow.fs.console.cmd.FSConsolePwd;
import com.hjow.fs.console.cmd.FSConsoleVar;
import com.hjow.fs.console.cmd.FSConsoleWho;
import com.hjow.fs.console.cmd.FSConsoleWrite;

import hjow.common.json.JsonArray;
import hjow.common.json.JsonCompatibleUtil;
import hjow.common.json.JsonObject;
import hjow.common.util.DataUtil;

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
            if(! commands.contains(FSConsoleCat.class   )) commands.add(FSConsoleCat.class   );
            if(! commands.contains(FSConsoleCd.class    )) commands.add(FSConsoleCd.class    );
            if(! commands.contains(FSConsoleDown.class  )) commands.add(FSConsoleDown.class  );
            if(! commands.contains(FSConsoleFirst.class )) commands.add(FSConsoleFirst.class );
            if(! commands.contains(FSConsoleHelp.class  )) commands.add(FSConsoleHelp.class  );
            if(! commands.contains(FSConsoleLs.class    )) commands.add(FSConsoleLs.class    );
            if(! commands.contains(FSConsoleNow.class   )) commands.add(FSConsoleNow.class   );
            if(! commands.contains(FSConsolePwd.class   )) commands.add(FSConsolePwd.class   );
            if(! commands.contains(FSConsoleWho.class   )) commands.add(FSConsoleWho.class   );
            if(! commands.contains(FSConsoleFind.class  )) commands.add(FSConsoleFind.class  );
            if(! commands.contains(FSConsoleDel.class   )) commands.add(FSConsoleDel.class   );
            if(! commands.contains(FSConsoleMkdir.class )) commands.add(FSConsoleMkdir.class );
            if(! commands.contains(FSConsoleConfig.class)) commands.add(FSConsoleConfig.class);
            if(! commands.contains(FSConsoleWrite.class )) commands.add(FSConsoleWrite.class );
            if(! commands.contains(FSConsoleVar.class   )) commands.add(FSConsoleVar.class   );
            if(! commands.contains(FSConsoleCmd.class   )) commands.add(FSConsoleCmd.class   );
            if(! commands.contains(FSConsoleJDBC.class  )) commands.add(FSConsoleJDBC.class  );
        }
        
        if(commandClasses != null) {
            for(String c : commandClasses) {
                try {
                    Class<? extends FSConsoleCommand> classObj = (Class<? extends FSConsoleCommand>) Class.forName(c);
                    if(! commands.contains(classObj)) commands.add(classObj);
                } catch(ClassNotFoundException e) {
                    FSControl.log("Cannot found " + c + " - " + e.getMessage(), FSConsole.class);
                }
            }
        }
    }
    public static FSConsole getInstance() {
        FSConsole c = new FSConsole();
        return c;
    }
    
    protected String path;
    protected List<FSConsoleCommand>    cmds = new ArrayList<FSConsoleCommand>();
    protected Map<String, Serializable> vars = new HashMap<String, Serializable>();
    protected String sysCmdCharset = "UTF-8";
    
    private FSConsole() {
        for(Class<? extends FSConsoleCommand> c : commands) {
            try { cmds.add(c.newInstance()); } catch(Throwable t) { FSControl.log("Fail to initialize console command " + c + " - Error : " + t.getMessage(), FSControl.class); }
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
    public FSConsoleResult run(FSControl ctrl, Map<String, Object> sessionMap, String command) {
        FSControl.log("Console called by " + sessionMap.get("id") + " - " + command + " at " + System.currentTimeMillis(), this.getClass());
        
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
            	FSControl.log("LINE   : " + lineOne, this.getClass());
                Map<String, String> parsed = DataUtil.parseParameter(lineOne);
                
                String commandName = parsed.get("ORDER");
                String parameter   = parsed.get("PARAMETER");
                
                if(parameter != null) parameter = parameter.trim();
                
                parsed.remove("ORDER");
                parsed.remove("PARAMETER");
                
                FSControl.log("CMD    : " + commandName, this.getClass());
                FSControl.log("PARAM  : " + parameter, this.getClass());
                FSControl.log("OPTION : " + parsed.toString(), this.getClass());
                
                lineOne = null;
                
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
                
                Object result = commandOne.run(ctrl, this, sessionMap, rootPath, parameter, parsed);
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
                
                if(parsed.containsKey("grep")) res = afterProcessResults(res, "grep", parsed.get("grep"));
                if(parsed.containsKey("GREP")) res = afterProcessResults(res, "grep", parsed.get("GREP"));
                if(parsed.containsKey("grgx")) res = afterProcessResults(res, "grgx", parsed.get("grgx"));
                if(parsed.containsKey("GRGX")) res = afterProcessResults(res, "grgx", parsed.get("GRGX"));
                
                multipleRes.add(res);
            }
        } catch(Throwable t) {
            t.printStackTrace();
            if(! (t instanceof RuntimeException)) FSControl.log(t, getClass());
            
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
    
    /** Filter console results. The input (the result object) will be affected.  */
    protected FSConsoleResult afterProcessResults(FSConsoleResult beforeResult, String afterProcess, String afterProcessParam) {
        if(afterProcess      == null) return beforeResult;
        if(afterProcessParam == null) return beforeResult;
        
        afterProcess = afterProcess.trim().toLowerCase();
        StringBuilder coll = null;
        StringTokenizer lineTokenizer = null;
        
        if(beforeResult instanceof FSConsoleMultipleResult) {
            FSConsoleMultipleResult b = (FSConsoleMultipleResult) beforeResult;
            for(FSConsoleResult r : b.getChildren()) {
                String beforeMsg = r.getDisplay();
                if(beforeMsg != null) {
                    coll = new StringBuilder("");
                    lineTokenizer = new StringTokenizer(beforeMsg, "\n");
                    while(lineTokenizer.hasMoreTokens()) {
                        String line = lineTokenizer.nextToken();
                        line = afterProcessResultEachLine(line, afterProcess, afterProcessParam);
                        if(line == null) continue;
                        coll = coll.append("\n").append(line);
                    }
                    r.setDisplay(coll.toString().trim());
                }
            }
        } else {
            String beforeMsg = beforeResult.getDisplay();
            if(beforeMsg != null) {
                coll = new StringBuilder("");
                lineTokenizer = new StringTokenizer(beforeMsg, "\n");
                while(lineTokenizer.hasMoreTokens()) {
                    String line = lineTokenizer.nextToken();
                    line = afterProcessResultEachLine(line, afterProcess, afterProcessParam);
                    if(line == null) continue;
                    coll = coll.append("\n").append(line);
                }
                beforeResult.setDisplay(coll.toString().trim());
            }
        }
        
        return beforeResult;
    }
    
    protected String afterProcessResultEachLine(String lineOne, String afterProcess, String afterProcessParam) {
        if(lineOne == null) return null;
        if(afterProcess.equals("grep")) {
            if(lineOne.indexOf(afterProcessParam) >= 0) return lineOne;
            else return null;
        }
        if(afterProcess.equals("grgx")) {
            if(lineOne.matches(afterProcessParam)) return lineOne;
            else return null;
        }
        return lineOne;
    }
    
    /** Check EDIT privileges */
    public static boolean hasEditPriv(FSControl ctrl, Map<String, Object> sessionMap, File root, File target) throws IOException {
        String pathCalc = target.getCanonicalPath();
        
        if(! target.exists()   ) throw new FileNotFoundException("There is no " + target.getName());
        if(! pathCalc.startsWith(root.getCanonicalPath())) return false;
        if(pathCalc.startsWith(new File(root.getCanonicalPath() + File.separator + ".garbage").getCanonicalPath())) return false;
        if(pathCalc.startsWith(new File(root.getCanonicalPath() + File.separator + ".upload" ).getCanonicalPath())) return false;
        if(sessionMap               == null) return false;
        if(sessionMap.get("idtype") == null) return false;
        
        String idtype = sessionMap.get("idtype").toString().toUpperCase().trim();
        if(idtype.equals("A")) return true;
        
        Object oDirPrv = (Object) sessionMap.get("privileges");
        if(oDirPrv == null) return false;
        
        JsonArray dirPrv = (JsonArray) JsonCompatibleUtil.parseJson(oDirPrv);
        oDirPrv = null;
        
        for(Object row : dirPrv) {
            JsonObject dirOne = null;
            if(row instanceof JsonObject) dirOne = (JsonObject) row;
            else                          dirOne = (JsonObject) JsonCompatibleUtil.parseJson(row.toString().trim());
            
            String dPath = dirOne.get("path"     ).toString().trim();
            String dPrv  = dirOne.get("privilege").toString().trim().toLowerCase();
            
            String pathDisp = pathCalc.replace(root.getCanonicalPath(), "").replace("\\", "/");
            if(pathDisp.startsWith(dPath) || ("/" + pathDisp).startsWith(dPath)) {
                if(dPrv.equals("edit")) {
                    return true;
                }
            }
        }
        
        return false;
    }

	public List<FSConsoleCommand> getCmds() {
		return cmds;
	}

	public void setCmds(List<FSConsoleCommand> cmds) {
		this.cmds = cmds;
	}

	public Map<String, Serializable> getVars() {
		return vars;
	}

	public void setVars(Map<String, Serializable> vars) {
		this.vars = vars;
	}

	public String getSysCmdCharset() {
		return sysCmdCharset;
	}

	public void setSysCmdCharset(String sysCmdCharset) {
		this.sysCmdCharset = sysCmdCharset;
	}
}
