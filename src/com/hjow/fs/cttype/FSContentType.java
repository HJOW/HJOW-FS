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
package com.hjow.fs.cttype;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FSContentType implements Serializable {
	private static final long serialVersionUID = 3398714274767470486L;
	
	public static int PREVIEW_TYPE_NONE   = -1;
    public static int PREVIEW_TYPE_IMG    = 1;
    public static int PREVIEW_TYPE_VIDEO  = 2;
    public static int PREVIEW_TYPE_AUDIO  = 3;
    public static int PREVIEW_TYPE_IFRAME = 4;
	
	protected String extension;
    protected String contentType;
    protected int previewType = PREVIEW_TYPE_NONE;
    
    public FSContentType() {
    	
    }
    public FSContentType(String ext, String contentType) {
    	this();
    	this.extension   = ext;
    	this.contentType = contentType;
    }
    public FSContentType(String ext, String contentType, int previewType) {
    	this(ext, contentType);
    	this.previewType = previewType;
    }
	public String getExtension() {
		if(extension == null) return "";
		return extension.toLowerCase().trim();
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public String getContentType() {
		if(contentType == null) return null;
		return contentType.trim();
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public int getPreviewType() {
		return previewType;
	}
	public void setPreviewType(int previewType) {
		this.previewType = previewType;
	}
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(! (o instanceof FSContentType)) return false;
		
		FSContentType t = (FSContentType) o;
		return t.getExtension().equals(getExtension());
	}
	
	public static List<FSContentType> getDefaults() {
		List<FSContentType> res = new ArrayList<FSContentType>();
		
		res.add(new FSContentType("jpg" , "image/jpeg"     , PREVIEW_TYPE_IMG));
		res.add(new FSContentType("jpeg", "image/jpeg"     , PREVIEW_TYPE_IMG));
		res.add(new FSContentType("png" , "image/png"      , PREVIEW_TYPE_IMG));
		res.add(new FSContentType("gif" , "image/gif"      , PREVIEW_TYPE_IMG));
		res.add(new FSContentType("pdf" , "application/pdf", PREVIEW_TYPE_IFRAME));
		res.add(new FSContentType("rtf" , "application/rtf", PREVIEW_TYPE_IFRAME));
		res.add(new FSContentType("ppt" , "application/vnd.ms-powerpoint", PREVIEW_TYPE_NONE));
		res.add(new FSContentType("xls" , "application/vnd.ms-excel"     , PREVIEW_TYPE_NONE));
		res.add(new FSContentType("doc" , "application/msword"           , PREVIEW_TYPE_NONE));
		res.add(new FSContentType("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", PREVIEW_TYPE_NONE));
		res.add(new FSContentType("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"        , PREVIEW_TYPE_NONE));
		res.add(new FSContentType("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"  , PREVIEW_TYPE_NONE));
		res.add(new FSContentType("odt" , "application/vnd.oasis.opendocument.text"        , PREVIEW_TYPE_NONE));
		res.add(new FSContentType("ods" , "application/vnd.oasis.opendocument.spreadsheet" , PREVIEW_TYPE_NONE));
		res.add(new FSContentType("odp" , "application/vnd.oasis.opendocument.presentation", PREVIEW_TYPE_NONE));
		res.add(new FSContentType("epub", "application/epub+zip", PREVIEW_TYPE_NONE));
		res.add(new FSContentType("wav" , "audio/wav"      , PREVIEW_TYPE_AUDIO));
		res.add(new FSContentType("mp3" , "audio/mpeg"     , PREVIEW_TYPE_AUDIO));
		res.add(new FSContentType("mp4" , "video/mp4"      , PREVIEW_TYPE_VIDEO));
		res.add(new FSContentType("mpeg", "video/mpeg"     , PREVIEW_TYPE_VIDEO));
		res.add(new FSContentType("avi" , "video/x-msvideo", PREVIEW_TYPE_VIDEO));
		
		return res;
	}
}
