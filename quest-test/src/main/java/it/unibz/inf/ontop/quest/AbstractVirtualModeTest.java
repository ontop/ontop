package it.unibz.inf.ontop.quest;

import it.unibz.inf.ontop.io.QueryIOManager;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.querymanager.QueryController;
import it.unibz.inf.ontop.querymanager.QueryControllerGroup;
import it.unibz.inf.ontop.querymanager.QueryControllerQuery;
import junit.framework.TestCase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Common initialization for many tests
 */
public abstract class AbstractVirtualModeTest extends TestCase {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String owlFileName;
    private final String obdaFileName;

    private OWLOntology ontology;

    protected QuestOWL reasoner;
    protected QuestOWLConnection conn;

    protected AbstractVirtualModeTest(String owlfile, String obdafile) {
        this.owlFileName = owlfile;
        this.obdaFileName = obdafile;
    }

    @Override
    public void setUp() throws Exception {
        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument((new File(owlFileName)));

        Properties p = new Properties();
        p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setProperty(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);
        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWLConfiguration config = QuestOWLConfiguration.builder()
                .preferences(new QuestPreferences(p))
                .nativeOntopMappingFile(new File(obdaFileName))
                .build();
        reasoner = factory.createReasoner(ontology, config);

        // Now we are ready for querying
        conn = reasoner.getConnection();
    }

    public void tearDown() throws Exception {
        conn.close();
        reasoner.dispose();
    }

    protected String runQueryAndReturnStringX(String query) throws Exception {
        QuestOWLStatement st = conn.createStatement();
        String retval;
        try {
            QuestOWLResultSet rs = st.executeTuple(query);

            assertTrue(rs.nextRow());
            OWLIndividual ind1 = rs.getOWLIndividual("x");
            retval = ind1.toString();

        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }
        return retval;
    }

    protected boolean runQueryAndReturnBooleanX(String query) throws Exception {
        QuestOWLStatement st = conn.createStatement();
        boolean retval;
        try {
            QuestOWLResultSet rs = st.executeTuple(query);
            assertTrue(rs.nextRow());
            OWLLiteral ind1 = rs.getOWLLiteral("x");
            retval = ind1.parseBoolean();
        } catch (Exception e) {
            throw e;
        } finally {
            try {

            } catch (Exception e) {
                st.close();
                assertTrue(false);
            }
            conn.close();
            reasoner.dispose();
        }
        return retval;
    }

    protected void countResults(String query, int expectedCount) throws OBDAException, OWLException {
        // Now we are ready for querying
        conn = reasoner.getConnection();

        QuestOWLStatement st = conn.createStatement();
        QuestOWLResultSet results = st.executeTuple(query);
        int count = 0;
        while (results.nextRow()) {
            count++;
        }
        assertEquals(expectedCount, count);
    }

    protected void checkReturnedUris(String query, List<String> expectedUris) throws Exception {
        QuestOWLStatement st = conn.createStatement();
        int i = 0;
        List<String> returnedUris = new ArrayList<>();
        try {
            QuestOWLResultSet rs = st.executeTuple(query);
            while (rs.nextRow()) {
                OWLNamedIndividual ind1 = (OWLNamedIndividual) rs.getOWLIndividual("x");
                // log.debug(ind1.toString());
                returnedUris.add(ind1.getIRI().toString());
                java.lang.System.out.println(ind1.getIRI());
                i++;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }
        assertTrue(String.format("%s instead of \n %s", returnedUris.toString(), expectedUris.toString()),
                returnedUris.equals(expectedUris));
        assertTrue(String.format("Wrong size: %d (expected %d)", i, expectedUris.size()), expectedUris.size() == i);
    }

    protected void checkThereIsAtLeastOneResult(String query) throws Exception {
        QuestOWLStatement st = conn.createStatement();
        try {
            QuestOWLResultSet rs = st.executeTuple(query);
            assertTrue(rs.nextRow());

        } catch (Exception e) {
            throw e;
        } finally {
            try {

            } catch (Exception e) {
                st.close();
                assertTrue(false);
            }
            conn.close();
            reasoner.dispose();
        }
    }

    protected boolean runASKTests(String query) throws Exception {
        QuestOWLStatement st = conn.createStatement();
        boolean retval;
        try {
            QuestOWLResultSet rs = st.executeTuple(query);
            assertTrue(rs.nextRow());
            OWLLiteral ind1 = rs.getOWLLiteral(1);
            retval = ind1.parseBoolean();
        } catch (Exception e) {
            throw e;
        } finally {
            try {

            } catch (Exception e) {
                st.close();
                assertTrue(false);
            }
            conn.close();
            reasoner.dispose();
        }
        return retval;
    }

    protected void runQueries(String queryFileName) throws Exception {

        QuestOWLStatement st = conn.createStatement();

        QueryController qc = new QueryController();
        QueryIOManager qman = new QueryIOManager(qc);
        qman.load(queryFileName);

        for (QueryControllerGroup group : qc.getGroups()) {
            for (QueryControllerQuery query : group.getQueries()) {

                log.debug("Executing query: {}", query.getID());
                log.debug("Query: \n{}", query.getQuery());

                long start = System.nanoTime();
                QuestOWLResultSet res = st.executeTuple(query.getQuery());
                long end = System.nanoTime();

                double time = (end - start) / 1000;

                int count = 0;
                while (res.nextRow()) {
                    count += 1;
                }
                log.debug("Total result: {}", count);
                assertFalse(count == 0);
                log.debug("Elapsed time: {} ms", time);
            }
        }
    }
}
