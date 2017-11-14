<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$add.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:template match="sparql:sparql">
		<xsl:if
			test="$info//sparql:binding[@name='id']/sparql:literal/text() = 'SYSTEM'">
			<p class="WARN">
				<xsl:value-of select="$SYSTEM-warning.desc" />
			</p>
		</xsl:if>
		<xsl:if test="//sparql:binding[@name='error-message']">
			<p class="error">
				<xsl:value-of select="//sparql:binding[@name='error-message']" />
			</p>
		</xsl:if>
		<form method="post" action="add" enctype="multipart/form-data">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$base-uri.label" />
						</th>
						<td>
							<input id="baseURI" name="baseURI" type="text"
								value="{//sparql:binding[@name='baseURI']/sparql:literal}"
								onchange="workbench.add.handleBaseURIUse()" />
							<br />
							<input type="checkbox" id="useForContext" name="useForContext"
								checked="true" onchange="workbench.add.handleBaseURIUse()" />
							use base URI as context identifier
						</td>
						<td></td>

					</tr>
					<tr>
						<th>
							<xsl:value-of select="$context.label" />
						</th>
						<td>
							<input id="context" readonly="readonly" name="context"
								type="text" value="{//sparql:binding[@name='context']/sparql:literal}" />
						</td>
						<td></td>

					</tr>
					<tr>
						<th>
							<xsl:value-of select="$data-format.label" />
						</th>
						<td>
							<select id="Content-Type" name="Content-Type"
								onchange="workbench.add.handleFormatSelection(this.value)">
								<option id='autodetect' value="autodetect" selected="selected">(autodetect)
								</option>
								<xsl:for-each
									select="document(//sparql:link/@href)//sparql:binding[@name='upload-format']">
									<option value="{substring-before(sparql:literal, ' ')}">
										<xsl:value-of select="substring-after(sparql:literal, ' ')" />
									</option>
								</xsl:for-each>
							</select>
						</td>
						<td></td>
					</tr>

					<tr>
						<td></td>
						<td>
							<input type="radio" id="source-url" name="source" value="url"
								onchange="workbench.add.enabledInput('url')" />
							<xsl:value-of select="$upload-url.desc" />
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$upload-url.label" />
						</th>
						<td>
							<input id="url" name="url" type="text" value=""
								onchange="workbench.add.enabledInput('url');" />
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input type="radio" id="source-file" name="source" value="file"
								onchange="workbench.add.enabledInput('file')" />
							<xsl:value-of select="$upload-file.desc" />
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$upload-file.label" />
						</th>
						<td>
							<input type="file" id="file" name="content" onchange="workbench.add.enabledInput('file')" />
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input type="radio" id="source-text" name="source" value="contents"
								onchange="workbench.add.enabledInput('text')" />
							<xsl:value-of select="$upload-text.desc" />
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$upload-text.label" />
						</th>
						<td>
							<textarea id="text" name="content" rows="6" cols="70"
								onchange="workbench.add.enabledInput('text')">
							</textarea>
						</td>
						<td></td>
					</tr>

					<tr>
						<td></td>

						<td>
							<input type="submit" value="Upload" />
						</td>
						<td></td>
					</tr>
				</tbody>
			</table>
		</form>
	    <script src="../../scripts/add.js" type="text/javascript"></script>
	</xsl:template>

</xsl:stylesheet>
