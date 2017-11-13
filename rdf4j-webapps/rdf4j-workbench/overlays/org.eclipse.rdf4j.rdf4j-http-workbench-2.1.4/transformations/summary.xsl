<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$summary.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:template match="sparql:sparql">
		<h2>
			<xsl:value-of select="$repository-location.title" />
		</h2>
		<table class="simple">
			<tbody>
				<tr>
					<th>
						<xsl:value-of select="$repository-id.label" />
					</th>
					<td>
						<xsl:value-of select="//sparql:binding[@name='id']" />
					</td>
				</tr>
				<tr>
					<th>
						<xsl:value-of select="$repository-title.label" />
					</th>
					<td>
						<xsl:value-of select="//sparql:binding[@name='description']" />
					</td>
				</tr>
				<tr>
					<th>
						<xsl:value-of select="$repository-location.label" />
					</th>
					<td>
						<xsl:value-of select="//sparql:binding[@name='location']" />
					</td>
				</tr>
				<tr>
					<th>
						<xsl:value-of select="$server.label" />
					</th>
					<td>
						<xsl:value-of select="//sparql:binding[@name='server']" />
					</td>
				</tr>
			</tbody>
		</table>
		<h2>
			<xsl:value-of select="$repository-size.title" />
		</h2>
		<table class="simple">
			<tbody>
				<tr>
					<th>
						<xsl:value-of select="$repository-size.label" />
					</th>
					<td>
						<xsl:value-of select="//sparql:binding[@name='size']" />
					</td>
				</tr>
				<tr>
					<th>
						<xsl:value-of select="$repository-contexts-size.label" />
					</th>
					<td>
						<xsl:value-of select="//sparql:binding[@name='contexts']" />
					</td>
				</tr>
			</tbody>
		</table>
	</xsl:template>

	<xsl:template match="sparql:literal">
		<xsl:value-of select="." />
	</xsl:template>

</xsl:stylesheet>
