package it.unibz.inf.ontop.docker.mssql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

public class OredaObdaJoinTest extends AbstractVirtualModeTest {
    private static final String owlFile = "/mssql/oreda/oreda_bootstrapped_ontology.owl";
    private static final String obdaFile = "/mssql/oreda/oreda_bootstrapped_mapping.obda";
    private static final String propertyFile = "/mssql/oreda/oreda_bootstrapped_mapping.properties";

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

