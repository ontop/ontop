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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to test if annotation property can be treated as data property and object property
 *
 *
 */
public class AnnotationMovieTest extends AbstractVirtualModeTest{
    private final static String owlFile = "/pgsql/annotation/movieontology.owl";
    private final static String obdaFile = "/pgsql/annotation/newSyntaxMovieontology.obda";
    private final static String propertyFile = "/pgsql/annotation/newSyntaxMovieontology.properties";

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
    public void testAnnotationInOntology() throws Exception {
        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "SELECT  ?r " +
                "WHERE {<http://dbpedia.org/ontology/birthDate> rdfs:label ?r . \n" +
                "}";

        countResults(4, queryBind);
    }


    @Test
    public void testAnnotationIRI() throws Exception {
        String queryBind = "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "SELECT  ?r " +
                " ?movie \n" +
                "WHERE {?movie dc:description ?r . \n" +
                "}";

        countResults(444090, queryBind);
    }

    @Test
    public void testAnnotationLiteral() throws Exception {
        String queryBind = "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "SELECT  ?r " +
                " ?movie \n" +
                "WHERE {?movie dc:date ?r . \n" +
                "}";

        countResults(443300, queryBind);
    }

    @Test
    public void testAnnotationString() throws Exception {
        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "SELECT  ?r " +
                " ?movie \n" +
                "WHERE {?movie dbpedia:gross ?r . \n" +
                "}";

        countResults(112576, queryBind);
    }


    @Test
    public void testAnnotationDatabaseValue() throws Exception {
        String queryBind = "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "SELECT  ?r " +
                " ?movie \n" +
                "WHERE {?movie mo:belongsToGenre ?r . \n" +

                "}";

        countResults(546032, queryBind);
    }

    @Test //no check is executed to verify that the value is a valid uri
    public void testNewSyntaxUri() throws Exception {
        String queryBind = "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "SELECT  ?r " +
                " ?movie \n" +
                "WHERE {?movie mo:hasMaleActor ?r . \n" +
                "} LIMIT 100000";

        // value without LIMIT
        // countResults(queryBind, 7530011);
        countResults(100000, queryBind);

    }

    @Test //no class in the ontology
    public void testClassUndefined() throws Exception {
        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "SELECT  ?r " +
                "WHERE {?r a mo:Vip . \n" +
                "} LIMIT 100000";

        // value without LIMIT
        // countResults(queryBind, 7530011);
        countResults(100000, queryBind);
    }

    @Test //no dataproperty in the ontology
    public void testDataPropertyUndefined() throws Exception {

        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "SELECT  ?r " +
                "WHERE {?company mo:companyId ?r . \n" +
                "}";

        countResults(131645, queryBind);
    }

    @Test //no objectproperty in the ontology
    public void testObjectPropertyUndefined() throws Exception {
        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "SELECT  ?r " +
                "WHERE {?movie mo:idTitle ?r . \n" +
                "}";

        countResults(444090, queryBind);
    }
}

