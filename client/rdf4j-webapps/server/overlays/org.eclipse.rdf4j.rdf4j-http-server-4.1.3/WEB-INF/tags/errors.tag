<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="errors" type="org.springframework.validation.Errors" required="true" %>
<%@ attribute name="errorstitle" required="true" %>

	<c:if test="${errors != null && fn:length(errors.globalErrors) > 0}">
	<h2><fmt:message key="${errorstitle}" /></h2>
	<ul>
	<c:forEach var="error" items="${errors.globalErrors}">
		<li class="error"><fmt:message key="${error.code}" /></li>
	</c:forEach>
	</ul>
	</c:if>