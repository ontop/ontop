package it.unibz.inf.ontop.spec.mapping.parser;


import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class BasicR2RMLMappingMistakeTest extends AbstractBasicMappingMistakeTest {

    @Test
    public void testUnboundTargetVariable() {
        assertThrows(InvalidMappingSourceQueriesException.class, () -> execute(ROOT + "unbound-r2rml.ttl"),
                "Error: Cannot find relation \"QUESTREPOSITORY\".\"PUBLIC\".PERSON (available choices: []) PERSON \n" +
                        "Problem location: source query of triplesMap \n" +
                        "[id: mapping--879907594\n" +
                        "target atoms: triple(s,p,o) with s/RDF(http:/localhost/person/{}(TmpToTEXT(ID)),IRI), p/<http://example.org/voc#firstName>, o/RDF(TmpToTEXT(FNAME),xsd:string)\n" +
                        "source query: SELECT ID FROM PERSON]\n");
    }

    @Disabled ("The incorrect SQL is wrapped up as a black-box view")
    @Test
    public void testInvalidSQLQuery1() {
        assertThrows(InvalidMappingSourceQueriesException.class,
                () -> execute("/mistake/invalid-sql1-r2rml.ttl"));
    }

    @Test
    public void testInvalidSQLQuery2() {
        assertThrows(InvalidMappingSourceQueriesException.class, () -> execute(ROOT + "invalid-sql2-r2rml.ttl"),
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
                        "[id: mapping-1836635551\n" +
                        "target atoms: triple(s,p,o) with s/RDF(http:/localhost/person/{}(TmpToTEXT(ID)),IRI), p/<http://example.org/voc#firstName>, o/RDF(TmpToTEXT(FNAME),xsd:string)\n" +
                        "source query: SELECTID,FNAMEFROMPERSON]\n");
    }

    @Test
    public void testInvalidPredicateObject1() {
        assertThrows(InvalidMappingException.class, () -> execute(ROOT + "invalid-predicate-object1-r2rml.ttl"),
                "Invalid mapping: PredicateObjectMap _:79516f0a75e346b695e2397dbab2af752 [urn:uuid:c691d4c2-2979-4b73-8aa3-fbb442dc4816#79516f0a75e346b695e2397dbab2af752]" +
                        " in TripleMap urn:mapping-unbound-target-variable has no ObjectMaps or RefObjectMaps.\n");
    }

    @Override
    protected <T extends OntopMappingSQLAllConfiguration.Builder<T>> OntopMappingSQLAllConfiguration.Builder<T> createConfiguration(OntopMappingSQLAllConfiguration.Builder<T> builder, String r2rmlFile) {
        return builder.r2rmlMappingFile(r2rmlFile);
    }
}
