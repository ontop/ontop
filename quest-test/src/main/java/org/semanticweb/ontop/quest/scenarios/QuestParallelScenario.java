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
import junit.framework.TestSuite;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.ontop.quest.ResultSetInfo;
import org.semanticweb.ontop.quest.ResultSetInfoTupleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * Parallel version of QuestScenarioParent
 */
public abstract class QuestParallelScenario implements Runnable {

	static final Logger logger = LoggerFactory.getLogger(QuestParallelScenario.class);

    private final String suiteName;
    private final List<String> names;
	protected final List<String> testURIs;
	protected final List<String> queryFileURLs;
	protected final List<String> resultFileURLs;
	protected final String owlFileURL;
	protected final String obdaFileURL;
	protected final String parameterFileURL;
	protected Repository dataRep;
    private int counter = 0;

	public interface ParallelFactory {

		QuestParallelScenario createQuestParallelScenarioTest(String suiteName,
                                                              List<String> testURIs, List<String> names,  List<String> queryFileURLs,
                                                                    List<String> resultFileURLs, String owlFileURL, String obdaFileURL,
                                                                    String parameterFileURL);

		String getMainManifestFile();
	}

    public class ThreadExceptionHandler implements Thread.UncaughtExceptionHandler {

        private Map<Thread, Throwable> exceptions = new HashMap<>();

        public void uncaughtException(Thread th, Throwable ex) {
            exceptions.put(th, ex);
        }

        public Map<Thread, Throwable> getThrownExceptions() {
            return exceptions;
        }
    };

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
					logger.error(e2.toString(), e2);
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
        List<Thread> threads = new ArrayList<>();
        for (String name : names) {
            Thread t = new Thread(this);
            t.setUncaughtExceptionHandler(exceptionHandler);
            t.start();
            threads.add(t);
        }

        for (Thread thread : threads) {
            thread.join();
        }
        tearDown();

