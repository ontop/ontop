PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>
SELECT DISTINCT ?wellbore ?wc ?well ?length
 WHERE { 
  ?wellbore npdv:wellboreForDiscovery ?discovery;
            npdv:belongsToWell ?well.  
  ?wc npdv:coreForWellbore ?wellbore.
  {  
     ?wc npdv:coresTotalLength ?lmeters ;
         npdv:coreIntervalUOM "000002"^^xsd:string .
         BIND(?lmeters AS ?length)
  } 
  UNION
  {
    ?wc npdv:coresTotalLength ?lfeets ;
        npdv:coreIntervalUOM "000001"^^xsd:string .
    BIND((?lfeets) AS ?length) # originally  * 0.3048 
  }                
  FILTER (?length < 22337)
}