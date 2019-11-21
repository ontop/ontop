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

import java.util.ArrayList;
import java.util.List;

/**
 * Tests to check if the SQL parser supports ORDER BY properly (SPARQL to SQL).
 */
public class OrderByTest extends AbstractVirtualModeTest {

    static final String owlFile = "/pgsql/order/stockBolzanoAddress.owl";
    static final String obdaFile = "/pgsql/order/stockBolzanoAddress.obda";
    static final String propertiesFile = "/pgsql/order/stockBolzanoAddress.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertiesFile);
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
    public void testBolzanoOrderingAsc() throws Exception {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> "
                + "SELECT ?x ?street WHERE {?x :inState \"Bolzano\"^^<http://www.w3.org/2001/XMLSchema#string>; :inStreet ?street } "
                + "ORDER BY ?street "
                ;

        List<String> expectedUris = new ArrayList<>();
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-995");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-992");
        checkReturnedUris(expectedUris, query);
    }

    @Test
    public void testBolzanoOrderingAsc2() throws Exception {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> "
                + "SELECT ?x ?street WHERE {?x :inState \"Bolzano\"^^<http://www.w3.org/2001/XMLSchema#string>; :inStreet ?street } "
                + "ORDER BY ASC(?street)"
                ;

        List<String> expectedUris = new ArrayList<>();
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-995");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-992");
        checkReturnedUris(expectedUris, query);
    }

    @Test
    public void testBolzanoOrderingDesc() throws Exception {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> "
                + "SELECT ?x ?street WHERE {?x :inState \"Bolzano\"^^<http://www.w3.org/2001/XMLSchema#string>; :inStreet ?street } "
                + "ORDER BY DESC(?street)"
                ;

        List<String> expectedUris = new ArrayList<>();
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-992");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-995");
        checkReturnedUris(expectedUris, query);
    }

    @Test
    public void testBolzanoMultipleOrdering() throws Exception {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> "
                + "SELECT ?x ?street ?country ?number "
                + "WHERE {?x :hasNumber ?number ;"
                + ":inCountry ?country ;"
                + ":inStreet ?street . } "
                + "ORDER BY DESC(?country) ?number DESC(?street)"
                ;

        List<String> expectedUris = new ArrayList<>();
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-993");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-991");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-997");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-992");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-995");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-996");
        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-998");
        checkReturnedUris(expectedUris, query);
    }

}
