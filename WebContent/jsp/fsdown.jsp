<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.io.*"%><%@ include file="common.pront.jsp"%><%
request.setCharacterEncoding("UTF-8");
String clients = request.getHeader("User-Agent");

String pathParam = request.getParameter("path");
String fileName  = request.getParameter("filename");
String speed     = request.getParameter("speed");
String capt      = request.getParameter("captcha");

String code = (String) session.getAttribute("captcha_code");
Long   time = (Long)   session.getAttribute("captcha_time");

if(code == null) code = "REFRESH";
if(capt == null) capt = "";

if(pathParam == null) pathParam = "";
pathParam = pathParam.trim();
if(pathParam.equals("/")) pathParam = "";
pathParam = pathParam.replace(".", "").replace("'", "").replace("\"", "").replace("\\", "/").trim(); // 상대경로 방지를 위해 . 기호는 반드시 제거 !
if(pathParam.startsWith("/")) pathParam = pathParam.substring(1);

if(speed != null) {
	speed = speed.trim();
	if(speed.equalsIgnoreCase("andante")) {
		sleepRoutine = sleepRoutine / 10;
	} else if(speed.equalsIgnoreCase("slow")) {
    	sleepRoutine = sleepRoutine / 5;
    } else if(speed.equalsIgnoreCase("")) {
        sleepRoutine = sleepRoutine / 2;
    }
}

FileInputStream fIn = null;
OutputStream outputs = null;
File file = null;
byte[] buffers = new byte[bufferSize];
try {
	if(! code.equals(capt)) {
        throw new RuntimeException("Wrong captcha code !");
    }
	
	if(now - time.longValue() >= captchaLimitTime) {
	    code = "REFRESH";
	    session.setAttribute("captcha_code", code);
	}
	
    if(code.equals("REFRESH")) {
        throw new RuntimeException("Too old captcha code !");
    }
    
    if(fileName == null || fileName.equals("")) {
        throw new FileNotFoundException("File name is needed.");
    }

    file = new File(rootPath.getAbsolutePath() + File.separator + pathParam.replace("/", File.separator) + File.separator + fileName);
    if(! file.exists()) {
        throw new FileNotFoundException("There is no file !");
    }
    if(file.isDirectory()) {
        throw new FileNotFoundException("Cannot download directory !");
    }

    long dataLength = file.length();
    if(dataLength / 1024 >= limitSize) throw new RuntimeException("File size is bigger than limits.");
    
	response.reset();
	response.setContentType("application/octet-stream");
	response.setHeader("Content-Description", "Download Broker");
	response.setHeader("Content-Length", dataLength + "");
	
	if(clients.indexOf("MSIE") >= 0) {
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
	} else {
		response.setHeader("Content-Type", "application/octet-stream; charset=utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
	}
	
	outputs = response.getOutputStream();
	
	int sleepCnt = sleepRoutine;
    fIn = new FileInputStream(file);
	while(true) {
		int readLengths = fIn.read(buffers, 0, bufferSize);
		if(readLengths < 0) break;
		outputs.write(buffers, 0, readLengths);
		
		sleepCnt--;
		if(sleepCnt <= 0) {
			sleepCnt = sleepRoutine;
			Thread.sleep(sleepGap);
		}
	}
	
	try { fIn.close();       } catch(Throwable te) {}
    try { outputs.close();   } catch(Throwable te) {}
    return;
} catch(Throwable tx) {
	// tx.printStackTrace();
	System.out.println("Exception message while sending file : " + tx.getMessage());
	response.reset();
	response.setContentType("text/html;charset=UTF-8");
	out.println("<pre>");
	out.println("ERROR : " + tx.getMessage());
	/*
	StackTraceElement[] elements = tx.getStackTrace();
	for(StackTraceElement e : elements) {
		out.println(" at " + e);
	}
	*/
	out.println("</pre>");
	return;
} finally {
	if(fIn     != null) { try { fIn.close();       } catch(Throwable te) {}}
	if(outputs != null) { try { outputs.close();   } catch(Throwable te) {}}
}
%>