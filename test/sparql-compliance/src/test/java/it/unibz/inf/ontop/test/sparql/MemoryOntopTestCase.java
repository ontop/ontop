package it.unibz.inf.ontop.test.sparql;

import com.google.common.collect.ImmutableSet;
import junit.framework.TestCase;
import org.eclipse.rdf4j.query.Dataset;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assume.assumeTrue;

public abstract class MemoryOntopTestCase extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryOntopTestCase.class);
    private final String testIRI;
    private final String queryFileURL;
    private final String resultFileURL;
    private final Dataset dataSet;
    private final boolean laxCardinality;
    private final boolean checkOrder;
    private final ImmutableSet<String> ignoredTests;

    public MemoryOntopTestCase(String testIRI, String name, String queryFileURL, String resultFileURL,
                               Dataset dataSet, boolean laxCardinality, boolean checkOrder,
                               ImmutableSet<String> ignoredTests) {
        super(name);
        this.testIRI = testIRI;
        this.queryFileURL = queryFileURL;
        this.resultFileURL = resultFileURL;
        this.dataSet = dataSet;
        this.laxCardinality = laxCardinality;
        this.checkOrder = checkOrder;
        this.ignoredTests = ignoredTests;
    }

    @Test
    @Override
    public void runTest() throws Exception {
        assumeTrue(!ignoredTests.contains(testIRI));

        MemoryTestExecutor executor = new MemoryTestExecutor(testIRI, getName(), queryFileURL, resultFileURL, dataSet, laxCardinality, checkOrder);
        executor.runTest();
    }
}
