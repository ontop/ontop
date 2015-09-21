package org.semanticweb.ontop.io;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.exception.InvalidMappingExceptionWithIndicator;
import org.semanticweb.ontop.exception.InvalidPredicateDeclarationException;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDACoreModule;
import org.semanticweb.ontop.injection.OBDAFactoryWithException;
import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants;
import org.semanticweb.ontop.parser.TurtleOBDASyntaxParser;

import static org.junit.Assert.assertEquals;

public class SQLMappingParserUsingOwlTest {

    private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
    private final NativeQueryLanguageComponentFactory nativeQLFactory;
    private final OBDAFactoryWithException modelFactory;

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
    private OBDADataSource dataSource;
    private ImmutableMap<String, String> prefixes;

    public SQLMappingParserUsingOwlTest() {
        Injector injector = Guice.createInjector(new OBDACoreModule(new OBDAProperties()));
        nativeQLFactory = injector.getInstance(NativeQueryLanguageComponentFactory.class);
        modelFactory = injector.getInstance(OBDAFactoryWithException.class);
    }

    @Before
    public void setUp() throws Exception {
        PrefixManager prefixManager = setupPrefixManager();
        dataSource = setupSampleDataSource();

        // Setting up the CQ parser
        prefixes = prefixManager.getPrefixMap();
        parser = new TurtleOBDASyntaxParser(prefixes);
    }

    @Test
    public void testRegularFile() throws Exception {
        saveRegularFile();
        loadRegularFile();
    }

    @Test
    public void testFileWithMultipleDataSources() throws Exception {
        saveFileWithMultipleDataSources();
        loadFileWithMultipleDataSources();
    }

    @Test(expected=InvalidMappingExceptionWithIndicator.class)
    public void testLoadWithBlankMappingId()
            throws DuplicateMappingException, InvalidDataSourceException, InvalidMappingException, IOException, InvalidPredicateDeclarationException {
        loadObdaFile("src/test/resources/org/semanticweb/ontop/io/SchoolBadFile5.obda");
    }

    @Test(expected=InvalidMappingExceptionWithIndicator.class)
    public void testLoadWithBlankTargetQuery() throws DuplicateMappingException, InvalidMappingException, InvalidPredicateDeclarationException, InvalidDataSourceException, IOException {
        loadObdaFile("src/test/resources/org/semanticweb/ontop/io/SchoolBadFile6.obda");
    }

    @Test(expected=InvalidMappingExceptionWithIndicator.class)
    public void testLoadWithBlankSourceQuery() throws DuplicateMappingException, InvalidMappingException, InvalidPredicateDeclarationException, InvalidDataSourceException, IOException {
        loadObdaFile("src/test/resources/org/semanticweb/ontop/io/SchoolBadFile7.obda");
    }

    @Test(expected=IOException.class)
    public void testLoadWithBadTargetQuery() throws DuplicateMappingException, InvalidMappingException,
            InvalidPredicateDeclarationException, InvalidDataSourceException, IOException {
        loadObdaFile("src/test/resources/org/semanticweb/ontop/io/SchoolBadFile8.obda");
    }

    @Test(expected=IOException.class)
    public void testLoadWithPredicateDeclarations() throws Exception {
        loadObdaFile("src/test/resources/org/semanticweb/ontop/io/SchoolBadFile9.obda");
    }

    @Test(expected=InvalidMappingExceptionWithIndicator.class)
    public void testLoadWithAllMistakes() throws DuplicateMappingException, InvalidMappingException,
            InvalidPredicateDeclarationException, InvalidDataSourceException, IOException {
            loadObdaFile("src/test/resources/org/semanticweb/ontop/io/SchoolBadFile10.obda");
    }
    
    /*
     * Test saving to a file
     */

    private void saveRegularFile() throws Exception {
        OBDAModel model = modelFactory.createOBDAModel(ImmutableSet.<OBDADataSource>of(),
                ImmutableMap.<URI, ImmutableList<OBDAMappingAxiom>>of(),
                nativeQLFactory.create(ImmutableMap.<String, String>of()));
        OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer(model);
        writer.save(new File("src/test/java/org/semanticweb/ontop/io/SchoolRegularFile.obda"));
    }

