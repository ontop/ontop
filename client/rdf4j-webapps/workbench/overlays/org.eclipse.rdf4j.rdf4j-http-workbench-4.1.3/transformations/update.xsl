<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$update.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:template match="sparql:sparql">
		<form action="update" method="post" onsubmit="return workbench.update.doSubmit()">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$update-string.label" />
						</th>
						<td>
							<textarea id="update" name="update" rows="16" cols="80">
								<xsl:text>
								</xsl:text>
							</textarea>
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<span id="updateString.errors" class="error">
								<xsl:value-of select="//sparql:binding[@name='error-message']" />
							</span>
						</td>
					</tr>
					<tr>
						<td></td>
						<td colspan="2">
							<input type="submit" value="{$execute.label}" />
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		<script type="text/javascript">
        var namespaces = {
            <xsl:for-each
                select="document(//sparql:link[@href='namespaces']/@href)//sparql:results/sparql:result">
                <xsl:value-of
                    select="concat('&quot;', sparql:binding[@name='prefix']/sparql:literal, ':&quot;:&quot;', sparql:binding[@name='namespace']/sparql:literal, '&quot;,')" />
                <xsl:text>
                </xsl:text>
            </xsl:for-each>
        };
        </script>
		<script src="../../scripts/codemirror.4.5.0.min.js" type="text/javascript"></script>
        <script src="../../scripts/yasqe.min.js" type="text/javascript"></script>
        <script src="../../scripts/yasqeHelper.js" type="text/javascript"></script>
		<script src="../../scripts/update.js" type="text/javascript"></script>
	</xsl:template>

</xsl:stylesheet>
