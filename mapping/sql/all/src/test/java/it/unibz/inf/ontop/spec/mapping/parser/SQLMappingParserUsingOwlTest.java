package it.unibz.inf.ontop.spec.mapping.parser;

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
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;

import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.spec.mapping.impl.SimplePrefixManager;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.parser.impl.TurtleOBDASQLParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.junit.Before;
import org.junit.Test;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;


import static it.unibz.inf.ontop.utils.SQLAllMappingTestingTools.*;
import static org.junit.Assert.assertEquals;

public class SQLMappingParserUsingOwlTest {

    private final SQLPPMappingFactory ppMappingFactory;
    private final SpecificationFactory specificationFactory;
    private final SQLMappingParser mappingParser;

    private TargetQueryParser parser;

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
    private ImmutableMap<String, String> prefixes;

    public SQLMappingParserUsingOwlTest() {
        OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
                .jdbcName("http://www.example.org/db/dummy/")
                .jdbcUrl("jdbc:postgresql://www.example.org/dummy")
                .jdbcUser("dummy")
                .jdbcPassword("dummy")
                .jdbcDriver("org.postgresql.Driver")
                .build();

        Injector injector = configuration.getInjector();
        specificationFactory = injector.getInstance(SpecificationFactory.class);

        mappingParser = injector.getInstance(SQLMappingParser.class);
        ppMappingFactory = injector.getInstance(SQLPPMappingFactory.class);
    }

    @Before
    public void setUp() throws Exception {
        PrefixManager prefixManager = setupPrefixManager();

        // Setting up the CQ parser
        prefixes = prefixManager.getPrefixMap();
        parser = new TurtleOBDASQLParser(prefixes, TERM_FACTORY, TARGET_ATOM_FACTORY, RDF_FACTORY);
    }

    @Test
    public void testRegularFile() throws Exception {
        saveRegularFile();
        loadRegularFile();
    }

    @Test(expected=InvalidMappingExceptionWithIndicator.class)
    public void testLoadWithBlankMappingId()
            throws DuplicateMappingException, InvalidMappingException, MappingIOException {
        loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolBadFile5.obda");
    }

    @Test(expected=InvalidMappingExceptionWithIndicator.class)
    public void testLoadWithBlankTargetQuery() throws DuplicateMappingException, InvalidMappingException, MappingIOException {
        loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolBadFile6.obda");
    }

    @Test(expected=InvalidMappingExceptionWithIndicator.class)
    public void testLoadWithBlankSourceQuery() throws DuplicateMappingException, InvalidMappingException, MappingIOException {
        loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolBadFile7.obda");
    }

    @Test(expected=InvalidMappingExceptionWithIndicator.class)
    public void testLoadWithBadTargetQuery() throws DuplicateMappingException, InvalidMappingException,
            MappingIOException {
        loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolBadFile8.obda");
    }

    @Test(expected=MappingIOException.class)
    public void testLoadWithPredicateDeclarations() throws Exception {
        loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolBadFile9.obda");
    }

    @Test(expected=InvalidMappingExceptionWithIndicator.class)
    public void testLoadWithAllMistakes() throws DuplicateMappingException, InvalidMappingException,
            MappingIOException {
            loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolBadFile10.obda");
    }
    
    /*
     * Test saving to a file
     */

    private void saveRegularFile() throws Exception {
        SQLPPMapping ppMapping = ppMappingFactory.createSQLPreProcessedMapping(ImmutableList.of(),
                specificationFactory.createMetadata(specificationFactory.createPrefixManager(ImmutableMap.of()),
                        UriTemplateMatcher.create(Stream.of(), TERM_FACTORY)));
        OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer(ppMapping);
        writer.save(new File("src/test/resources/it/unibz/inf/ontop/io/SchoolRegularFile.obda"));
    }

    /*
     * Test loading the file
     */

    private void loadRegularFile() throws Exception {
        SQLPPMapping ppMapping = loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolRegularFile.obda");

        // Check the content
        assertEquals(ppMapping.getMetadata().getPrefixManager().getPrefixMap().size(), 5);
        assertEquals(ppMapping.getTripleMaps().size(), 0);
    }

    private void loadFileWithMultipleDataSources() throws Exception {
        SQLPPMapping ppMapping = loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolMultipleDataSources.obda");

        // Check the content
        assertEquals(ppMapping.getMetadata().getPrefixManager().getPrefixMap().size(), 6);
        assertEquals(ppMapping.getTripleMaps().size(), 2);
    }

    private SQLPPMapping loadObdaFile(String fileLocation) throws MappingIOException,
            InvalidMappingException, DuplicateMappingException {
        // Load the OBDA model
        return mappingParser.parse(new File(fileLocation));
    }

    private PrefixManager setupPrefixManager() {
        // Setting up the prefixes
        PrefixManager prefixManager = new SimplePrefixManager(ImmutableMap.of(PrefixManager.DEFAULT_PREFIX,
                "http://www.semanticweb.org/ontologies/2012/5/Ontology1340973114537.owl#"));
        return prefixManager;
    }
}
