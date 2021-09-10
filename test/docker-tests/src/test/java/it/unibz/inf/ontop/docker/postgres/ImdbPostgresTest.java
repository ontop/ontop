package it.unibz.inf.ontop.docker.postgres;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


/**
 * Test case for the IMDB database see wiki Example_MovieOntology
 * Created by Sarah on 30/07/14.
 */


public class ImdbPostgresTest extends AbstractVirtualModeTest {

    private static final String owlFile = "/pgsql/imdb/movieontology.owl";
    private static final String obdaFile = "/pgsql/imdb/movieontology.obda";
    private static final String propertyFile = "/pgsql/imdb/movieontology.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertyFile);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    @Test
    public void testOneQuery() throws Exception {
        countResults(15173, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  *\n" +
                "WHERE { \n" +
                "   $m a mo:Movie; mo:isProducedBy $y .\n" +
                "   $y :hasCompanyLocation [ a mo:Eastern_Asia ] .\n" +
                "}");
    }

    @Test
    public void testCompanyLocationQuery() throws Exception {
        countResults(7738, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $company_name\n" +
                "WHERE { \n" +
                "   $y :companyName $company_name; :hasCompanyLocation [ a mo:Eastern_Asia ] .\n" +
                "}");
    }

    @Test
    public void testIndividuals() throws Exception {
        countResults(29405, "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "SELECT DISTINCT $z \n" +
                "WHERE { \n" +
                "   $z a mo:Love .\n" +
                "}");
    }

    @Test
    public void testFindActress() throws Exception {
        countResults(1, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x\n" +
                "WHERE {\n" +
                "   $x a :Actress. $x dbpedia:birthName \"Pfeiffer, Michelle\"^^xsd:string\n" +
                "}");
    }

    @Test
    public void testFindActor() throws Exception {
        countResults(1, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x\n" +
                "WHERE {\n" +
                "   $x a dbpedia:Actor. $x dbpedia:birthName \"Aaker, Lee\"^^xsd:string\n" +
                "}");
    }

    @Test
    public void testFindMovie() throws Exception {
        countResults(1, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x\n" +
                "WHERE {\n" +
                "   $x a mo:Movie. $x mo:title \"Finding Nemo\"^^xsd:string\n" +
                "}");
    }

    @Test
    public void testFindTvSeries() throws Exception {
        countResults(1, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x\n" +
                "WHERE {\n" +
                "   $x a mo:TVSeries. $x mo:title \"24\"^^xsd:string\n" +
                "}");
    }

    @Test
    public void testFindWriter() throws Exception {
        countResults(2, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x\n" +
                "WHERE {\n" +
                "   $x a dbpedia:Writer. $x dbpedia:birthName \"Barker, Clive\"^^xsd:string\n" +
                "}");
    }

    @Test
    public void testFindProducer() throws Exception {
        countResults(1, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x\n" +
                "WHERE {\n" +
                "   $x a mo:Producer. $x dbpedia:birthName \"Silver, Joel\"^^xsd:string\n" +
                "}");
    }

    @Test
    public void testFindDirector() throws Exception {
        countResults(1, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX page: <http://dbpedia.org/page/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x\n" +
                "WHERE {\n" +
                "   $x a page:Film_Director. $x dbpedia:birthName \"Tarantino, Quentin\"^^xsd:string\n" +
                "}");
    }

    @Test
    public void testFindEditor() throws Exception {
        countResults(1, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x\n" +
                "WHERE {\n" +
                "   $x a mo:Editor. $x dbpedia:birthName \"Rawlings, Terry\"^^xsd:string\n" +
                "}");
    }

    @Test
    public void testFindMovieGenre() throws Exception {
        countResults(1, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $y\n" +
                "WHERE {\n" +
                "   $x a mo:Movie; mo:title \"Finding Nemo\"^^xsd:string; mo:belongsToGenre $y\n" +
                "}");
    }

