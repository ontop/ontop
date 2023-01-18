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


import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public abstract class QuestScenarioParent extends TestCase {
	
	static final Logger logger = LoggerFactory.getLogger(QuestScenarioParent.class);

	protected final String testIRI;
	protected final String queryFileURL;
	protected final String resultFileURL;
	protected final String owlFileURL;
	protected final String obdaFileURL;
	protected final String parameterFileURL;
	protected Repository dataRep;
	
	public interface Factory {
		
		QuestScenarioParent createQuestScenarioTest(String testIRI, String name, String queryFileURL,
                                                    String resultFileURL, String owlFileURL, String obdaFileURL, String parameterFileURL);
	
		String getMainManifestFile();
	}

	public QuestScenarioParent(String testIRI, String name, String queryFileURL, String resultFileURL,
                               String owlFileURL, String obdaFileURL, String parameterFileURL) {
		super(name);
		this.testIRI = testIRI;
		this.queryFileURL = queryFileURL;
		this.resultFileURL = resultFileURL;
		this.owlFileURL = owlFileURL;
		this.obdaFileURL = obdaFileURL;
		this.parameterFileURL = parameterFileURL;
	}
	
	@Override
	protected void setUp() throws Exception {
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
	
	@Override
	protected void tearDown() {
		if (dataRep != null) {
			dataRep.shutDown();
			dataRep = null;
		}
	}

	@Override
	protected void runTest() throws Exception {
		TestExecutor executor = new TestExecutor(getName(), queryFileURL, resultFileURL, dataRep, logger);
		executor.runTest();
	}

	public static TestSuite suite(String manifestFileURL, Factory factory) throws Exception {
		return suite(manifestFileURL, factory, true);
	}

	public static TestSuite suite(String manifestFileURL, Factory factory, boolean approvedOnly) throws Exception {
		logger.info("Building test suite for {}", manifestFileURL);

		TestSuite suite = new TestSuite(factory.getClass().getName());

		// Read manifest and create declared test cases
		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.init();
		RepositoryConnection con = manifestRep.getConnection();

		ScenarioManifestTestUtils.addTurtle(con, new URL(manifestFileURL), manifestFileURL);

		suite.setName(getManifestName(manifestRep, con, manifestFileURL));

		// Extract test case information from the manifest file. Note that we only
		// select those test cases that are mentioned in the list.
		StringBuilder query = new StringBuilder(512);
		query.append(" PREFIX mf: <http://obda.org/quest/tests/test-manifest#> \n");
		query.append(" PREFIX obdat: <http://obda.org/quest/tests/test-scenario#> \n");
		query.append(" PREFIX qt: <http://obda.org/quest/tests/test-query#> \n");
		query.append(" SELECT DISTINCT ?testIRI ?testName ?resultFile ?queryFile ?owlFile ?obdaFile ?parameterFile \n");
		query.append(" WHERE { [] rdf:first ?testIRI . \n");
		if (approvedOnly) {
			query.append(" ?testIRI obdat:approval obdat:Approved . \n");
		}
		query.append(" ?testIRI mf:name ?testName ; \n");
		query.append("    mf:result ?resultFile ; \n");
		query.append("    mf:knowledgebase ?owlFile ; \n");
		query.append("    mf:mappings ?obdaFile . \n");
		query.append(" OPTIONAL { ?testIRI mf:parameters ?parameterFile . }\n");
		query.append(" ?testIRI mf:action ?action . \n");
		query.append(" ?action qt:query ?queryFile . } \n");

		TupleQuery testCaseQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
		
		logger.debug("Evaluating query..");
		TupleQueryResult testCases = testCaseQuery.evaluate();
		while (testCases.hasNext()) {
			BindingSet bindingSet = testCases.next();

			IRI testIRI = (IRI) bindingSet.getValue("testIRI");
			String testName = bindingSet.getValue("testName").toString();
			String resultFile = bindingSet.getValue("resultFile").toString();
			String queryFile = bindingSet.getValue("queryFile").toString();
			String owlFile = bindingSet.getValue("owlFile").toString();
			String obdaFile = bindingSet.getValue("obdaFile").toString();
			String parameterFile = (bindingSet.getValue("parameterFile") == null 
					? "" : bindingSet.getValue("parameterFile").toString());

			logger.debug("Found test case: {}", testName);

			QuestScenarioParent test = factory.createQuestScenarioTest(testIRI.toString(), testName, queryFile,
					resultFile, owlFile, obdaFile, parameterFile);
			if (test != null) {
				suite.addTest(test);
			}
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
		TupleQuery manifestNameQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
				"SELECT ?ManifestName WHERE { ?ManifestURL rdfs:label ?ManifestName . }");
		manifestNameQuery.setBinding("ManifestURL", manifestRep.getValueFactory().createIRI(manifestFileURL));
		try (TupleQueryResult manifestNames = manifestNameQuery.evaluate()) {
			if (manifestNames.hasNext()) {
				return manifestNames.next().getValue("ManifestName").stringValue();
			}
		}
		// Derive name from manifest URL
		int lastSlashIdx = manifestFileURL.lastIndexOf('/');
		int secLastSlashIdx = manifestFileURL.lastIndexOf('/', lastSlashIdx - 1);
		return manifestFileURL.substring(secLastSlashIdx + 1, lastSlashIdx);
	}
}
