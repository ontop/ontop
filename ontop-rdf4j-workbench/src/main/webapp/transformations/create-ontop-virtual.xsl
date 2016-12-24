<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE rdf:RDF [
   <!ENTITY xsd  "http://www.w3.org/2001/XMLSchema#" >
 ]>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

    <xsl:include href="../locale/ontop-messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$repository-create.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:template match="sparql:sparql">
		<script src="../../scripts/create.js" type="text/javascript">
		</script>
		<form action="create" method="post">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of
								select="$repository-type.label" />
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
							<input type="text" id="owlfile"
								name="Owl File" size="48" value="" />
						</td>
						<td>
							<xsl:value-of select="$ontop-owlfilecommentvirt.label" />
							<a href="https://github.com/ontop/ontop/wiki/ObdalibOwlFile">OntopOwl</a>.
							
						</td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$ontop-obdafile.label" />
						</th>
						<td>
							<input type="text" id="obdafile"
								name="Obda File" size="48" value="" />
						</td>
						<td>
							<xsl:value-of select="$ontop-obdafilecomment.label" />
							<a href="https://github.com/ontop/ontop/wiki/ObdalibMappings">OntopObda</a>.
						</td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$ontop-reasoning.label" />
						</th>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of
								select="$ontop-existential.label" />
						</th>
						<td>
							<input type="radio" id="existential"
								name="Existential reasoning" size="48" value="true" />
							<xsl:value-of select="$true.label" />
							<xsl:value-of select="$false.label" />
						</td>
						<td>
							<xsl:value-of select="$ontop-exist.label" />
							<a href="https://babbage.inf.unibz.it/trac/obdapublic/wiki/ObdalibQuestReasoning#QuestReasoning">OntopReasoning</a>.
						</td>
					</tr>
					<tr>
						<th>
							<xsl:value-of
								select="$ontop-rewriting.label" />
						</th>
						<td>
							<select id="rewriting" name="Rewriting technique">
								<option  value="TreeWitness" selected="selected">
									TreeWitness
								</option>
								<option value="Default" >
									PerfectReformulationPlus
								</option>
								
							</select>
						</td>
						<td>
							<xsl:value-of select="$ontop-rewr.label" />
							<a href="https://babbage.inf.unibz.it/trac/obdapublic/wiki/ObdalibQuestRewriting">OntopRewriting</a>.
						</td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input type="button" value="{$cancel.label}"
								style="float:right" href="repositories"
								onclick="document.location.href=this.getAttribute('href')" />
							<input id="create" type="button" 
								value="{$create.label}" onclick="checkOverwrite()"/>
						</td>
					</tr>
				</tbody>
			</table>
		</form>
	</xsl:template>

</xsl:stylesheet>
