package it.unibz.inf.ontop.docker.mssql;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.AbstractDistinctInAggregateTest;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class DistinctInAggregateMssqlTest extends AbstractDistinctInAggregateTest {

    private static final String propertiesFile = "/mssql/university.properties";

    @BeforeClass
    public static void before() {
        CONNECTION = createReasoner(owlFile, obdaFile, propertiesFile);
    }

    @Test
    @Ignore("STRING_AGG(DISTINCT) is not supported by MSSQL")
    @Override
    public void testGroupConcatDistinct() {
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
