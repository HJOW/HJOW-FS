# HJOW-FS

JSP based simple File Storage.
This project is for classic web browsers, and classic JDK runtime.
No JSTL, No Spring !
Work on JDK 6 & Tomcat 7 or above !
(Servlet 3.0, Java Server Page 2.2 as least)

Sometimes using BASE64 converting in this program.
Until JDK 8, there are JAXB as a basic library.
Over JDK 9, JAXB not working anymore, so Apache Commons Codec is needed.

(Developing modern version will be started soon...)


JSP 기반 파일 저장소 프로젝트입니다.
이걸로 웹 기반 자료실을 구축할 수 있습니다.
JDK 6, 톰캣 7 이상 버전에서 동작하며, 스프링과 JSTL 의존성이 없어 다른 프로젝트와 혼용이 수월합니다.

이 프로그램에서는 BASE64 컨버팅을 사용합니다.
JDK 8 이전 버전에서는 JAXB 라이브러리가 기본 제공되어 써드파티 없이 BASE64가 가능했으나,
JDK 9 부터는 Apache Commons Codec 라이브러리를 WEB-INF 밑 lib 폴더에 넣어 주어야 동작합니다.
다시말해, JDK 8 이전 버전을 사용한다면 Apache Commons Codec 라이브러리를 제거해도 됩니다.
(버전 0.1.2 부터 적용되며, 0.1.1 이하에서는 Apache Commons Codec 라이브러리를 제거하면 안 됩니다.)

# Releases

Visit https://github.com/HJOW/HJOW-FS/releases

# Using...

+ jQuery (https://jquery.com/)
+ jQuery UI (https://jquery.com/)
+ bootstrap (https://getbootstrap.com/)
+ bootswatch (https://bootswatch.com/3/cyborg/)
+ video.js (http://videojs.com/) Copyright Brightcove, Inc.
+ promise-polyfill (https://github.com/taylorhakes/promise-polyfill)
+ Apache Common Codec (https://commons.apache.org/proper/commons-codec/)
+ json-simple (https://github.com/fangyidong/json-simple) - Removed on version 0.1.2
+ HttpServlet source (https://github.com/javaee/servlet-spec/blob/master/src/main/java/javax/servlet/http/HttpServlet.java)
+ favicon, 'folder' icon from freepik.com (https://kr.freepik.com/icon/files_220980#fromView=search&term=Folder&track=ais&page=1&position=30)
+ 'file' icon from freepik.com (https://kr.freepik.com/icon/pdf-file_4347587#fromView=search&term=%ED%8C%8C%EC%9D%BC&track=ais&page=3&position=7)
+ D2Coding font (https://github.com/Joungkyun/font-d2coding)
+ Java, Servlet API (https://javaee.github.io/servlet-spec/LICENSE)
+ React (https://github.com/facebook/react/blob/main/LICENSE)
+ Babel Standalone (https://github.com/babel/babel-standalone/blob/master/LICENSE)

# How to update from old version

1. Backup all files and directories in web root directory.
2. Overwrite all files with new version's.
3. Go to "/WEb-INF/lib" directory.
4. Check "hjow_libs_yyyyMMdd_hhmm.jar" named files. (For example, "hjow_libs_20240322_0500.jar")
   Then, delete all "hjow_libs_yyyyMMdd_hhmm.jar" named files except the newest one.
5. Run server and test it.