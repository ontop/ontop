

--[SPARQL Q1]
PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT DISTINCT ?product ?label
WHERE {
 ?product rdfs:label ?label .
 ?product a bsbm:Product .
 ?product bsbm:productFeature bsbm-inst:ProductFeature89 .
 ?product bsbm:productFeature bsbm-inst:ProductFeature91 .
 ?product bsbm:productPropertyNumeric1 ?value1 .
 FILTER (?value1 < 1000)
}

--[SPARQL Q2]
PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dc: <http://purl.org/dc/elements/1.1/>

SELECT ?label ?comment ?producer ?productFeature ?propertyTextual1 ?propertyTextual2 ?propertyTextual3
 ?propertyNumeric1 ?propertyNumeric2 ?propertyTextual4 ?propertyTextual5 ?propertyNumeric4
WHERE {
    ?product bsbm:productId ?id .
    FILTER (?id < 1000 )
    ?product rdfs:label ?label .
	?product rdfs:comment ?comment .
	?product bsbm:producer ?p .
	?p rdfs:label ?producer .
    ?product dc:publisher ?p .
	?product bsbm:productFeature ?f .
	?f rdfs:label ?productFeature .
	?product bsbm:productPropertyTextual1 ?propertyTextual1 .
	?product bsbm:productPropertyTextual2 ?propertyTextual2 .
    ?product bsbm:productPropertyTextual3 ?propertyTextual3 .
	?product bsbm:productPropertyNumeric1 ?propertyNumeric1 .
	?product bsbm:productPropertyNumeric2 ?propertyNumeric2 .
	OPTIONAL { ?product bsbm:productPropertyTextual4 ?propertyTextual4 }
    OPTIONAL { ?product bsbm:productPropertyTextual5 ?propertyTextual5 }
    OPTIONAL { ?product bsbm:productPropertyNumeric4 ?propertyNumeric4 }
}


--[SPARQL Q3]
PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT ?product ?label
WHERE {
 ?product rdfs:label ?label .
 ?product a bsbm:Product .
 ?product bsbm:productFeature bsbm-inst:ProductFeature89 .
 ?product bsbm:productPropertyNumeric1 ?p1 .
 FILTER ( ?p1 > 10 )
 ?product bsbm:productPropertyNumeric3 ?p3 .
 FILTER ( ?p3 < 5000 )
 OPTIONAL {
 ?product bsbm:productFeature bsbm-inst:ProductFeature91 .
 ?product rdfs:label ?testVar }
 FILTER (!bound(?testVar))
}

--[SPARQL Q4]
PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT DISTINCT ?product ?label ?propertyTextual
WHERE {
 {
 ?product rdfs:label ?label .
 ?product rdf:type bsbm:Product.
 ?product bsbm:productFeature bsbm-inst:ProductFeature89 .
 ?product bsbm:productFeature bsbm-inst:ProductFeature91 .
 ?product bsbm:productPropertyTextual1 ?propertyTextual .
 ?product bsbm:productPropertyNumeric1 ?p1 .
	FILTER ( ?p1 > 30 )
 }
 UNION
 {
 ?product rdfs:label ?label .
 ?product rdf:type bsbm:Product .
 ?product bsbm:productFeature bsbm-inst:ProductFeature89 .
 ?product bsbm:productFeature bsbm-inst:ProductFeature86 .
 ?product bsbm:productPropertyTextual1 ?propertyTextual .
 ?product bsbm:productPropertyNumeric2 ?p2 .
	FILTER ( ?p2> 50 )
 }
}

--[SPARQL Q5]
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>

SELECT ?product ?productLabel
WHERE {
	?product rdfs:label ?productLabel .
    FILTER (<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> != ?product)
	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> bsbm:productFeature ?prodFeature .
	?product bsbm:productFeature ?prodFeature .
	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> bsbm:productPropertyNumeric1 ?origProperty1 .
	?product bsbm:productPropertyNumeric1 ?simProperty1 .
	FILTER (?simProperty1 < (?origProperty1 + 120))
	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> bsbm:productPropertyNumeric2 ?origProperty2 .
	?product bsbm:productPropertyNumeric2 ?simProperty2 .
	FILTER (?simProperty2 < (?origProperty2 + 170) )
}

--[SPARQL Q6]
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>

