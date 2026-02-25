package it.unibz.inf.ontop.spec.mapping.parser;

import java.io.File;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;

import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.TargetQueryParserFactory;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class NativeMappingParserTest {

    private static final String ROOT = "src/test/resources/it/unibz/inf/ontop/io/";
    private static final String ROOT2 = "src/test/resources/format/obda/";
    private final SQLPPMappingFactory ppMappingFactory;
    private final SpecificationFactory specificationFactory;
    private final SQLMappingParser mappingParser;
    private final SQLPPSourceQueryFactory sourceQueryFactory;
    private final TargetQueryParser targetQueryParser;

    public NativeMappingParserTest() {
        OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
                .jdbcUrl("dummy")
                .jdbcDriver("dummy")
                .build();

        Injector injector = configuration.getInjector();
        specificationFactory = injector.getInstance(SpecificationFactory.class);

        mappingParser = injector.getInstance(SQLMappingParser.class);
        ppMappingFactory = injector.getInstance(SQLPPMappingFactory.class);
        sourceQueryFactory = injector.getInstance(SQLPPSourceQueryFactory.class);
        TargetQueryParserFactory targetQueryParserFactory = injector.getInstance(TargetQueryParserFactory.class);
        targetQueryParser = targetQueryParserFactory.createParser(specificationFactory.createPrefixManager(
                ImmutableMap.of(PrefixManager.DEFAULT_PREFIX, "http://obda.org/onto.owl#")));
    }

    @Test
    public void testRegularFile() throws Exception {
        String[][] mappings = {
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

        ImmutableList.Builder<SQLPPTriplesMap> builder = ImmutableList.builder();
        for (String[] m : mappings) {
            builder.add(new OntopNativeSQLPPTriplesMap(
                    m[0],
                    sourceQueryFactory.createSourceQuery(m[1]),
                    m[2],
                    targetQueryParser.parse(m[2])));
        }
        
        SQLPPMapping ppMapping0 = ppMappingFactory.createSQLPreProcessedMapping(builder.build(),
                specificationFactory.createPrefixManager(ImmutableMap.of()));
        OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer();
        writer.write(new File(ROOT + "SchoolRegularFile.obda"), ppMapping0);

        SQLPPMapping ppMapping = mappingParser.parse(new File(ROOT + "SchoolRegularFile.obda"));
        assertEquals(5, ppMapping.getPrefixManager().getPrefixMap().size());
        assertEquals(6, ppMapping.getTripleMaps().size());
    }

    @Test
    public void testLoadWithBlankMappingId() {
        var ex = assertThrows(InvalidMappingException.class,
                () -> mappingParser.parse(new File(ROOT + "SchoolBadFile5.obda")));
        assertEquals("\n" +
                        "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                        "\n" +
                        "Line 10: Mapping ID is missing\n", ex.getMessage());
    }

    @Test
    public void testLoadMultilineWithBlankMappingId() {
        var ex = assertThrows(InvalidMappingException.class,
                () -> mappingParser.parse(new File(ROOT + "SchoolBadFile5m.obda")));
        assertEquals("\n" +
                        "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                        "\n" +
                        "Line 10: Mapping ID is missing\n", ex.getMessage());
    }

    @Test
    public void testLoadWithBlankTargetQuery() {
        var ex = assertThrows(InvalidMappingException.class,
                () -> mappingParser.parse(new File(ROOT + "SchoolBadFile6.obda")));
        assertEquals("\n" +
                        "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                        "\n" +
                        "MappingId = 'M2'\n" +
                        "Line 15: Target query is missing\n", ex.getMessage());
    }

    @Test
    public void testLoadWithMissingTargetQuery() {
        var ex = assertThrows(InvalidMappingException.class,
                () -> mappingParser.parse(new File(ROOT + "SchoolBadFile6m.obda")));
        assertEquals("\n" +
                        "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                        "\n" +
                        "MappingId = 'M2'\n" +
                        "Line 16: Target is missing\n", ex.getMessage());
    }

    @Test
    public void testLoadWithBlankSourceQuery()  {
        var ex = assertThrows(InvalidMappingException.class,
                () -> mappingParser.parse(new File(ROOT + "SchoolBadFile7.obda")));
        assertEquals("\n" +
                        "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                        "\n" +
                        "MappingId = 'M3'\n" +
                        "Line 20: Source query is missing\n", ex.getMessage());
    }

    @Test
    public void testLoadWithMissingSourceQuery()  {
        var ex = assertThrows(InvalidMappingException.class,
                () -> mappingParser.parse(new File(ROOT + "SchoolBadFile7m.obda")));
        assertEquals("\n" +
                        "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                        "\n" +
                        "MappingId = 'M3'\n" +
                        "Line 24: Source is missing\n", ex.getMessage());
    }

    @Test
    public void testLoadWithBadTargetQuery() {
        var ex = assertThrows(InvalidMappingException.class,
                () -> mappingParser.parse(new File(ROOT + "SchoolBadFile8.obda")));
        assertEquals("\n" +
                        "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                        "\n" +
                        "MappingId = 'M1'\n" +
                        "Line 14: Invalid target: ':P{id} :Student ; :firstName {fname} ; :lastName {lname} ; :age {age}^^xsd:integer .'\n" +
                        "Debug information\n" +
                        "extraneous input ';' expecting {'true', 'false', 'TRUE', 'True', 'FALSE', 'False', ENCLOSED_COLUMN_NAME, IRIREF, PNAME_LN, BLANK_NODE_LABEL, INTEGER, DECIMAL, DOUBLE, STRING_LITERAL_QUOTE, ANON}\n" +
                        "MappingId = 'M2'\n" +
                        "Line 19: Invalid target: ':C{id} a :Course ; :title {title} ; :hasLecturer :L{id} ; description {description}@en-US .'\n" +
                        "Debug information\n" +
                        "mismatched input 'description' expecting {'.', ';', 'a', IRIREF, PNAME_LN}", ex.getMessage());
    }

    @Test
    public void testLoadWithBadMultilineTargetQuery() {
        var ex = assertThrows(InvalidMappingException.class,
                () -> mappingParser.parse(new File(ROOT + "SchoolBadFile8m.obda")));
        assertEquals("\n" +
                "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                "\n" +
                "MappingId = 'M1'\n" +
                "Line 17: Invalid target: ':P{id} :Student ; :firstName {fname} ; :lastName {lname} ; :age {age}^^xsd:integer .'\n" +
                "Debug information\n" +
                "extraneous input ';' expecting {'true', 'false', 'TRUE', 'True', 'FALSE', 'False', ENCLOSED_COLUMN_NAME, IRIREF, PNAME_LN, BLANK_NODE_LABEL, INTEGER, DECIMAL, DOUBLE, STRING_LITERAL_QUOTE, ANON}\n" +
                "MappingId = 'M2'\n" +
                "Line 25: Invalid target: ':C{id} a :Course ; :title {title} ; :hasLecturer :L{id} ; description {description}@en-US .'\n" +
                "Debug information\n" +
                "mismatched input 'description' expecting {'.', ';', 'a', IRIREF, PNAME_LN}", ex.getMessage());
    }

    @Test
    public void testLoadWithPredicateDeclarations()  {
        var ex = assertThrows(MappingIOException.class,
                () -> mappingParser.parse(new File(ROOT + "SchoolBadFile9.obda")));
        assertEquals("java.io.IOException: ERROR reading SchoolBadFile9.obda at line: 9\n" +
                        "MESSAGE: The tag [ClassDeclaration] is no longer supported. You may safely remove the content from the file.", ex.getMessage());
    }

    @Test
    public void testLoadWithAllMistakes() {
            var ex = assertThrows(InvalidMappingException.class,
                    () -> mappingParser.parse(new File(ROOT + "SchoolBadFile10.obda")));
            assertEquals("\n" +
                            "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                            "\n" +
                            "Line 11: Mapping ID is missing\n" +
                            "\n" +
                            "MappingId = 'M2'\n" +
                            "Line 17: Target query is missing\n" +
                            "\n" +
                            "MappingId = 'M3'\n" +
                            "Line 23: Source query is missing\n", ex.getMessage());
    }

    @Test
    public void testLoadConcat() throws Exception {
        var mapping = mappingParser.parse(new File(ROOT2 + "mapping-northwind.obda"));
        assertEquals(7, mapping.getTripleMaps().get(0).getTargetAtoms().size());
    }

    @Test
    public void testLoadMultiline() throws Exception {
        var mapping = mappingParser.parse(new File(ROOT2 + "mapping-northwind-multiline.obda"));
        assertEquals(7, mapping.getTripleMaps().get(0).getTargetAtoms().size());
    }

    @Test
    public void testLoadMultilineWithMultilineTargetNoSpace() throws Exception {
        var mapping = mappingParser.parse(new File(ROOT2 + "mapping-northwind-multiline-m.obda"));
        assertEquals(7, mapping.getTripleMaps().get(0).getTargetAtoms().size());
    }

    @Test
    public void testLoadMultilineWithMultilineTarget() throws Exception {
        var mapping = mappingParser.parse(new File(ROOT2 + "mapping-northwind-multiline-m2.obda"));
        assertEquals(7, mapping.getTripleMaps().get(0).getTargetAtoms().size());
    }

    @Test
    public void testLoadMultilineWithMultilineTargetNotLast() throws Exception {
        var mapping = mappingParser.parse(new File(ROOT2 + "mapping-northwind-multiline-m3.obda"));
        assertEquals(7, mapping.getTripleMaps().get(0).getTargetAtoms().size());
    }

    @Test
    public void testLoadMultilineWithMultilineTargetNotLastNpSpace() throws Exception {
        var ex = assertThrows(MappingIOException.class, () -> mappingParser.parse(new File(ROOT2 + "mapping-northwind-multiline-m4.obda")));
        assertEquals("java.io.IOException: ERROR reading mapping-northwind-multiline-m4.obda at line: 11\n" +
                "MESSAGE: Unknown parameter name \"NORTHWIND.EMPLOYEES\" at line: 11.", ex.getMessage());
    }

    @Test
    public void testSpaceBeforeEndCollectionSymbol() throws Exception {
        mappingParser.parse(new File(ROOT2 + "unusualCollectionEnding.obda"));
    }

    @Test
    public void testEndCollectionSymbolRequirement() {
        var ex = assertThrows(MappingIOException.class,
                () -> mappingParser.parse(new File(ROOT2 + "missingCollectionEnding.obda")));
        assertEquals("java.io.IOException: ERROR reading missingCollectionEnding.obda at line: 48\n" +
                        "MESSAGE: End collection symbol ]] is missing.", ex.getMessage());
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
