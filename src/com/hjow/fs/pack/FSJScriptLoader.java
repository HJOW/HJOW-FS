/*
Copyright 2024 HJOW

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
package com.hjow.fs.pack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletContext;

import com.hjow.fs.FSControl;
import com.hjow.fs.schedule.FSScheduler;
import com.hjow.fs.schedule.JSchedule;

import hjow.common.json.JsonArray;
import hjow.common.json.JsonCompatibleUtil;
import hjow.common.json.JsonObject;
import hjow.common.script.MathObject;
import hjow.common.script.PrimitiveObject;
import hjow.common.util.ClassUtil;
import hjow.common.util.DataUtil;
import hjow.common.util.FileUtil;

/** Load script files from config directory and register as schedules. */
public class FSJScriptLoader {
    protected List<JSchedule> schedules = new Vector<JSchedule>();
    protected ScriptEngineManager man;
    
    private int     mainSerial = new Random().nextInt();
    private boolean debug      = false;
    
    private PrimitiveObject primitives      = PrimitiveObject.getInstance();
    private MathObject      maths           = MathObject.getInstance();
    private int             primitiveSerial = new Random().nextInt();
    private int             mathSerial      = new Random().nextInt();
    
    /** Called by FSServletContextListener */
    public void initializing(ServletContext ctx, FSControl ctrl) {
        InputStream       in1 = null;
        InputStreamReader in2 = null;
        BufferedReader    in3 = null;
        StringBuilder     reads = new StringBuilder("");
        try {
            debug = FSControl.getInstance().getBoolConfig("JSDebug");
            
            File fCf = ctrl.getFileConfigPath();
            if(! fCf.exists()) fCf.mkdirs();
            
            File fJs = new File(fCf.getCanonicalPath() + File.separator + "jscript");
            if(! fJs.exists()) {
                fJs.mkdirs();
                createManualFile(new File(fJs.getCanonicalPath() + File.separator + "README.txt"));
            }
            
            File fJsLib = new File(fJs.getCanonicalPath() + File.separator + "lib");
            if(! fJsLib.exists()) fJsLib.mkdirs();
            
            in1 = this.getClass().getResourceAsStream("/jscript.json");
            if(in1 == null) return;
            
            in2 = new InputStreamReader(in1, "UTF-8");
            in3 = new BufferedReader(in2);
            String line;
            while(true) {
                line = in3.readLine();
                if(line == null) break;
                reads = reads.append(line).append("\n");
            }
            
            String sjson = reads.toString().trim();
            reads.setLength(0);
            reads = null;
            
            ClassUtil.closeAll(in3, in2, in1);
            in3 = null;
            in2 = null;
            in1 = null;
            
            JsonObject json = (JsonObject) JsonCompatibleUtil.parseJson(sjson);
            sjson = null;
            
            String sUse = json.get("use") == null ? null : json.get("use").toString();
            if(! DataUtil.parseBoolean(sUse)) return;
            
            if(! debug) {
                String sDebug = json.get("debug") == null ? null : json.get("debug").toString();
                debug = DataUtil.parseBoolean(sDebug);
            }
            
            if(man == null) man = new ScriptEngineManager();
            man.put("ctx_" + mainSerial, ctx );
            man.put("ctrl_"+ mainSerial, ctrl);
            man.put(primitives.getPrefixName() + "_" + primitiveSerial, primitives);
            man.put(maths.getPrefixName()      + "_" + mathSerial     , maths     );
            
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if(pathname.isDirectory()) return false;
                    return pathname.getName().toLowerCase().endsWith(".js");
                }
            };
            
            File[] listLib = fJsLib.listFiles(filter);
            File[] listJs  = fJs.listFiles(filter);
            
            for(File f : listJs) {
                loadOne(f, listLib);
            }
            listJs = null;
            
            File[] listJsDirs = fJs.listFiles(new FileFilter() {    
                @Override
                public boolean accept(File pathname) {
                    if(pathname.isDirectory()) {
                        String name = pathname.getName().toLowerCase();
                        if(name.equals("lib")) return false;
                        if(name.startsWith(".")) return false;
                        return true;
                    }
                    return false;
                }
            });
            
            for(File dir : listJsDirs) {
                listJs = dir.listFiles(filter);
                for(File f : listJs) {
                    loadOne(f, listLib);
                }
            }
            listJs = null;
            
            JsonArray arr = (JsonArray) JsonCompatibleUtil.parseJson(json.get("file"));
            if(arr == null) return;
            
