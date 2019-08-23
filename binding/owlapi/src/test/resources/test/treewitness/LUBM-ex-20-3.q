[QueryItem="Q2"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?student1 ?student2
WHERE { 
    ?student1 a lubm:Subj3Student .
    ?student1 lubm:takesCourse ?course .
    ?student2 a lubm:Subj4Student . 
    ?student2 lubm:takesCourse ?course .
 }
