package it.unibz.inf.ontop.docker.lightweight;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractDistinctInAggregateTest extends AbstractDockerRDF4JTest {

    protected static final String OWL_FILE = "/university/university.ttl";
    protected static final String OBDA_FILE = "/university/university.obda";

    protected static final String sumDistinctQueryFile = "/distinctInAggregates/sumDistinct.rq";
    protected static final String avgDistinctQueryFile = "/distinctInAggregates/avgDistinct.rq";
    protected static final String countDistinctQueryFile = "/distinctInAggregates/countDistinct.rq";
    protected static final String groupConcatDistinctQueryFile = "/distinctInAggregates/groupConcatDistinct.rq";


    @Test
    public void testGroupConcatDistinct() throws Exception {
        Assertions.assertEquals(
                getTuplesForConcat(),
                executeQueryWithDatatypes(readQueryFile(groupConcatDistinctQueryFile))
        );
    }

    @Test
    public void testSumDistinct() throws Exception {
        Assertions.assertEquals(
                getTuplesForSum(),
                executeQueryWithDatatypes(readQueryFile(sumDistinctQueryFile))
        );
    }

    @Test
    public void testAvgDistinct() throws Exception {
        Assertions.assertEquals(
                getTuplesForAvg(),
                executeQueryWithDatatypes(readQueryFile(avgDistinctQueryFile))
        );
    }

    @Test
    public void testCountDistinct() throws Exception {
        Assertions.assertEquals(
                getTuplesForCount(),
                executeQueryWithDatatypes(readQueryFile(countDistinctQueryFile))
        );
    }

    protected ImmutableList<ImmutableMap<String, String>> getTuplesForCount() {
        return ImmutableList.of(
                ImmutableMap.of(
                        "p", buildAnswerIRI("1"),
                        "cd", "\"2\"^^xsd:integer"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("3"),
                        "cd", "\"1\"^^xsd:integer"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("8"),
                        "cd", "\"1\"^^xsd:integer"
                )
        );
    }

    protected ImmutableList<ImmutableMap<String, String>> getTuplesForSum() {
        return ImmutableList.of(
                ImmutableMap.of(
                        "p", buildAnswerIRI("1"),
                        "sd", "\"21\"^^xsd:integer"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("3"),
                        "sd", "\"12\"^^xsd:integer"
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("8"),
                        "sd", "\"13\"^^xsd:integer"
                ));
    }

    protected ImmutableList<ImmutableMap<String, String>> getTuplesForAvg() {
        return ImmutableList.of(
                ImmutableMap.of(
                        "p",buildAnswerIRI("1"),
                        "ad", "\"10.5000\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p",buildAnswerIRI("3"),
                        "ad", "\"12.0000\"^^xsd:decimal"
                ),
                ImmutableMap.of(
                        "p",buildAnswerIRI("8"),
                        "ad", "\"13.0000\"^^xsd:decimal"
                )
        );
    }

    protected ImmutableList<ImmutableMap<String, String>> getTuplesForConcat() {
        return ImmutableList.of(
                ImmutableMap.of(
                        "p", buildAnswerIRI("1"),
                        "sd", "\"10|11\""
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("3"),
                        "sd", "\"12\""
                ),
                ImmutableMap.of(
                        "p", buildAnswerIRI("8"),
                        "sd", "\"13\""
                ));
    }

    protected String buildAnswerIRI(String s) {
        return "<http://www.example.org/test#"+s+">";
    }

    private String readQueryFile(String queryFile) throws IOException {
        URL path = AbstractDistinctInAggregateTest.class.getResource(queryFile);
        try {
            Path filePath = Paths.get(path.toURI());
            return new String(Files.readAllBytes(filePath));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Cannot read input file");
        }
    }

}
