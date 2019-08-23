[QueryItem="Q3"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?staff 
WHERE { 
    ?staff a lubm:Faculty .
    ?staff lubm:degreeFrom ?uni .
    ?uni a lubm:University . 
    ?dept lubm:subOrganizationOf ?uni .
    ?dept a lubm:Department .
    ?staff lubm:memberOf ?dept .
}

