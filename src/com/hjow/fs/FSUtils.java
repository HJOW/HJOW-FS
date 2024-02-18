package com.hjow.fs;

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
