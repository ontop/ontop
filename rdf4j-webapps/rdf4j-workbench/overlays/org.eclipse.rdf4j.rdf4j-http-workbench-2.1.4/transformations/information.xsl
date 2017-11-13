<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$information.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:template match="sparql:sparql">
		<xsl:variable name="info" select="/" />
		<h2>
			<xsl:value-of select="$application-information.title" />
		</h2>
		<table class="simple">
			<tbody>

				<tr>
					<th>
						<xsl:value-of select="$application-name.label" />
					</th>
					<td>
						<xsl:value-of select="$workbench.label" />
					</td>
				</tr>
				<tr>
					<th>
						<xsl:value-of
							select="$application-version.label" />
					</th>
					<td>
						<xsl:value-of
							select="$info//sparql:binding[@name='version']/sparql:literal" />
					</td>
				</tr>
			</tbody>
		</table>

		<h2>
			<xsl:value-of select="$runtime-information.title" />
		</h2>

		<table class="simple">
			<tbody>
				<tr>
					<th>
						<xsl:value-of select="$operating-system.label" />
					</th>
					<td>
						<xsl:value-of
							select="$info//sparql:binding[@name='os']/sparql:literal" />
					</td>
				</tr>
				<tr>
					<th>
						<xsl:value-of select="$java-runtime.label" />
					</th>
					<td>
						<xsl:value-of
							select="$info//sparql:binding[@name='jvm']/sparql:literal" />
					</td>
				</tr>
				<tr>
					<th>
						<xsl:value-of select="$process-user.label" />
					</th>
					<td>
						<xsl:value-of
							select="$info//sparql:binding[@name='user']/sparql:literal" />
					</td>
				</tr>
			</tbody>

		</table>

		<h2>
			<xsl:value-of select="$memory.title" />
		</h2>
		<table class="simple">
			<tbody>
				<tr>
					<th>
						<xsl:value-of select="$memory-used.label" />
					</th>
					<td>
						<xsl:value-of
							select="$info//sparql:binding[@name='memory-used']/sparql:literal" />
					</td>
				</tr>

				<tr>
					<th>
						<xsl:value-of select="$maximum-memory.label" />
					</th>
					<td>
						<xsl:value-of
							select="$info//sparql:binding[@name='maximum-memory']/sparql:literal" />
					</td>
				</tr>
			</tbody>
		</table>
	</xsl:template>

	<xsl:template match="sparql:literal">
		<xsl:value-of select="." />
	</xsl:template>

</xsl:stylesheet>
