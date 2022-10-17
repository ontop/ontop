package it.unibz.inf.ontop.docker.lightweight.mssql;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.MSSQLLightweightTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

@MSSQLLightweightTest
public class DistinctInAggregateSQLServerTest extends AbstractDistinctInAggregateTest {

    private static String propertiesFile = "/university/university-mssql.properties";

    @BeforeAll
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertiesFile);
        CONNECTION = REASONER.getConnection();
    }

    @Test
    @Disabled("STRING_AGG(DISTINCT) is not supported by MSSQL")
    @Override
    public void testGroupConcatDistinct() throws Exception {
    }

    @Override
    protected ImmutableSet<ImmutableMap<String, String>> getTuplesForAvg() {
        return ImmutableSet.of(
                ImmutableMap.of(
                        "p",buildAnswerIRI("1"),
                        "ad", "\"10.500000\"^^xsd:decimal"
                ));
    }
}

