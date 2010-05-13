<html>
<head>
	<title>Login Page for Whist</title>
	<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
	<link rel="stylesheet" type="text/css" href="<c:url value="/css/common.css"/>" />
</head>

<body>
	<form method="POST" action="j_spring_security_check" >
  		<table border="0" cellspacing="5">
    		<tr>
      			<th align="right">Username:</th>
      			<td align="left"><input type="text" name="j_username"></td>
    		</tr>
    		<tr>
      			<th align="right">Password:</th>
      			<td align="left"><input type="password" name="j_password"></td>
    		</tr>
    		<tr>
      			<td align="right"><input type="submit" value="Log In"></td>
      			<td align="left"><input type="reset"></td>
    		</tr>
  		</table>
	</form>
</body>
</html>