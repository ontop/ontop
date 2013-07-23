[QueryItem="Query1"]
PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT DISTINCT ?product ?label
WHERE { 
    ?product rdfs:label ?label .
    ?product bsbm:type <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType10> .
    ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature414> . 
    ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature369> . 
    ?product bsbm:productPropertyNumeric1 ?value1 . 
	FILTER (?value1 > 354) 
	}
ORDER BY ?label
LIMIT 10

[QueryItem="Query 2"]
PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dc: <http://purl.org/dc/elements/1.1/>

SELECT ?label ?comment ?producer ?productFeature ?propertyTextual1 ?propertyTextual2 ?propertyTextual3
 ?propertyNumeric1 ?propertyNumeric2 ?propertyTextual4 ?propertyTextual5 ?propertyNumeric4 
WHERE {
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product484> rdfs:label ?label .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product484> rdfs:comment ?comment .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product484> bsbm:producer ?p .
    ?p rdfs:label ?producer .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product484> dc:publisher ?p . 
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product484> bsbm:productFeature ?f .
    ?f rdfs:label ?productFeature .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product484> bsbm:productPropertyTextual1 ?propertyTextual1 .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product484> bsbm:productPropertyTextual2 ?propertyTextual2 .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product484> bsbm:productPropertyTextual3 ?propertyTextual3 .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product484> bsbm:productPropertyNumeric1 ?propertyNumeric1 .
    <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product484> bsbm:productPropertyNumeric2 ?propertyNumeric2 .
    OPTIONAL { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product484> bsbm:productPropertyTextual4 ?propertyTextual4 }
    OPTIONAL { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product484> bsbm:productPropertyTextual5 ?propertyTextual5 }
    OPTIONAL { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product484> bsbm:productPropertyNumeric4 ?propertyNumeric4 }
}

[QueryItem="Query 3"]
PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT ?product ?label
WHERE {
    ?product rdfs:label ?label .
    ?product bsbm:type <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType23> .
	?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature61> .
	?product bsbm:productPropertyNumeric1 ?p1 .
	FILTER ( ?p1 > 314 ) 
	?product bsbm:productPropertyNumeric3 ?p3 .
	FILTER (?p3 < 270 )
    OPTIONAL { 
        ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature1055> .
        ?product rdfs:label ?testVar }
    FILTER (!bound(?testVar)) 
}
ORDER BY ?label
LIMIT 10

[QueryItem="Query 4"]
PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT DISTINCT ?product ?label ?propertyTextual
WHERE {
    { 
       ?product rdfs:label ?label .
       ?product bsbm:type <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType32> .
       ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature1638> .
	   ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature1638> .
       ?product bsbm:productPropertyTextual1 ?propertyTextual .
	   ?product bsbm:productPropertyNumeric1 ?p1 .
	   FILTER ( ?p1 > 280 )
    } UNION {
       ?product rdfs:label ?label .
       ?product bsbm:type <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType32> .
       ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature1638> .
	   ?product bsbm:productFeature <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature177> .
       ?product bsbm:productPropertyTextual1 ?propertyTextual .
	   ?product bsbm:productPropertyNumeric2 ?p2 .
	   FILTER ( ?p2> 324 ) 
    } 
}
ORDER BY ?label
OFFSET 5
LIMIT 10

[QueryItem="Query 5"]
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>

SELECT DISTINCT ?product ?productLabel
WHERE { 
	?product rdfs:label ?productLabel .
    FILTER (<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product516> != ?product)
	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product516> bsbm:productFeature ?prodFeature .
	?product bsbm:productFeature ?prodFeature .
	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product516> bsbm:productPropertyNumeric1 ?origProperty1 .
	?product bsbm:productPropertyNumeric1 ?simProperty1 .
	FILTER (?simProperty1 < (?origProperty1 + 120) && ?simProperty1 > (?origProperty1 - 120))
	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer11/Product516> bsbm:productPropertyNumeric2 ?origProperty2 .
	?product bsbm:productPropertyNumeric2 ?simProperty2 .
	FILTER (?simProperty2 < (?origProperty2 + 170) && ?simProperty2 > (?origProperty2 - 170))
}
ORDER BY ?productLabel
LIMIT 5

[QueryItem="Query 6"]
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>

SELECT ?product ?label
WHERE {
    ?product rdfs:label ?label .
    ?product a bsbm:Product .
    FILTER regex(?label, "%word%")
}

