<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:template match="sparql:head"></xsl:template>

	<xsl:template match="sparql:results">
		<table class="simple">
			<xsl:apply-templates select="*" />
		</table>
	</xsl:template>

	<xsl:template match="sparql:result">
		<tbody>
			<xsl:apply-templates select="*" />
		</tbody>
	</xsl:template>

	<xsl:template match="sparql:binding">
		<tr>
			<th>
				<xsl:value-of select="@name" />
			</th>
			<td>
				<xsl:apply-templates select="*" />
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="sparql:literal">
		<xsl:value-of select="." />
	</xsl:template>

</xsl:stylesheet>
