<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$change-server.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:template match="sparql:sparql">
		<form action="server" method="post" onsubmit="changeServer(event)">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$change-server.label" />
						</th>
						<td>
							<input id="workbench-server"
								name="workbench-server" type="text" size="40"
								value="{normalize-space(//sparql:binding[@name='server'])}" />
						</td>
						<td>
							<span class="error">
								<xsl:value-of
									select="//sparql:binding[@name='error-message']" />
							</span>
						</td>
					</tr>
					<tr>
						<td></td>
						<td>
							<xsl:value-of select="$change-server.desc" />
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$server-user.label" />
						</th>
						<td>
							<input id="server-user"
								name="server-user" type="text"
								value="{normalize-space(//sparql:binding[@name='server-user'])}" />
						</td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$server-password.label" />
						</th>
						<td>
							<input id="server-password"
								name="server-password" type="password"
								value="" />
						</td>
					</tr>
					<tr>
						<td></td>
						<td colspan="2">
							<input type="submit"
								value="{$change.label}" />
						</td>

					</tr>
				</tbody>
			</table>
		</form>
		<script src="../../scripts/server.js" type="text/javascript">
		</script>
	</xsl:template>

</xsl:stylesheet>
