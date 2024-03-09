package com.hjow.fs.console;
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

import java.io.Serializable;

public class FSConsoleResult implements Serializable {
    private static final long serialVersionUID = 256793532787714176L;
    protected boolean success = false;
    protected boolean nulll   = false;
    protected boolean logout  = false;
    protected boolean closepopup = false;
    protected String  display = "";
    protected String  path    = null;
    protected String downloadAccepted = null;
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public String getDisplay() {
        return display;
    }
    public void setDisplay(String display) {
        this.display = display;
    }
    public boolean isNulll() {
        return nulll;
    }
    public void setNulll(boolean nulll) {
        this.nulll = nulll;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getDownloadAccepted() {
        return downloadAccepted;
    }
    public void setDownloadAccepted(String downloadAccepted) {
        this.downloadAccepted = downloadAccepted;
    }
    public boolean isLogout() {
        return logout;
    }
    public void setLogout(boolean logout) {
        this.logout = logout;
    }
    public boolean isClosepopup() {
        return closepopup;
    }
    public void setClosepopup(boolean closepopup) {
        this.closepopup = closepopup;
    }
}
