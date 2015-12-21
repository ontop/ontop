[QueryItem="q1"]
PREFIX : <http://www.semanticweb.org/elem/ontologies/2015/11/mesowest2#>
SELECT ?rain ?start ?end
WHERE{
?rain a :Rain.
?rain :startTime ?start.
?rain :endTime ?end.
}
