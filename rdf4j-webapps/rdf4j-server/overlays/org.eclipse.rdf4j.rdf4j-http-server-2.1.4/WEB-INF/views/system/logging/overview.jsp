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
<%@ include file="/WEB-INF/includes/components/logfilterform.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/logpaginationheader.html.jspf" %>
	<table class="data">
		<thead>
			<tr><th><fmt:message key="system.logging.overview.level" /></th><th><fmt:message key="system.logging.overview.date" /></th><th><fmt:message key="system.logging.overview.time" /></th><th><fmt:message key="system.logging.overview.thread" /></th><th><fmt:message key="system.logging.overview.message" /></th></tr>
		</thead>
		<tbody>
			<c:forEach var="logrecord" items="${logreader}">
				<tr>
					<td><img src="${path}/images/${logrecord.level}.png" alt="${logrecord.level}" title="${logrecord.level}" /></td>
					<td><fmt:formatDate value="${logrecord.time}" pattern="yyyy-MM-dd" /></td>
					<td><fmt:formatDate value="${logrecord.time}" pattern="HH:mm:ss,SSS" /></td>
					<td><c:out value="${logrecord.threadName}" /></td>
					<td>
						<c:out value="${logrecord.message}" />
						<c:if test="${fn:length(logrecord.stackTrace) > 0}" >
							<div class="stacktrace">
								<c:forEach var="trace" items="${logrecord.stackTrace}">
									<br />
									<c:out value="${trace}" />
								</c:forEach>
							</div>
						</c:if>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<%@ include file="/WEB-INF/includes/components/logpaginationfooter.html.jspf" %>
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>
