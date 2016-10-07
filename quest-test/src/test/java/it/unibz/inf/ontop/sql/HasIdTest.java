package it.unibz.inf.ontop.sql;

import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * When the ID is both used for creating the URI and as the literal of a datatype property.
 */
public class HasIdTest {

    Logger log = LoggerFactory.getLogger(this.getClass());

    final String owlFileName = "resources/pullOutEq/pullOutEq.ttl";
    final String obdaFileName = "resources/pullOutEq/pullOutEq.obda";

    private QuestOWL reasoner;
    private QuestOWLConnection conn;

    @Before
    public void setUp() throws Exception {

        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument((new File(owlFileName)));

        // Loading the OBDA data
        OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
        OBDAModel obdaModel = fac.getOBDAModel();
        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(obdaFileName);


        Properties p = new Properties();
        p.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.put(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);

        QuestPreferences preferences = new QuestPreferences(p);
        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWLConfiguration config = QuestOWLConfiguration.builder()
                .obdaModel(obdaModel)
                .preferences(new QuestPreferences(p))
                .build();

        reasoner = factory.createReasoner(ontology, config);
    }

    @After
    public void tearDown() throws Exception{
        conn.close();
        reasoner.dispose();
    }


    private QuestOWLResultSet runQuery(String query) throws OBDAException, OWLException {

        // Now we are ready for querying
        conn = reasoner.getConnection();

        QuestOWLStatement st = conn.createStatement();
        return st.executeTuple(query);
    }

    @Test
    public void test() throws OBDAException, OWLException {
        QuestOWLResultSet results = runQuery("PREFIX : <http://example.com/vocab#>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "SELECT ?p ?firstName ?lastName " +
                "WHERE { " +
                "    ?p :hasId \"3\"^^xsd:int . " +
                "    OPTIONAL { ?p :firstName ?firstName }" +
                "    OPTIONAL { ?p :lastName ?lastName }" +
                "}");
        // At least one result
        assertTrue(results.nextRow());
        assertEquals(results.getOWLIndividual(1).toString(), "<http://example.com/persons/3>");
        assertEquals(results.getOWLLiteral(2), null);
        assertEquals(results.getOWLLiteral(3), null);
        assertFalse(results.nextRow());
    }
}
