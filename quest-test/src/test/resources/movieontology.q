[QueryGroup="SimpleQueries"] @collection [[
[QueryItem="Find actress"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x
WHERE { 
   $x a :Actress. $x dbpedia:birthName "Pfeiffer, Michelle"
}

[QueryItem="Find actor"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x 
WHERE { 
   $x a dbpedia:Actor. $x dbpedia:birthName "Aaker, Lee"
}

[QueryItem="Find movie"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x 
WHERE { 
   $x a mo:Movie. $x mo:title "Finding Nemo"
}

[QueryItem="Find TV series"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x 
WHERE { 
   $x a mo:TVSeries. $x mo:title "24"
}

[QueryItem="Find writer"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x 
WHERE { 
   $x a dbpedia:Writer. $x dbpedia:birthName "Barker, Clive"
}

[QueryItem="Find producer"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x 
WHERE { 
   $x a mo:Producer. $x dbpedia:birthName "Silver, Joel"
}

[QueryItem="Find director"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX page: <http://dbpedia.org/page/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x 
WHERE { 
   $x a page:Film_Director. $x dbpedia:birthName "Tarantino, Quentin"
}

[QueryItem="Find editor"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x 
WHERE { 
   $x a mo:Editor. $x dbpedia:birthName "Rawlings, Terry"
}

[QueryItem="Find movie genre"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $y 
WHERE { 
   $x a mo:Movie; mo:title "Finding Nemo"; mo:belongsToGenre $y 
}

[QueryItem="Find TV series genre"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $y
WHERE { 
   $x a mo:TVSeries; mo:title "24"; mo:belongsToGenre $y
}

[QueryItem="Find movie budget"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $y 
WHERE { 
   $x a mo:Movie; mo:title "Finding Nemo"; dbpedia:budget $y
}

[QueryItem="Find TV series budget"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $y
WHERE { 
   $x a mo:TVSeries; mo:title "24"; dbpedia:budget $y
}

[QueryItem="Find movie gross"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $y 
WHERE { 
   $x a mo:Movie; mo:title "Finding Nemo"; dbpedia:gross $y
}

[QueryItem="Find TV series gross"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $y
WHERE { 
   $x a mo:TVSeries; mo:title "24"; dbpedia:gross $y
}

[QueryItem="Find movie production year"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $y
WHERE { 
   $x a mo:Movie. $x mo:title "Finding Nemo";  dbpedia:productionStartYear $y
}

[QueryItem="Find TV series production year"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $y
WHERE { 
   $x a mo:TVSeries; mo:title "24"; dbpedia:productionStartYear $y
}

[QueryItem="Find movie actors (male and female)"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $z
WHERE { 
   $x a mo:Movie. $x mo:title "Finding Nemo";  mo:hasActor $y .
   $y dbpedia:birthName $z
}

[QueryItem="Find movie male actor"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $z
WHERE { 
   $x a mo:Movie. $x mo:title "Finding Nemo";  mo:hasMaleActor $y .
   $y dbpedia:birthName $z
}

[QueryItem="Find movie female actor"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $z
WHERE { 
   $x a mo:Movie. $x mo:title "Finding Nemo";  mo:hasActress $y .
   $y dbpedia:birthName $z
}

[QueryItem="Find movie director"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $z
WHERE { 
   $x a mo:Movie. $x mo:title "Finding Nemo";  mo:hasDirector $y .
   $y dbpedia:birthName $z
}

[QueryItem="Find movie producer"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $z
WHERE { 
   $x a mo:Movie. $x mo:title "Finding Nemo";  mo:hasProducer $y .
   $y dbpedia:birthName $z
}

[QueryItem="Find movie editor"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $z
WHERE { 
   $x a mo:Movie. $x mo:title "Finding Nemo";  mo:hasEditor $y .
   $y dbpedia:birthName $z
}

[QueryItem="Find movie producing company"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $z
WHERE { 
   $x a mo:Movie. $x mo:title "Finding Nemo";  mo:isProducedBy $y .
   $y :companyName $z
}

[QueryItem="Find movie producing company country origin"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $z1 $z2
WHERE { 
   $x a mo:Movie. $x mo:title "Finding Nemo";  mo:isProducedBy $y .
   $y :companyName $z1; :hasCompanyLocation $z2
}
]]

[QueryGroup="ComplexQueries"] @collection [[
[QueryItem="Find the Top 25 movie titles based on a specific genre"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
select ?x ?title ?rating
where {
   ?x a mo:Movie; 
        mo:title ?title; 
        mo:imdbrating ?rating;
        mo:belongsToGenre [ a mo:Brute_Action ] .
}
order by desc(?rating)
limit 25

[QueryItem="Find the Bottom 10 movie titles based on rating"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
select ?x ?title ?rating
where { 
  ?x a mo:Movie;
       mo:title ?title;
       mo:imdbrating ?rating .
}
order by ?rating
limit 10

[QueryItem="Find all movie information given a particular movie title"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
select ?x ?genre ?production_year ?budget ?gross ?rating ?actor_name ?director_name ?producer_name ?editor_name
where { 
  ?x a mo:Movie;
       mo:title "Finding Nemo";
       mo:belongsToGenre ?genre;
       dbpedia:productionStartYear ?production_year;
       dbpedia:budget ?budget;
       dbpedia:gross ?gross;
       mo:imdbrating ?rating .
  ?x mo:hasActor ?actor . ?actor dbpedia:birthName ?actor_name .
  ?x mo:hasDirector ?director . ?director dbpedia:birthName ?director_name .
  ?x mo:hasProducer ?producer . ?producer dbpedia:birthName ?producer_name .
  ?x mo:hasEditor ?editor . ?editor dbpedia:birthName ?editor_name .
}

[QueryItem="Find production companies in a specific region"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
select ?x ?company_name ?country_code
where { 
  ?x :hasCompanyLocation ?y; :companyName ?company_name .
  ?y a mo:America; :countryCode ?country_code .
}

[QueryItem="Find movie titles produced by production companies in Eastern Asia and has gross revenue over 1 million USD"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
SELECT ?y ?name ?movie_title ?prod_year
WHERE {
  ?x mo:title ?movie_title;
       dbpedia:productionStartYear ?prod_year;
       mo:isProducedBy ?y .
  ?y :companyName ?name;
       :hasCompanyLocation [ a mo:Eastern_Asia ] .
  FILTER ( ?prod_year >= 2000 && ?prod_year <= 2010 )
}

[QueryItem="Find action movies produced in eastern asia"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $title $company_name
WHERE { 
   $x a mo:Movie; mo:title ?title; mo:isProducedBy $y; mo:belongsToGenre $z .
   $y :companyName $company_name; :hasCompanyLocation [ a mo:Eastern_Asia ] .
   $z a mo:Brute_Action .
}

[QueryItem="Find names that act as both the director and the actor at the same time produced in eastern asia "]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $title $company_name
WHERE { 
   $m a mo:Movie; mo:title ?title; mo:hasActor ?x; mo:hasDirector ?x; mo:isProducedBy $y; mo:belongsToGenre $z .
   $x dbpedia:birthName $actor_name .
   $y :companyName $company_name; :hasCompanyLocation [ a mo:Eastern_Asia ] .
   $z a mo:Love .
}
]]

[QueryGroup="ISWCQueries"] @collection [[
[QueryItem="Q1"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX page: <http://dbpedia.org/page/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $title
WHERE { 
   $x a page:Film_Director; dbpedia:birthName "Tarantino, Quentin";
        mo:isDirectorOf $y . $y mo:title $title .
}

[QueryItem="Q2"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $title
WHERE { 
   $x mo:belongsToGenre $y; mo:title ?title . 
   $y a mo:Actionreach .
}

[QueryItem="Q3"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $name $birthdate
WHERE { 
   $x mo:isActorIn $y; dbpedia:birthName $name; dbpedia:birthDate $birthdate . 
   $y mo:title "Finding Nemo"
}

[QueryItem="Q4"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT $x $budget $gross $director_name $producer_name $editor_name $company_name
WHERE { 
   $x a mo:Movie; mo:title "Finding Nemo";
        dbpedia:budget $budget;
        dbpedia:gross $gross .
   $x mo:hasDirector $director . $director dbpedia:birthName $director_name .
   $x mo:hasDirector $producer . $producer dbpedia:birthName $producer_name .
   $x mo:hasDirector $editor . $editor dbpedia:birthName $editor_name .
   $x mo:isProducedBy $y . $y :companyName $company_name
}

[QueryItem="Q5"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT DISTINCT $x $title $actor_name ?prod_year ?rating
WHERE { 
   $m a mo:Movie; 
         mo:title ?title; 
         mo:imdbrating ?rating; 
         dbpedia:productionStartYear ?prod_year; 
         mo:hasActor ?x; 
         mo:hasDirector ?x .
   $x dbpedia:birthName $actor_name .
   FILTER ( ?rating > '7.0' && ?prod_year >= 2000 && ?prod_year <= 2010 )
}
order by desc(?rating) ?prod_year
limit 25
]]

[QueryGroup="InterestingQuery"] @collection [[
[QueryItem="IQ1"]
PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>
PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>
PREFIX dbpedia: <http://dbpedia.org/ontology/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT *
WHERE { 
   $m a mo:Movie; mo:isProducedBy $y .
   $y :hasCompanyLocation [ a mo:Eastern_Asia ] .
}
]]
