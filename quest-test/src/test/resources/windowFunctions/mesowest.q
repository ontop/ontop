[QueryItem="q1"]
PREFIX : <http://www.semanticweb.org/elem/ontologies/2015/11/mesowest#>
SELECT DISTINCT ?rain
WHERE{
?rain a :Rain.
}

[QueryItem="q2"]
PREFIX : <http://www.semanticweb.org/elem/ontologies/2015/11/mesowest#>
SELECT ?rain ?start ?end
WHERE{
?rain a :Rain.
?rain :startTime ?start.
?rain :endTime ?end.
}