            for(Object one : arr) {
                if(one == null) continue;
                
                String fileNameOne = one.toString();
                File fileOne = new File(fileNameOne);
                if(! fileOne.exists()) continue;
                
                if(fileOne.isDirectory()) {
                    File[] children = fileOne.listFiles(filter);
                    for(File child : children) {
                        loadOne(child, listLib);
                    }
                } else {
                    if(fileOne.getName().toLowerCase().endsWith(".js")) loadOne(fileOne, listLib);
                }
            }
        } catch (Exception e) {
            FSControl.log(e, getClass());
        } finally {
            ClassUtil.closeAll(in3, in2, in1);
        }
    }
    
    protected void loadOne(File fileJson, File[] libs) throws IOException, ScriptException {
        if(! fileJson.exists()) return;
        
        String content = FileUtil.readString(fileJson, "UTF-8");
        if(DataUtil.isEmpty(content)) return;
        
        ScriptEngine engine = man.getEngineByName("JavaScript");
        engine.eval(maths.getInitScript(maths.getPrefixName() + "_" + mathSerial));
        engine.eval(primitives.getInitScript(primitives.getPrefixName() + "_" + primitiveSerial));
        
        if(libs != null) {
            for(File f : libs) {
                String contentLib = FileUtil.readString(f);
                if(! DataUtil.isEmpty(contentLib)) engine.eval(contentLib);
            }
        }
        
        JSchedule schedule = new JSchedule(engine, content, mainSerial, debug);
        schedules.add(schedule);
        
        FSScheduler.add(schedule);
    }
    
    /** Called by FSServletContextListener */
    public void destroying(ServletContext ctx, FSControl ctrl) {
        for(JSchedule sche : schedules) {
            try { sche.onDispose(); } catch(Exception e) { FSControl.log(e, getClass()); }
        }
        schedules.clear();
    }
    
    /** Create README.txt file 
     * @throws IOException */
    protected void createManualFile(File f) throws IOException {
        StringBuilder res = new StringBuilder("");
        
        res = res.append("/*").append("\n");
        res = res.append("This file is written on UTF-8.").append("\n");
        res = res.append("").append("\n");
        res = res.append("Write js file in this directory to register 'schedule' on FS.").append("\n");
        res = res.append("See following example...").append("\n");
        res = res.append("*/").append("\n");
        res = res.append("").append("\n");
        res = res.append("function getName() {                                                                             ").append("\n");
        res = res.append("    // Return unique name                                                                          ").append("\n");
        res = res.append("    return 'schedule_01';                                                                          ").append("\n");
        res = res.append("}                                                                                                ").append("\n");
        res = res.append("                                                                                                 ").append("\n");
        res = res.append("function isLoop() {                                                                              ").append("\n");
        res = res.append("    // If return false, this schedule will be called once only.                                    ").append("\n");
        res = res.append("    // If return true, this schedule will be called repeats.                                       ").append("\n");
        res = res.append("    return false;                                                                                  ").append("\n");
        res = res.append("}                                                                                                ").append("\n");
        res = res.append("                                                                                                 ").append("\n");
        res = res.append("function after() {                                                                               ").append("\n");
        res = res.append("    // If isLoop() return false, this schedule will be called after 'AFTER' seconds.               ").append("\n");
        res = res.append("    // If isLoop() return true , this schedule will be called repeatly between 'AFTER' seconds.    ").append("\n");
        res = res.append("    // This value is used as a 'AFTER' value.                                                      ").append("\n");
        res = res.append("    return 1;                                                                                      ").append("\n");
        res = res.append("}                                                                                                ").append("\n");
        res = res.append("                                                                                                 ").append("\n");
        res = res.append("function run(ctx, ctrl) {                                                                        ").append("\n");
        res = res.append("    // This function is a real job of the schedule.                                                ").append("\n");
        res = res.append("    //                                                                                             ").append("\n");
        res = res.append("    // ctx  : ServletContext instance                                                              ").append("\n");
        res = res.append("    // ctrl : FSControl instance                                                                   ").append("\n");
        res = res.append("    //                                                                                             ").append("\n");
        res = res.append("    // Write codes here to do something when this schedule called.                                 ").append("\n");
        res = res.append("}                                                                                                ").append("\n");
        res = res.append("                                                                                                 ").append("\n");
        res = res.append("function dispose(ctx, ctrl) {                                                                    ").append("\n");
        res = res.append("    // This function is called when the server preparing to shutdown.                              ").append("\n");
        res = res.append("    //                                                                                             ").append("\n");
        res = res.append("    // ctx  : ServletContext instance                                                              ").append("\n");
        res = res.append("    // ctrl : FSControl instance                                                                   ").append("\n");
        res = res.append("    //                                                                                             ").append("\n");
        res = res.append("    // Write codes here to do something when the server is shutdown.                               ").append("\n");
        res = res.append("}                                                                                                ").append("\n");
        
        FileUtil.writeString(f, "UTF-8", res.toString().trim());
    }
}
