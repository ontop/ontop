package it.unibz.inf.ontop.docker.postgres;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.querymanager.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.net.URI;
import java.util.Optional;

import static org.junit.Assert.assertNotEquals;

/**
 * Test case for the IMDB database see wiki Example_MovieOntology
 * Created by Sarah on 30/07/14.
 */


public class ImdbPostgresTest extends AbstractVirtualModeTest {

    private static final String owlFile = "/pgsql/imdb/movieontology.owl";
    private static final String obdaFile = "/pgsql/imdb/movieontology.obda";
    private static final String propertyFile = "/pgsql/imdb/movieontology.properties";
    private static final String queriesFile = "/pgsql/imdb/movieontology.q";

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
    public void testIMDBSeries() throws Exception {
        QueryController qc = new QueryController();
        QueryIOManager qman = new QueryIOManager(qc);
        String queryFileName = ImdbPostgresTest.class.getResource(queriesFile).toString();
        qman.load(new File(new URI(queryFileName)));

        for (QueryControllerEntity entity : qc.getElements()) {
            if (entity instanceof QueryControllerGroup) {
                for (QueryControllerQuery query : ((QueryControllerGroup)entity).getQueries()) {
                    runQuery(query);
                }
            }
            else if (entity instanceof QueryControllerQuery) {
                runQuery((QueryControllerQuery)entity);
            }
            else
                throw new IllegalArgumentException("Unexpected entity type");
        }
    }

    private void runQuery(QueryControllerQuery qcq) throws Exception {
        String query = qcq.getQuery();
        System.out.println("QUERY FROM QC: " + query);
        try (OntopOWLStatement st = createStatement()) {
            int count = 0;
            try (TupleOWLResultSet rs = st.executeSelectQuery(query)) {
                while (rs.hasNext()) {
                    OWLBindingSet bindingSet = rs.next();
                    count++;
                }
            }
            System.out.println("Query " + query + " total result: " + count);
            assertNotEquals(0, count);
        }
    }

    /*
        String query = "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $x $title $company_name\n" +
                "WHERE { \n" +
                "   $m a mo:Movie; mo:title ?title; mo:hasActor ?x; mo:hasDirector ?x; mo:isProducedBy $y; mo:belongsToGenre $z .\n" +
                "   $x dbpedia:birthName $actor_name .\n" +
                "   $y :companyName $company_name; :hasCompanyLocation [ a mo:Eastern_Asia ] .\n" +
                "   $z a mo:Love .\n" +
                "}\n";
     */


    @Test
    public void testOneQuery() throws Exception {
        String query2 = "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  *\n" +
                "WHERE { \n" +
                "   $m a mo:Movie; mo:isProducedBy $y .\n" +
                "   $y :hasCompanyLocation [ a mo:Eastern_Asia ] .\n" +
                "}\n";

        countResults(15173, query2);
    }

    @Test
    public void testCompanyLocationQuery() throws Exception {

        String query = "PREFIX : <http://www.movieontology.org/2009/11/09/movieontology.owl#>\n" +
                "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "PREFIX dbpedia: <http://dbpedia.org/ontology/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT $company_name\n" +
                "WHERE { \n" +
                "   $y :companyName $company_name; :hasCompanyLocation [ a mo:Eastern_Asia ] .\n" +
                "}\n";

        countResults(7738, query);
    }

    @Test
    public void testIndividuals() throws Exception {
        String query = "PREFIX mo: <http://www.movieontology.org/2009/10/01/movieontology.owl#>\n" +
                "SELECT DISTINCT $z \n" +
                "WHERE { \n" +
                "   $z a mo:Love .\n" +
                "}\n";

        countResults(29405, query);
    }
}

