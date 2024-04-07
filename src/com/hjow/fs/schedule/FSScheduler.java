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

import java.util.LinkedList;
import java.util.List;
import com.hjow.fs.FSControl;
import hjow.common.core.Releasable;

public class FSScheduler extends Thread implements Releasable {
	private static final long serialVersionUID = -3110662917346474589L;
	protected volatile boolean threadSwitch = true;
	protected volatile boolean running = false;
	protected volatile List<FSSchedule> schedules = new LinkedList<FSSchedule>();
    private FSScheduler() { }
    
    @Override
	public void run() {
		while(threadSwitch) {
			oneCycle();
			
            try {
				Thread.sleep(1000L);
			} catch(InterruptedException e) {
				break;
			}
		}
		running = false;
	}
    
    @Override
    public void start() {
    	if(running) return;
    	running      = true;
    	threadSwitch = true;
    	super.start();
    }
    
    protected synchronized void prepareCycle() {
    	int index = 0;
    	while(index < schedules.size()) {
    		FSSchedule once = schedules.get(index);
    		once.setLefts(once.after());
    		index++;
    	}
    }
    
    protected synchronized void oneCycle() {
    	int index = 0;
    	while(threadSwitch && index < schedules.size()) {
    		try {
    			FSSchedule once = schedules.get(index);
    			
    			once.decrease();
    			
    			if(once.getLefts() == 0) {
    				once.run();
    				
    				if(once.loop()) {
    					once.setLefts(once.after());
    				} else {
    					once.setLefts(-1);
    					schedules.remove(once);
    					continue;
    				}
    			}
    		} catch(Exception e) {
    			if(e instanceof InterruptedException) break;
    			FSControl.log(e, FSScheduler.class);
    		}
    		
    		index++;
    	}
    } 
    
    @Override
	public void releaseResource() {
    	threadSwitch = false;
    	try { interrupt();       } catch(Throwable ignores) {}
    	try { schedules.clear(); } catch(Throwable ignores) {}
	}
    
    public static void add(FSSchedule schedule) {
    	instance.schedules.add(schedule);
    }
    
    public static boolean isRunning() {
    	return instance.running;
    }
    
    public static void startCycles() {
    	instance.start();
    }
    
    public static void dispose() {
    	instance.releaseResource();
    }
    
    public static final FSScheduler instance = new FSScheduler();
}
