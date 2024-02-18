<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="com.hjow.fs.util.*, java.util.*, java.io.*, java.awt.*, java.awt.image.*, javax.imageio.*,org.apache.commons.codec.binary.Base64" %><%@ include file="common.pront.jsp"%><%
String code  = (String) request.getSession().getAttribute("captcha_code");
Long   time  = (Long)   request.getSession().getAttribute("captcha_time");
String theme = request.getParameter("theme");

boolean captDarkMode  = false;
if(theme != null) {
	if(theme.equals("dark")) captDarkMode =  true;
}

if(code == null) {
    code = "REFRESH";
}

if(now - time.longValue() >= captchaLimitTime) {
    code = "REFRESH";
    request.getSession().setAttribute("captcha_code", code);
}

int colorPad = 0;
if(captDarkMode) colorPad = 100;

BufferedImage image    = new BufferedImage(captchaWidth, captchaHeight, BufferedImage.TYPE_INT_RGB);
Graphics2D    graphics = image.createGraphics();

if(captDarkMode) graphics.setColor(new Color(0, 0, 0));
else graphics.setColor(new Color(250, 250, 250));
graphics.fillRect(0, 0, captchaWidth, captchaHeight);

FontMetrics metrics = graphics.getFontMetrics();
int fontWidth = metrics.stringWidth(code);
int gap       = (captchaWidth - fontWidth) / (code.length() + 1);
int x         = gap;
int y         = (captchaHeight - metrics.getHeight()) / 2 + metrics.getAscent();

Font font = new Font("Serif", Font.BOLD, captchaFontSize);

// 방해물 출력
for(int ndx=0; ndx<10; ndx++) {
    int x1, y1, x2, y2;
    x1 = (int) (Math.random() * captchaWidth);
    y1 = (int) (Math.random() * captchaHeight);
    x2 = x1 + (int) (Math.random() * (captchaWidth  / 2));
    y2 = y1 + (int) (Math.random() * (captchaHeight / 2));
    graphics.setColor(new Color( (colorPad + (int) (Math.random() * 120)), (colorPad + (int) (Math.random() * 120)), (colorPad + (int) (Math.random() * 120))  ));
    graphics.drawLine(x1, y1, x2, y2);
}

// 글자 출력
for(int idx=0; idx<code.length(); idx++) {
    char charNow = code.charAt(idx);
    graphics.setColor(new Color( (colorPad + (int) (Math.random() * 120)), (colorPad + (int) (Math.random() * 120)), (colorPad + (int) (Math.random() * 120))  ));
    
    int nowX = x + metrics.charWidth(charNow) / 2;
    int ang  = (((int) Math.random()) * 41) - 20;
    
    graphics.rotate(Math.toRadians(ang), nowX, y);
    graphics.setFont(font);
    graphics.drawString(String.valueOf(charNow), nowX, y + ((int) ((Math.random() * captchaHeight) / 2.0)));
    graphics.rotate(Math.toRadians(ang) * (-1), nowX, y);
    
    x += metrics.charWidth(charNow) + gap;
}

ByteArrayOutputStream binary = new ByteArrayOutputStream();
ImageIO.write(image, "jpg", binary);
image    = null;
graphics = null;

String bs64str = Base64.encodeBase64String(binary.toByteArray());
binary = null;
%>
<!DOCTYPE html>
<html>
<head>
    <jsp:include page="./common.header.jsp"></jsp:include>
    <script type='text/javascript'>
    $(function() {
        setTimeout(function() { location.reload(); }, <%= captchaLimitTime %>);
        if(<%=captDarkMode%>) { $('body').css('background-color', 'black'); }
    });
    </script>
</head>
<body>
    <img src="data:image/jpeg;base64,<%=bs64str%>"/>
</body>
</html>