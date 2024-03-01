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
package com.hjow.fs.pack;

import java.io.Serializable;
import java.util.List;

import com.hjow.fs.FSControl;

public abstract class FSPack implements Serializable {
	private static final long serialVersionUID = 8739449348309321444L;
	public abstract long getSerial();
	public abstract String getName();
	public abstract String getDescription();
	public abstract List<String> getCommandClasses();
	public abstract void init(FSControl ctrl);
	public abstract void dispose(FSControl ctrl);
	
	public boolean isAvail(int[] version) {
		return true;
	}
	
	@Override
	public boolean equals(Object others) {
		if(others == null) return false;
		if(! (others instanceof FSPack)) return false;
		
		return (getSerial() == ((FSPack) others).getSerial());
	}
}
