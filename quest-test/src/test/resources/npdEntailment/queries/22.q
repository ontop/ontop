PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#>
SELECT DISTINCT ?wc 
		   WHERE { 
		      ?wc npdv:coreForWellbore [ rdf:type npdv:Wellbore ]. 
		   }