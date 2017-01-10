<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
   <!ENTITY xsd  "http://www.w3.org/2001/XMLSchema#" >
 ]>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$repository-create.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:template match="sparql:sparql">
		<form action="create">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$repository-type.label" />
						</th>
						<td>
							<select id="type" name="type">
                                <!-- Options added by Ontop -->
                                <option value="ontop-virtual">
                                    Ontop Virtual RDF Store
                                </option>
								<option value="memory">
									In Memory Store
								</option>
								<option value="memory-rdfs">
									In Memory Store RDF Schema
								</option>
								<option value="memory-rdfs-dt">
									In Memory Store RDF Schema and
									Direct Type
									Hierarchy
								</option>
								<option value="memory-spin">
									In Memory Java Store with basic SPIN support
								</option>
								<option value="memory-spin-rdfs">
									In Memory Store with RDFS+SPIN support
								</option>
								<option value="memory-customrule">
									In Memory Store Custom Graph Query
									Inference
								</option>
								<option value="native">
									Native Java Store
								</option>
								<option value="native-rdfs">
									Native Java Store RDF Schema
								</option>
								<option value="native-rdfs-dt">
									Native Java Store RDF Schema and
									Direct Type
									Hierarchy
								</option>
								<option value="native-customrule">
									Native Java Store Custom
									Graph Query
									Inference
								</option>
								<option value="native-spin">
									Native Java Store with basic SPIN support
								</option>
								<option value="native-spin-rdfs">
									Native Java Store with RDFS+SPIN support
								</option>
								<option value="remote">
									Remote RDF Store
								</option>
								<option value="sparql">
									SPARQL endpoint proxy
								</option>
								<option value="federate">Federation Store</option>
							</select>
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$repository-id.label" />
						</th>
						<td>
							<input type="text" id="id" name="id" size="16" />
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$repository-title.label" />
						</th>
						<td>
							<input type="text" id="title" name="title" size="48" />
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input type="button" value="{$cancel.label}" style="float:right"
								data-href="repositories"
								onclick="document.location.href=this.getAttribute('data-href')" />
							<input type="submit" name="next" value="{$next.label}" />
						</td>
					</tr>
				</tbody>
			</table>
		</form>
	</xsl:template>

</xsl:stylesheet>
