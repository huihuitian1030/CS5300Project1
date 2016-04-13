<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import="javax.servlet.http.Cookie,java.util.*,java.io.*,cs5300Project1b.*"
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>errorPage</title>
</head>
<body>



Request timeout, click the button to back to login.<br />


<form action ="server" method = "GET">
<input type = "submit" name = "function" value = "Back to Login" />

</form>

</body>
</html>