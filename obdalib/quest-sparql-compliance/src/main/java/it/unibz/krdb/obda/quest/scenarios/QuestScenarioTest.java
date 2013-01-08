package it.unibz.krdb.obda.quest.scenarios;

import info.aduna.io.IOUtil;
import info.aduna.iteration.Iterations;
import info.aduna.text.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.dawg.DAWGTestResultSetUtil;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultParser;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class QuestScenarioTest extends TestCase {
	
	static final Logger logger = LoggerFactory.getLogger(QuestScenarioTest.class);

	protected final String testURI;
	protected final String queryFileURL;
	protected final String resultFileURL;
	protected final String owlFileURL;
	protected final String obdaFileURL;
	protected final String parameterFileURL;
	protected Repository dataRep;
	
	public interface Factory {
		QuestScenarioTest createQuestScenarioTest(String testURI, String name, String queryFileURL, 
				String resultFileURL, String owlFileURL, String obdaFileURL);
		
		QuestScenarioTest createQuestScenarioTest(String testURI, String name, String queryFileURL, 
				String resultFileURL, String owlFileURL, String obdaFileURL, String parameterFileURL);
	
		String getMainManifestFile();
	}

	public QuestScenarioTest(String testURI, String name, String queryFileURL, String resultFileURL, 
			String owlFileURL, String obdaFileURL) {
		this(testURI, name, queryFileURL, resultFileURL, owlFileURL, obdaFileURL, "");
	}

	public QuestScenarioTest(String testURI, String name, String queryFileURL, String resultFileURL,
			String owlFileURL, String obdaFileURL, String parameterFileURL) {
		super(name);
		this.testURI = testURI;
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
	protected void tearDown() throws Exception {
		if (dataRep != null) {
			dataRep.shutDown();
			dataRep = null;
		}
	}

	@Override
	protected void runTest() throws Exception {
		RepositoryConnection con = dataRep.getConnection();
		try {
			String queryString = readQueryString();
			Query query = con.prepareQuery(QueryLanguage.SPARQL, queryString, queryFileURL);
			if (query instanceof TupleQuery) {
				TupleQueryResult queryResult = ((TupleQuery)query).evaluate();
				TupleQueryResult expectedResult = readExpectedTupleQueryResult();
				compareTupleQuerySizeResults(queryResult, expectedResult);
			} else if (query instanceof GraphQuery) {
				GraphQueryResult gqr = ((GraphQuery)query).evaluate();
				Set<Statement> queryResult = Iterations.asSet(gqr);
				Set<Statement> expectedResult = readExpectedGraphQueryResult();
				compareGraphs(queryResult, expectedResult);
			} else {
				throw new RuntimeException("Unexpected query type: " + query.getClass());
			}
		} catch (Exception e) {
			e.printStackTrace();
			compareTupleQuerySizeResults(null, readExpectedTupleQueryResult());
		}
		finally {
			con.close();
		}
	}

	private void compareTupleQuerySizeResults(TupleQueryResult queryResult, TupleQueryResult expectedResult) throws Exception {
		int queryResultSize = countTuple(queryResult);
		int expectedResultSize = attributeValue(expectedResult, "counter");
		if (queryResultSize != expectedResultSize) {
			StringBuilder message = new StringBuilder(128);
			message.append("\n============ ");
			message.append(getName());
			message.append(" =======================\n");
			message.append("Expected result: ");
			message.append(expectedResultSize);
			message.append("\n");
			message.append("Query result: ");
			message.append(queryResultSize);
			message.append("\n");		
			message.append("=====================================\n");

			logger.error(message.toString());
			fail(message.toString());
		}
	}

	private int countTuple(TupleQueryResult tuples) throws QueryEvaluationException {
		if (tuples == null) {
			return -1;
		}
		int counter = 0;
		while (tuples.hasNext()) {
			counter++;
			tuples.next();
		}
		return counter;
	}
	
	private int attributeValue(TupleQueryResult tuples, String attribute) throws QueryEvaluationException {
		BindingSet binding = tuples.next();
		return Integer.parseInt(binding.getValue(attribute).stringValue());
	}
	
	private void compareGraphs(Set<Statement> queryResult, Set<Statement> expectedResult) throws Exception {
		if (!ModelUtil.equals(expectedResult, queryResult)) {
			StringBuilder message = new StringBuilder(128);
			message.append("\n============ ");
			message.append(getName());
			message.append(" =======================\n");
			message.append("Expected result: \n");
			for (Statement st : expectedResult) {
				message.append(st.toString());
				message.append("\n");
			}
			message.append("=============");
			StringUtil.appendN('=', getName().length(), message);
			message.append("========================\n");

			message.append("Query result: \n");
			for (Statement st : queryResult) {
				message.append(st.toString());
				message.append("\n");
			}
			message.append("=============");
			StringUtil.appendN('=', getName().length(), message);
			message.append("========================\n");

			logger.error(message.toString());
			fail(message.toString());
		}
	}
	
	private String readQueryString() throws IOException {
		InputStream stream = new URL(queryFileURL).openStream();
		try {
			return IOUtil.readString(new InputStreamReader(stream, "UTF-8"));
		} finally {
			stream.close();
		}
	}
	
	private TupleQueryResult readExpectedTupleQueryResult() throws Exception {
		TupleQueryResultFormat tqrFormat = QueryResultIO.getParserFormatForFileName(resultFileURL);
		if (tqrFormat != null) {
			InputStream in = new URL(resultFileURL).openStream();
			try {
				TupleQueryResultParser parser = QueryResultIO.createParser(tqrFormat);
				parser.setValueFactory(dataRep.getValueFactory());

				TupleQueryResultBuilder qrBuilder = new TupleQueryResultBuilder();
				parser.setTupleQueryResultHandler(qrBuilder);

				parser.parse(in);
				return qrBuilder.getQueryResult();
			} finally {
				in.close();
			}
		} else {
			Set<Statement> resultGraph = readExpectedGraphQueryResult();
			return DAWGTestResultSetUtil.toTupleQueryResult(resultGraph);
		}
	}
	
	private Set<Statement> readExpectedGraphQueryResult() throws Exception {
		RDFFormat rdfFormat = Rio.getParserFormatForFileName(resultFileURL);
		if (rdfFormat != null) {
			RDFParser parser = Rio.createParser(rdfFormat);
			parser.setDatatypeHandling(DatatypeHandling.IGNORE);
			parser.setPreserveBNodeIDs(true);
			parser.setValueFactory(dataRep.getValueFactory());

			Set<Statement> result = new LinkedHashSet<Statement>();
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

	public static TestSuite suite(String manifestFileURL, Factory factory) throws Exception {
		return suite(manifestFileURL, factory, true);
	}

	public static TestSuite suite(String manifestFileURL, Factory factory, boolean approvedOnly) throws Exception {
		logger.info("Building test suite for {}", manifestFileURL);

		TestSuite suite = new TestSuite(factory.getClass().getName());

		// Read manifest and create declared test cases
		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.initialize();
		RepositoryConnection con = manifestRep.getConnection();

		ScenarioManifestTest.addTurtle(con, new URL(manifestFileURL), manifestFileURL);

		suite.setName(getManifestName(manifestRep, con, manifestFileURL));

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

			logger.debug("Found test case: {}", testName);

			QuestScenarioTest test = factory.createQuestScenarioTest(testURI.toString(), testName, queryFile,
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