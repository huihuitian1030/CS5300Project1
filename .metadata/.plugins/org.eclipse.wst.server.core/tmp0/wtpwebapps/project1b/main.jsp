<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
	import="javax.servlet.http.Cookie,java.util.*,java.io.*,cs5300Project1b.*"
    session="false" 
%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>first servlet</title>
</head>
<body>

<% 
	PrintWriter output =  response.getWriter();
	Date newDate = new Date();
    SessionState newSession = (SessionState) request.getAttribute("SessionState");
	Cookie curCookie = (Cookie) request.getAttribute("cookie");
    
%>

<div>

Cookie name: <%= curCookie.getValue() %><br />
Expiration time: <%= new Date(newSession.getExpireTime()).toString() %> <br />
</div>

<div>
Session: <%= newSession.getSessionID().serialize()  %> <br />
Version: <%= newSession.getVersion() %> <br />
Date: <%= newDate.toString() %><br />
</div>


<h1><%= newSession.getMessage() %></h1>
<br />
<form action="server" method="GET">
<input type="submit" name = "function" value="Replace" /> <input type="text" name="newStr" >
<br />
<input type="submit" name = "function" value="Refresh" /><br />
</form>

<form  action="server" method="POST">
<input type="submit" name = "function" value="Logout" />
</form>
<br />









</body>
</html>