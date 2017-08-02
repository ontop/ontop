[QueryGroup="SimpleQueries"] @collection [[
[QueryItem="raisOptional"]
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rais: <http://www.ontorais.de/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
SELECT DISTINCT ?ao ?title ?date WHERE {
?ao a rais:ArchiveObject .
?ao rais:title ?title.
 OPTIONAL{ ?ao rais:archivalDate ?date.}
}

[QueryItem="raisNoOptional"]
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rais: <http://www.ontorais.de/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
SELECT DISTINCT ?ao ?title ?date WHERE {
?ao a rais:ArchiveObject .
?ao rais:title ?title.
?ao rais:archivalDate ?date.
}
]]
