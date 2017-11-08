<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
   <!ENTITY xsd  "http://www.w3.org/2001/XMLSchema#" >
   <!ENTITY rdf  "http://www.w3.org/1999/02/22-rdf-syntax-ns#" >
 ]>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#" xmlns:q="http://www.openrdf.org/schema/qname#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="url-encode.xsl" />

	<xsl:template match="sparql:sparql">
		<table class="data">
			<xsl:apply-templates select="*" />
		</table>
	</xsl:template>

	<xsl:template match="sparql:head">
		<thead>
			<tr>
				<xsl:apply-templates select="sparql:variable" />
			</tr>
		</thead>
	</xsl:template>

	<xsl:template match="sparql:variable">
		<th>
			<xsl:value-of select="@name" />
		</th>
	</xsl:template>

	<xsl:template match="sparql:results">
		<tbody>
			<xsl:apply-templates select="*" />
		</tbody>
	</xsl:template>

	<xsl:template match="sparql:boolean">
		<tr>
			<td>
				<xsl:value-of select="text()" />
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="sparql:result">
		<xsl:variable name="result" select="." />
		<tr>
			<xsl:for-each select="../../sparql:head/sparql:variable">
				<xsl:variable name="name" select="@name" />
				<td>
					<xsl:apply-templates select="$result/sparql:binding[@name=$name]" />
				</td>
			</xsl:for-each>
		</tr>
	</xsl:template>

	<xsl:template name="explore">
		<xsl:param name="resource" />
		<xsl:param name="url" select="string('')" />
		<xsl:param name="shortform" select="string('')" />
		<div class="resource">
			<xsl:if test="$shortform != string('')">
				<xsl:attribute name="data-longform">
					<xsl:call-template name="url-encode">
						<xsl:with-param name="str" select="$resource" />
					</xsl:call-template>
				</xsl:attribute>
				<xsl:attribute name="data-shortform">
					<xsl:call-template name="url-encode">
						<xsl:with-param name="str" select="$shortform" />
					</xsl:call-template>
				</xsl:attribute>
			</xsl:if>
			<a>
				<xsl:attribute name="href">
					<xsl:text>explore?resource=</xsl:text>
					<xsl:call-template name="url-encode">
						<xsl:with-param name="str" select="$resource" />
					</xsl:call-template>
				</xsl:attribute>
				<xsl:value-of select="$resource" />
			</a>
			<xsl:if test="$url != string('')">
				<a class="resourceURL" href="{$url}" target="_blank">
					<img src="../../images/external.png" alt="web" />
				</a>
			</xsl:if>
		</div>
	</xsl:template>

	<xsl:template name="explore-literal">
		<xsl:param name="literal" />
		<a>
			<xsl:attribute name="href">
				<xsl:text>explore?resource=</xsl:text>
				<xsl:choose>
					<xsl:when test="$literal/@q:qname">
						<xsl:call-template name="url-encode">
							<xsl:with-param name="str"
				select="concat('&quot;', $literal/text(), '&quot;^^', $literal/@q:qname)" />
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="url-encode">
							<xsl:with-param name="str"
				select="concat('&quot;', $literal/text(), '&quot;^^&lt;', $literal/@datatype, '&gt;')" />
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:value-of select="$literal/text()" />
		</a>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype]">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource"
				select="concat('&quot;', text(), '&quot;^^&lt;', @datatype, '&gt;')" />
			<xsl:with-param name="shortform"
				select="concat('&quot;', text(), '&quot;')" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@q:qname]">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource"
				select="concat('&quot;', text(), '&quot;^^', @q:qname)" />
			<xsl:with-param name="shortform"
				select="concat('&quot;', text(), '&quot;')" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;boolean']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;integer']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;decimal']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;double']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;date']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;dateTime']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;time']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;duration']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:uri[@q:qname]">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource" select="@q:qname" />
			<xsl:with-param name="url" select="text()" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&rdf;XMLLiteral']">
		<pre>
			<xsl:value-of select="text()" />
		</pre>
	</xsl:template>

	<xsl:template match="sparql:literal[@xml:lang]">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource"
				select="concat('&quot;', text(), '&quot;@', @xml:lang)" />
			<xsl:with-param name="shortform"
				select="concat('&quot;', text(), '&quot;')" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal">
		<xsl:choose>
			<xsl:when test="contains(text(), '&#10;')">
				<pre>
					<xsl:value-of select="text()" />
				</pre>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="explore">
					<xsl:with-param name="resource"
						select="concat('&quot;', text(), '&quot;')" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="sparql:bnode">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource" select="concat('_:', text())" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:uri">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource" select="concat('&lt;', text(), '&gt;')" />
			<xsl:with-param name="url" select="text()" />
		</xsl:call-template>
	</xsl:template>

</xsl:stylesheet>
