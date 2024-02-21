package com.hjow.fs;
/*
Copyright 2024 HJOW (Heo Jin Won)

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

import java.io.File;
import java.util.StringTokenizer;

public class FSUtils {
	public static String removeSpecials(String originals) {
		return removeSpecials(originals, true, true, true, true, true);
	}
    public static String removeSpecials(String originals, boolean noSpace, boolean noQuote, boolean noTagBlace, boolean noSlice, boolean noDot) {
    	if(originals == null) return null;
    	String res = originals.replace("\n", "").replace("\t", "");
    	if(noSpace   ) res = res.replace(" ", "");
    	if(noSlice   ) res = res.replace("\\", "").replace("/", "");
    	if(noQuote   ) res = res.replace("'", "").replace("\"", "");
    	if(noTagBlace) res = res.replace("<", "").replace(">", "");
    	if(noDot     ) res = res.replace(".", "");
    	return res.trim();
    }
    public static String removeLineComments(String original, char commentStartChar) {
    	boolean firstline = true;
    	StringBuilder result = new StringBuilder("");
    	StringTokenizer lineTokenizer = new StringTokenizer(original, "\n");
    	while(lineTokenizer.hasMoreTokens()) {
    		String line = lineTokenizer.nextToken();
    		StringBuilder resLine = new StringBuilder("");
    		
    		char quote = ' ';
    		for(int idx=0; idx<line.length(); idx++) {
    			char charOne = line.charAt(idx);
    			
    			if(quote == ' ') {
    				if(charOne == '"') {
    					quote = '"';
    					resLine = resLine.append(String.valueOf(charOne));
    					continue;
    				} else if(charOne == '\'') {
    					quote = '\'';
    					resLine = resLine.append(String.valueOf(charOne));
    					continue;
    				}
    				if(charOne == commentStartChar) break;
    			} else if(quote == '\'') {
    				if(charOne == '\'') {
    					quote = ' ';
    					resLine = resLine.append(String.valueOf(charOne));
    					continue;
    				}
    			} else if(quote == '"') {
    				if(charOne == '"') {
    					quote = ' ';
    					resLine = resLine.append(String.valueOf(charOne));
    					continue;
    				}
    			}
    			
    			resLine = resLine.append(String.valueOf(charOne));
    		}
    		
    		if(firstline) result = result.append("\n");
    		result = result.append(resLine.toString());
    		firstline = false;
    	}
    	return result.toString();
    }
    
    public static String getFileSize(File f) {
	    long   lSize = f.length();
	    String sUnit = "byte";
	    String comp  = "" + lSize + " " + sUnit;
	    
	    if(lSize < 0) lSize = 0;
	    if(lSize <= 1) {
	        sUnit = "byte";
	        comp = lSize + " " + sUnit;
	    }
	    
	    if(lSize >= 1024) {
	        sUnit = "KB";
	        comp  = (Math.round(( lSize / 1024.0 ) * 10) / 10.0) + " " + sUnit;
	        lSize = lSize / 1024;
	    }
	    
	    if(lSize >= 1024) {
	        sUnit = "MB";
	        comp  = (Math.round(( lSize / 1024.0 ) * 10) / 10.0) + " " + sUnit;
	        lSize = lSize / 1024;
	    }
	    
	    if(lSize >= 1024) {
	        sUnit = "GB";
	        comp  = (Math.round(( lSize / 1024.0 ) * 10) / 10.0) + " " + sUnit;
	        lSize = lSize / 1024;
	    }
	    
	    if(lSize >= 1024) {
	        sUnit = "TB";
	        comp  = (Math.round(( lSize / 1024.0 ) * 10) / 10.0) + " " + sUnit;
	    }
	    
	    return comp;
	}
}
