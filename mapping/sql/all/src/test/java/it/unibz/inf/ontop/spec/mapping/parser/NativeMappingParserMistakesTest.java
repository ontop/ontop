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
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class NativeMappingParserMistakesTest {

    private static final String ROOT = "src/test/resources/it/unibz/inf/ontop/io/";
    private static final String ROOT2 = "src/test/resources/format/obda/";
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

    public NativeMappingParserMistakesTest() {
        OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
                .jdbcUrl("dummy")
                .jdbcDriver("dummy")
                .build();

        Injector injector = configuration.getInjector();
        specificationFactory = injector.getInstance(SpecificationFactory.class);

        mappingParser = injector.getInstance(SQLMappingParser.class);
        ppMappingFactory = injector.getInstance(SQLPPMappingFactory.class);
    }

    @Test
    public void testRegularFile() throws Exception {
        SQLPPMapping ppMapping0 = ppMappingFactory.createSQLPreProcessedMapping(ImmutableList.of(),
                specificationFactory.createPrefixManager(ImmutableMap.of()));
        OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer();
        writer.write(new File(ROOT + "SchoolRegularFile.obda"), ppMapping0);

        SQLPPMapping ppMapping = mappingParser.parse(new File(ROOT + "SchoolRegularFile.obda"));
        assertEquals(5, ppMapping.getPrefixManager().getPrefixMap().size());
        assertEquals(0, ppMapping.getTripleMaps().size());
    }

    @Test
    public void testLoadWithBlankMappingId() {
        assertThrows(InvalidMappingException.class,
                () -> mappingParser.parse(new File(ROOT + "SchoolBadFile5.obda")),
                "\n" +
                        "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                        "\n" +
                        "Line 10: Mapping ID is missing\n");
    }

    @Test
    public void testLoadWithBlankTargetQuery() {
        assertThrows(InvalidMappingException.class,
                () -> mappingParser.parse(new File(ROOT + "SchoolBadFile6.obda")),
                "\n" +
                        "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                        "\n" +
                        "MappingId = 'M2'\n" +
                        "Line 15: Target is missing\n" +
                        "\n");
    }

    @Test
    public void testLoadWithBlankSourceQuery()  {
        assertThrows(InvalidMappingException.class,
                () -> mappingParser.parse(new File(ROOT + "SchoolBadFile7.obda")),
                "\n" +
                        "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                        "\n" +
                        "MappingId = 'M3'\n" +
                        "Line 20: Source query is missing\n" +
                        "\n");
    }

    @Test
    public void testLoadWithBadTargetQuery() {
        assertThrows(InvalidMappingException.class,
                () -> mappingParser.parse(new File(ROOT + "SchoolBadFile8.obda")),
                "\n" +
                        "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                        "\n" +
                        "MappingId = 'M1'\n" +
                        "Line 12: Invalid target: ':P{id} :Student ; :firstName {fname} ; :lastName {lname} ; :age {age}^^xsd:integer .'\n" +
                        "Debug information\n" +
                        "extraneous input ';' expecting {'true', 'false', 'TRUE', 'True', 'FALSE', 'False', ENCLOSED_COLUMN_NAME, IRIREF, PNAME_LN, BLANK_NODE_LABEL, INTEGER, DECIMAL, DOUBLE, STRING_LITERAL_QUOTE, ANON}\n" +
                        "MappingId = 'M2'\n" +
                        "Line 17: Invalid target: ':C{id} a :Course ; :title {title} ; :hasLecturer :L{id} ; description {description}@en-US .'\n" +
                        "Debug information\n" +
                        "mismatched input 'description' expecting {'.', ';', 'a', IRIREF, PNAME_LN}\n");
    }

    @Test
    public void testLoadWithPredicateDeclarations()  {
        assertThrows(MappingIOException.class,
                () -> mappingParser.parse(new File(ROOT + "SchoolBadFile9.obda")),
                "java.io.IOException: ERROR reading SchoolBadFile9.obda at line: 9\n" +
                        "MESSAGE: The tag [ClassDeclaration] is no longer supported. You may safely remove the content from the file.\n");
    }

    @Test
    public void testLoadWithAllMistakes() {
            assertThrows(InvalidMappingException.class,
                    () -> mappingParser.parse(new File(ROOT + "SchoolBadFile10.obda")),
                    "\n" +
                            "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                            "\n" +
                            "Line 11: Mapping ID is missing\n" +
                            "\n" +
                            "MappingId = 'M2'\n" +
                            "Line 17: Target is missing\n" +
                            "\n" +
                            "MappingId = 'M3'\n" +
                            "Line 23: Source query is missing\n" +
                            "\n");
    }

    @Test
    public void testLoadConcat() throws Exception {
        mappingParser.parse(new File(ROOT2 + "mapping-northwind.obda"));
    }

    @Test
    public void testSpaceBeforeEndCollectionSymbol() throws Exception {
        mappingParser.parse(new File(ROOT2 + "unusualCollectionEnding.obda"));
    }

    @Test
    public void testEndCollectionSymbolRequirement() {
        assertThrows(MappingIOException.class,
                () -> mappingParser.parse(new File(ROOT2 + "missingCollectionEnding.obda")),
                "java.io.IOException: ERROR reading missingCollectionEnding.obda at line: 48\n" +
                        "MESSAGE: End collection symbol ]] is missing.\n");
    }

    @Test
    public void testEqualSymbol() throws Exception {
        mappingParser.parse(new File(ROOT2 + "sudtyrol.obda"));
    }

    @Test
    public void testBootstrappedMapping() throws Exception {
        mappingParser.parse(new File(ROOT2 + "bootstrapped.obda"));
    }

}
