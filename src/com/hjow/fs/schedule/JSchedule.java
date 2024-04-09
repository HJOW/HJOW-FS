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
package com.hjow.fs.schedule;

import javax.script.ScriptEngine;

import com.hjow.fs.FSControl;

import hjow.common.util.DataUtil;

/** JScript based schedule instance */
public class JSchedule extends FSSchedule {
	private static final long serialVersionUID = -6770579881656925300L;
	
	private ScriptEngine engine;
	private String       script;
	private int          serial;
	private boolean      debug;
	
	public JSchedule(ScriptEngine engine, String script, int mainSerial) {
		this(engine, script, mainSerial, false);
	}
	
	public JSchedule(ScriptEngine engine, String script, int mainSerial, boolean debug) {
		this.engine = engine;
		this.script = script;
		this.serial = mainSerial;
		this.debug  = debug;
		
		run(this.script);
	}
	
	protected final Object run(String scripts) {
		try {
			if(debug) FSControl.log("[SCHEDULE]" + scripts + "[/SCHEDULE]", getClass());
			
			Object r = engine.eval(scripts);
			if(debug) FSControl.log(r, getClass());
			return r;
		} catch (Exception e) {
			if(debug) FSControl.log("Exception (" + e.getClass().getName() + ") " + e.getMessage(), getClass());
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public String getName() {
		return run("getName()").toString();
	}

	@Override
	public boolean loop() {
		return DataUtil.parseBoolean(run("isLoop()").toString());
	}

	@Override
	public void run() throws Exception {
		run("run(ctx_" + serial + ", ctrl_" + serial + ")");
	}

	@Override
	public int after() {
		return (int) Double.parseDouble(run("after()").toString());
	}
    
	public void onDispose() {
		run("dispose(ctx_" + serial + ", ctrl_" + serial + ")");
	}

	public boolean isDebugMode() {
		return debug;
	}

	public void setDebugMode(boolean debug) {
		this.debug = debug;
	}
}
