package it.unibz.inf.ontop.docker.postgres;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Test;


/**
 * Created by elem on 21/09/15.
 */
public class MetaMappingExpanderTest extends AbstractVirtualModeTest {

        final static String owlFile = "/pgsql/EPNet.owl";
        final static String obdaFile = "/pgsql/EPNet.obda";
        final static String propertyFile = "/pgsql/EPNet.properties";

    public MetaMappingExpanderTest() {
        super(owlFile, obdaFile, propertyFile);
    }

    @Test
    public void testQuery() throws Exception {

        /*
		 * Get the book information that is stored in the database
		 */
//        String sparqlQuery = "PREFIX : <http://www.semanticweb.org/ontologies/2015/1/EPNet-ONTOP_Ontology#>\n" +
//                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                "PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
//                "select *\n" +
//                "where {\n" +
//                "?x rdf:type :Amphora .\n" +
//                "?x :hasProductionPlace ?pl .\n" +
//                "?pl rdf:type :Place .\n" +
//                "?pl dcterms:title \"La Corregidora\" .\n" +
//                "?pl :hasLatitude ?lat .\n" +
//                "?pl :hasLongitude ?long\n" +
//                "}\n" +
//                "limit 50\n";
        String sparqlQuery = "PREFIX : <http://www.semanticweb.org/ontologies/2015/1/EPNet-ONTOP_Ontology#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
                "select ?x\n" +
                "where {\n" +
                "?x rdf:type :AmphoraSection2026 .\n" +
                "}\n" +
                "limit 5\n";

        runQuery(sparqlQuery);

        }
}