        summarizeThreadResults(threads, exceptionHandler.getThrownExceptions());

	}

    private void summarizeThreadResults(List<Thread> threads, Map<Thread, Throwable> exceptions) throws Exception {
        int testNb = threads.size();
        int failureNb = exceptions.size();
        String separationLine = "--------------------------";
        System.out.println(separationLine);
        String msg = suiteName + "\n";
        msg += String.format("Tests passed: %d over %d\n", testNb - failureNb, testNb);

        if (failureNb > 0) {
            msg += "Failures: \n";
            for (Thread th : exceptions.keySet()) {
                int index = threads.indexOf(th);
                String name = names.get(index);
                msg += ("  " + name + "\n");
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

    private synchronized int getIndex() {
        int index = counter++;
        return index;
    }

    public void run() {
        int index = getIndex();
        String testName = names.get(index);
        logger.debug(String.format("Thread %d: %s", index, testName));

        try {
            ResultSetInfo expectedResult = readResultSetInfo(index);
            RepositoryConnection con = dataRep.getConnection();

            try {
                String queryString = readQueryString(index);
                Query query = con.prepareQuery(QueryLanguage.SPARQL, queryString, queryFileURLs.get(index));
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

			logger.error(message.toString());
            // TODO: improve it
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

			logger.error(message.toString());
            // TODO: improve it
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
			logger.debug(msg);
		}
		return counter;
	}
	
	private Object attributeValue(ResultSetInfo rsInfo, String attribute) throws QueryEvaluationException {
		return rsInfo.get(attribute);
	}
	
	
	private String readQueryString(int index) throws IOException {
		InputStream stream = new URL(queryFileURLs.get(index)).openStream();
		try {
			return IOUtil.readString(new InputStreamReader(stream, "UTF-8"));
		} finally {
			stream.close();
		}
	}
	
	private ResultSetInfo readResultSetInfo(int index) throws Exception {
		Set<Statement> resultGraph = readGraphResultSetInfo(index);
		return ResultSetInfoTupleUtil.toResuleSetInfo(resultGraph);
	}
	
	private Set<Statement> readGraphResultSetInfo(int index) throws Exception {
		RDFFormat rdfFormat = Rio.getParserFormatForFileName(resultFileURLs.get(index));
		if (rdfFormat != null) {
			RDFParser parser = Rio.createParser(rdfFormat, dataRep.getValueFactory());
			ParserConfig config = parser.getParserConfig();
			// To emulate DatatypeHandling.IGNORE 
			config.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
			config.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
			config.addNonFatalError(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);
			config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);

//			parser.setDatatypeHandling(DatatypeHandling.IGNORE);
//			parser.setPreserveBNodeIDs(true);

			Set<Statement> result = new LinkedHashSet<>();
			parser.setRDFHandler(new StatementCollector(result));

			InputStream in = new URL(resultFileURLs.get(index)).openStream();
			try {
				parser.parse(in, resultFileURLs.get(index));
			} finally {
				in.close();
			}
			return result;
		} else {
			throw new RuntimeException("Unable to determine file type of results file");
		}
	}

	public static TestSuite suite(String manifestFileURL, ParallelFactory factory) throws Exception {
		return suite(manifestFileURL, factory, true);
	}

	public static TestSuite suite(String manifestFileURL, ParallelFactory factory, boolean approvedOnly) throws Exception {
		logger.info("Building test suite for {}", manifestFileURL);

		TestSuite suite = new TestSuite(factory.getClass().getName());

		// Read manifest and create declared test cases
		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.initialize();
		RepositoryConnection con = manifestRep.getConnection();

		ScenarioManifestTestUtils.addTurtle(con, new URL(manifestFileURL), manifestFileURL);

        String suiteName = getManifestName(manifestRep, con, manifestFileURL);
		suite.setName(suiteName);

		// Extract test case information from the manifest file. Note that we only
		// select those test cases that are mentioned in the list.
		StringBuilder query = new StringBuilder(512);
		query.append(" SELECT DISTINCT testURI, testName, resultFile, queryFile, owlFile, obdaFile, parameterFile \n");
		query.append(" FROM {} rdf:first {testURI} \n");
		if (approvedOnly) {
			query.append("    obdat:approval {obdat:Approved}; \n");
		}
		query.append("    mf:name {testName}; \n");
		query.append("    mf:result {resultFile}; \n");
		query.append("    mf:knowledgebase {owlFile}; \n");
		query.append("    mf:mappings {obdaFile}; \n");
		query.append("    [ mf:parameters {parameterFile} ]; \n");
		query.append("    mf:action {action} qt:query {queryFile} \n");
		query.append(" USING NAMESPACE \n");
		query.append("    mf = <http://obda.org/quest/tests/test-manifest#>, \n");
		query.append("    obdat = <http://obda.org/quest/tests/test-scenario#>, \n");
		query.append("    qt = <http://obda.org/quest/tests/test-query#> ");
		TupleQuery testCaseQuery = con.prepareTupleQuery(QueryLanguage.SERQL, query.toString());
		
		logger.debug("Evaluating query..");
		TupleQueryResult testCases = testCaseQuery.evaluate();

        List<String> testURIs = new ArrayList<>();
        List<String> testNames = new ArrayList<>();
        List<String> queryFiles = new ArrayList<>();
        List<String> resultFiles = new ArrayList<>();

        String mainOwlFile = null;
        String mainObdaFile = null;
        String mainParameterFile = null;

        /**
         * Test cases that share the same configuration
         * will be run in parallel.
         */
		while (testCases.hasNext()) {
			BindingSet bindingSet = testCases.next();

			URI testURI = (URI) bindingSet.getValue("testURI");
			String testName = bindingSet.getValue("testName").toString();
			String resultFile = bindingSet.getValue("resultFile").toString();
			String queryFile = bindingSet.getValue("queryFile").toString();
			String owlFile = bindingSet.getValue("owlFile").toString();
			String obdaFile = bindingSet.getValue("obdaFile").toString();
			String parameterFile = (bindingSet.getValue("parameterFile") == null 
					? "" : bindingSet.getValue("parameterFile").toString());

            /**
             * Checks the config files of the Quest instance.
             *
             * Uses the first given files and ignores tests that use different files.
             */
            if (mainOwlFile == null)
                mainOwlFile = owlFile;
            else if (!mainOwlFile.equals(owlFile)) {
                logger.warn(String.format("%s use a different OWL file (%s vs %s). Ignored", testName, owlFile, mainOwlFile));
                continue;
            }
            if (mainObdaFile == null)
                mainObdaFile = obdaFile;
            else if (!mainObdaFile.equals(obdaFile)) {
                logger.warn("{} use a different OBDA file. Ignored", testName);
                continue;
            }
            if (mainParameterFile == null)
                mainParameterFile = parameterFile;
            else if (!mainParameterFile.equals(parameterFile)) {
                logger.warn("{} use a different parameter file. Ignored", testName);
                continue;
            }

			logger.debug("Found test case: {}", testName);

            testURIs.add(testURI.toString());
            testNames.add(testName);
            queryFiles.add(queryFile);
            resultFiles.add(resultFile);
		}

        QuestParallelScenario scenario = factory.createQuestParallelScenarioTest(suiteName, testURIs, testNames,
                queryFiles, resultFiles, mainOwlFile, mainObdaFile, mainParameterFile);
        if (scenario != null) {
            suite.addTest(new ParallelTestCase(scenario));
        }

		testCases.close();
		con.close();

		manifestRep.shutDown();
		logger.info("Created test suite with " + suite.countTestCases() + " test cases.");
		return suite;
	}

	protected static String getManifestName(Repository manifestRep, RepositoryConnection con, String manifestFileURL)
		throws QueryEvaluationException, RepositoryException, MalformedQueryException
	{
		// Try to extract suite name from manifest file
		TupleQuery manifestNameQuery = con.prepareTupleQuery(QueryLanguage.SERQL,
				"SELECT ManifestName FROM {ManifestURL} rdfs:label {ManifestName}");
		manifestNameQuery.setBinding("ManifestURL", manifestRep.getValueFactory().createURI(manifestFileURL));
		TupleQueryResult manifestNames = manifestNameQuery.evaluate();
		try {
			if (manifestNames.hasNext()) {
				return manifestNames.next().getValue("ManifestName").stringValue();
			}
		}
		finally {
			manifestNames.close();
		}
		// Derive name from manifest URL
		int lastSlashIdx = manifestFileURL.lastIndexOf('/');
		int secLastSlashIdx = manifestFileURL.lastIndexOf('/', lastSlashIdx - 1);
		return manifestFileURL.substring(secLastSlashIdx + 1, lastSlashIdx);
	}
}
