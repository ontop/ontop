<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$namespaces.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:include href="table.xsl" />

	<xsl:template match="sparql:literal">
		<xsl:value-of select="." />
	</xsl:template>

	<xsl:template match="sparql:sparql">
		<form action="namespaces" method="post">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$prefix.label" />
						</th>
						<td>
							<input type="text" id="prefix" name="prefix"
								size="8" />
							<select id="prefix-select"
								onchange="workbench.namespaces.updatePrefix()">
								<option></option>
								<xsl:for-each
									select="//sparql:result">
									<option
										value="{sparql:binding[@name='namespace']/sparql:literal}">
										<xsl:value-of
											select="sparql:binding[@name='prefix']/sparql:literal" />
									</option>
								</xsl:for-each>
							</select>
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$namespace.label" />
						</th>
						<td>
							<input type="text" id="namespace"
								name="namespace" size="48" />
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td colspan="2">
							<input type="submit"
								value="{$update.label}" />
							<input type="submit"
								onclick="$('#namespace').val('');return true"
								value="{$delete.label}" />
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		<table class="data">
			<xsl:apply-templates select="*" />
		</table>
		<script src="../../scripts/namespaces.js" type="text/javascript">
		</script>
	</xsl:template>

</xsl:stylesheet>
