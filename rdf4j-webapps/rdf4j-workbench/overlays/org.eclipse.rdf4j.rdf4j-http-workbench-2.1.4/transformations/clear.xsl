<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$clear.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:template match="sparql:sparql">
		<xsl:variable name="info"
			select="document(sparql:head/sparql:link[@href='info']/@href)" />
		<xsl:if
			test="$info//sparql:binding[@name='id']/sparql:literal/text() = 'SYSTEM'">
			<p class="WARN">
				<xsl:value-of select="$SYSTEM-warning.desc" />
			</p>
		</xsl:if>
		<p class="WARN">
			<xsl:value-of select="$clear-warning.desc" />
		</p>
		<xsl:if test="//sparql:binding[@name='error-message']">
			<p class="error">
				<xsl:value-of
					select="//sparql:binding[@name='error-message']" />
			</p>
		</xsl:if>

		<form method="post" action="clear">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$context.label" />
						</th>
						<td>
							<input id="context" name="context"
								type="text" value="{//sparql:binding[@name='context']/sparql:literal}" />
						</td>
						<td></td>

					</tr>
					<tr>
						<td></td>
						<td>
							<input type="submit"
								value="{$clear-context.label}" />
						</td>
						<td></td>
					</tr>
				</tbody>
			</table>
		</form>

	</xsl:template>

</xsl:stylesheet>
