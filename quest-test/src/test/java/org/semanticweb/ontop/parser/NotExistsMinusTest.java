//package org.semanticweb.ontop.parser;
//
///*
// * #%L
// * ontop-test
// * %%
// * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
// * %%
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * #L%
// */
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.semanticweb.ontop.io.ModelIOManager;
//import org.semanticweb.ontop.model.OBDADataFactory;
//import org.semanticweb.ontop.model.OBDAModel;
//import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
//import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
//import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
//import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
//import org.semanticweb.owlapi.apibinding.OWLManager;
//import org.semanticweb.owlapi.model.OWLNamedIndividual;
//import org.semanticweb.owlapi.model.OWLOntology;
//import org.semanticweb.owlapi.model.OWLOntologyManager;
//import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.assertTrue;
//
///**
// * Tests to check if Ontop supports MINUS and NOT EXISTS properly (SPARQL to SQL).
// */
//public class NotExistsMinusTest {
//
//    private OBDADataFactory factory;
//    private QuestOWLConnection conn;
//
//    Logger log = LoggerFactory.getLogger(this.getClass());
//    private OBDAModel obdaModel;
//    private OWLOntology ontology;
//
//    // Same bindings than the ORDER BY test
//    final String owlFile = "src/test/resources/orderBy/stockBolzanoAddress.owl";
//    final String obdaFile = "src/test/resources/orderBy/stockBolzanoAddress.obda";
//    private QuestOWL reasoner;
//
//    @Before
//    public void setUp() throws Exception {
//
//        // Loading the OWL file
//        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//        ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));
//
//        // Loading the OBDA data
//        factory = OBDADataFactoryImpl.getInstance();
//        obdaModel = factory.getOBDAModel();
//
//        ModelIOManager ioManager = new ModelIOManager(obdaModel);
//        ioManager.load(obdaFile);
//
//        QuestPreferences p = new QuestPreferences();
//        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
//        p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA,
//                QuestConstants.FALSE);
//        // Creating a new instance of the reasoner
//        QuestOWLFactory factory = new QuestOWLFactory();
//        factory.setOBDAController(obdaModel);
//        factory.setPreferenceHolder(p);
//
//        reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
//
//        // Now we are ready for querying
//        conn = reasoner.getConnection();
//
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        conn.close();
//        reasoner.dispose();
//    }
//
//    private void checkReturnedUris(String query, List<String> expectedUris) throws Exception {
//        QuestOWLStatement st = conn.createStatement();
//        int i = 0;
//        List<String> returnedUris = new ArrayList<>();
//        try {
//            QuestOWLResultSet rs = st.executeTuple(query);
//            while (rs.nextRow()) {
//                OWLNamedIndividual ind1 = (OWLNamedIndividual) rs.getOWLIndividual("x");
//                // log.debug(ind1.toString());
//                returnedUris.add(ind1.getIRI().toString());
//                System.out.println(ind1.getIRI());
//                i++;
//            }
//        } catch (Exception e) {
//            throw e;
//        } finally {
//            conn.close();
//            reasoner.dispose();
//        }
//        assertTrue(String.format("%s instead of \n %s", returnedUris.toString(), expectedUris.toString()),
//                returnedUris.equals(expectedUris));
//        assertTrue(String.format("Wrong size: %d (expected %d)", i, expectedUris.size()), expectedUris.size() == i);
//    }
//
//    @Test
//    public void testExistsSameCity() throws Exception {
//        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> "
//                + "SELECT ?x WHERE {"
//                + "  ?x :inCity ?city . "
//                + "  FILTER EXISTS { ?y :inCity ?city . FILTER( ?x != ?y ) } "
//                + "}";
//
//        List<String> expectedUris = new ArrayList<>();
//        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-992");
//        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-995");
//        checkReturnedUris(query, expectedUris);
//    }
//
//
//    @Test
//    public void testNotExistsSameCountry() throws Exception {
//        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> "
//                + "SELECT ?x WHERE {"
//                + "  ?x :inState ?state . "
//                + "  FILTER NOT EXISTS { ?y :inState ?state } "
//                + "}";
//
//        List<String> expectedUris = new ArrayList<>();
//        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-993");
//        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-998");
//        checkReturnedUris(query, expectedUris);
//    }
//
//    @Test
//    public void testItalyMinusBolzanoProvince() throws Exception {
//        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> " +
//                "SELECT ?x WHERE {" +
//                "   ?x :inCountry \"Italy\" . " +
//                "MINUS { ?x :inState \"Bolzano\" }" +
//                "}";
//
//        List<String> expectedUris = new ArrayList<>();
//        expectedUris.add("http://www.owl-ontologies.com/Ontology1207768242.owl#Address-996");
//        checkReturnedUris(query, expectedUris);
//    }
//
//    /**
//     * TODO: Create examples inspired by http://www.w3.org/TR/sparql11-query/#neg-notexists-minus
//     */
//}

