[QueryItem="CACHE EXTS"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?staff ?uni ?org ?course ?staff2 ?course2 ?student ?staff3 ?org2 ?student2 ?dept ?emp ?org3 ?emp2
WHERE { 
	?staff lubm:worksFor ?uni .
	?uni lubm:affiliatedOrganizationOf ?org .
	?staff2 a lubm:Person .
	?staff lubm:teacherOf ?course .
	?course2 a lubm:Course .
	?student2 a lubm:Student .
	?student lubm:advisor ?staff .
	?staff3 a lubm:Faculty .
	?student lubm:takesCourse ?course .
	?org2 a lubm:Organization .
	?org lubm:hasAlumnus ?staff .
    ?dept a lubm:Department .
    ?student lubm:memberOf ?dept .	
	?uni lubm:subOrganizationOf ?org .
	?org3 a lubm:University .
	?emp a lubm:Employee .
	?emp2 a lubm:Professor .
	?course a lubm:GraduateCourse .
}

[QueryItem="C1"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?student ?staff
WHERE { 
    ?student a lubm:Student .
    ?student lubm:takesCourse ?course .
    ?course a lubm:Subj1Course . 
    ?staff lubm:teacherOf ?course .
    ?staff a lubm:Professor .
    ?staff lubm:headOf ?dept .
    ?dept a lubm:Department .
    ?student lubm:memberOf ?dept .
	}



[QueryItem="C4"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?x ?y  
WHERE { 
    ?x a lubm:Department .
    ?z lubm:memberOf ?x .
    ?z a lubm:Student . 
    ?z lubm:takesCourse ?v .
    ?w lubm:teacherOf ?w .
    ?w a lubm:Professor . 
    ?w lubm:memberOf ?y .
    ?y a lubm:Department .
	}
	
[QueryItem="C5"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?x  
WHERE { 
    ?x a lubm:Person .
    ?x lubm:worksFor ?y .
    ?y a lubm:Department .
    ?x lubm:takesCourse ?z .
    ?z a lubm:Course .
	}
	
[QueryItem="C6"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?x  
WHERE { 
    ?x a lubm:Student .
    ?y lubm:publicationAuthor ?x .
    ?y a lubm:Publication .
    ?x lubm:teachingAssistantOf ?z .
    ?z a lubm:Course .
	}

