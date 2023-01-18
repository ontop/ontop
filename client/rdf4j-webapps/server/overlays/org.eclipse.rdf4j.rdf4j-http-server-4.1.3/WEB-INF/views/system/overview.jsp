<%@ include file="/WEB-INF/includes/components/page.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/head.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Navigation.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/ContentHeader.html.jspf" %>

<div id="content">
	<h1><fmt:message key="${view.i18n}" /></h1>

<%@ include file="/WEB-INF/includes/components/Message.html.jspf" %>
	
	<h2><fmt:message key="system.overview.application" /></h2>
	<table class="simple">
		<tbody>
			<tr><th><fmt:message key="system.overview.application.name" /></th><td>${appConfig.longName}</td></tr>
			<tr><th><fmt:message key="system.overview.application.version" /></th><td>${appConfig.version}</td></tr>
			<tr><th><fmt:message key="system.overview.application.datadir" /></th><td>${appConfig.dataDir}</td></tr>
		</tbody>
	</table>
	
	<h2><fmt:message key="system.overview.runtime" /></h2>
	<table class="simple">
		<tbody>
			<tr><th><fmt:message key="system.overview.runtime.os" /></th><td>${server.os}</td></tr>
			<tr><th><fmt:message key="system.overview.runtime.java" /></th><td>${server.java}</td></tr>
			<tr><th><fmt:message key="system.overview.runtime.user" /></th><td>${server.user}</td></tr>
		</tbody>
	</table>
	
	<h2><fmt:message key="system.overview.memory" /></h2>
	<table class="simple">
		<tbody>
			<tr><th><fmt:message key="system.overview.memory.percentage" /></th><td><fmt:message key="system.overview.memory.percentage.value"><fmt:param value="${memory.percentageInUse}"/></fmt:message></td></tr>
			<tr><th><fmt:message key="system.overview.memory.used" /></th><td><fmt:message key="system.overview.memory.used.value"><fmt:param value="${memory.used}"/></fmt:message></td></tr>
			<tr><th><fmt:message key="system.overview.memory.maximum" /></th><td><fmt:message key="system.overview.memory.maximum.value"><fmt:param value="${memory.maximum}"/></fmt:message></td></tr>
		</tbody>
	</table>		
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>
