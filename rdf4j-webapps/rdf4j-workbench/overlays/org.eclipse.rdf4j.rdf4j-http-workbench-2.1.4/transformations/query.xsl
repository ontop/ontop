<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$query.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:template match="sparql:sparql">
		<xsl:variable name="queryLn"
			select="sparql:results/sparql:result/sparql:binding[@name='queryLn']" />
		<xsl:variable name="query"
			select="sparql:results/sparql:result/sparql:binding[@name='query']" />
		<form action="query" method="post" onsubmit="return workbench.query.doSubmit()">
			<input type="hidden" name="action" id="action" />
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$query-language.label" />
						</th>
						<td>
							<select id="queryLn" name="queryLn" onchange="workbench.query.onQlChange()">
								<xsl:for-each select="$info//sparql:binding[@name='query-format']">
									<option value="{substring-before(sparql:literal, ' ')}">
										<xsl:choose>
											<xsl:when
												test="$info//sparql:binding[@name='default-queryLn']/sparql:literal = substring-before(sparql:literal, ' ')">
												<xsl:attribute name="selected">true</xsl:attribute>
											</xsl:when>
											<xsl:when test="$queryLn = substring-before(sparql:literal, ' ')">
												<xsl:attribute name="selected">true</xsl:attribute>
											</xsl:when>
										</xsl:choose>
										<xsl:value-of select="substring-after(sparql:literal, ' ')" />
									</option>
								</xsl:for-each>
							</select>
						</td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$query-string.label" />
						</th>
						<td>
							<textarea id="query" name="query" rows="16" cols="80"
								wrap="soft">
								<xsl:value-of select="$query" />
							</textarea>
						</td>
						<td>
							<input type="hidden" name="ref" value="text" />
						</td>
					</tr>
					<tr>
						<td></td>
						<td>
							<span id="queryString.errors" class="error">
								<xsl:value-of select="//sparql:binding[@name='error-message']" />
							</span>
						</td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$result-limit.label" />
						</th>
						<td>
							<xsl:call-template name="limit-select">
                                <xsl:with-param name="limit_id">limit_query</xsl:with-param>
                            </xsl:call-template>
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$query-options.label" />
						</th>
						<td>
							<input id="infer" name="infer" type="checkbox" value="true">
								<xsl:if
									test="$info//sparql:binding[@name='default-infer']/sparql:literal = 'true'">
									<xsl:attribute name="checked">true</xsl:attribute>
								</xsl:if>
							</input>
							<xsl:value-of select="$include-inferred.label" />
							<input id="save-private" name="save-private" type="checkbox"
								value="true" />
							<xsl:value-of select="$save-private.label" />
						</td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$query-actions.label" />
						</th>
						<td>
							<input type="button" onclick="workbench.query.resetNamespaces()" value="Clear" />
							<input id="exec" type="submit" value="{$execute.label}" />
							<input id="save" type="submit" value="{$save.label}"
								disabled="disabled" />
							<input id="query-name" name="query-name" type="text" size="32"
								maxlength="32" value="" />
							<span id="save-feedback"></span>
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		<script type="text/javascript">
        var sparqlNamespaces = {
          <xsl:for-each
                select="document(//sparql:link[@href='namespaces']/@href)//sparql:results/sparql:result">
                <xsl:value-of
                    select="concat('&quot;', sparql:binding[@name='prefix']/sparql:literal, ':&quot;:&quot;', sparql:binding[@name='namespace']/sparql:literal, '&quot;,')" />
                <xsl:text>
                </xsl:text>
            </xsl:for-each>
        };
        </script>
		<pre id="SeRQL-namespaces" style="display:none">
			`
			<xsl:text>
USING NAMESPACE</xsl:text>
			<xsl:for-each
				select="document(//sparql:link[@href='namespaces']/@href)//sparql:results/sparql:result">
				<xsl:text>
</xsl:text>
				<xsl:choose>
					<xsl:when test="following-sibling::sparql:result">
						<xsl:value-of
							select="concat(sparql:binding[@name='prefix']/sparql:literal, ' = &lt;', sparql:binding[@name='namespace']/sparql:literal, '&gt;,')" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of
							select="concat(sparql:binding[@name='prefix']/sparql:literal, ' = &lt;', sparql:binding[@name='namespace']/sparql:literal, '&gt;')" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</pre>
		<script src="../../scripts/codemirror.4.5.0.min.js" type="text/javascript"></script>
		<script src="../../scripts/yasqe.min.js" type="text/javascript"></script>
		<script src="../../scripts/yasqeHelper.js" type="text/javascript"></script>
		<script src="../../scripts/query.js" type="text/javascript"></script>
		
	</xsl:template>
</xsl:stylesheet>
