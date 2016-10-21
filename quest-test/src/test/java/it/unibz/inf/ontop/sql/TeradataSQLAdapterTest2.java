package it.unibz.inf.ontop.sql;

import it.unibz.inf.ontop.owlrefplatform.owlapi.*;

import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;


import java.io.File;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by elem on 06/07/16.
 */
public class TeradataSQLAdapterTest2 {

    private OBDADataFactory fac;
    private Connection conn;

    private OBDAModel obdaModel;
    private OWLOntology ontology;

    final String owlfile = "src/test/resources/teradata/financial.owl";
    final String obdafile = "src/test/resources/teradata/financial.obda";

    @Before
    public void setUp() throws Exception {


        fac = OBDADataFactoryImpl.getInstance();


        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

        // Loading the OBDA data
        obdaModel = fac.getOBDAModel();
        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(obdafile);
    }


    private void runTests(QuestPreferences p, String query) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).preferences(p).build();
        QuestOWL reasoner = factory.createReasoner(ontology, config);

        // Now we are ready for querying
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();


        int i = 0;

        try {
            QuestOWLResultSet rs = st.executeTuple(query);
            while (rs.nextRow()) {
                OWLObject ind1 = rs.getOWLObject("w");


                System.out.println(ind1);
                i++;
            }
            assertTrue(i > 0);

        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }
    }

    @Test
    public void testFilter() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");


        String sparqlQuery =
            "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "select ?w where {?x a :Customer." +
                    "?x :hasFirstName ?w" +
                    " FILTER(?w > \"Norma\")}\n";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("Robyn");
        expectedValues.add("Rhonda");
         checkReturnedLiterals(p, sparqlQuery, expectedValues);
    }

    @Test
    public void testContains() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");


        String sparqlQuery =
            "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "select ?w where {?x a :Customer." +
                    "?x :hasFirstName ?w" +
                    " FILTER(CONTAINS(?w, \"honda\"))}\n";


        List<String> expectedValues = new ArrayList<>();
        //expectedValues.add("Robyn");
        expectedValues.add("Rhonda");
        checkReturnedLiterals(p, sparqlQuery, expectedValues);
    }

    @Test
    public void testBind() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");


        String sparqlQuery =
            "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "select ?w where {?x a :Customer." +
                    "?x :hasFirstName ?y ." +
                    "BIND(?y AS ?w)}";


        List<String> expectedValues = new ArrayList<>();
        //expectedValues.add("Robyn");
        expectedValues.add("Dan");
        expectedValues.add("Nick");
        expectedValues.add("Kyle");
        expectedValues.add("Cary");
        expectedValues.add("Inge");
        expectedValues.add("Dan");
        expectedValues.add("Nick");
        expectedValues.add("Kyle");
        expectedValues.add("Cary");
        expectedValues.add("Inge");
        expectedValues.add("Robyn");
        expectedValues.add("Rhonda");
        expectedValues.add("Faye");
        expectedValues.add("Eva");
        expectedValues.add("Norma");
        expectedValues.add("Robyn");
        expectedValues.add("Rhonda");
        expectedValues.add("Faye");
        expectedValues.add("Eva");
        expectedValues.add("Norma");

        checkReturnedLiterals(p, sparqlQuery, expectedValues);
    }

    @Test
    public void testLimit() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");


            String sparqlQuery =
                "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "select ?w where {?w :hasAccount ?y}" +
                        "LIMIT 2";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("<http://www.semanticweb.org/elem/ontologies/2016/5/financial#Customer/1362672>");
        expectedValues.add("<http://www.semanticweb.org/elem/ontologies/2016/5/financial#Customer/1362500>");
       // checkReturnedValues(p, sparqlQuery, expectedValues);
    }

    @Test
    public void testStrStarts() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");


            String sparqlQuery =
            "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "select ?w where {?x a :Customer. " +
                    "?x :hasFirstName ?w." +
                    "   FILTER(STRSTARTS(?w,\"Rh\"))}\n";



        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("Rhonda");

        checkReturnedLiterals(p, sparqlQuery, expectedValues);
    }

    @Test
    public void testStrEnds() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");


        String sparqlQuery =
                "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "select ?w where {?x a :Customer. " +
                        "?x :hasFirstName ?w." +
                        "   FILTER(STRLEN(?w) > 5)}\n";



        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("Rhonda");

        checkReturnedLiterals(p, sparqlQuery, expectedValues);
    }


    private void checkReturnedLiterals(QuestPreferences p, String query, List<String> expectedValues) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).preferences(p).build();
        QuestOWL reasoner = factory.createReasoner(ontology, config);
        // Now we are ready for querying
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();



        int i = 0;
        List<String> returnedValues = new ArrayList<>();
        try {
            QuestOWLResultSet rs = st.executeTuple(query);
            while (rs.nextRow()) {
                OWLLiteral ind1 = rs.getOWLLiteral("w");
                // log.debug(ind1.toString());
                returnedValues.add(ind1.getLiteral());
                i++;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }
        assertTrue(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()),
                returnedValues.equals(expectedValues));
        assertTrue(String.format("Wrong size: %d (expected %d)", i, expectedValues.size()), expectedValues.size() == i);

    }



}
