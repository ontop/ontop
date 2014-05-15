package org.semanticweb.ontop.api.io;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import junit.framework.TestCase;

import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.exception.InvalidPredicateDeclarationException;
import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.io.SimplePrefixManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants;
import org.semanticweb.ontop.parser.TurtleOBDASyntaxParser;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class ModelIOManagerUsingOwlTest extends TestCase {

    private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

    private OBDAModel model;

    private ModelIOManager ioManager;

    private TurtleOBDASyntaxParser parser;

    private String[][] mappings = {
            { "M1", "select id, fname, lname, age from student",
                    ":P{id} a :Student ; :firstName {fname} ; :lastName {lname} ; :age {age}^^xsd:int ." },
            { "M2", "select id, title, lecturer, description from course",
                    ":C{id} a :Course ; :title {title} ; :hasLecturer :L{id} ; :description {description}@en-US ." },
            { "M3", "select sid, cid from enrollment",
                    ":P{sid} :hasEnrollment :C{cid} ." },
                    
            { "M4", "select id, nome, cognome, eta from studenti",
                    ":P{id} a :Student ; :firstName {nome} ; :lastName {cognome} ; :age {eta}^^xsd:int ." },
            { "M5", "select id, titolo, professore, descrizione from corso",
                    ":C{id} a :Course ; :title {titolo} ; :hasLecturer :L{id} ; :description {decrizione}@it ." },
            { "M6", "select sid, cid from registrare", 
                    ":P{sid} :hasEnrollment :C{cid} ." }
    };

    @Override
    public void setUp() throws Exception {
        PrefixManager prefixManager = setupPrefixManager();
        OBDADataSource datasource = setupSampleDataSource();

        // Setting up the CQ parser
        parser = new TurtleOBDASyntaxParser(prefixManager);

        // Construct the model
        model = dfac.getOBDAModel();
        model.setPrefixManager(prefixManager);
        model.addSource(datasource);

        loadOntologyToModel(model);
        
        addSampleMappings(model, datasource.getSourceID());
    }
    
    private void loadOntologyToModel(OBDAModel model) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File file = new File("src/test/java/it/unibz/krdb/obda/api/io/School.owl");
        OWLOntology schoolOntology = manager.loadOntologyFromOntologyDocument(file);
        
        // Setup the entity declarations
        for (OWLClass c : schoolOntology.getClassesInSignature()) {
            Predicate pred = dfac.getClassPredicate(c.getIRI().toString());
            model.declareClass(pred);
        }
        for (OWLObjectProperty r : schoolOntology.getObjectPropertiesInSignature()) {
            Predicate pred = dfac.getObjectPropertyPredicate(r.getIRI().toString());
            model.declareObjectProperty(pred);
        }
        for (OWLDataProperty p : schoolOntology.getDataPropertiesInSignature()) {
            Predicate pred = dfac.getDataPropertyPredicate(p.getIRI().toString());
            model.declareDataProperty(pred);
        }
    }

    public void testRegularFile() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
        saveRegularFile();
        loadRegularFile();
    }

    public void testFileWithMultipleDataSources() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
        saveFileWithMultipleDataSources();
        loadFileWithMultipleDataSources();
    }

    public void testLoadWithBlankMappingId() {
        resetCurrentModel();
        try {
            loadObdaFile("src/test/resources/it/unibz/krdb/obda/api/io/SchoolBadFile5.obda");
        } catch (IOException e) {
            assertFalse(true);
        } catch (InvalidPredicateDeclarationException e) {
            assertFalse(true);
        } catch (InvalidMappingException e) {
            // The wrong mapping doesn't get loaded.
            assertTrue(e.getMessage(), (countElement(model.getMappings()) == 2));
        }
    }

    public void testLoadWithBlankTargetQuery() {
        resetCurrentModel();
        try {
            loadObdaFile("src/test/resources/it/unibz/krdb/obda/api/io/SchoolBadFile6.obda");
        } catch (IOException e) {
            assertFalse(true);
        } catch (InvalidPredicateDeclarationException e) {
            assertFalse(true);
        } catch (InvalidMappingException e) {
            // The wrong mapping doesn't get loaded.
            assertTrue(e.getMessage(), (countElement(model.getMappings()) == 2));
        }
    }
    
    public void testLoadWithBlankSourceQuery() {
        resetCurrentModel();
        try {
            loadObdaFile("src/test/resources/it/unibz/krdb/obda/api/io/SchoolBadFile7.obda");
        } catch (IOException e) {
            assertFalse(true);
        } catch (InvalidPredicateDeclarationException e) {
            assertFalse(true);
        } catch (InvalidMappingException e) {
            // The wrong mapping doesn't get loaded.
            assertTrue(e.getMessage(), (countElement(model.getMappings()) == 2));
        }
    }
    
    public void testLoadWithBadTargetQuery() {
        resetCurrentModel();
        try {
            loadObdaFile("src/test/resources/it/unibz/krdb/obda/api/io/SchoolBadFile8.obda");
        } catch (IOException e) {
        	assertTrue(true);
        } catch (InvalidPredicateDeclarationException e) {
            assertFalse(true);
        } catch (InvalidMappingException e) {
            assertFalse(true);
        }
    }
    
    public void testLoadWithPredicateDeclarations() {
        resetCurrentModel();
        try {
            loadObdaFile("src/test/resources/it/unibz/krdb/obda/api/io/SchoolBadFile9.obda");
        } catch (IOException e) {
        	assertTrue(true);
        } catch (InvalidPredicateDeclarationException e) {
            assertFalse(true);
        } catch (InvalidMappingException e) {
            assertFalse(true);
        }
    }
    
    public void testLoadWithAllMistakes() {
        resetCurrentModel();
        try {
            loadObdaFile("src/test/resources/it/unibz/krdb/obda/api/io/SchoolBadFile10.obda");
        } catch (IOException e) {
            assertFalse(true);
        } catch (InvalidPredicateDeclarationException e) {
            assertFalse(true);
        } catch (InvalidMappingException e) {
            // The wrong mapping doesn't get loaded, i.e., all of them
            assertTrue(e.getMessage(), (countElement(model.getMappings()) == 0));
        }
    }
    
    /*
     * Test saving to a file
     */

    private void saveRegularFile() throws IOException {
        ioManager = new ModelIOManager(model);
        ioManager.save("src/test/java/it/unibz/krdb/obda/api/io/SchoolRegularFile.obda");
    }

    private void saveFileWithMultipleDataSources() throws IOException {
        // Setup another data source
        OBDADataSource datasource2 = setupAnotherSampleDataSource();

        // Add another data source
        model.addSource(datasource2);

        // Add some more mappings
        addMoreSampleMappings(model, datasource2.getSourceID());

        // Save the model
        ioManager = new ModelIOManager(model);
        ioManager.save("src/test/java/it/unibz/krdb/obda/api/io/SchoolMultipleDataSources.obda");
    }

    /*
     * Test loading the file
     */

    private void loadRegularFile() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
        ioManager = new ModelIOManager(model);
        ioManager.load("src/test/java/it/unibz/krdb/obda/api/io/SchoolRegularFile.obda");

        // Check the content
        assertTrue(model.getPrefixManager().getPrefixMap().size() == 6);
        assertTrue(model.getSources().size() == 1);
        assertTrue(countElement(model.getMappings()) == 3);
    }

    private void loadFileWithMultipleDataSources() throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
        ioManager = new ModelIOManager(model);
        ioManager.load("src/test/java/it/unibz/krdb/obda/api/io/SchoolMultipleDataSources.obda");

        // Check the content
        assertTrue(model.getPrefixManager().getPrefixMap().size() == 6);
        assertTrue(model.getSources().size() == 2);
        assertTrue(countElement(model.getMappings()) == 6);
    }

    private void loadObdaFile(String fileLocation) throws IOException, InvalidPredicateDeclarationException, InvalidMappingException {
        // Load the OBDA model
        ModelIOManager modelIO = new ModelIOManager(model);
        modelIO.load(fileLocation);
    }

    private void resetCurrentModel() {
        model.removeAllMappings();
//        model.removeSource(id);
    }

    private PrefixManager setupPrefixManager() {
        // Setting up the prefixes
        PrefixManager prefixManager = new SimplePrefixManager();
        prefixManager.addPrefix(PrefixManager.DEFAULT_PREFIX, "http://www.semanticweb.org/ontologies/2012/5/Ontology1340973114537.owl#");
        return prefixManager;
    }
    
    private OBDADataSource setupSampleDataSource() {
        // Setting up the data source
        URI sourceId = URI.create("http://www.example.org/db/dummy/");
        OBDADataSource datasource = dfac.getDataSource(sourceId);
        datasource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, "jdbc:postgresql://www.example.org/dummy");
        datasource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, "dummy");
        datasource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, "dummy");
        datasource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, "org.postgresl.Driver");
        return datasource;
    }
    
    private OBDADataSource setupAnotherSampleDataSource() {
        // Setting up the data source
        URI sourceId2 = URI.create("http://www.example.org/db/dummy2/");
        OBDADataSource datasource2 = dfac.getDataSource(sourceId2);
        datasource2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, "jdbc:postgresql://www.example.org/dummy2");
        datasource2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, "dummy2");
        datasource2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, "dummy2");
        datasource2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, "org.postgresl.Driver");
        return datasource2;
    }
    
    private void addSampleMappings(OBDAModel model, URI sourceId) {
        // Add some mappings
        try {
            model.addMapping(sourceId, dfac.getRDBMSMappingAxiom(mappings[0][0], mappings[0][1], parser.parse(mappings[0][2])));
            model.addMapping(sourceId, dfac.getRDBMSMappingAxiom(mappings[1][0], mappings[1][1], parser.parse(mappings[1][2])));
            model.addMapping(sourceId, dfac.getRDBMSMappingAxiom(mappings[2][0], mappings[2][1], parser.parse(mappings[2][2])));
        } catch (Exception e) {
            // NO-OP
        }
    }
    
    private void addMoreSampleMappings(OBDAModel model, URI sourceId) {
        // Add some mappings
        try {
            model.addMapping(sourceId, dfac.getRDBMSMappingAxiom(mappings[3][0], mappings[3][1], parser.parse(mappings[3][2])));
            model.addMapping(sourceId, dfac.getRDBMSMappingAxiom(mappings[4][0], mappings[4][1], parser.parse(mappings[4][2])));
            model.addMapping(sourceId, dfac.getRDBMSMappingAxiom(mappings[5][0], mappings[5][1], parser.parse(mappings[5][2])));
        } catch (Exception e) {
            // NO-OP
        }
    }
    
    private int countElement(Hashtable<URI, ArrayList<OBDAMappingAxiom>> mappings) {
        int total = 0;
        for (List<OBDAMappingAxiom> list : mappings.values()) {
            total += list.size();
        }
        return total;
    }
}
