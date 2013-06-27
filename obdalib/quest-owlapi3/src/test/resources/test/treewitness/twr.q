[QueryItem="Q1"]

PREFIX : <http://www.semanticweb.org/roman/ontologies/twr#>
SELECT ?x WHERE {
?x :S ?y1 .
?y1 :P ?y2 .
?y3 :P ?y2 .
?y4 :S ?y3 .
?y4 a :B .
}