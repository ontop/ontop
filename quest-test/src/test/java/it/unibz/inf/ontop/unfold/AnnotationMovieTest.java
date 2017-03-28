package it.unibz.inf.ontop.unfold;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Class to test if annotation property can be treated as data property and object property
 *
 *
 */
public class AnnotationMovieTest {
    Logger log = LoggerFactory.getLogger(this.getClass());

    final String owlFile = "src/test/resources/annotation/movieontology.owl";
    final String obdaFile = "src/test/resources/annotation/newSyntaxMovieontology.obda";
    final String propertyFile = "src/test/resources/annotation/newSyntaxMovieontology.properties";


    @Test
    public void testAnnotationInOntology() throws Exception {
        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                "WHERE {<http://dbpedia.org/ontology/birthDate> rdfs:label ?r . \n" +

                "}";



        int results = runTestQuery(queryBind);
        assertEquals(4, results);
    }


    @Test
    public void testAnnotationIRI() throws Exception {



        String queryBind = "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                " ?movie \n" +
                "WHERE {?movie dc:description ?r . \n" +

                "}";



        int results = runTestQuery(queryBind);
        assertEquals(444090, results);
    }

    @Test
    public void testAnnotationLiteral() throws Exception {



        String queryBind = "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                " ?movie \n" +
                "WHERE {?movie dc:date ?r . \n" +

                "}";



        int results = runTestQuery(queryBind);
        assertEquals(443300, results);
    }

    @Test
    public void testAnnotationString() throws Exception {



        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                " ?movie \n" +
                "WHERE {?movie dbpedia:gross ?r . \n" +

                "}";



        int results = runTestQuery(queryBind);
        assertEquals(112576, results);
    }


    @Test
    public void testAnnotationDatabaseValue() throws Exception {



        String queryBind = "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                " ?movie \n" +
                "WHERE {?movie mo:belongsToGenre ?r . \n" +

                "}";



        int results = runTestQuery(queryBind);
        assertEquals(876722, results);
    }

    @Test //no check is executed to verify that the value is a valid uri
    public void testNewSyntaxUri() throws Exception {



        String queryBind = "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                " ?movie \n" +
                "WHERE {?movie mo:hasMaleActor ?r . \n" +

                "}";



        int results = runTestQuery(queryBind);
        assertEquals(7530011, results);
    }

    @Test //no class in the ontology
    public void testClassUndefined() throws Exception {



        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                "WHERE {?r a mo:Vip . \n" +

                "}";



        int results = runTestQuery(queryBind);
        assertEquals(7530011, results);
    }

    @Test //no dataproperty in the ontology
    public void testDataPropertyUndefined() throws Exception {



        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                "WHERE {?company mo:companyId ?r . \n" +

                "}";



        int results = runTestQuery(queryBind);
        assertEquals(705859, results);

    }

    @Test //no objectproperty in the ontology
    public void testObjectPropertyUndefined() throws Exception {



        String queryBind = "PREFIX dbpedia: <http://dbpedia.org/ontology/>" +
                "PREFIX mo:		<http://www.movieontology.org/2009/10/01/movieontology.owl#>" +
                "\n" +
                "SELECT  ?r " +
                "WHERE {?movie mo:idTitle ?r . \n" +

                "}";



        int results = runTestQuery(queryBind);
        assertEquals(444090, results);

    }


    private int runTestQuery(String query) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdaFile)
                .ontologyFile(owlFile)
                .propertyFile(propertyFile)
                .enableOntologyAnnotationQuerying(true)
                .enableTestMode()
                .build();
        QuestOWL reasoner = factory.createReasoner(config);

        // Now we are ready for querying
        OntopOWLConnection conn = reasoner.getConnection();
        OntopOWLStatement st = conn.createStatement();


        log.debug("Executing query: ");
        log.debug("Query: \n{}", query);

        long start = System.nanoTime();
        QuestOWLResultSet res = st.executeTuple(query);
        long end = System.nanoTime();

        double time = (end - start) / 1000;
        String result = "";
        int count = 0;
        while (res.nextRow()) {
            count += 1;
            if (count == 1) {
                for (int i = 1; i <= res.getColumnCount(); i++) {
                    log.debug("Example result " + res.getSignature().get(i - 1) + " = " + res.getOWLObject(i));

                }
            }
        }
        log.debug("Total results: {}", count);

        assertFalse(count == 0);

        log.debug("Elapsed time: {} ms", time);

        st.close();
        conn.close();
        reasoner.dispose();

        return count;



    }


}

