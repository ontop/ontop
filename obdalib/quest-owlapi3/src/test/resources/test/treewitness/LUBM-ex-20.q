[QueryItem="Q1"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?student ?staff
WHERE { 
    ?student a lubm:Student .
    ?student lubm:takesCourse ?course .
    ?course a lubm:Course . 
    ?staff lubm:teacherOf ?course .
    ?staff a lubm:Faculty .
    ?staff lubm:worksFor ?dept .
    ?dept a lubm:Department .
    ?student lubm:memberOf ?dept .
	}

[QueryItem="Q2"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?student1 ?student2
WHERE { 
    ?student1 a lubm:Subj3Student .
    ?student1 lubm:takesCourse ?course .
    ?student2 a lubm:Subj4Student . 
    ?student2 lubm:takesCourse ?course .
 }

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

[QueryItem="Q4"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?dept1 ?dept4
WHERE { 
    ?dept1 a lubm:Subj3Department .
    ?dept4 a lubm:Subj4Department .
    ?p0 a lubm:Professor . 
    ?p0 lubm:memberOf ?dept1 .
    ?publ lubm:publicationAuthor ?p0 .
    ?p3 a lubm:Professor .
    ?p3 lubm:memberOf ?dept4 .
    ?publ lubm:publicationAuthor ?p3 .
	}

[QueryItem="Q5"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?publ 
WHERE { 
    ?publ a lubm:Publication .
    ?publ lubm:publicationAuthor ?p1 .
    ?p1 a lubm:Professor . 
    ?publ lubm:publicationAuthor ?p2 .
    ?p2 a lubm:Student
	}

[QueryItem="Q6"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?uni0 ?uni1  
WHERE { 
    ?uni0 a lubm:University .
    ?uni1 a lubm:University .
    ?stud lubm:memberOf ?uni0 .
    ?stud a lubm:Student . 
    ?uni1 a lubm:University .
    ?prof lubm:memberOf ?uni1 .
    ?prof a lubm:Professor . 
    ?stud lubm:advisor ?prof .
	}