    @Test
    public void testFindTvSeriesGenre() throws Exception {
        countResults(1, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $y\n" +
                "WHERE {\n" +
                "   $x a mo:TVSeries; mo:title \"24\"^^xsd:string; mo:belongsToGenre $y\n" +
                "}");
    }

    @Test
    public void testFindMovieBudget() throws Exception {
        countResults(1, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $y\n" +
                "WHERE {\n" +
                "   $x a mo:Movie; mo:title \"Finding Nemo\"^^xsd:string; dbpedia:budget $y\n" +
                "}");
    }

    @Test
    public void testFindTvSeriesBudget() throws Exception {
        countResults(1, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $y\n" +
                "WHERE {\n" +
                "   $x a mo:TVSeries; mo:title \"24\"^^xsd:string; dbpedia:budget $y\n" +
                "}");
    }

    @Test
    public void testFindMovieGross() throws Exception {
        countResults(89, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $y\n" +
                "WHERE {\n" +
                "   $x a mo:Movie; mo:title \"Finding Nemo\"^^xsd:string; dbpedia:gross $y\n" +
                "}");
    }

    @Test
    public void testFindTvSeriesGross() throws Exception {
        countResults(2, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $y\n" +
                "WHERE {\n" +
                "   $x a mo:TVSeries; mo:title \"Vendetta\"^^xsd:string; dbpedia:gross $y\n" +
                "}");
    }

    @Test
    public void testFindMovieProductionYear() throws Exception {
        countResults(1, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $y\n" +
                "WHERE {\n" +
                "   $x a mo:Movie. $x mo:title \"Finding Nemo\"^^xsd:string;  dbpedia:productionStartYear $y\n" +
                "}");
    }

    @Test
    public void testFindTvSeriesProductionYear() throws Exception {
        countResults(1, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $y\n" +
                "WHERE {\n" +
                "   $x a mo:TVSeries; mo:title \"24\"^^xsd:string; dbpedia:productionStartYear $y\n" +
                "}");
    }

    @Test
    public void testFindMovieActors() throws Exception {
        countResults(24, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $z\n" +
                "WHERE {\n" +
                "   $x a mo:Movie. $x mo:title \"Finding Nemo\"^^xsd:string;  mo:hasActor $y .\n" +
                "   $y dbpedia:birthName $z\n" +
                "}");
    }

    @Test
    public void testFindMovieMaleActors() throws Exception {
        countResults(18, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $z\n" +
                "WHERE {\n" +
                "   $x a mo:Movie. $x mo:title \"Finding Nemo\"^^xsd:string;  mo:hasMaleActor $y .\n" +
                "   $y dbpedia:birthName $z\n" +
                "}");
    }

    @Test
    public void testFindMovieFealeActors() throws Exception {
        countResults(6, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $z\n" +
                "WHERE {\n" +
                "   $x a mo:Movie. $x mo:title \"Finding Nemo\"^^xsd:string;  mo:hasActress $y .\n" +
                "   $y dbpedia:birthName $z\n" +
                "}");
    }

    @Test
    public void testFindMovieDirectors() throws Exception {
        countResults(2, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $z\n" +
                "WHERE {\n" +
                "   $x a mo:Movie. $x mo:title \"Finding Nemo\"^^xsd:string;  mo:hasDirector $y .\n" +
                "   $y dbpedia:birthName $z\n" +
                "}");
    }

    @Test
    public void testFindMovieProducers() throws Exception {
        countResults(3, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $z\n" +
                "WHERE {\n" +
                "   $x a mo:Movie. $x mo:title \"Finding Nemo\"^^xsd:string;  mo:hasProducer $y .\n" +
                "   $y dbpedia:birthName $z\n" +
                "}");
    }

    @Test
    public void testFindMovieEditors() throws Exception {
        countResults(1, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $z\n" +
                "WHERE {\n" +
                "   $x a mo:Movie. $x mo:title \"Finding Nemo\"^^xsd:string;  mo:hasEditor $y .\n" +
                "   $y dbpedia:birthName $z\n" +
                "}");
    }

