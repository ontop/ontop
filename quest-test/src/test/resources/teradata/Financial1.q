[QueryItem="q1"]
PREFIX : <http://www.semanticweb.org/xiao/ontologies/2016/2/untitled-ontology-123#>
SELECT * WHERE {?x a :Customer }

[QueryItem="q2"]
PREFIX : <http://www.semanticweb.org/xiao/ontologies/2016/2/untitled-ontology-123#>
SELECT * WHERE {?x a :Customer ; :age ?age}
