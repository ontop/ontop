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
								<option value="memory">
									Memory Store
								</option>
								<option value="memory-lucene">
									Memory Store + Lucene 
                                </option>
								<option value="memory-rdfs">
									Memory Store + RDFS
								</option>
								<option value="memory-rdfs-dt">
									Memory Store + RDFS and Direct Type
								</option>
								<option value="memory-rdfs-lucene">
									Memory Store + RDFS and Lucene
								</option>
								<option value="memory-customrule">
									Memory Store + Custom Graph Query Inference
								</option>
								<option value="memory-spin">
									Memory Store + SPIN support
								</option>
								<option value="memory-spin-rdfs">
									Memory Store + RDFS and SPIN support
								</option>
								<option value="memory-shacl">
									Memory Store + SHACL
								</option>
                                <!-- disabled pending GH-1304  option value="memory-spin-rdfs-lucene">
                                    In Memory Store with RDFS+SPIN+Lucene support
                                </option -->
								<option value="native">
									Native Store
								</option>
								<option value="native-lucene">
                                   Native Store + Lucene
                                </option>
								<option value="native-rdfs">
									Native Store + RDFS
								</option>
								<option value="native-rdfs-dt">
									Native Store + RDFS and Direct Type
								</option>
								<option value="memory-rdfs-lucene">
									Native Store + RDFS and Lucene
								</option>
								<option value="native-customrule">
									Native Store + Custom Graph Query Inference
								</option>
								<option value="native-spin">
									Native Store + SPIN support
								</option>
								<option value="native-spin-rdfs">
									Native Store + RDFS and SPIN support
								</option>
								<option value="native-shacl">
									Native Store + SHACL
								</option>
                                <!-- disabled pending GH-1304  option value="native-spin-rdfs-lucene">
									Native Java Store with RDFS+SPIN+Lucene support
								</option -->
								<option value="remote">
									Remote RDF Store
								</option>
								<option value="sparql">
									SPARQL endpoint proxy
								</option>
								<option value="federate">Federation</option>
								<option value="lmdb">LMDB Store</option>
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
