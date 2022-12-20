package it.unibz.inf.ontop.spec.mapping.parser;


import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class BasicNativeMappingMistakeTest extends AbstractBasicMappingMistakeTest {

    @Test
    public void testUnboundTargetVariable() {
        assertThrows(InvalidMappingSourceQueriesException.class, () -> execute(ROOT + "unbound.obda"),
                "Error: Cannot find relation \"QUESTREPOSITORY\".\"PUBLIC\".PERSON (available choices: []) PERSON \n" +
                        "Problem location: source query of triplesMap \n" +
                        "[id: mapping-unbound-target-variable\n" +
                        "target atoms: triple(s,p,o) with s/RDF(http:/localhost/person/{}(TmpToTEXT(ID)),IRI), p/<http://example.org/voc#firstName>, o/RDF(TmpToTEXT(FNAME),xsd:string)\n" +
                        "source query: SELECT ID FROM PERSON]\n");
    }

    @Disabled ("The incorrect SQL is wrapped up as a black-box view")
    @Test
    public void testInvalidSQLQuery1() {
        assertThrows(InvalidMappingSourceQueriesException.class, () -> execute(ROOT + "invalid-sql1.obda"));
    }

    @Test
    public void testInvalidSQLQuery2() {
        assertThrows(InvalidMappingSourceQueriesException.class, () -> execute(ROOT + "invalid-sql2.obda"),
                "FAILED TO PARSE: SELECTID,FNAMEFROMPERSON net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: \"SELECTID\" <S_IDENTIFIER>\n" +
                        "    at line 1, column 1.\n" +
                        "\n" +
                        "Was expecting one of:\n" +
                        "\n" +
                        "    \"(\"\n" +
                        "    \"ALTER\"\n" +
                        "    \"CALL\"\n" +
                        "    \"COMMENT\"\n" +
                        "    \"COMMIT\"\n" +
                        "    \"CREATE\"\n" +
                        "    \"DECLARE\"\n" +
                        "    \"DELETE\"\n" +
                        "    \"DESCRIBE\"\n" +
                        "    \"DROP\"\n" +
                        "    \"EXEC\"\n" +
                        "    \"EXECUTE\"\n" +
                        "    \"EXPLAIN\"\n" +
                        "    \"GRANT\"\n" +
                        "    \"IF\"\n" +
                        "    \"INSERT\"\n" +
                        "    \"MERGE\"\n" +
                        "    \"PURGE\"\n" +
                        "    \"RENAME\"\n" +
                        "    \"REPLACE\"\n" +
                        "    \"RESET\"\n" +
                        "    \"ROLLBACK\"\n" +
                        "    \"SAVEPOINT\"\n" +
                        "    \"SET\"\n" +
                        "    \"SHOW\"\n" +
                        "    \"TRUNCATE\"\n" +
                        "    \"UPDATE\"\n" +
                        "    \"UPSERT\"\n" +
                        "    \"USE\"\n" +
                        "    \"VALUES\"\n" +
                        "    \"WITH\"\n" +
                        "    <K_SELECT>\n" +
                        "\n" +
                        "Error: Error parsing SQL query: Couldn't find SELECT clause SELECTID,FNAMEFROMPERSON \n" +
                        "Problem location: source query of triplesMap \n" +
                        "[id: mapping-invalid-sql2\n" +
                        "target atoms: triple(s,p,o) with s/RDF(http:/localhost/person/{}(TmpToTEXT(ID)),IRI), p/<http://example.org/voc#firstName>, o/RDF(TmpToTEXT(FNAME),xsd:string)\n" +
                        "source query: SELECTID,FNAMEFROMPERSON]\n");
    }

    @Test
    public void testMissingTargetTerm() {
        assertThrows(InvalidMappingException.class, () -> execute(ROOT + "missing-target-term.obda"),
                "\n" +
                        "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                        "\n" +
                        "MappingId = 'mapping-unbound-target-variable'\n" +
                        "Line 10: Invalid target: '<http:/localhost/person/{ID}> :firstName  .'\n" +
                        "Debug information\n" +
                        "mismatched input '.' expecting {'true', 'false', 'TRUE', 'True', 'FALSE', 'False', ENCLOSED_COLUMN_NAME, IRIREF, PNAME_LN, BLANK_NODE_LABEL, INTEGER, DECIMAL, DOUBLE, STRING_LITERAL_QUOTE, ANON}\n");
    }

    @Test
    public void testFQDNInTargetTerm1() {
        assertThrows( InvalidMappingException.class, () -> execute(ROOT + "fqdn1.obda"),
                "\n" +
                        "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                        "\n" +
                        "MappingId = 'mapping-unbound-target-variable'\n" +
                        "Line 11: Invalid target: '<http:/localhost/person/{PERSON.ID}> :firstName \"{FNAME}\"^^xsd:string .'\n" +
                        "Debug information\n" +
                        "Fully qualified columns as PERSON.ID are not accepted.\n" +
                        "Please, use an alias instead.\n");
    }

    @Test
    public void testFQDNInTargetTerm2() {
        assertThrows(InvalidMappingException.class, () -> execute(ROOT + "fqdn2.obda"),
                "\n" +
                        "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                        "\n" +
                        "MappingId = 'mapping-unbound-target-variable'\n" +
                        "Line 11: Invalid target: '<http:/localhost/person/{PERSON.ID}> :firstName \"{FNAME}\"^^xsd:string .'\n" +
                        "Debug information\n" +
                        "Fully qualified columns as PERSON.ID are not accepted.\n" +
                        "Please, use an alias instead.\n");
    }

    @Test
    public void testFQDNInTargetTerm3() {
        assertThrows(InvalidMappingException.class, () -> execute(ROOT + "fqdn3.obda"),
                "\n" +
                        "The syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n" +
                        "\n" +
                        "MappingId = 'mapping-unbound-target-variable'\n" +
                        "Line 11: Invalid target: '<http:/localhost/person/{P1.ID}> :firstName \"{FNAME}\"^^xsd:string .'\n" +
                        "Debug information\n" +
                        "Fully qualified columns as P1.ID are not accepted.\n" +
                        "Please, use an alias instead.\n");
    }

    @Override
    protected <T extends OntopMappingSQLAllConfiguration.Builder<T>> OntopMappingSQLAllConfiguration.Builder<T> createConfiguration(OntopMappingSQLAllConfiguration.Builder<T> builder, String obdaFile) {
        return builder.nativeOntopMappingFile(obdaFile);
    }

}
