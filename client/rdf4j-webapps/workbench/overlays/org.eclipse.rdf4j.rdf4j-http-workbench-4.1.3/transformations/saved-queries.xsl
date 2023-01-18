<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="url-encode.xsl" />

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title" select="$saved-queries.title" />

	<xsl:include href="template.xsl" />

	<xsl:template match="sparql:sparql/sparql:results">
		<xsl:for-each select="sparql:result">
			<xsl:variable name="queryLn"
				select="normalize-space(sparql:binding[@name='queryLn'])" />
			<xsl:variable name="queryText" select="sparql:binding[@name='queryText']" />
			<xsl:variable name="query-url-encoded">
				<xsl:call-template name="url-encode">
					<xsl:with-param name="str" select="normalize-space($queryText)" />
				</xsl:call-template>
			</xsl:variable>
			<xsl:variable name="infer"
				select="normalize-space(sparql:binding[@name='infer'])" />
			<xsl:variable name="rowsPerPage"
				select="normalize-space(sparql:binding[@name='rowsPerPage'])" />
			<xsl:variable name="query"
				select="normalize-space(sparql:binding[@name='query'])" />
			<xsl:variable name="queryHREF"
				select="concat('query?action=exec&amp;queryLn=', $queryLn, '&amp;query=', $query-url-encoded, '&amp;infer=', $infer, '&amp;limit_query=', $rowsPerPage)" />
			<xsl:variable name="user"
				select="normalize-space(sparql:binding[@name='user'])" />
			<xsl:variable name="previousUser"
				select="normalize-space(preceding::sparql:result[1]/sparql:binding[@name='user'])" />
			<xsl:variable name="queryName"
				select="normalize-space(sparql:binding[@name='queryName'])" />
			<xsl:if test="$user != $previousUser">
				<h2>
					<xsl:value-of select="$user" />
				</h2>
			</xsl:if>
			<div id="{$query}-div">
				<table>
					<tr>
						<th style="vertical-align:middle;width:24em">
							<xsl:value-of select="$queryName" />
						</th>
						<td style="vertical-align:middle">
							<form method="post" name="exec-query" action="query">
								<input type="hidden" name="action" value="exec" />
								<input type="hidden" name="queryLn" value="{$queryLn}" />
								<input type="hidden" name="query" value="{$queryName}" />
								<input type="hidden" name="ref" value="id" />
								<input type="hidden" name="owner" value="{$user}" />
								<input type="hidden" name="infer" value="{$infer}" />
								<input type="hidden" name="limit_query" value="{$rowsPerPage}" />
								<input type="submit" value="Execute" />
							</form>
						</td>
						<td style="vertical-align:middle">
							<!-- the path may only be up to 2048 characters long in Internet Explorer -->
							<xsl:choose>
								<xsl:when test="string-length($queryHREF) &lt; 2049">
									<a href="{$queryHREF}">
										<img src="../../images/bookmark.png" alt="Bookmarkable link" />
									</a>
								</xsl:when>
								<xsl:otherwise>
									<img src="../../images/cancel.png"
										alt="Can't produce bookmarkable link...query too long." />
								</xsl:otherwise>
							</xsl:choose>
						</td>
						<td style="vertical-align:middle">
							<input type="button" id="{$query}-toggle" value="Show"
								onclick="workbench.savedQueries.toggle('{$query}');" />
						</td>
						<td style="vertical-align:middle">
							<form method="post" name="edit-query" action="query">
								<input type="hidden" name="action" value="edit" />
								<input type="hidden" name="queryLn" value="{$queryLn}" />
								<input type="hidden" name="query" value="{$queryName}" />
								<input type="hidden" name="ref" value="id" />
								<input type="hidden" name="owner" value="{$user}" />
								<input type="hidden" name="infer" value="{$infer}" />
								<input type="hidden" name="limit_query" value="{$rowsPerPage}" />
								<input type="submit" value="Edit" />
							</form>
						</td>
						<td style="vertical-align:middle">
							<form method="post" id="{$query}" action="saved-queries?delete={$query}">
								<input type="button" value="Delete..."
									onclick="workbench.savedQueries.deleteQuery('{$user}', '{$queryName}', '{$query}');" />
							</form>
						</td>
					</tr>
				</table>
				<table class="data" id="{$query}-metadata" style="display: none;">
					<tr>
						<th>Query Language</th>
						<td>
							<xsl:value-of select="$queryLn" />
						</td>
						<th>Include Inferred Statements</th>
						<td>
							<xsl:value-of select="$infer" />
						</td>
						<th>Rows Per Page</th>
						<td>
							<xsl:value-of select="$rowsPerPage" />
						</td>
						<th>Shared</th>
						<td>
							<xsl:value-of select="sparql:binding[@name='shared']" />
						</td>
					</tr>
				</table>
				<textarea id="{$query}-text" style="display: none;"><xsl:value-of select="sparql:binding[@name='queryText']" /></textarea>
			</div>
		</xsl:for-each>
		<script src="../../scripts/codemirror.4.5.0.min.js" type="text/javascript"></script>
        <script src="../../scripts/yasqe.min.js" type="text/javascript"></script>
		<script src="../../scripts/saved-queries.js" type="text/javascript"></script>
	</xsl:template>
</xsl:stylesheet>
