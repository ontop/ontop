[QueryItem="q"]
PREFIX :  <http://www.semanticweb.org/ontologies/2018/4/mimic/>
PREFIX icd: <http://purl.bioontology.org/ontology/HOM-ICD9CM/>
SELECT ?s 
WHERE {
?s a icd:995.91 .
}

[QueryItem="q1"]
PREFIX mt: <http://www.semanticweb.org/ontologies/2018/4/mimic/temporal/>
PREFIX ms: <http://www.semanticweb.org/ontologies/2018/4/mimic/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX time: <http://www.w3.org/2006/time#>
PREFIX icd: <http://purl.bioontology.org/ontology/ICD9CM/>
SELECT ?id ?v ?l ?bInc ?b ?e ?eInc
WHERE {
GRAPH ?g {?p mt:hasFirstDayCreatinineLevel ?v.}
?g time:hasTime _:intv .
_:inv time:isBeginInclusive ?bInc .
_:intv time:hasBeginning _:beginInst .
_:beginInst rdf:type time:Instant .
_:beginInst time:inXSDDateTime ?b .
_:intv time:hasEnd _:endInst .
_:endInst rdf:type time:Instant .
_:endInst time:inXSDDateTime ?e .
_:inv time:isEndInclusive ?eInc .
?p ms:hasBeenDiagnosedWith ?d.
?p ms:hasPatientID ?id .
?d ms:icd9Code ?cd .
?d ms:icd9Class icd:995.91 .
icd:995.91 rdfs:label ?l .
}
