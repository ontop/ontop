<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$repository-create.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:template match="sparql:sparql">
		<form action="create" method="post">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$repository-type.label" />
						</th>
						<td>
							<select id="type" name="type">
								<option value="native-customrule">
									Native Java Store Custom
									Graph Query
									Inference
								</option>
							</select>
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$repository-id.label" />
						</th>
						<td>
							<input type="text" id="id" name="Repository ID" size="16"
								value="native-customrule" />
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$repository-title.label" />
						</th>
						<td>
							<input type="text" id="title" name="Repository title" size="48"
								value="Native store with custom graph query inferencing rule" />
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$query-language.label" />
						</th>
						<td>
							<select id="queryLn" name="Query Language">
								<xsl:for-each select="$info//sparql:binding[@name='query-format']">
									<option value="{substring-before(sparql:literal, ' ')}">
										<xsl:choose>
											<xsl:when
												test="$info//sparql:binding[@name='default-queryLn']/sparql:literal = substring-before(sparql:literal, ' ')">
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
							<xsl:value-of select="$rule-query.label" />
						</th>
						<td>
							<textarea id="ruleQuery" name="Rule query" rows="8"
								cols="80" wrap="hard">
							</textarea>
						</td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$matcher-query.label" />
						</th>
						<td>
							<textarea id="matcherQuery" name="Matcher query (optional)" rows="8"
								cols="80" wrap="hard">
							</textarea>
						</td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$repository-indexes.label" />
						</th>
						<td>
							<input type="text" id="indexes" name="Triple indexes" size="16"
								value="spoc,posc" />
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input type="button" value="{$cancel.label}" style="float:right"
								data-href="repositories"
                                onclick="document.location.href=this.getAttribute('data-href')" />
							<input id="create" type="button" value="{$create.label}"
								onclick="checkOverwrite()" />
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		<script src="../../scripts/create.js" type="text/javascript">
		</script>
	</xsl:template>

</xsl:stylesheet>
