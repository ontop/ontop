<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

    <xsl:include href="../locale/ontop-messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$repository-create.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:template match="sparql:sparql">
		<form action="create" method="post">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$repository-type.label" />
						</th>
						<td>
							<select id="type" name="type">
								<option value="ontop-virtual">
									Ontop Virtual RDF Store
								</option>
							</select>
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$repository-id.label" />
						</th>
						<td>
							<input type="text" id="id"
								name="Repository ID" size="16" value="ontop-virtual" />
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$repository-title.label" />
						</th>
						<td>
							<input type="text" id="title"
								name="Repository title" size="16" />
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$ontop-owlfile.label" />
						</th>
						<td>
							<input type="text" id="owlFile"
								name="owlFile" size="48" value="" />
                        </td>
						<td>
							<xsl:value-of select="$ontop-owlfilecommentvirt.label" />
							<!--<a href="https://github.com/ontop/ontop/wiki/ObdalibOwlFile">OntopOwl</a>.-->
						</td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$ontop-obdafile.label" />
						</th>
						<td>
							<input type="text" id="obdaFile"
								name="obdaFile" size="48" value="" />
						</td>
						<td>
							<xsl:value-of select="$ontop-obdafilecomment.label" />
							<!--<a href="https://github.com/ontop/ontop/wiki/ObdalibMappings">OntopObda</a>.-->
						</td>
					</tr>

					<tr>
						<th>
							<xsl:value-of select="$ontop-properties.label" />
						</th>
						<td>
							<input type="text" id="propertiesFile"
								name="propertiesFile" size="48" value=""/>
						</td>
						<td>
							<xsl:value-of select="$ontop-propertiesfilecomment.label" />
						</td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$ontop-constraintfile.label" />
						</th>
						<td>
							<input type="text" id="constraintFile"
								   name="constraintFile" size="48" value="" />
						</td>
						<td>
							<xsl:value-of select="$ontop-constraintfilecomment.label" />
						</td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input type="button" value="{$cancel.label}" style="float:right"
								data-href="repositories"
								onclick="document.location.href=this.getAttribute('data-href')" />
							<input id="create" type="button" value="{$create.label}"
								onclick="checkOverwrite()" />
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		<script src="../../scripts/create.js" type="text/javascript">
		</script>
	</xsl:template>

</xsl:stylesheet>