    @Test
    public void testFindMovieProdicingCompany() throws Exception {
        countResults(3, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $z\n" +
                "WHERE {\n" +
                "   $x a mo:Movie. $x mo:title \"Finding Nemo\"^^xsd:string;  mo:isProducedBy $y .\n" +
                "   $y :companyName $z\n" +
                "}");
    }

    @Test
    public void testFindMovieFromAsianCompany() throws Exception {
        countResults(15173, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $title $company\n" +
                "WHERE {\n" +
                "   $x a mo:Movie. $x mo:title ?title;  mo:isProducedBy $y .\n" +
                "   $y a mo:East_Asian_Company ; :companyName $company .\n" +
                "}");
    }

    @Test
    public void testFindTop25MovieTitlesBasedOnSpecificGenre() throws Exception {
        countResults(25, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "select distinct ?x ?title ?year ?rating\n" +
                "where {\n" +
                "   ?x a mo:Movie;\n" +
                "        mo:title ?title;\n" +
                "        mo:imdbrating ?rating;\n" +
                "        mo:belongsToGenre [ a mo:Brute_Action ] .\n" +
                "   ?x dbpedia:productionStartYear ?year .\n" +
                "}\n" +
                "order by desc(?rating)\n" +
                "limit 25");
    }

    @Test
    public void testFindBottom10MovieTitlesBasedOnRating() throws Exception {
        countResults(10, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "select distinct ?x ?title ?rating\n" +
                "where {\n" +
                "  ?x a mo:Movie;\n" +
                "       mo:title ?title;\n" +
                "       mo:imdbrating ?rating .\n" +
                "}\n" +
                "order by ?rating\n" +
                "limit 10");
    }

    @Test
    public void testFindAllMovieInformationForATitle() throws Exception {
        countResults(12816, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "select ?x ?genre ?production_year ?budget ?gross ?rating ?actor_name ?director_name ?producer_name ?editor_name\n" +
                "where {\n" +
                "  ?x a mo:Movie;\n" +
                "       mo:title \"Finding Nemo\"^^xsd:string;\n" +
                "       mo:belongsToGenre ?genre;\n" +
                "       dbpedia:productionStartYear ?production_year;\n" +
                "       dbpedia:budget ?budget;\n" +
                "       dbpedia:gross ?gross;\n" +
                "       mo:imdbrating ?rating .\n" +
                "  ?x mo:hasActor ?actor . ?actor dbpedia:birthName ?actor_name .\n" +
                "  ?x mo:hasDirector ?director . ?director dbpedia:birthName ?director_name .\n" +
                "  ?x mo:hasProducer ?producer . ?producer dbpedia:birthName ?producer_name .\n" +
                "  ?x mo:hasEditor ?editor . ?editor dbpedia:birthName ?editor_name .\n" +
                "}");
    }

    @Test
    public void testFindProductionCompaniesInSpecificRegion() throws Exception {
        countResults(19167, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "select ?x ?company_name ?country_code\n" +
                "where {\n" +
                "  ?x :hasCompanyLocation ?y; :companyName ?company_name ; :countryCode ?country_code .\n" +
                " ?y a mo:Western_Europe .\n" +
                "}");
    }

    @Test
    public void testFindMovieTitlesProducedByProductionCompaniesInEasternAsia() throws Exception {
        countResults(8519, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "SELECT ?y ?name ?movie_title ?prod_year\n" +
                "WHERE {\n" +
                "  ?x mo:title ?movie_title;\n" +
                "       dbpedia:productionStartYear ?prod_year;\n" +
                "       mo:isProducedBy ?y .\n" +
                "  ?y :companyName ?name;\n" +
                "      :hasCompanyLocation [ a mo:Eastern_Asia ] .\n" +
                "  FILTER ( ?prod_year >= 2000 && ?prod_year <= 2010 )\n" +
                "}");
    }

