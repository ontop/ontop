package org.semanticweb.ontop.quest.scenarios;


import junit.framework.TestCase;
import org.junit.Ignore;

/**
 * This class allows to separate the scenario
 * from the TestCase.
 *
 * Needed because there is a conflict for the run() method
 * (used for thread and used for launching a testCase).
 *
 */
@Ignore
public class ParallelTestCase extends TestCase {

    private final QuestParallelScenario scenario;

    public ParallelTestCase(String name, QuestParallelScenario scenario) {
        super(name);
        this.scenario = scenario;
    }

    @Override
    protected void runTest() throws Exception {
        scenario.runTest();
    }
}