[QueryItem="Query 7"]
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rev: <http://purl.org/stuff/rev#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>

SELECT ?productLabel ?offer ?price ?vendor ?vendorTitle ?review ?revTitle 
       ?reviewer ?revName ?rating1 ?rating2
WHERE { 
	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer3/Product114> rdfs:label ?productLabel .
    OPTIONAL {
        ?offer bsbm:product <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer3/Product114> .
		?offer bsbm:price ?price .
		?offer bsbm:vendor ?vendor .
		?vendor rdfs:label ?vendorTitle .
        ?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#DE> .
        ?offer dc:publisher ?vendor . 
        ?offer bsbm:validTo ?date .
        FILTER (?date > "2008-06-20T00:00:00"^^<http://www.w3.org/2001/XMLSchema#dateTime> )
    }
    OPTIONAL {
	?review bsbm:reviewFor <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer3/Product114> .
	?review rev:reviewer ?reviewer .
	?reviewer foaf:name ?revName .
	?review dc:title ?revTitle .
    OPTIONAL { ?review bsbm:rating1 ?rating1 . }
    OPTIONAL { ?review bsbm:rating2 ?rating2 . } 
    }
}

[QueryItem="Query 8"]
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX rev: <http://purl.org/stuff/rev#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>

SELECT ?title ?text ?reviewDate ?reviewer ?reviewerName ?rating1 ?rating2 ?rating3 ?rating4 
WHERE { 
     ?review bsbm:reviewFor <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer245/Product11970> .
     ?review dc:title ?title .
     ?review rev:text ?text .
     FILTER langMatches( lang(?text), "EN" ) 
     
     ?review bsbm:reviewDate ?reviewDate .
     ?review rev:reviewer ?reviewer .
     ?reviewer foaf:name ?reviewerName .
     OPTIONAL { ?review bsbm:rating1 ?rating1 . }
     OPTIONAL { ?review bsbm:rating2 ?rating2 . }
     OPTIONAL { ?review bsbm:rating3 ?rating3 . }
     OPTIONAL { ?review bsbm:rating4 ?rating4 . }
}
ORDER BY DESC(?reviewDate)
LIMIT 20

[QueryItem="Query 9"]
PREFIX rev: <http://purl.org/stuff/rev#>

DESCRIBE ?x
WHERE { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite1/Review992> rev:reviewer ?x }

[QueryItem="Query 10"]
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> 
PREFIX dc: <http://purl.org/dc/elements/1.1/>

SELECT DISTINCT ?offer ?price
WHERE {
     ?offer bsbm:product <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer7/Product290> .
     ?offer bsbm:vendor ?vendor .
     ?offer dc:publisher ?vendor .
     ?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#US> .
     ?offer bsbm:deliveryDays ?deliveryDays .
     FILTER (?deliveryDays <= 3)
     
     ?offer bsbm:price ?price .
     ?offer bsbm:validTo ?date .
     FILTER (?date > "2008-04-20"^^<http://www.w3.org/2001/XMLSchema#dateTime> )
}
ORDER BY ?price
LIMIT 10

[QueryItem="Query 11"]
SELECT ?property ?hasValue ?isValueOf
WHERE {
  { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer1058> ?property ?hasValue }
  UNION
  { ?isValueOf ?property <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer1058> }
}

[QueryItem="Query 12"]
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rev: <http://purl.org/stuff/rev#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX bsbm-export: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/export/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>

CONSTRUCT {  <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer648> bsbm-export:product ?productURI .
             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer648> bsbm-export:productlabel ?productlabel .
             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer648> bsbm-export:vendor ?vendorname .
             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer648> bsbm-export:vendorhomepage ?vendorhomepage . 
             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer648> bsbm-export:offerURL ?offerURL .
             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer648> bsbm-export:price ?price .
             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer648> bsbm-export:deliveryDays ?deliveryDays .
             <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer648> bsbm-export:validuntil ?validTo } 
WHERE { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer648> bsbm:product ?productURI .
        ?productURI rdfs:label ?productlabel .
        <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer648> bsbm:vendor ?vendorURI .
        ?vendorURI rdfs:label ?vendorname .
        ?vendorURI foaf:homepage ?vendorhomepage .
        <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer648> bsbm:offerWebpage ?offerURL .
        <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer648> bsbm:price ?price .
        <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer648> bsbm:deliveryDays ?deliveryDays .
        <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor1/Offer648> bsbm:validTo ?validTo }
