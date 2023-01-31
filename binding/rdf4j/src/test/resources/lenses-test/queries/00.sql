PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>
PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

SELECT ?product (count(?feature) as ?featureNum)
WHERE {
 ?product bsbm:productId ?id .
     FILTER (?id <= 2 )
 ?product bsbm:productFeature ?feature .
 }
GROUP BY ?product