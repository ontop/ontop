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

public class OredaObdaJoinTest extends AbstractVirtualModeTest {
    private static final String owlFile = "/mssql/oreda/oreda_bootstrapped_ontology.owl";
    private static final String obdaFile = "/mssql/oreda/oreda_bootstrapped_mapping.obda";
    private static final String propertyFile = "/mssql/oreda/oreda_bootstrapped_mapping.properties";

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

