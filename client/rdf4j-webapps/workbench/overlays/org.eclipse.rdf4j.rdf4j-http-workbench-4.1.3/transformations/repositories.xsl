<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
   <!ENTITY xsd  "http://www.w3.org/2001/XMLSchema#" >
 ]>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$repository-list.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:include href="table.xsl" />

	<xsl:template match="sparql:variable[@name='readable']">
		<th>
			<img src="../../images/view.png" alt="{$readable.label}"
				title="{$readable.label}" />
		</th>
	</xsl:template>

	<xsl:template match="sparql:variable[@name='writeable']">
		<th>
			<img src="../../images/edit.png"
				alt="{$writeable.label}" title="{$writeable.label}" />
		</th>
	</xsl:template>

	<xsl:template match="sparql:binding[@name='id']">
		<a href="../{sparql:literal}/">
			<xsl:value-of select="sparql:literal" />
		</a>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;boolean']">
		<xsl:choose>
			<xsl:when test="text() = 'true'">
				<img src="../../images/affirmative.png"
					alt="{$true.label}" title="{$true.label}" />
			</xsl:when>
			<xsl:otherwise>
				<img src="../../images/negative.png"
					alt="{$false.label}" title="{$false.label}" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="sparql:literal">
		<xsl:value-of select="text()" />
	</xsl:template>

</xsl:stylesheet>
