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
package com.hjow.fs.console;

import java.util.ArrayList;
import java.util.List;

public class FSConsoleMultipleResult extends FSConsoleResult {
	private static final long serialVersionUID = -2445571763390309432L;
    protected List<FSConsoleResult> children = new ArrayList<FSConsoleResult>();
    
    public FSConsoleMultipleResult(List<FSConsoleResult> children) {
    	this.children = children;
    }
    
	public List<FSConsoleResult> getChildren() {
		return children;
	}
	public void setChildren(List<FSConsoleResult> children) {
		this.children = children;
	}
	public void setPath(String path) {
		this.path = path;
		for(FSConsoleResult c : children) {
			c.setPath(path);
		}
	}
	public boolean isNulll() {
		for(FSConsoleResult c : children) {
			if(! c.isNulll()) {
				return false;
			}
		}
		return true;
	}
	public void setNulll(boolean nulll) {
		throw new RuntimeException("Not supported method");
	}
	public void setDisplay(String display) {
		throw new RuntimeException("Not supported method");
	}
	public void setSuccess(boolean b) {
		throw new RuntimeException("Not supported method");
	}
	public String getDisplay() {
		StringBuilder res = new StringBuilder("");
		for(FSConsoleResult c : children) {
			if(c.isNulll()) continue;
			res = res.append(c.getDisplay()).append("\n");
		}
		return res.toString().trim();
	}
	public boolean isSuccess() {
		for(FSConsoleResult c : children) {
			if(! c.isSuccess()) {
				return false;
			}
		}
		return true;
	}
}
