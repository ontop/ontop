[QueryItem="Q1"]
PREFIX : <http://siemens.com/ns#>
PREFIX st:  <http://siemens.com/temporal/ns#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX time: <http://www.w3.org/2006/time#>
SELECT ?tb ?bInc ?b ?e ?eInc
WHERE {
    ?tb a :Turbine .
    GRAPH ?g {?tb rdf:type st:PurgingIsOver .}
    ?g time:hasTime _:intv .
    _:inv time:isBeginInclusive ?bInc .
    _:intv time:hasBeginning _:beginInst .
    _:beginInst rdf:type time:Instant .
    _:beginInst time:inXSDDateTime ?b .
    _:intv time:hasEnd _:endInst .
    _:endInst rdf:type time:Instant .
    _:endInst time:inXSDDateTime ?e .
    _:inv time:isEndInclusive ?eInc .
}