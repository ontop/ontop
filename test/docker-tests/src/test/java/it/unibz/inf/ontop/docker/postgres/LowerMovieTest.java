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

import static org.junit.Assert.assertEquals;

/**
 * Class to test if equality and unification of functions is correct
 *
 *
 */
public class LowerMovieTest extends AbstractVirtualModeTest{
    Logger log = LoggerFactory.getLogger(this.getClass());

    final static String owlFile = "/pgsql/movieontology.owl";
    final static String obdaFile = "/pgsql/lowerMovie.obda";
    final static String propertyFile = "/pgsql/lowerMovie.properties";

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
    public void testLowerInSQL() throws Exception {
        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "PREFIX imdb:		<http://www.imdb.com/>" +
                "\n" +
                "SELECT  ?x " +
                "WHERE {<http://www.imdb.com/name/222> dbpedia:birthName ?x . \n" +

                "}";



        String name = runQueryAndReturnStringOfLiteralX(queryBind);
        assertEquals("\"a.j.\"^^xsd:string", name);

    }

    @Test
    public void testLower2InSQL() throws Exception {
        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "PREFIX imdb:		<http://www.imdb.com/>" +
                "\n" +
                "SELECT  ?x " +
                "WHERE {<http://www.imdb.com/title/97263> mo:title ?x . \n" +

                "}";



        String name = runQueryAndReturnStringOfLiteralX(queryBind);
        assertEquals("\"Colleen\"^^xsd:string" , name );

    }




}

