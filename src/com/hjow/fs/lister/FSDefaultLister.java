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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.hjow.fs.FSControl;

public class FSDefaultLister implements FSFileLister {
	@Override
	public FSFileListingResult list(FSControl ctrl, File targetDir, List<String> excepts) {
		File[] list = targetDir.listFiles();
	    List<File> fileList = new ArrayList<File>();
	    if(list == null) list = new File[0];
	    
	    int excepted  = 0;
	    
	    for(File f : list) {
	    	String nm = f.getName();
	    	if(excepts.contains(nm)) {
	    		excepted++;
	    		continue;
	    	}
	    	fileList.add(f);
	    }
	    list = null;
	    
	    Collections.sort(fileList, new Comparator<File>() {
	    	@Override
	    	public int compare(File o1, File o2) {
	    		if(o1.isDirectory() && (! o2.isDirectory())) return -1;
	    		if((! o1.isDirectory()) && o2.isDirectory()) return 1;
	    		return (o1.getName().compareTo(o2.getName()));
	    	}
	    });
	    
	    List<File> chDirs  = new ArrayList<File>();
	    List<File> chFiles = new ArrayList<File>();
	    
	    int fileIndex = 0;
	    int skipped   = 0;
	    for(File f : fileList) {
	    	String nm = f.getName();
	    	if(ctrl.getLimitCount() >= 0 && fileIndex >= ctrl.getLimitCount()) {
	    		skipped++;
	    		continue;
	    	}
	    	if(nm.indexOf("." ) == 0) continue;
            if(nm.indexOf("/" ) >= 0) continue;
            if(nm.indexOf("\\") >= 0) continue;
            if(nm.indexOf("<" ) >= 0) continue;
            if(nm.indexOf(">" ) >= 0) continue;
            if(nm.indexOf("..") >= 0) continue;
	        if(f.isDirectory()) {
	            chDirs.add(f);
	        } else {
	            if(f.length() / 1024 >= ctrl.getLimitSize()) continue;
	            chFiles.add(f);
	        }
	        fileIndex++;
	    }
	    
	    FSFileListingResult res = new FSFileListingResult(chDirs, chFiles);
	    res.setExceptsCount(excepted);
	    res.setSkippedCount(skipped);
	    
	    return res;
	}
}
