package it.unibz.inf.ontop.docker.mssql;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.AbstractDistinctInAggregateTest;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class DistinctInAggregateMssqlTest extends AbstractDistinctInAggregateTest {

    private static String propertiesFile = "/mssql/university.properties";

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertiesFile);
        CONNECTION = REASONER.getConnection();
    }

    @Test
    @Ignore("STRING_AGG(DISTINCT) is not supported by MSSQL")
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
