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


[QueryItem="R1"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?staff   
WHERE { 
	?staff lubm:worksFor ?uni .
	?uni lubm:affiliatedOrganizationOf ?org .
}

[QueryItem="R2"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?staff ?course   
WHERE { 
	?staff a lubm:Person .
	?staff lubm:teacherOf ?course .
	?course a lubm:Course .
}

[QueryItem="R3"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?student ?staff ?course   
WHERE { 
	?student a lubm:Student .
	?student lubm:advisor ?staff .
	?staff a lubm:Faculty .
	?student lubm:takesCourse ?course .
	?staff lubm:teacherOf ?course .
	?course a lubm:Course .
}

[QueryItem="R4"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?staff ?org   
WHERE { 
	?staff a lubm:Person .
	?staff lubm:worksFor ?org .
	?org a lubm:Organization .
}

[QueryItem="R5"]
PREFIX lubm: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>

SELECT ?staff   
WHERE { 
	?staff a lubm:Person .
	?staff lubm:worksFor ?org .
	?org a lubm:University .
	?org lubm:hasAlumnus ?staff .
}



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
    ?p2 a lubm:Student .
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
	

[QueryItem="V7"]
PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#>
SELECT ?X0 
WHERE {
      ?X0 a ub:Publication .
      
      ?X11 a ub:Professor .
      ?X0 ub:publicationAuthor  ?X11 .
      ?X11 ub:worksFor ?X12 .
      ?X12  a ub:Subj1Department .

      ?X21 a ub:Professor .
      ?X0 ub:publicationAuthor  ?X21 .
      ?X21 ub:worksFor ?X22 .
      ?X22 a ub:Subj2Department .


      ?X31 a ub:Professor .
      ?X0 ub:publicationAuthor  ?X31 .
      ?X31 ub:worksFor ?X32 .
      ?X32 a ub:Subj3Department .
}

[QueryItem="V8"]
PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#>
SELECT ?X0 
WHERE {
      ?X0 a ub:Publication .
      
      ?X11 a ub:Subj1Professor .
      ?X0 ub:publicationAuthor  ?X11 .
      ?X11 ub:worksFor ?X12 .
      ?X12  a ub:Department .

      ?X21 a ub:Subj2Professor .
      ?X0 ub:publicationAuthor  ?X21 .
      ?X21 ub:worksFor ?X22 .
      ?X22 a ub:Department .


      ?X31 a ub:Subj3Professor .
      ?X0 ub:publicationAuthor  ?X31 .
      ?X31 ub:worksFor ?X32 .
      ?X32 a ub:Department .
}

[QueryItem="V9"]
PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#>
SELECT ?X10  
WHERE {

    ?X31 ub:publicationAuthor ?X10 .
    ?X31 ub:publicationAuthor ?X11 .
    ?X31 a ub:JournalArticle .
    ?X32 ub:publicationAuthor ?X11 .
    ?X32 ub:publicationAuthor ?X12 .
    ?X32 a ub:JournalArticle .

    ?X10 ub:teacherOf ?X20 .
    ?X20 a ub:Subj6Course .
    ?X10 ub:worksFor  ?X50 .
    ?X50 a ub:Subj6Department .
    ?X00 a ub:Subj6Student .
    ?X00 a ub:GraduateStudent .
    ?X00 ub:takesCourse ?X20 .
    ?X3 ub:publicationAuthor ?X00 .
    ?X3 a ub:JournalArticle .

    ?X11 ub:teacherOf ?X21 .
    ?X21 a ub:Subj6Course .
    ?X11 ub:worksFor  ?X51 .
    ?X51 a ub:Subj6Department .
    ?X01 a ub:Subj6Student .
    ?X01 a ub:GraduateStudent .
    ?X01 ub:takesCourse ?X21 .
    ?X11 a ub:AssociateProfessor .
    ?X21 a ub:GraduateCourse .

    ?X12 ub:teacherOf ?X22 .
    ?X12 ub:worksFor  ?X52 .
    ?X02 a ub:GraduateStudent .
    ?X02 ub:takesCourse ?X22 .
    ?X22 a ub:GraduateCourse .
    ?X12 ub:memberOf ?X42 .
    ?X42 a ub:Institute .
    ?X22 a ub:GraduateCourse .
    ?X33 ub:publicationAuthor ?X12 .
    ?X33 a ub:JournalArticle .
}

[QueryItem="V10"]
PREFIX ub:<http://swat.cse.lehigh.edu/onto/univ-bench.owl#>
SELECT ?X10  
WHERE {

    ?X31 ub:publicationAuthor ?X10 .
    ?X31 ub:publicationAuthor ?X11 .
    ?X31 a ub:JournalArticle .
    ?X32 ub:publicationAuthor ?X11 .
    ?X32 ub:publicationAuthor ?X12 .
    ?X32 a ub:JournalArticle .

    ?X10 ub:teacherOf ?X20 .
    ?X20 a ub:Subj6Course .
    ?X10 ub:worksFor  ?X50 .
    ?X50 a ub:Subj6Department .
    ?X00 a ub:Subj6Student .
    ?X00 a ub:GraduateStudent .
    ?X00 ub:takesCourse ?X20 .
    ?X3 ub:publicationAuthor ?X00 .
    ?X3 a ub:JournalArticle .

    ?X11 ub:teacherOf ?X21 .
    ?X21 a ub:Subj6Course .
    ?X11 ub:worksFor  ?X51 .
    ?X51 a ub:Subj6Department .
    ?X01 a ub:Subj6Student .
    ?X01 a ub:GraduateStudent .
    ?X01 ub:takesCourse ?X21 .
    ?X11 a ub:AssociateProfessor .
    ?X21 a ub:GraduateCourse .

    ?X12 ub:teacherOf ?X22 .
    ?X12 ub:worksFor  ?X52 .
    ?X02 a ub:GraduateStudent .
    ?X02 ub:takesCourse ?X22 .
    ?X22 a ub:GraduateCourse .
    ?X12 ub:memberOf ?X42 .
    ?X42 a ub:Institute .
    ?X22 a ub:GraduateCourse .
    ?X33 ub:publicationAuthor ?X12 .
    ?X33 a ub:JournalArticle .

}
