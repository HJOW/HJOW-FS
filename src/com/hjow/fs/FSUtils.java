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
}
