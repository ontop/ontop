<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

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
								<option value="lmdb">
									LMDB Store
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
							<input type="text" id="id" name="Repository ID" size="16"
								value="lmdb" />
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$repository-title.label" />
						</th>
						<td>
							<input type="text" id="title" name="Repository title" size="48"
								value="LMDB store" />
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$repository-evaluation-mode.label" />
						</th>
						<td>
							<select id="evalStratFactory" name="EvaluationStrategyFactory">
								<option selected="selected"
									value="org.eclipse.rdf4j.query.algebra.evaluation.impl.StrictEvaluationStrategyFactory">
									Strict
								</option>
								<option
									value="org.eclipse.rdf4j.query.algebra.evaluation.impl.ExtendedEvaluationStrategyFactory">
									Extended
								</option>
							</select>
						</td>
						<td></td>
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
