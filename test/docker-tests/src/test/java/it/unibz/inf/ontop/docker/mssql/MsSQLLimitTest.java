package it.unibz.inf.ontop.docker.mssql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import static org.junit.Assert.assertEquals;

public class MsSQLLimitTest extends AbstractVirtualModeTest {

    private static final String owlfile = "/mssql/identifiers.owl";
    private static final String obdafile = "/mssql/identifiers-mssql.obda";
    private static final String propertyfile = "/mssql/identifiers-mssql.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlfile, obdafile, propertyfile);
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

    /**
     * Test whether LIMIT without ORDER BY works in main query
     */
    @Test
    public void testLimitMainQuery() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
                "SELECT ?x WHERE {?x a :Country} \n LIMIT 1";
        String val = runQueryAndReturnStringOfIndividualX(query);
        assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-991>", val);
    }

    /**
     * Test whether LIMIT without ORDER BY works in sub query
     */
    @Test
    public void testLimitSubQuery() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
                "SELECT ?x ?y WHERE {?x a :Country .\n" +
                "{SELECT (?y AS ?z) WHERE {?y a :Country .} LIMIT 1}\n"+
                "} ";
        String val = runQueryAndReturnStringOfIndividualX(query);
        assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-991>", val);
    }

}

