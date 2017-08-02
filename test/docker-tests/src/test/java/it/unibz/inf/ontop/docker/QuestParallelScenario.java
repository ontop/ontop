package it.unibz.inf.ontop.docker;

/*
 * #%L
 * ontop-test
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parallel adaptation of QuestScenarioParent
 */
public abstract class QuestParallelScenario {

    /**
     * Attributes
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestParallelScenario.class);
    private static final int THREAD_COEFFICIENT = 5;

    private final String suiteName;
    private final List<String> names;
    private final List<String> testURIs;
    private final List<String> queryFileURLs;
    private final List<String> resultFileURLs;
    protected final String owlFileURL;
    protected final String obdaFileURL;
    protected final String parameterFileURL;
    private Repository dataRep;


	public QuestParallelScenario(String suiteName,
                                 List<String> testURIs, List<String> names, List<String> queryFileURLs, List<String> resultFileURLs,
                                 String owlFileURL, String obdaFileURL, String parameterFileURL) {
        this.suiteName = suiteName;
        this.names = names;
		this.testURIs = testURIs;
		this.queryFileURLs = queryFileURLs;
		this.resultFileURLs = resultFileURLs;
		this.owlFileURL = owlFileURL;
		this.obdaFileURL = obdaFileURL;
		this.parameterFileURL = parameterFileURL;
	}

	public void setUp() throws Exception {
		if (!owlFileURL.isEmpty() && !obdaFileURL.isEmpty()) {
			try {
				dataRep = createRepository();
			} catch (Exception exc) {
				try {
					dataRep.shutDown();
					dataRep = null;
				} catch (Exception e2) {
					LOGGER.error(e2.toString(), e2);
				}
				throw exc;
			}
		}
	}

	protected abstract Repository createRepository() throws Exception;

	protected void tearDown() throws Exception {
		if (dataRep != null) {
			dataRep.shutDown();
			dataRep = null;
		}
	}

	public void runTest() throws Exception {
        ThreadExceptionHandler exceptionHandler = new ThreadExceptionHandler();

        setUp();
        List<QuestThread> threads = new ArrayList<>();
        for (int i = 0; i < THREAD_COEFFICIENT; i++) {
            for (int j = 0; j < names.size(); j++ ) {
                String testName = names.get(j);
                String queryFileURI = queryFileURLs.get(j);
                String resultFileURL = resultFileURLs.get(j);

                QueryExecution queryExecution = new QueryExecution(testName, queryFileURI, resultFileURL);
                QuestThread t = new QuestThread(queryExecution);
                t.setUncaughtExceptionHandler(exceptionHandler);
                threads.add(t);
            }
        }

        for(Thread thread: threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
        tearDown();

        summarizeThreadResults(threads, exceptionHandler.getThrownExceptions());

	}

    /**
     * Displays information about the test.
     */
    private void summarizeThreadResults(List<QuestThread> threads, Map<QuestThread, Throwable> exceptions) throws Exception {
        final int testNb = threads.size();
        final int failureNb = exceptions.size();
        String separationLine = "--------------------------";
        System.out.println(separationLine);
        String msg = suiteName + "\n";
        msg += String.format("Tests passed: %d over %d\n", testNb - failureNb, testNb);

        if (failureNb > 0) {
            msg += "Failures: \n";
            Map<String, Integer> failureCountByTest = new HashMap<>();
            
            for (QuestThread thread : exceptions.keySet()) {
                String testName = thread.getQueryExecution().getTestName();
                if (failureCountByTest.containsKey(testName)) {
                    failureCountByTest.put(testName, failureCountByTest.get(testName)+1);
                }
                else {
                    failureCountByTest.put(testName, 1);
                }
            }
            
            for (String failedTest: failureCountByTest.keySet()) {
                msg += String.format("  %s (%d times) \n", failedTest, 
                		failureCountByTest.get(failedTest));
            }
            /**
             * Declares the test as failed
             */
            throw new Exception(msg);
        }
        else {
            System.out.println(msg);
            System.out.println(separationLine);
        }
    }

    public class QueryExecution implements Runnable {

        private final String testName;
        private final String queryFileURL;
        private final String resultFileURL;

        public QueryExecution(String testName, String queryFileURI, String resultFileURL) {
            this.testName = testName;
            this.queryFileURL = queryFileURI;
            this.resultFileURL = resultFileURL;
        }

        public String getTestName() {
            return testName;
        }

        @Override
        public void run() {
            LOGGER.debug(String.format("Thread: %s", testName));
            TestExecutor executor = new TestExecutor(testName, queryFileURL, resultFileURL, dataRep, LOGGER);
            try {
                executor.runTest();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    public class QuestThread extends Thread {
        private final QueryExecution queryExecution;

        public QuestThread(QueryExecution queryExecution) {
            super(queryExecution);
            this.queryExecution = queryExecution;
        }

        public QueryExecution getQueryExecution() {
            return queryExecution;
        }
    }

    public class ThreadExceptionHandler implements Thread.UncaughtExceptionHandler {

        private Map<QuestThread, Throwable> exceptions = new ConcurrentHashMap<>();

        public void uncaughtException(Thread th, Throwable ex) {
            exceptions.put((QuestThread) th, ex);
        }

        public Map<QuestThread, Throwable> getThrownExceptions() {
            return exceptions;
        }
    }


}
