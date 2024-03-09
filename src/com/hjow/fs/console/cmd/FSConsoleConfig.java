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

import hjow.common.json.JsonObject;
import hjow.common.util.DataUtil;

public class FSConsoleConfig implements FSBundledConsoleCommand {
	private static final long serialVersionUID = -7730396143867553901L;

	@Override
    public String getName() {
        return "config";
    }

    @Override
    public String getShortName() {
        return "cf";
    }

    @Override
    public Object run(FSControl ctrl, FSConsole console, Map<String, Object> sessionMap, File root, String parameter, Map<String, String> options) throws Throwable {
        if(sessionMap               == null) throw new RuntimeException("No privilege");
        if(sessionMap.get("idtype") == null) throw new RuntimeException("No privilege");
        
        String idtype = sessionMap.get("idtype").toString().trim().toUpperCase();
        if(! idtype.equals("A")) throw new RuntimeException("No privilege");
        
        StringBuilder collector = new StringBuilder("");
        
        JsonObject json = ctrl.getConfig();
        if(DataUtil.isEmpty(parameter)) {
        	Set<String> keys = json.keySet();
        	for(String k : keys) {
        		if(k.equals("S1") || k.equals("S2") || k.equals("S3")) continue;
        		if(k.equals("Salt")) continue;
        		if(k.equals("sHiddenDirs")) continue;
        		if(k.equals("HiddenDirs" )) continue;
        		if(k.equals("Installed"  )) continue;
        		if(k.equals("UseAccount" )) continue;
        		if(k.equals("UseConsole" )) continue;
        		if(k.equals("Path"       )) continue;
        		if(k.equals("Packs"      )) continue;
        		
        		collector = collector.append("\n").append(k);
        	}
        } else {
        	if(! json.containsKey(parameter)) throw new RuntimeException("There is no config key " + parameter);
        	if(parameter.equals("S1") || parameter.equals("S2") || parameter.equals("S3")) throw new RuntimeException("Cannot edit the config " + parameter);
    		if(parameter.equals("Salt")) throw new RuntimeException("Cannot edit the config " + parameter);
    		if(parameter.equals("sHiddenDirs")) throw new RuntimeException("Cannot edit the config " + parameter);
    		if(parameter.equals("HiddenDirs" )) throw new RuntimeException("Cannot edit the config " + parameter);
    		if(parameter.equals("Installed"  )) throw new RuntimeException("Cannot edit the config " + parameter);
    		if(parameter.equals("UseAccount" )) throw new RuntimeException("Cannot edit the config " + parameter);
    		if(parameter.equals("UseConsole" )) throw new RuntimeException("Cannot edit the config " + parameter);
    		if(parameter.equals("Path"       )) throw new RuntimeException("Cannot edit the config " + parameter);
    		if(parameter.equals("Packs"      )) throw new RuntimeException("Cannot edit the config " + parameter);
        	
        	String editOpt = null;
        	if(options.get("e") != null) {
        		editOpt = options.get("e").trim();
        	} else if(options.get("edit") != null) {
        		editOpt = options.get("edit").trim();
        	}
        	
        	if(editOpt != null) {
        		json.put(parameter, editOpt);
        		ctrl.setConfig(json);
        		ctrl.applyModifiedConfig(sessionMap.get("id").toString());
        		ctrl.applyConfigs();
        		return "Success !";
        	} else {
        		collector = collector.append(json.get(parameter));
        	}
        }
        
        
        return collector.toString().trim();
    }

    @Override
    public String getHelp(String lang, boolean detail) {
        StringBuilder res = new StringBuilder("");
        if(detail) {
            if(lang.equals("ko")) {
                res = res.append(" * config").append("\n");
                res = res.append("                                                                                  ").append("\n");
                res = res.append("    FS 설정을 조회하거나 변경할 수 있습니다.                                      ").append("\n");
                res = res.append("    관리자 권한이 필요합니다.                                                     ").append("\n");
                res = res.append("    매개변수 없이 호출 시, 설정 항목들을 출력합니다.                              ").append("\n");
                res = res.append("    매개변수로 설정 항목을 입력하면 그 값을 출력합니다.                           ").append("\n");
                res = res.append("    -e 옵션 지정 후 값 입력 시 설정 항목의 값을 변경합니다.                       ").append("\n");
                res = res.append("                                                                                  ").append("\n");
                res = res.append(" * 예").append("\n");
                res = res.append("                                                                                  ").append("\n");
                res = res.append("    config                                                                        ").append("\n");
                res = res.append("    config Title                                                                  ").append("\n");
                res = res.append("    config Title -e 'File Storage'                                                ").append("\n");
                res = res.append("                                                                                  ").append("\n");
            } else {
                res = res.append(" * config").append("\n");
                res = res.append("                                                                                  ").append("\n");
                res = res.append("    You can see or edit FS configs.                                               ").append("\n");
                res = res.append("    Need administration privilege.                                                ").append("\n");
                res = res.append("    Run without parameter, then all keys of config will be printed.               ").append("\n");
                res = res.append("    Run with parameter as config key, then current value of key will be printed.  ").append("\n");
                res = res.append("    Put -e option with value to edit config.                                      ").append("\n");
                res = res.append("                                                                                  ").append("\n");
                res = res.append(" * example").append("\n");
                res = res.append("                                                                                  ").append("\n");
                res = res.append("    config                                                                        ").append("\n");
                res = res.append("    config Title                                                                  ").append("\n");
                res = res.append("    config Title -e 'File Storage'                                                ").append("\n");
                res = res.append("                                                                                  ").append("\n");
            }
        } else {
            if(lang.equals("ko")) {
                res = res.append("FS 설정을 조회하거나 변경할 수 있습니다.").append("\n");
            } else {
                res = res.append("You can see or edit FS configs.").append("\n");
            }
        }
        return res.toString().trim();
    }
}
