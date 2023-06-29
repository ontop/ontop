package it.unibz.inf.ontop.docker.postgres;


import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import static org.junit.Assert.assertEquals;

/**
 * Class to test if equality and unification of functions is correct
 *
 *
 */
public class LowerMovieTest extends AbstractVirtualModeTest{

    private final static String owlFile = "/pgsql/movieontology.owl";
    private final static String obdaFile = "/pgsql/lowerMovie.obda";
    private final static String propertyFile = "/pgsql/lowerMovie.properties";

    private static EngineConnection CONNECTION;

    @BeforeClass
    public static void before() {
        CONNECTION = createReasoner(owlFile, obdaFile, propertyFile);
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
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