SELECT ?product ?label
WHERE {
	?product rdfs:label ?label .
 ?product rdf:type bsbm:Product .
	FILTER regex(?label, "%word1%")
}

--[SPARQL Q7]
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rev: <http://purl.org/stuff/rev#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>

SELECT ?productLabel ?offer ?price ?vendor ?vendorTitle ?review ?revTitle
 ?reviewer ?revName ?rating1 ?rating2
WHERE {
	<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> rdfs:label ?productLabel .
 OPTIONAL {
 ?offer bsbm:product <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> .
 ?offer bsbm:price ?price .
 ?offer bsbm:vendor ?vendor .
 ?vendor rdfs:label ?vendorTitle .
 ?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#DE> .
 ?offer dc:publisher ?vendor .
 ?offer bsbm:validTo ?date .
 FILTER (?date > '1988-01-01'^^xsd:date)
 }
 OPTIONAL {
	?review bsbm:reviewFor <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> .
	?review rev:reviewer ?reviewer .
	?reviewer foaf:name ?revName .
	?review dc:title ?revTitle .
 OPTIONAL { ?review bsbm:rating1 ?rating1 . }
 OPTIONAL { ?review bsbm:rating2 ?rating2 . }
 }
}


--[SPARQL Q8]
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX rev: <http://purl.org/stuff/rev#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>

SELECT ?title ?text ?reviewDate ?reviewer ?reviewerName ?rating1 ?rating2 ?rating3 ?rating4
WHERE {
	?review bsbm:reviewFor <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> .
	?review dc:title ?title .
	?review rev:text ?text .
	?review bsbm:reviewDate ?reviewDate .
	?review rev:reviewer ?reviewer .
	?reviewer foaf:name ?reviewerName .
	OPTIONAL { ?review bsbm:rating1 ?rating1 . }
	OPTIONAL { ?review bsbm:rating2 ?rating2 . }
	OPTIONAL { ?review bsbm:rating3 ?rating3 . }
	OPTIONAL { ?review bsbm:rating4 ?rating4 . }
}

--[SPARQL Q9]
PREFIX rev: <http://purl.org/stuff/rev#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>

SELECT ?p ?mbox_sha1sum ?country ?r ?product ?title
WHERE {
<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite/Review88> rev:reviewer ?p .
?p foaf:name ?name .
?p foaf:mbox_sha1sum ?mbox_sha1sum .
?p bsbm:country ?country .
?r rev:reviewer ?p .
?r bsbm:reviewFor ?product .
?r dc:title ?title .
}

--[SPARQL Q10]
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX dc: <http://purl.org/dc/elements/1.1/>

SELECT DISTINCT ?offer ?product
WHERE {
	?offer bsbm:product  ?product .
	?product bsbm:productId ?id .
	FILTER (?id<100)
	?offer bsbm:vendor ?vendor .
    ?offer dc:publisher ?vendor .
	?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#US> .
	?offer bsbm:deliveryDays ?deliveryDays .
	FILTER (?deliveryDays <= 3)
	?offer bsbm:price ?price .
 ?offer bsbm:validTo ?date .
 FILTER (?date > '1988-01-01'^^xsd:date)
}

--[SPARQL Q11]
SELECT ?property ?hasValue ?isValueOf
WHERE {
 { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Offer88> ?property ?hasValue }
 UNION
 { ?isValueOf ?property <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromVendor/Offer88> }
}

--[SPARQL Q12]
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rev: <http://purl.org/stuff/rev#>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX bsbm-export: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/export/>
PREFIX dc: <http://purl.org/dc/elements/1.1/>

SELECT ?offer ?productURI ?productlabel ?vendorURI ?vendorname ?offerURL ?price ?deliveryDays ?validTo
WHERE {
 ?offer bsbm:offerId ?id .
 FILTER ( ?id < 1000)
 ?offer bsbm:product ?productURI .
 ?productURI rdfs:label ?productlabel .
 ?offer bsbm:vendor ?vendorURI .
 ?vendorURI rdfs:label ?vendorname .
 ?vendorURI foaf:homepage ?vendorhomepage .
 ?offer bsbm:offerWebpage ?offerURL .
 ?offer bsbm:price ?price .
 ?offer bsbm:deliveryDays ?deliveryDays .
 ?offer bsbm:validTo ?validTo
 }
