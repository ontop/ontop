<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title"><xsl:value-of select="$types.title"/></xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:include href="table.xsl" />
</xsl:stylesheet>
