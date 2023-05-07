package federationOptimization.queryRewriting;

import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QueryRewritingTest {

//    private static final String PROPERTY = "src/test/resources/compareIRI/boot-multiple-inheritance.properties";
//    private static final String OWL = "src/test/resources/compareIRI/boot-multiple-inheritance.owl";
//    private static final String OBDA = "src/test/resources/compareIRI/boot-multiple-inheritance.obda";

    private static final String owlFile = "src/test/resources/federation-test/bsbm-ontology.owl";
    private static final String obdaFile = "src/test/resources/federation-test/bsbm-mappings-sc2.obda";
    private static final String propertyFile = "src/test/resources/federation-test/sc2.properties";
    private static final String hintFile = "src/test/resources/federation-test/hintFile.txt";
    private static final String labFile = "src/test/resources/federation-test/SourceLab.txt";
    private static final String sourceFile = "src/test/resources/federation-test/SourceFile.txt";
    private static final String effLabel = "src/test/resources/federation-test/effLabel.txt";

    private static final String query = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" +
            "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
            "\n" +
            "SELECT ?label ?comment ?producer ?productFeature ?propertyTextual1 ?propertyTextual2 ?propertyTextual3\n" +
            " ?propertyNumeric1 ?propertyNumeric2 ?propertyTextual4 ?propertyTextual5 ?propertyNumeric4\n" +
            "WHERE {\n" +
            "    ?product bsbm:productId ?id .\n" +
            "    FILTER (?id < 1000 )\n" +
            "    ?product rdfs:label ?label .\n" +
            "\t?product rdfs:comment ?comment .\n" +
            "\t?product bsbm:producer ?p .\n" +
            "\t?p rdfs:label ?producer .\n" +
            "    ?product dc:publisher ?p .\n" +
            "\t?product bsbm:productFeature ?f .\n" +
            "\t?f rdfs:label ?productFeature .\n" +
            "\t?product bsbm:productPropertyTextual1 ?propertyTextual1 .\n" +
            "\t?product bsbm:productPropertyTextual2 ?propertyTextual2 .\n" +
            "    ?product bsbm:productPropertyTextual3 ?propertyTextual3 .\n" +
            "\t?product bsbm:productPropertyNumeric1 ?propertyNumeric1 .\n" +
            "\t?product bsbm:productPropertyNumeric2 ?propertyNumeric2 .\n" +
            "\tOPTIONAL { ?product bsbm:productPropertyTextual4 ?propertyTextual4 }\n" +
            "    OPTIONAL { ?product bsbm:productPropertyTextual5 ?propertyTextual5 }\n" +
            "    OPTIONAL { ?product bsbm:productPropertyNumeric4 ?propertyNumeric4 }\n" +
            "}";

    @Test
    public void testQueryRewriting() throws Exception {
        QueryRewriting QR = new QueryRewriting(owlFile, obdaFile, propertyFile, hintFile, labFile, sourceFile, effLabel);
        System.out.println(QR.getOptimizedSQL(query));
    }

}