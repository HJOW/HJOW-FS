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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import hjow.common.util.DataUtil;
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
    		backr = 0;
    		backg = 0;
    		backb = 0;
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
    	
    	return createImageCaptchaBase64(code, width, height, noises, fontSize, backr, backg, backb, forer, foreg, foreb, fontFamily);
    }
    
    /** Create captcha image and return base64 code */
    public static String createImageCaptchaBase64(String code, int width, int height, int noises, int fontSize, int backr, int backg, int backb, int forer, int foreg, int foreb, List<String> fontFamily) throws IOException {
    	if(code == null) {
            code = "REFRESH";
        }
        
        if(fontFamily == null) {
        	fontFamily = new ArrayList<String>();
        }
        if(fontFamily.isEmpty()) {
        	fontFamily.add("Serif");
        	fontFamily.add("Arial");
        	fontFamily.add("Helvetica");
        }
        
        BufferedImage image    = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D    graphics = image.createGraphics();
        
        if(backr <    0) backr =   0;
        if(backr >= 255) backr = 255;
        
        if(backg <    0) backg =   0;
        if(backg >= 255) backg = 255;
        
        if(backb <    0) backb =   0;
        if(backb >= 255) backb = 255;

        graphics.setColor(new Color(backr, backg, backb));
        graphics.fillRect(0, 0, width, height);

        FontMetrics metrics = graphics.getFontMetrics();
        int fontWidth = metrics.stringWidth(code);
        int gap       = (width - fontWidth) / (code.length() + 1);
        int x         = gap;
        int y         = (height - metrics.getHeight()) / 2 + metrics.getAscent();
        
        int calcr, calcg, calcb, colorChangeRange, fontRand, fontFamilyCnt, fontStyle;
        
        colorChangeRange = 80;
        fontFamilyCnt = fontFamily.size();
        fontStyle = Font.BOLD;

        // 방해물 출력
        for(int ndx=0; ndx<noises; ndx++) {
            int x1, y1, x2, y2;
            x1 = (int) (Math.random() * width);
            y1 = (int) (Math.random() * height);
            x2 = x1 + (int) (Math.random() * (width  / 2));
            y2 = y1 + (int) (Math.random() * (height / 2));
            
            if(forer >= 128) calcr = forer - (int) (Math.random() * colorChangeRange);
            else             calcr = forer + (int) (Math.random() * colorChangeRange);
            
            if(forer >= 128) calcg = foreg - (int) (Math.random() * colorChangeRange);
            else             calcg = foreg + (int) (Math.random() * colorChangeRange);
            
            if(forer >= 128) calcb = foreb - (int) (Math.random() * colorChangeRange);
            else             calcb = foreb + (int) (Math.random() * colorChangeRange);
            
            while(Math.abs(backr - calcr) <= 15 && Math.abs(backg - calcg) <= 15 && Math.abs(backb - calcb) <= 15) {
            	if(forer >= 128) calcr = forer - (int) (Math.random() * colorChangeRange);
                else             calcr = forer + (int) (Math.random() * colorChangeRange);
                
                if(forer >= 128) calcg = foreg - (int) (Math.random() * colorChangeRange);
                else             calcg = foreg + (int) (Math.random() * colorChangeRange);
                
                if(forer >= 128) calcb = foreb - (int) (Math.random() * colorChangeRange);
                else             calcb = foreb + (int) (Math.random() * colorChangeRange);
            }
            
            if(calcr <    0) calcr =   0;
            if(calcr >= 255) calcr = 255;
            
            if(calcg <    0) calcg =   0;
            if(calcg >= 255) calcg = 255;
            
            if(calcb <    0) calcb =   0;
            if(calcb >= 255) calcb = 255;
            
            graphics.setColor(new Color(calcr, calcg, calcb));
            graphics.drawLine(x1, y1, x2, y2);
        }

        // 글자 출력
        for(int idx=0; idx<code.length(); idx++) {
            char charNow = code.charAt(idx);
            
            if(forer >= 128) calcr = forer - (int) (Math.random() * colorChangeRange);
            else             calcr = forer + (int) (Math.random() * colorChangeRange);
            
            if(forer >= 128) calcg = foreg - (int) (Math.random() * colorChangeRange);
            else             calcg = foreg + (int) (Math.random() * colorChangeRange);
            
            if(forer >= 128) calcb = foreb - (int) (Math.random() * colorChangeRange);
            else             calcb = foreb + (int) (Math.random() * colorChangeRange);
            
            while(Math.abs(backr - calcr) <= 15 && Math.abs(backg - calcg) <= 15 && Math.abs(backb - calcb) <= 15) {
            	if(forer >= 128) calcr = forer - (int) (Math.random() * colorChangeRange);
                else             calcr = forer + (int) (Math.random() * colorChangeRange);
                
                if(forer >= 128) calcg = foreg - (int) (Math.random() * colorChangeRange);
                else             calcg = foreg + (int) (Math.random() * colorChangeRange);
                
                if(forer >= 128) calcb = foreb - (int) (Math.random() * colorChangeRange);
                else             calcb = foreb + (int) (Math.random() * colorChangeRange);
            }
            
            if(calcr <    0) calcr =   0;
            if(calcr >= 255) calcr = 255;
            
            if(calcg <    0) calcg =   0;
            if(calcg >= 255) calcg = 255;
            
            if(calcb <    0) calcb =   0;
            if(calcb >= 255) calcb = 255;
            
            graphics.setColor(new Color(calcr, calcg, calcb));
            
            int nowX = x + metrics.charWidth(charNow) / 2;
            int ang  = ((int) (Math.random() * 41)) - 20;
            
            graphics.rotate(Math.toRadians(ang), nowX, y);
            
            fontRand = (int) Math.floor(Math.random() * fontFamilyCnt);
            if(fontRand >= fontFamilyCnt) fontRand = fontFamilyCnt - 1;
            if(fontRand < 0) fontRand = 0;
            
            if(Math.random() >= 0.5) fontStyle = Font.BOLD;
            else                     fontStyle = Font.BOLD | Font.ITALIC;
            
            graphics.setFont(new Font(fontFamily.get(fontRand), fontStyle, fontSize + ((int) Math.random() * 4) - 2));
            graphics.drawString(String.valueOf(charNow), nowX, y + ((int) ((Math.random() * height) / 3.0)));
            graphics.rotate(Math.toRadians(ang) * (-1), nowX, y);
            
            x += metrics.charWidth(charNow) + gap;
        }

        ByteArrayOutputStream binary = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", binary);
        image    = null;
        graphics = null;

        String bs64str = SecurityUtil.base64String(binary.toByteArray());
        binary = null;
        return bs64str;
    }
}
