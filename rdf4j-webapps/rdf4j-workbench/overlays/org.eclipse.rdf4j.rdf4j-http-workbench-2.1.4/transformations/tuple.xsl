<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$query-result.title" />
		<xsl:text> (</xsl:text>
		<xsl:value-of select="count(//sparql:result)" />
		<xsl:text>)</xsl:text>
	</xsl:variable>

	<xsl:variable name="nextX.label">
		<xsl:value-of select="$next.label" />
		<xsl:text> </xsl:text>
		<xsl:value-of select="count(//sparql:result)" />
	</xsl:variable>

	<xsl:variable name="previousX.label">
		<xsl:value-of select="$previous.label" />
		<xsl:text> </xsl:text>
		<xsl:value-of select="count(//sparql:result)" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:include href="table.xsl" />

	<xsl:template match="sparql:sparql">
		<form>
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$download-format.label" />
						</th>
						<td>
							<select id="Accept" name="Accept">
								<xsl:for-each
									select="$info//sparql:binding[@name='tuple-download-format']">
									<option value="{substring-before(sparql:literal, ' ')}">
										<xsl:if
											test="$info//sparql:binding[@name='default-Accept']/sparql:literal = substring-before(sparql:literal, ' ')">
											<xsl:attribute name="selected">true</xsl:attribute>
										</xsl:if>
										<xsl:value-of select="substring-after(sparql:literal, ' ')" />
									</option>
								</xsl:for-each>
							</select>
						</td>
						<td>
							<input type="submit" onclick="workbench.paging.addGraphParam('Accept');return false"
								value="{$download.label}" />
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		<form>
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$result-limit.label" />
						</th>
						<td>
							<xsl:call-template name="limit-select">
								<xsl:with-param name="onchange">
									workbench.paging.addLimit('query');
								</xsl:with-param>
                                <xsl:with-param name="limit_id">limit_query</xsl:with-param>
							</xsl:call-template>
						</td>
						<td id="result-limited">
							<xsl:if
								test="$info//sparql:binding[@name='default-limit']/sparql:literal = count(//sparql:result)">
								<xsl:value-of select="$result-limited.desc" />
							</xsl:if>
						</td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$result-offset.label" />
						</th>
						<td>
							<input id="previousX" type="button" value="{$previousX.label}"
								onclick="workbench.paging.previousOffset('query');" />
						</td>
						<td>
							<input id="nextX" type="button" value="{$nextX.label}"
								onclick="workbench.paging.nextOffset('query');" />
						</td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$show-datatypes.label" />
						</th>
						<td>
							<input type="checkbox" name="show-datatypes" value="show-dataypes"
								checked="checked" />
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		<table class="data">
			<xsl:apply-templates select="*" />
		</table>
		<script src="../../scripts/paging.js" type="text/javascript">
		</script>
		<script src="../../scripts/tuple.js" type="text/javascript">
		</script>
	</xsl:template>

</xsl:stylesheet>
