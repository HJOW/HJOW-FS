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
package com.hjow.fs.etc;

import java.io.Serializable;

import com.hjow.fs.FSUtils;

import hjow.common.json.JsonObject;

public class BrowserInfo implements Serializable {
	private static final long serialVersionUID = -6262644840723973886L;

	protected String name    = "Unknown";
    protected String nm      = "unknown";
    protected String version = "-1";
    protected double ver     = -1;
    protected String agent   = "unknown";
    protected BrowserInfo compatible = null;
    
    public BrowserInfo() {
    	
    }
    
	public BrowserInfo(String name, String nm, String version, double ver, String agent) {
		super();
		this.name = name;
		this.nm = nm;
		this.version = version;
		this.ver = ver;
		this.agent = agent;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNm() {
		return nm;
	}
	public void setNm(String nm) {
		this.nm = nm;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public double getVer() {
		return ver;
	}
	public void setVer(double ver) {
		this.ver = ver;
	}
	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
	}
	public BrowserInfo getCompatible() {
		return compatible;
	}
	public void setCompatible(BrowserInfo compatible) {
		this.compatible = compatible;
	}
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		
		json.put("name"   , getName());
		json.put("nm"     , getNm());
		json.put("version", getVersion());
		json.put("ver"    , getVer());
		json.put("agent"  , getAgent());
		
		return json;
	}
	@Override
	public String toString() {
		return toJson().toJSON();
	}
	
	public static BrowserInfo byUserAgent(String userAgent) {
    	String   agent  = userAgent;
        String[] splits = agent == null ? new String[0] : agent.split(" ");
        int idx=0;
        
        if(agent == null) agent = "Unknown";
        
        // Detect Microsoft Internet Explorer
        for(idx=0; idx<splits.length; idx++) {
            String   blockOne = splits[idx];
            String[] splitIn  = blockOne.split("/");
            if(splitIn.length != 2) continue;
            if("Trident".equals(splitIn[0])) {
                BrowserInfo res = new BrowserInfo();
                res.setName("Microsoft Internet Explorer");
                try {
                    double version = Math.round(Double.parseDouble(String.valueOf(splitIn[1])));
                    version += 4;

                    res.setName("ie");
                    res.setVer(version);
                    res.setVersion(version + "");
                    
                    // Check Compatible Mode
                    boolean compatible = false;
                    int idx2 = 0;
                    for(idx2=0; idx2<splits.length; idx2++) {
                        String blockTwo = splits[idx2];
                        if(blockTwo.indexOf("compatible;") >= 0) { compatible = true; break; }
                    }
                    if(compatible) {
                    	BrowserInfo comp = new BrowserInfo();
                    	comp.setName(res.getName());
                    	comp.setNm(res.getNm());
                    	comp.setVer(-1);
                    	
                    	res.setCompatible(comp);
                        
                        int msieIdx = -1; 
                        for(idx2=0; idx2<splits.length; idx2++) {
                            String blockTwo = splits[idx2];
                            if(blockTwo.indexOf("MSIE") >= 0) { msieIdx = idx2; break; }
                        }
                        if(msieIdx >= 0 && splits.length > msieIdx + 1) {
                            String blockTwo = splits[msieIdx + 1];
                            comp.setVer(FSUtils.parseFloatFirstBlock(blockTwo));
                        }
                        comp.setVersion(comp.getVer() + "");
                        
                        res.setCompatible(comp);
                    } else {
                        res.setCompatible(null);
                    }
                    
                    res.setAgent(agent);
                    return res;
                } catch(Throwable tx) {
                	return new BrowserInfo(res.getName(), "ie", "Unknown", (double) -1, agent);
                }
            }
        }
        
        // Detect Opera
        for(idx=0; idx<splits.length; idx++) {
            String blockOne = splits[idx];
            String[] splitIn = blockOne.split("/");
            if(splitIn.length != 2) continue;
            if("OPR".equals(splitIn[0]  )) return new BrowserInfo("Opera", "opera", splitIn[1], FSUtils.parseFloatFirstBlock(splitIn[1]), agent);
            if("opera".equals(splitIn[0])) return new BrowserInfo("Opera", "opera", splitIn[1], FSUtils.parseFloatFirstBlock(splitIn[1]), agent);
        }
        
        // Detect Brave
        for(idx=0; idx<splits.length; idx++) {
        	String   blockOne = splits[idx];
        	String[] splitIn  = blockOne.split("/");
            if(splitIn.length != 2) continue;
            if("Brave".equals(splitIn[0])) return new BrowserInfo("Brave", "brave", splitIn[1], FSUtils.parseFloatFirstBlock(splitIn[1]), agent);
        }
        
        // Detect Microsoft Edge
        for(idx=0; idx<splits.length; idx++) {
        	String   blockOne = splits[idx];
        	String[] splitIn  = blockOne.split("/");
            if(splitIn.length != 2) continue;
            if("Edge".equals(splitIn[0])) return new BrowserInfo("Microsoft Edge", "edge", splitIn[1], FSUtils.parseFloatFirstBlock(splitIn[1]), agent);
            if("Edg".equals(splitIn[0] )) return new BrowserInfo("Microsoft Edge", "edge", splitIn[1], FSUtils.parseFloatFirstBlock(splitIn[1]), agent);
        }
        
        // Detect Google Chrome
        for(idx=0; idx<splits.length; idx++) {
        	String   blockOne = splits[idx];
        	String[] splitIn  = blockOne.split("/");
            if(splitIn.length != 2) continue;
            if("Chrome".equals(splitIn[0])) return new BrowserInfo("Google Chrome", "chrome", splitIn[1], FSUtils.parseFloatFirstBlock(splitIn[1]), agent);
        }
        
        // Detect Safari
        for(idx=0; idx<splits.length; idx++) {
        	String   blockOne = splits[idx];
        	String[] splitIn  = blockOne.split("/");
            if(splitIn.length != 2) continue;
            if("Safari".equals(splitIn[0])) return new BrowserInfo("Apple Safari", "safari", splitIn[1], FSUtils.parseFloatFirstBlock(splitIn[1]), agent);
        }

        // Detect Mypal
        for(idx=0; idx<splits.length; idx++) {
            String   blockOne = splits[idx];
            String[] splitIn  = blockOne.split("/");
            if(splitIn.length != 2) continue;
            if("Mypal".equals(splitIn[0])) return new BrowserInfo("Mypal", "mypal", splitIn[1], FSUtils.parseFloatFirstBlock(splitIn[1]), agent);
        }
        
        // Detect Firefox
        for(idx=0; idx<splits.length; idx++) {
            String   blockOne = splits[idx];
            String[] splitIn  = blockOne.split("/");
            if(splitIn.length != 2) continue;
            if("Firefox".equals(splitIn[0])) return new BrowserInfo("Mozilla Firefox", "firefox", splitIn[1], FSUtils.parseFloatFirstBlock(splitIn[1]), agent);
        }
        
        return new BrowserInfo();
    }
}
