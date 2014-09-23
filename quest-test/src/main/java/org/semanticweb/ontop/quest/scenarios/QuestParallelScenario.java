package org.semanticweb.ontop.quest.scenarios;

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

import info.aduna.io.IOUtil;
import org.openrdf.model.Statement;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.ontop.quest.ResultSetInfo;
import org.semanticweb.ontop.quest.ResultSetInfoTupleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
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

            try {
                ResultSetInfo expectedResult = readResultSetInfo();
                RepositoryConnection con = dataRep.getConnection();

                try {
                    String queryString = readQueryString();
                    Query query = con.prepareQuery(QueryLanguage.SPARQL, queryString, queryFileURL);
                    if (query instanceof TupleQuery) {
                        TupleQueryResult queryResult = ((TupleQuery) query).evaluate();
                        //TODO:  Should we do a more sophisticated verification?
                        compareResultSize(testName, queryResult, expectedResult);
                    } else {
                        throw new RuntimeException("Unexpected query type: " + query.getClass());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    compareThrownException(testName, e, expectedResult); // compare the thrown exception class
                } finally {
                    con.close();
                }
            } catch(Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        private void compareResultSize(String testName, TupleQueryResult queryResult, ResultSetInfo expectedResult) throws Exception {
            int queryResultSize = countTuple(queryResult);
            int expectedResultSize = (Integer) attributeValue(expectedResult, "counter");
            if (queryResultSize != expectedResultSize) {
                StringBuilder message = new StringBuilder(128);
                message.append("\n============ ");
                message.append(testName);
                message.append(" =======================\n");
                message.append("Expected result: ");
                message.append(expectedResultSize);
                message.append("\n");
                message.append("Query result: ");
                message.append(queryResultSize);
                message.append("\n");
                message.append("=====================================\n");

                LOGGER.error(message.toString());
                throw new Exception(message.toString());
            }
        }

        private void compareThrownException(String testName, Exception ex, ResultSetInfo expectedResult) throws Exception {
            String thrownException = ex.getClass().getName();
            String expectedThrownException = (String) attributeValue(expectedResult, "thrownException");
            if (!thrownException.equals(expectedThrownException)) {
                StringBuilder message = new StringBuilder(128);
                message.append("\n============ ");
                message.append(testName);
                message.append(" =======================\n");
                message.append("Expected thrown exception: ");
                message.append(expectedThrownException);
                message.append("\n");
                message.append("Thrown exception: ");
                message.append(thrownException);
                message.append("\n");
                message.append("Message:" + ex.getMessage());
                message.append("=====================================\n");

                LOGGER.error(message.toString());
                throw new Exception(message.toString());
            }
        }

        private int countTuple(TupleQueryResult tuples) throws QueryEvaluationException {
            if (tuples == null) {
                return -1;
            }
            int counter = 0;
            while (tuples.hasNext()) {
                counter++;
                BindingSet bs = tuples.next();
                String msg = String.format("x: %s, y: %s\n", bs.getValue("x"), bs.getValue("y"));
                LOGGER.debug(msg);
            }
            return counter;
        }

        private Object attributeValue(ResultSetInfo rsInfo, String attribute) throws QueryEvaluationException {
            return rsInfo.get(attribute);
        }


        private String readQueryString() throws IOException {
            InputStream stream = new URL(queryFileURL).openStream();
            try {
                return IOUtil.readString(new InputStreamReader(stream, "UTF-8"));
            } finally {
                stream.close();
            }
        }

        private ResultSetInfo readResultSetInfo() throws Exception {
            Set<Statement> resultGraph = readGraphResultSetInfo();
            return ResultSetInfoTupleUtil.toResuleSetInfo(resultGraph);
        }

        private Set<Statement> readGraphResultSetInfo() throws Exception {
            RDFFormat rdfFormat = Rio.getParserFormatForFileName(resultFileURL);
            if (rdfFormat != null) {
                RDFParser parser = Rio.createParser(rdfFormat, dataRep.getValueFactory());
                ParserConfig config = parser.getParserConfig();
                // To emulate DatatypeHandling.IGNORE
                config.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
                config.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
                config.addNonFatalError(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);
                config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);

                Set<Statement> result = new LinkedHashSet<>();
                parser.setRDFHandler(new StatementCollector(result));

                InputStream in = new URL(resultFileURL).openStream();
                try {
                    parser.parse(in, resultFileURL);
                } finally {
                    in.close();
                }
                return result;
            } else {
                throw new RuntimeException("Unable to determine file type of results file");
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
