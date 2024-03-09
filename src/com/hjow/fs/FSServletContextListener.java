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
package com.hjow.fs;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/** Declare actions on server starts or server shutdown. */
public class FSServletContextListener implements ServletContextListener {
    public FSServletContextListener() { }

    @Override
    public synchronized void contextDestroyed(ServletContextEvent sce) {
        System.out.println(this.getClass().getName() + ".contextDestroyed");
        FSControl.disposeInstance();
    }

    @Override
    public synchronized void contextInitialized(ServletContextEvent sce) {
        System.out.println(this.getClass().getName() + ".contextInitialized");
        String ctxPath = sce.getServletContext().getContextPath();
        FSControl.init(ctxPath);
    }
    
}
