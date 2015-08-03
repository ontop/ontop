PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#> 
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
PREFIX npd: <http://sws.ifi.uio.no/data/npd-v2/> 
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> 
PREFIX owl: <http://www.w3.org/2002/07/owl#> 
PREFIX nlx: <http://sws.ifi.uio.no/data/norlex/> 
PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#> 
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
SELECT DISTINCT ?licensee   
		   WHERE {
		         ?licensee npdv:licenseeForLicence [ npdv:licenceOperatorCompany ?company ] 
		   }
