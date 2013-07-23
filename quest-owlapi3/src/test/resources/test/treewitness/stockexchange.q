[QueryItem="Q1"]
PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>
SELECT ?x WHERE {
?x a :StockExchangeMember.
}


[QueryItem="Q2"]
PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>
SELECT ?x ?y WHERE {
?x a :Person.
?x :hasStock ?y.
?y a :Stock .
}


[QueryItem="Q3"]
PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>
SELECT ?x ?y ?z WHERE {
?x a :FinantialInstrument.
?x :belongsToCompany ?y. 
?y a :Company.
?y :hasStock ?z.
?z a :Stock.
}


[QueryItem="Q4"]
PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>
SELECT ?x ?y ?z WHERE {
?x a :Person.
?x :hasStock ?y. 
?y a :Stock.
?y :isListedIn ?z.
?z a :StockExchangeList.
}


[QueryItem="Q5"]
PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>
SELECT ?x ?y ?z ?w WHERE {
?x a :FinantialInstrument.
?x :belongsToCompany ?y. 
?y a :Company.
?y :hasStock ?z.
?z a :Stock.
?z :isListedIn ?w.
?w a :StockExchangeList.
}