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
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import hjow.common.util.DataUtil;
import hjow.common.util.GUIUtil;
import hjow.common.util.SecurityUtil;

public class FSUtils {
    /** Remove special characters */
    public static String removeSpecials(String originals) {
        return removeSpecials(originals, true, true, true, true, true);
    }
    
    /** Remove special characters */
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
    
    /** Remove comment string with custom comment symbol (Only support single line comment) */
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
    
    /** Get file's size to display */
    public static String getFileSize(File f) {
        return getFileSize(f.length());
    }
    
    /** Get file's size to display */
    public static String getFileSize(long lSize) {
        return DataUtil.toByteUnit(lSize, 2);
    }
    
    /** Make spaces to fill digits of string */
    public static String leftSpaces(String filled, int targettedDigit) {
        if(filled.length() >= targettedDigit) return "";
        StringBuilder res = new StringBuilder("");
        for(int idx=filled.length(); idx<targettedDigit; idx++) {
            res = res.append(" ");
        }
        return res.toString();
    }
    
    /** Find files in root directory with search keyword and maximum sizes. */
    public static List<String> find(File rootPath, String pPath, String pKeyword, long limitSize) throws IOException {
        return find(rootPath, pPath, pKeyword, limitSize);
    }
    
    /** Find files in root directory with search keyword and maximum sizes. */
    public static List<String> find(File rootPath, String pPath, String pKeyword, long limitSize, FileFilter filter) throws IOException {
        String pathParam = pPath;
        if(pathParam == null) pathParam = "";
        pathParam = pathParam.trim();
        if(pathParam.equals("/")) pathParam = "";
        pathParam = FSUtils.removeSpecials(pathParam, false, true, true, false, true).replace("\\", "/").trim();
        if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);
        
        String keyword = pKeyword;
        if(keyword == null) keyword = "";
        keyword = keyword.replace("'", "").replace("\"", "").replace("<", "").replace(">", "").trim();
        
        File dir = new File(rootPath.getCanonicalPath() + File.separator + pathParam);
        if(filter != null) {
            if(! filter.accept(dir)) return new ArrayList<String>();
        }
        
        List<String> res = find(rootPath, dir, keyword, limitSize, 0, filter);
        Collections.sort(res);
        return res;
    }
    
    /** Find files in root directory with search keyword and maximum sizes. */
    private static List<String> find(File rootPath, File dir, String keyword, long limitSize, int recursiveDepth, FileFilter filter) throws IOException {
        if(recursiveDepth >= 20) return new ArrayList<String>();
        List<String> res = new ArrayList<String>();
        
        File[] lists = dir.listFiles();
        for(File f : lists) {
            String nm = f.getName();
            if(nm.indexOf("." ) == 0) continue;
            if(nm.indexOf("/" ) >= 0) continue;
            if(nm.indexOf("\\") >= 0) continue;
            if(nm.indexOf("<" ) >= 0) continue;
            if(nm.indexOf(">" ) >= 0) continue;
            if(nm.indexOf("..") >= 0) continue;
            
            if(f.isDirectory()) {
                res.addAll(find(rootPath, f, keyword, limitSize, recursiveDepth + 1, filter));
            } else {
                if(f.length() / 1024 >= limitSize) continue;
            }
            
            if(! keyword.equals("")) { if(! nm.toLowerCase().contains(keyword.toLowerCase())) continue; }
            if(filter != null) { if(! filter.accept(f)) continue; }
            
            res.add(f.getCanonicalPath().replace(rootPath.getCanonicalPath(), ""));
        }
        
        return res;
    }
    
    /** Cut string with byte length */
    public static String cutStringSizeByte(String original, String charset, long sizes) {
        try {
            StringBuilder res = new StringBuilder("");
            int  len = original.length();
            long nowSizes = 0;
            String charOne;
            for(int idx=0; idx<len; idx++) {
                charOne = String.valueOf(original.charAt(idx));
                nowSizes += charOne.getBytes(charset).length;
                if(nowSizes > sizes) break;
                res = res.append(charOne);
            }
            return res.toString();
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    /** Receive whole parameter string to separated blocks. */
    public static List<String> delimiterSpace(String parameterString) {
        if(parameterString == null) return new ArrayList<String>();
        List<String> res = new ArrayList<String>();
        StringBuilder blockOne = new StringBuilder("");
        int len = parameterString.length();
        char quotes = ' ';
        for(int idx=0; idx<len; idx++) {
            char charOne = parameterString.charAt(idx);
            
            if(quotes == '\'') {
                if(charOne == '\'') {
                    res.add(blockOne.toString());
                    blockOne.setLength(0);
                    quotes = ' ';
                } else {
                    blockOne = blockOne.append(String.valueOf(charOne));
                }
            } else if(quotes == '"') {
                if(charOne == '"') {
                    res.add(blockOne.toString());
                    blockOne.setLength(0);
                    quotes = ' ';
                } else {
                    blockOne = blockOne.append(String.valueOf(charOne));
                }
            } else {
                if(charOne == ' ') {
                    if(blockOne.length() >= 1) res.add(blockOne.toString());
                    blockOne.setLength(0);
                } else if(charOne == '\'') {
                    quotes = '\'';
                    blockOne.setLength(0);
                } else if(charOne == '"') {
                    quotes = '"';
                    blockOne.setLength(0);
                } else {
                    blockOne = blockOne.append(String.valueOf(charOne));
                }
            }
        }
        if(blockOne.length() >= 1) res.add(blockOne.toString());
        return res;
    }
    
    /** Create random token string */
    public static String createToken(String id) {
        return createToken(id, "1", "2", "3");
    }
    
    /** Create random token string */
    public static String createToken(String id, String s1, String s2, String s3) {
        if(id == null) return "";
        if(s1 == null) s1 = "";
        if(s2 == null) s2 = "";
        if(s3 == null) s3 = "";
        String originalString = SecurityUtil.hash(s1 + id, "SHA-256") + s3 + SecurityUtil.hash(Math.round(Math.random() * 100000000) + s2 + System.currentTimeMillis(), "SHA-256");
        return SecurityUtil.hash(originalString, "SHA-256");
    }
    
    /** Create captcha image and return base64 code */
    public static String createImageCaptchaBase64(String code, int width, int height, int noises, int fontSize, boolean darkMode, List<String> fontFamily) throws IOException {
        int backr, backg, backb, forer, foreg, foreb;
        
        if(darkMode) {
            backr = 59;
            backg = 59;
            backb = 59;
            forer = 250;
            foreg = 250;
            foreb = 250;
        } else {
            backr = 250;
            backg = 250;
            backb = 250;
            forer = 0;
            foreg = 0;
            foreb = 0;
        }
        
        return GUIUtil.createImageCaptchaBase64(code, width, height, noises, fontSize, backr, backg, backb, forer, foreg, foreb, fontFamily);
    }
    
    /** Check the string can be as file's name */
    public static boolean canBeFileName(String name) {
    	if(name == null) return false;
    	if(name.contains("/") || name.contains("\\") || name.contains(File.separator)) return false;
        if(name.contains(":") || name.contains("*" ) || name.contains("&"           )) return false;
        if(name.contains("?") || name.contains("'" ) || name.contains("\""          )) return false;
        if(name.contains("|") || name.contains("<" ) || name.contains(">"           )) return false;
        if(name.length() >= 256) return false;
        return true;
    }
}
