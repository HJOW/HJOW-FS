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
package com.hjow.fs.lister;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.hjow.fs.FSControl;
import com.hjow.fs.etc.Counter;

public class FSDefaultLister implements FSFileLister {
	@Override
	public FSFileListingResult list(FSControl ctrl, File targetDir, String keyword, final List<String> excepts) {
		List<File> chDirs  = new ArrayList<File>();
	    List<File> chFiles = new ArrayList<File>();
	    
	    if(keyword != null) {
	    	keyword = keyword.trim();
	    	if(keyword.equals("")) keyword = null;
	    	else keyword = keyword.toLowerCase();
	    }
	    
	    final Counter excepted = new Counter();
	    final String  kKeyword = keyword;
	    
	    // DIR
		File[] list = targetDir.listFiles(new FileFilter() {	
			@Override
			public boolean accept(File pathname) {
				if(! pathname.isDirectory()) return false;
				String nm = pathname.getName();
				if(nm.indexOf("." ) == 0) return false;
	            if(nm.indexOf("/" ) >= 0) return false;
	            if(nm.indexOf("\\") >= 0) return false;
	            if(nm.indexOf("<" ) >= 0) return false;
	            if(nm.indexOf(">" ) >= 0) return false;
	            if(nm.indexOf("..") >= 0) return false;
	            if(kKeyword != null && (! nm.toLowerCase().contains(kKeyword))) return false;
		    	if(excepts.contains(nm)) {
		    		excepted.increase();
		    		return false;
		    	}
				return true;
			}
		});
		
		for(File f : list) {
	    	chDirs.add(f);
	    }
		
		// Files
		list = targetDir.listFiles(new FileFilter() {	
			@Override
			public boolean accept(File pathname) {
				if(pathname.isDirectory()) return false;
				String nm = pathname.getName();
				if(nm.indexOf("." ) == 0) return false;
	            if(nm.indexOf("/" ) >= 0) return false;
	            if(nm.indexOf("\\") >= 0) return false;
	            if(nm.indexOf("<" ) >= 0) return false;
	            if(nm.indexOf(">" ) >= 0) return false;
	            if(nm.indexOf("..") >= 0) return false;
	            if(kKeyword != null && (! nm.toLowerCase().contains(kKeyword))) return false;
		    	if(excepts.contains(nm)) {
		    		excepted.increase();
		    		return false;
		    	}
				return true;
			}
		});
		
		for(File f : list) {
			chFiles.add(f);
	    }
		
		Comparator<File> comp = new Comparator<File>() {
	    	@Override
	    	public int compare(File o1, File o2) {
	    		return (o1.getName().compareTo(o2.getName()));
	    	}
	    };
		
	    Collections.sort(chDirs , comp);
	    Collections.sort(chFiles, comp);
	    
	    List<File> tDirs  = chDirs; 
	    List<File> tFiles = chFiles; 
	    chDirs  = new ArrayList<File>();
	    chFiles = new ArrayList<File>();
	    int fileIndex = 0;
	    int skipped   = 0;
	    
	    for(File f : tDirs) {
	    	if(ctrl.getLimitCount() >= 0 && fileIndex >= ctrl.getLimitCount()) {
	    		skipped++;
	    		continue;
	    	}
	    	
	    	chDirs.add(f);
	    	fileIndex++;
	    }
	    tDirs.clear();
	    
	    for(File f : tFiles) {
	    	if(ctrl.getLimitCount() >= 0 && fileIndex >= ctrl.getLimitCount()) {
	    		skipped++;
	    		continue;
	    	}
	    	
	    	if(f.length() / 1024 >= ctrl.getLimitSize()) continue;
	    	
	    	chFiles.add(f);
	    	fileIndex++;
	    }
	    tFiles.clear();
	    
	    FSFileListingResult res = new FSFileListingResult(chDirs, chFiles);
	    res.setExceptsCount(excepted.getCount());
	    res.setSkippedCount(skipped);
	    
	    return res;
	}
}
