package it.unibz.inf.ontop.docker.mssql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

public class OredaR2rmlJoinTest extends AbstractVirtualModeTest {
    private static final String owlFile = "/mssql/oreda/oreda_bootstrapped_ontology.owl";
    private static final String r2rmlFile = "/mssql/oreda/oreda_bootstrapped_mapping.ttl";
    private static final String propertyFile = "/mssql/oreda/oreda_bootstrapped_mapping.properties";

    private static OntopOWLEngine REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before()  {
        REASONER = createR2RMLReasoner(owlFile, r2rmlFile, propertyFile);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
        REASONER.close();
    }

    @Test
    public void testValueDate() throws Exception {
        countResults(0, "PREFIX : <http://www.optique-project.eu/resource/Oreda/oreda/item_data/>\n" +
                "select *\n" +
                "{?x :value_date ?y}");
    }

    @Test
    public void testValueInventory() throws Exception {
        countResults(0, "PREFIX : <http://www.optique-project.eu/resource/Oreda/oreda/inv_spec/>\n" +
                "select *\n" +
                "{?x :ref-inventory ?y}");
    }
}
