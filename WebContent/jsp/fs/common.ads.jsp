<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.*, hjow.common.util.*"%><%
String adsPub = FSControl.getInstance().getStringConfig("AdsPublisherId");
if(DataUtil.isNotEmpty(adsPub)) {
	adsPub = adsPub.trim().toLowerCase();
	adsPub = adsPub.replace("\"", "").replace("'", "").replace("\n", "").replace("\t", "").replace("<", "").replace(">", "");
	%><script async src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-<%= adsPub %>" crossorigin="anonymous"></script><%
} else {
	%><!-- <script async src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-[PUT_PUBLISHER_CODE_HERE]" crossorigin="anonymous"></script> --><%
}
%>