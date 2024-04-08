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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hjow.fs.schedule.FSScheduler;

/** Declare actions on server starts or server shutdown. */
public class FSServletContextListener implements ServletContextListener {
    public FSServletContextListener() { }

    @Override
    public synchronized final void contextDestroyed(ServletContextEvent sce) {
        System.out.println(this.getClass().getName() + ".contextDestroyed STARTS");
        destroying(sce.getServletContext(), FSControl.getInstance());
        FSScheduler.dispose();
        try { Thread.sleep(2000L);  } catch(InterruptedException ignores) {}
        FSProtocolHandler.disposeInstance();
        FSControl.disposeInstance();
        System.out.println(this.getClass().getName() + ".contextDestroyed END");
    }

    @Override
    public synchronized final void contextInitialized(ServletContextEvent sce) {
        System.out.println(this.getClass().getName() + ".contextInitialized STARTS");
        ServletContext ctx = sce.getServletContext();
        
        String ctxPath = ctx.getContextPath();
        FSControl.init(ctxPath);
        
        FSControl ctrl = FSControl.getInstance();
        ctrl.setRealPath(ctx);
        
        try { ctrl.removeAllTokens();         } catch(Throwable ignores) {}
        try { ctrl.emptyTempDirectory();      } catch(Throwable ignores) {}
        
        // Warming up
        
        try { Class.forName("java.awt.image.BufferedImage");  } catch(ClassNotFoundException ignores) {}
        try { Class.forName("java.awt.Graphics2D");           } catch(ClassNotFoundException ignores) {}
        try { Class.forName("java.awt.Font");                 } catch(ClassNotFoundException ignores) {}
        try { Class.forName("javax.imageio.ImageIO");         } catch(ClassNotFoundException ignores) {}
        try { FSUtils.createImageCaptchaBase64("123456789", 200, 100, 10, 0, false, null); } catch(Throwable ignores) {}
        
        initializing(ctx, ctrl);
        FSControl.waitProperInit();
        System.out.println(this.getClass().getName() + ".contextInitialized END at " + System.currentTimeMillis());
    }
    
    /** This method is called on the server starts. Can be override. */
    public void initializing(ServletContext ctx, FSControl ctrl) {  }
    /** This method is called on the server stops. Can be override. */
    public void destroying(ServletContext ctx, FSControl ctrl) { }
}