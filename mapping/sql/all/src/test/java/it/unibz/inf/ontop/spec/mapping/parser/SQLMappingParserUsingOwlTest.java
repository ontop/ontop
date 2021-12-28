package it.unibz.inf.ontop.spec.mapping.parser;

import java.io.File;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;

import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import org.junit.Test;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;


import static org.junit.Assert.assertEquals;

public class SQLMappingParserUsingOwlTest {

    private final SQLPPMappingFactory ppMappingFactory;
    private final SpecificationFactory specificationFactory;
    private final SQLMappingParser mappingParser;

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

    public SQLMappingParserUsingOwlTest() {
        OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
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

    @Test
    public void testRegularFile() throws Exception {
        saveRegularFile();
        loadRegularFile();
    }

    @Test(expected=InvalidMappingExceptionWithIndicator.class)
    public void testLoadWithBlankMappingId()
            throws InvalidMappingException, MappingIOException {
        loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolBadFile5.obda");
    }

    @Test(expected=InvalidMappingExceptionWithIndicator.class)
    public void testLoadWithBlankTargetQuery() throws InvalidMappingException, MappingIOException {
        loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolBadFile6.obda");
    }

    @Test(expected=InvalidMappingExceptionWithIndicator.class)
    public void testLoadWithBlankSourceQuery() throws InvalidMappingException, MappingIOException {
        loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolBadFile7.obda");
    }

    @Test(expected=InvalidMappingExceptionWithIndicator.class)
    public void testLoadWithBadTargetQuery() throws InvalidMappingException,
            MappingIOException {
        loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolBadFile8.obda");
    }

    @Test(expected=MappingIOException.class)
    public void testLoadWithPredicateDeclarations() throws Exception {
        loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolBadFile9.obda");
    }

    @Test(expected=InvalidMappingExceptionWithIndicator.class)
    public void testLoadWithAllMistakes() throws InvalidMappingException,
            MappingIOException {
            loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolBadFile10.obda");
    }
    
    /*
     * Test saving to a file
     */

    private void saveRegularFile() throws Exception {
        SQLPPMapping ppMapping = ppMappingFactory.createSQLPreProcessedMapping(ImmutableList.of(),
                specificationFactory.createPrefixManager(ImmutableMap.of()));
        OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer();
        writer.write(new File("src/test/resources/it/unibz/inf/ontop/io/SchoolRegularFile.obda"), ppMapping);
    }

    /*
     * Test loading the file
     */

    private void loadRegularFile() throws Exception {
        SQLPPMapping ppMapping = loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolRegularFile.obda");

        // Check the content
        assertEquals(ppMapping.getPrefixManager().getPrefixMap().size(), 5);
        assertEquals(ppMapping.getTripleMaps().size(), 0);
    }

    private void loadFileWithMultipleDataSources() throws Exception {
        SQLPPMapping ppMapping = loadObdaFile("src/test/resources/it/unibz/inf/ontop/io/SchoolMultipleDataSources.obda");

        // Check the content
        assertEquals(ppMapping.getPrefixManager().getPrefixMap().size(), 6);
        assertEquals(ppMapping.getTripleMaps().size(), 2);
    }

    private SQLPPMapping loadObdaFile(String fileLocation) throws MappingIOException,
            InvalidMappingException {
        // Load the OBDA model
        return mappingParser.parse(new File(fileLocation));
    }
}
