<%@ page language="java" contentType="plain/text; charset=UTF-8" pageEncoding="UTF-8" import="java.io.*, java.util.*, java.security.*, org.apache.commons.codec.binary.Base64"%><%
String alg = request.getParameter("alg");
if(alg == null) alg = "256";

String content = request.getParameter("content");
if(content == null) content = "";

MessageDigest digest = MessageDigest.getInstance("SHA-" + alg);
byte[] res = digest.digest(content.getBytes("UTF-8"));
%><%= Base64.encodeBase64String(res) %>