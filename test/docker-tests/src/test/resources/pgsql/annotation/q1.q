PREFIX : <http://purl.obolibrary.org/obo/doid.owl#>
PREFIX obo:	<http://purl.obolibrary.org/obo/>
PREFIX owl:	<http://www.w3.org/2002/07/owl#>
PREFIX rdf:	<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xml:	<http://www.w3.org/XML/1998/namespace>
PREFIX xsd:	<http://www.w3.org/2001/XMLSchema#>
PREFIX doid: <http://purl.obolibrary.org/obo/doid#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX oboInOwl: <http://www.geneontology.org/formats/oboInOwl#>

SELECT *
{
?x rdfs:comment "NT MGI."^^xsd:string .
}
