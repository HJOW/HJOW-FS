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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FSFileListingResult implements Serializable {
    private static final long serialVersionUID = 6582714864695423495L;
    protected List<File> dirs  = new ArrayList<File>();
    protected List<File> files = new ArrayList<File>();
    protected int exceptsCount = 0;
    protected int skippedCount = 0;
    
    public FSFileListingResult() {
        
    }

    public FSFileListingResult(List<File> dirs, List<File> files) {
        super();
        this.dirs = dirs;
        this.files = files;
    }

    public List<File> getDirs() {
        return dirs;
    }

    public void setDirs(List<File> dirs) {
        this.dirs = dirs;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public int getExceptsCount() {
        return exceptsCount;
    }

    public void setExceptsCount(int exceptsCount) {
        this.exceptsCount = exceptsCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }
}
