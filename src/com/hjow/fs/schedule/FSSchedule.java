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

import java.io.Serializable;

public abstract class FSSchedule implements Serializable {
	private static final long serialVersionUID = -5030699318326146541L;
	private int lefts = 0;
	
	/** Get schedule's name */
	public abstract String  getName();
	/** If this is true, then called repeats. If this is first, then called only once. */
	public abstract boolean loop();
	/** Action */
	public abstract void    run() throws Exception;
	/** Calling time (seconds) If 'loop' is true, this value will be a gap. */
	public abstract int     after();
	
	/** This method is called by FSScheduler. */
	public final void decrease() { lefts--; }
	/** This method is called by FSScheduler. */
	public final int getLefts() {
		return lefts;
	}
	/** This method is called by FSScheduler. */
	public final void setLefts(int lefts) {
		this.lefts = lefts;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(obj instanceof FSSchedule) {
			return getName().equals(((FSSchedule) obj).getName());
		}
		return false;
	}
}
