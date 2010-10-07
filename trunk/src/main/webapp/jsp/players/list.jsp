<html>
  <head>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
	<title>Players</title>
    <script type="text/javascript" src="<c:url value="/js/jquery-1.4.2.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/instructions.js"/>"></script>
	<link rel="stylesheet" type="text/css" href="<c:url value="/css/common.css"/>" />
  </head>
  <body>
  	<div id="userList">
  		<c:forEach var="player" items="${players}">
    		<p><a href="<c:url value="/admin/players/${player.username}"/>">${player.prettyName}</a> - <a href="<c:url value="/admin/players/${player.username}/delete"/>">Delete</a></p>
    	</c:forEach>
    	<p>Click on a user to view and edit their account, or <a href="<c:url value="/admin/players/new"/>">create a new account</a></p>
  	</div>
  	
 	<jsp:include page="/footer">
	 	<jsp:param name="nonInstructionsDiv" value="userList" />
 	</jsp:include>
  	
  </body>
</html>