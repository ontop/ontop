[QueryGroup="TestQueries"] @collection [[
[QueryItem="q1Materialize"]
 PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>
 PREFIX isc: <http://resource.geosciml.org/classifier/ics/ischart/>
 PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>
 PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
 PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>
 PREFIX void: <http://rdfs.org/ns/void#>
 PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
 PREFIX owl: <http://www.w3.org/2002/07/owl#>
 PREFIX ex: <http://example.org/ex#>
 PREFIX quest: <http://obda.org/quest#>
 PREFIX diskos: <http://sws.ifi.uio.no/data/diskos/>
 PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>
 PREFIX ptl: <http://sws.ifi.uio.no/vocab/npd-v2-ptl#>
 PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>
 PREFIX geos: <http://www.opengis.net/ont/geosparql#>
 PREFIX sql: <http://sws.ifi.uio.no/vocab/sql#>
 PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
 PREFIX dc: <http://purl.org/dc/elements/1.1/>
 PREFIX diskosv: <http://sws.ifi.uio.no/vocab/diskos#>
 SELECT DISTINCT ?x ?licenceURI ?interest ?date
 WHERE {
 	?x rdfs:subClassOf :Agent .
     ?licenceURI a ?x .

     [ ] a npdv:ProductionLicenceLicensee ;
       	npdv:dateLicenseeValidFrom ?date ;
       	npdv:licenseeInterest ?interest ;
       	npdv:licenseeForLicence ?licenceURI .
    FILTER(?date > "1979-12-31T00:00:00"^^xsd:dateTime)
 }

 [QueryItem="q1Property"]
 PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>
 PREFIX isc: <http://resource.geosciml.org/classifier/ics/ischart/>
 PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>
 PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
 PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>
 PREFIX void: <http://rdfs.org/ns/void#>
 PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
 PREFIX owl: <http://www.w3.org/2002/07/owl#>
 PREFIX ex: <http://example.org/ex#>
 PREFIX quest: <http://obda.org/quest#>
 PREFIX diskos: <http://sws.ifi.uio.no/data/diskos/>
 PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>
 PREFIX ptl: <http://sws.ifi.uio.no/vocab/npd-v2-ptl#>
 PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>
 PREFIX geos: <http://www.opengis.net/ont/geosparql#>
 PREFIX sql: <http://sws.ifi.uio.no/vocab/sql#>
 PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
 PREFIX dc: <http://purl.org/dc/elements/1.1/>
 PREFIX diskosv: <http://sws.ifi.uio.no/vocab/diskos#>
 SELECT DISTINCT ?licenceURI ?y ?interest ?date
 WHERE {

     ?licenceURI a npdv:ProductionLicence .
     ?licenceURI ?y ?z .

     [ ] a npdv:ProductionLicenceLicensee ;

       	npdv:dateLicenseeValidFrom ?date ;
       	npdv:licenseeInterest ?interest ;
       	npdv:licenseeForLicence ?licenceURI .
    FILTER(?date > "1979-12-31T00:00:00"^^xsd:dateTime)
 }

[QueryItem="q5Materialize"]
PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>
PREFIX isc: <http://resource.geosciml.org/classifier/ics/ischart/>
PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>
PREFIX void: <http://rdfs.org/ns/void#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX ex: <http://example.org/ex#>
PREFIX quest: <http://obda.org/quest#>
PREFIX diskos: <http://sws.ifi.uio.no/data/diskos/>
PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>
PREFIX ptl: <http://sws.ifi.uio.no/vocab/npd-v2-ptl#>
PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>
PREFIX geos: <http://www.opengis.net/ont/geosparql#>
PREFIX sql: <http://sws.ifi.uio.no/vocab/sql#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX diskosv: <http://sws.ifi.uio.no/vocab/diskos#>
SELECT  ?fr ?OE ?oil ?gas ?NGL ?con
WHERE {
		?x rdfs:subClassOf :Reserve .
   		?fr a ?x ;
       #npdv:name ?field ;
       npdv:remainingCondensate     ?con ;
       npdv:remainingGas            ?gas ;
       npdv:remainingNGL            ?NGL ;
       npdv:remainingOil            ?oil ;
       npdv:remainingOilEquivalents ?OE  .

	FILTER(?gas < 100)

} ORDER BY DESC(?OE)

[QueryItem="q5NoData"]
PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>
PREFIX isc: <http://resource.geosciml.org/classifier/ics/ischart/>
PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>
PREFIX void: <http://rdfs.org/ns/void#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX ex: <http://example.org/ex#>
PREFIX quest: <http://obda.org/quest#>
PREFIX diskos: <http://sws.ifi.uio.no/data/diskos/>
PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>
PREFIX ptl: <http://sws.ifi.uio.no/vocab/npd-v2-ptl#>
PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>
PREFIX geos: <http://www.opengis.net/ont/geosparql#>
PREFIX sql: <http://sws.ifi.uio.no/vocab/sql#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX diskosv: <http://sws.ifi.uio.no/vocab/diskos#>
SELECT  ?fr ?y ?OE ?oil ?gas ?NGL ?con
WHERE {
   ?fr a npdv:FieldReserve ;
   ?y ?z ;
       #npdv:name ?field ;
       npdv:remainingCondensate     ?con ;
       npdv:remainingGas            ?gas ;
       npdv:remainingNGL            ?NGL ;
       npdv:remainingOil            ?oil ;
       npdv:remainingOilEquivalents ?OE  .

	FILTER(?gas < 100)

} ORDER BY DESC(?OE)

[QueryItem="q5Disjoint"]
PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>
PREFIX isc: <http://resource.geosciml.org/classifier/ics/ischart/>
PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>
PREFIX void: <http://rdfs.org/ns/void#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX ex: <http://example.org/ex#>
PREFIX quest: <http://obda.org/quest#>
PREFIX diskos: <http://sws.ifi.uio.no/data/diskos/>
PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>
PREFIX ptl: <http://sws.ifi.uio.no/vocab/npd-v2-ptl#>
PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>
PREFIX geos: <http://www.opengis.net/ont/geosparql#>
PREFIX sql: <http://sws.ifi.uio.no/vocab/sql#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX diskosv: <http://sws.ifi.uio.no/vocab/diskos#>
SELECT  ?fr ?OE ?oil ?gas ?NGL ?con
WHERE {
		{npdv:FieldReserve owl:disjointWith ?x .
   		?fr a ?x  ;
       	npdv:remainingGas            ?gas . }

       	UNION
       	{
       	npdv:FieldReserve owl:disjointWith ?x .
       ?fr a ?x  ;
       	npdv:remainingOil            ?oil ;

	}

}

[QueryItem="q6ActiveRange"]
PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>
PREFIX isc: <http://resource.geosciml.org/classifier/ics/ischart/>
PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>
PREFIX void: <http://rdfs.org/ns/void#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX ex: <http://example.org/ex#>
PREFIX quest: <http://obda.org/quest#>
PREFIX diskos: <http://sws.ifi.uio.no/data/diskos/>
PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>
PREFIX ptl: <http://sws.ifi.uio.no/vocab/npd-v2-ptl#>
PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>
PREFIX geos: <http://www.opengis.net/ont/geosparql#>
PREFIX sql: <http://sws.ifi.uio.no/vocab/sql#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX diskosv: <http://sws.ifi.uio.no/vocab/diskos#>
SELECT DISTINCT ?y ?wellbore (?length AS ?lenghtM) ?company ?year
WHERE {
  ?wc npdv:coreForWellbore
        [ rdf:type                      ?y ;
          npdv:name                     ?wellbore ;
          npdv:wellboreCompletionYear   ?year ;
          npdv:drillingOperatorCompany  [ npdv:name ?company ]
        ] .
  { ?wc npdv:coresTotalLength ?length }

  FILTER(?year >= "2008"^^xsd:integer &&
         ?length > 50
  )
} ORDER BY ?wellbore

[QueryItem="q6StaticRange"]
PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>
PREFIX isc: <http://resource.geosciml.org/classifier/ics/ischart/>
PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>
PREFIX void: <http://rdfs.org/ns/void#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX ex: <http://example.org/ex#>
PREFIX quest: <http://obda.org/quest#>
PREFIX diskos: <http://sws.ifi.uio.no/data/diskos/>
PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>
PREFIX ptl: <http://sws.ifi.uio.no/vocab/npd-v2-ptl#>
PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>
PREFIX geos: <http://www.opengis.net/ont/geosparql#>
PREFIX sql: <http://sws.ifi.uio.no/vocab/sql#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dc: <http://purl.org/dc/elements/1.1/>
PREFIX diskosv: <http://sws.ifi.uio.no/vocab/diskos#>
SELECT DISTINCT ?y ?wellbore (?length AS ?lenghtM) ?company ?year
WHERE {
 npdv:coreForWellbore rdfs:range   ?y  .
?wc  npdv:coreForWellbore
        [
                  npdv:name                     ?wellbore ;
                  npdv:wellboreCompletionYear   ?year ;
                  npdv:drillingOperatorCompany  [ npdv:name ?company ]
                ] .
          { ?wc npdv:coresTotalLength ?length .
          }

          FILTER(?year >= "2008"^^xsd:integer &&
                 ?length > 50
          )
        } ORDER BY ?wellbore



[QueryItem="q7Materialize"]
PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>
 PREFIX isc: <http://resource.geosciml.org/classifier/ics/ischart/>
 PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>
 PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
 PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>
 PREFIX void: <http://rdfs.org/ns/void#>
 PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
 PREFIX owl: <http://www.w3.org/2002/07/owl#>
 PREFIX ex: <http://example.org/ex#>
 PREFIX quest: <http://obda.org/quest#>
 PREFIX diskos: <http://sws.ifi.uio.no/data/diskos/>
 PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>
 PREFIX ptl: <http://sws.ifi.uio.no/vocab/npd-v2-ptl#>
 PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>
 PREFIX geos: <http://www.opengis.net/ont/geosparql#>
 PREFIX sql: <http://sws.ifi.uio.no/vocab/sql#>
 PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
 PREFIX dc: <http://purl.org/dc/elements/1.1/>
 PREFIX diskosv: <http://sws.ifi.uio.no/vocab/diskos#>
SELECT *
WHERE {

?x rdfs:subClassOf <http://www.ifomis.org/bfo/1.1/span#ProcessualEntity> .
  ?fmp a ?x ;
       #npdv:productionForField [ npdv:name ?field ] ;
       npdv:productionYear         ?year;
       npdv:productionMonth        ?month;
       npdv:producedCondensate     ?con ;
       npdv:producedGas            ?gas ;
       npdv:producedNGL            ?NGL ;
       npdv:producedOil            ?oil ;
       npdv:producedOilEquivalents ?maxOE  .

   FILTER(?gas < 100)
}

[QueryItem="q7NoData"]

 PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#>
 PREFIX isc: <http://resource.geosciml.org/classifier/ics/ischart/>
 PREFIX nlxv: <http://sws.ifi.uio.no/vocab/norlex#>
 PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
 PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/>
 PREFIX void: <http://rdfs.org/ns/void#>
 PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
 PREFIX owl: <http://www.w3.org/2002/07/owl#>
 PREFIX ex: <http://example.org/ex#>
 PREFIX quest: <http://obda.org/quest#>
 PREFIX diskos: <http://sws.ifi.uio.no/data/diskos/>
 PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/>
 PREFIX ptl: <http://sws.ifi.uio.no/vocab/npd-v2-ptl#>
 PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>
 PREFIX geos: <http://www.opengis.net/ont/geosparql#>
 PREFIX sql: <http://sws.ifi.uio.no/vocab/sql#>
 PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
 PREFIX dc: <http://purl.org/dc/elements/1.1/>
 PREFIX diskosv: <http://sws.ifi.uio.no/vocab/diskos#>
SELECT ?x ?y
WHERE {

    ?x a npdv:FieldMonthlyProduction ;
     ?y ?z ;
       #npdv:productionForField [ npdv:name ?field ] ;
       npdv:productionYear         ?year;
       npdv:productionMonth        ?month;
       npdv:producedCondensate     ?con ;
       npdv:producedGas            ?gas ;
       npdv:producedNGL            ?NGL ;
       npdv:producedOil            ?oil ;
       npdv:producedOilEquivalents ?maxOE  .

   FILTER(?gas < 100)
}


]]
