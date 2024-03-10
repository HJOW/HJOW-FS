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
package com.hjow.fs;

import java.io.Closeable;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import hjow.common.util.ClassUtil;

public class FSProtocolHandler implements Closeable {
	private static final FSProtocolHandler instances = new FSProtocolHandler();
	public static FSProtocolHandler getInstnace() { return instances; }
	private FSProtocolHandler() { }
	
    protected FSControl ctrl = FSControl.getInstance();
    
    public void handle(HttpServletRequest request, HttpServletResponse response) {
    	if(ctrl == null) ctrl = FSControl.getInstance();
    }
    
    public static void disposeInstance() {
    	ClassUtil.closeAll(getInstnace());
    }
    
	@Override
	public void close() throws IOException {
		ctrl = null;
	}
}