    @Test
    public void testFindActionMoviesProducedInEasternAsia() throws Exception {
        countResults(2127, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT DISTINCT $x $title $company_name\n" +
                "WHERE {\n" +
                "   $x a mo:Movie; mo:title ?title; mo:isProducedBy $y; mo:belongsToGenre $z .\n" +
                "   $y :companyName $company_name; :hasCompanyLocation [ a mo:Eastern_Asia ] .\n" +
                "   $z a mo:Brute_Action .\n" +
                "}");
    }

    @Test
    public void testFindNamesThatActAsBothDirectorAndActorAtTheSamTimeProducedInEasternAsia() throws Exception {
        countResults(36, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT DISTINCT $x $title $actor_name $company_name\n" +
                "WHERE {\n" +
                "   $m a mo:Movie; mo:title ?title; mo:hasActor ?x; mo:hasDirector ?x; mo:isProducedBy $y; mo:belongsToGenre $z .\n" +
                "   $x dbpedia:birthName $actor_name .\n" +
                "   $y :companyName $company_name; :hasCompanyLocation [ a mo:Eastern_Asia ] .\n" +
                "   $z a mo:Love .\n" +
                "}");
    }

    @Test
    public void testQ1() throws Exception {
        countResults(18, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX page: <http://dbpedia.org/page/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $title\n" +
                "WHERE {\n" +
                "   $x a page:Film_Director; dbpedia:birthName \"Tarantino, Quentin\"^^xsd:string;\n" +
                "        mo:isDirectorOf $y . $y mo:title $title .\n" +
                "}");
    }

    @Test
    public void testQ2() throws Exception {
        countResults(57216, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $title\n" +
                "WHERE {\n" +
                "   $x mo:belongsToGenre $y; mo:title ?title .\n" +
                "   $y a mo:Actionreach .\n" +
                "}");
    }

    @Test
    public void testQ3() throws Exception {
        countResults(42, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $name $birthdate\n" +
                "WHERE {\n" +
                "   $x mo:isActorIn $y; dbpedia:birthName $name; dbpedia:birthDate $birthdate .\n" +
                "   $y mo:title \"Finding Nemo\"^^xsd:string\n" +
                "}");
    }

    @Test
    public void testQ4() throws Exception {
        countResults(2136, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $budget $gross $director_name $producer_name $editor_name $company_name\n" +
                "WHERE {\n" +
                "   $x a mo:Movie; mo:title \"Finding Nemo\"^^xsd:string;\n" +
                "        dbpedia:budget $budget;\n" +
                "        dbpedia:gross $gross .\n" +
                "   $x mo:hasDirector $director . $director dbpedia:birthName $director_name .\n" +
                "   $x mo:hasDirector $producer . $producer dbpedia:birthName $producer_name .\n" +
                "   $x mo:hasDirector $editor . $editor dbpedia:birthName $editor_name .\n" +
                "   $x mo:isProducedBy $y . $y :companyName $company_name\n" +
                "}");
    }

    @Test
    public void testQ5() throws Exception {
        countResults(0, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT DISTINCT $x $title $actor_name ?prod_year ?rating\n" +
                "WHERE {\n" +
                "   $m a mo:Movie;\n" +
                "         mo:title ?title;\n" +
                "         mo:imdbrating ?rating;\n" +
                "         dbpedia:productionStartYear ?prod_year;\n" +
                "         mo:hasActor ?x;\n" +
                "         mo:hasDirector ?x .\n" +
                "   $x dbpedia:birthName $actor_name .\n" +
                "   FILTER ( ?rating > '7.0' && ?prod_year >= 2000 && ?prod_year <= 2010 )\n" +
                "}\n" +
                "order by desc(?rating) ?prod_year\n" +
                "limit 25");
    }

    /**
     * Technical test
     */
    @Test
    public void testBirthNameContainsZ() throws Exception {
        countResults(196531, "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "" +
                "SELECT ?p WHERE {\n" +
                " ?p  :birthNameContainsZ true .\n" +
                "}");
    }
}

