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

public class Counter implements Serializable {
    private static final long serialVersionUID = -8351283205016681277L;
    protected int count = 0;
    public Counter() {}
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public void increase() {
        count++;
    }
}