    private void saveFileWithMultipleDataSources() throws Exception {

        // Setup another data source
        OBDADataSource datasource2 = setupAnotherSampleDataSource();

        // Add some more mappings
        Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = addSampleMappings(dataSource.getSourceID());
        mappingIndex = addMoreSampleMappings(mappingIndex, datasource2.getSourceID());

        OBDAModel model = modelFactory.createOBDAModel(ImmutableSet.of(dataSource, datasource2),
                ImmutableMap.copyOf(mappingIndex), nativeQLFactory.create(prefixes));
        OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer(model);

        // Save the model
        writer.save(new File("src/test/java/org/semanticweb/ontop/io/SchoolMultipleDataSources.obda"));
    }

    /*
     * Test loading the file
     */

    private void loadRegularFile() throws Exception {
        OBDAModel model = loadObdaFile("src/test/java/org/semanticweb/ontop/io/SchoolRegularFile.obda");

        // Check the content
        assertEquals(model.getPrefixManager().getPrefixMap().size(), 5);
        assertEquals(model.getSources().size(), 0);
        assertEquals(countElement(model.getMappings()), 0);
    }

    private void loadFileWithMultipleDataSources() throws Exception {
        OBDAModel model = loadObdaFile("src/test/java/org/semanticweb/ontop/io/SchoolMultipleDataSources.obda");

        // Check the content
        assertEquals(model.getPrefixManager().getPrefixMap().size(), 6);
        assertEquals(model.getSources().size(), 2);
        assertEquals(countElement(model.getMappings()), 2);
    }

    private OBDAModel loadObdaFile(String fileLocation) throws IOException,
            InvalidPredicateDeclarationException, InvalidMappingException, DuplicateMappingException,
            InvalidDataSourceException {
        // Load the OBDA model
        MappingParser mappingParser = nativeQLFactory.create(new File(fileLocation));
        return mappingParser.getOBDAModel();
    }

    private PrefixManager setupPrefixManager() {
        // Setting up the prefixes
        PrefixManager prefixManager = new SimplePrefixManager(ImmutableMap.of(PrefixManager.DEFAULT_PREFIX,
                "http://www.semanticweb.org/ontologies/2012/5/Ontology1340973114537.owl#"));
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
    
    private Map<URI, ImmutableList<OBDAMappingAxiom>> addSampleMappings(URI sourceId) {
        Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>();
        // Add some mappings
        try {
            mappingIndex.put(sourceId, ImmutableList.<OBDAMappingAxiom>of(dfac.getMappingAxiom(mappings[0][0],
                    dfac.getSQLQuery(mappings[0][1]), parser.parse(mappings[0][2]))));
            mappingIndex.put(sourceId, ImmutableList.<OBDAMappingAxiom>of(dfac.getMappingAxiom(mappings[1][0],
                    dfac.getSQLQuery(mappings[1][1]), parser.parse(mappings[1][2]))));
            mappingIndex.put(sourceId, ImmutableList.<OBDAMappingAxiom>of(dfac.getMappingAxiom(mappings[2][0],
                    dfac.getSQLQuery(mappings[2][1]), parser.parse(mappings[2][2]))));
        } catch (Exception e) {
            // NO-OP
        }
        return mappingIndex;
    }
    
    private Map<URI, ImmutableList<OBDAMappingAxiom>> addMoreSampleMappings(
            Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex, URI sourceId) {
        // Add some mappings
        try {
            mappingIndex.put(sourceId, ImmutableList.<OBDAMappingAxiom>of(dfac.getMappingAxiom(mappings[3][0],
                    dfac.getSQLQuery(mappings[3][1]), parser.parse(mappings[3][2]))));
            mappingIndex.put(sourceId, ImmutableList.<OBDAMappingAxiom>of(dfac.getMappingAxiom(mappings[4][0],
                    dfac.getSQLQuery(mappings[4][1]), parser.parse(mappings[4][2]))));
            mappingIndex.put(sourceId, ImmutableList.<OBDAMappingAxiom>of(dfac.getMappingAxiom(mappings[5][0],
                    dfac.getSQLQuery(mappings[5][1]), parser.parse(mappings[5][2]))));
        } catch (Exception e) {
            // NO-OP
        }
        return mappingIndex;
    }
    
    private int countElement(Map<URI, ImmutableList<OBDAMappingAxiom>> mappings) {
        int total = 0;
        for (List<OBDAMappingAxiom> list : mappings.values()) {
            total += list.size();
        }
        return total;
    }
}
