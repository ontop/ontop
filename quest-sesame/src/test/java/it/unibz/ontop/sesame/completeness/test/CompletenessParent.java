/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.ontop.sesame.completeness.test;

import info.aduna.io.IOUtil;
import info.aduna.iteration.Iterations;
import info.aduna.text.StringUtil;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.dawg.DAWGTestResultSetUtil;
import org.openrdf.query.impl.MutableTupleQueryResult;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultParserRegistry;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultParser;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sesameWrapper.SesameClassicInMemoryRepo;

public abstract class CompletenessParent extends TestCase {

	static final Logger logger = LoggerFactory.getLogger(CompletenessParent.class);

	protected final String testId;
	protected final String queryFile;
	protected final String resultFile;
	protected final String ontologyFile;
	protected final String parameterFile;
	
	protected final boolean laxCardinality = false;
	
	protected QuestPreferences preferences;
	protected OWLOntology ontology;
	
	protected Repository repository;

	protected QuestOWL reasoner;
	
	public interface Factory 
	{
		CompletenessParent createCompletenessTest(String tid, String name, String resf, String propf, String owlf, String sparqlf) throws Exception;
	
		String getMainManifestFile();
	}
	
	public CompletenessParent(String tid, String name, String resf, String propf, String owlf, String sparqlf) throws Exception {
		super(name);
		testId = tid;
		resultFile = resf;
		parameterFile = propf;
		ontologyFile = owlf;
		queryFile = sparqlf;
	}

	protected void loadReasonerParameters(String path) throws IOException {
		preferences = new QuestPreferences();
		preferences.load(new URL(path).openStream());
	}

	protected void loadOntology(String path) throws OWLOntologyCreationException, IOException {
		OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
		ontology = owlManager.loadOntologyFromOntologyDocument(new URL(path).openStream());
	}

	protected Repository createRepository() throws Exception {
		SesameClassicInMemoryRepo repository = new SesameClassicInMemoryRepo("CompletenessTest", ontologyFile, parameterFile);
		repository.initialize();
		return repository;
	}
	
	@Override
	protected void setUp() throws Exception {
		repository = createRepository();
	}
	
	@Override
	protected void tearDown() throws Exception {
		if (repository != null) {
			repository.shutDown();
			repository = null;
		}
	}

	@Override
	protected void runTest() throws Exception {
		logger.info("\n\n\n============== " + testId + " ==============\n");
		RepositoryConnection con = repository.getConnection();
		try {
			String queryString = readQueryString();
			Query query = con.prepareQuery(QueryLanguage.SPARQL, queryString, queryFile);
			if (query instanceof TupleQuery) {
				TupleQueryResult queryResult = ((TupleQuery) query).evaluate();
				TupleQueryResult expectedResult = readExpectedTupleQueryResult();
				compareTupleQueryResults(queryResult, expectedResult);
			} else if (query instanceof GraphQuery) {
				GraphQueryResult gqr = ((GraphQuery) query).evaluate();
				Set<Statement> queryResult = Iterations.asSet(gqr);
				Set<Statement> expectedResult = readExpectedGraphQueryResult();
				compareGraphs(queryResult, expectedResult);
			} else if (query instanceof BooleanQuery) {
				boolean queryResult = ((BooleanQuery)query).evaluate();
				boolean expectedResult = readExpectedBooleanQueryResult();
				assertEquals(expectedResult, queryResult);
			} else {
				throw new RuntimeException("Unexpected query type: " + query.getClass());
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		finally {
			con.close();
		}
	}

	private void compareTupleQueryResults(TupleQueryResult queryResult, TupleQueryResult expectedResult)
			throws Exception
		{
			// Create MutableTupleQueryResult to be able to re-iterate over the
			// results
			MutableTupleQueryResult queryResultTable = new MutableTupleQueryResult(queryResult);
			MutableTupleQueryResult expectedResultTable = new MutableTupleQueryResult(expectedResult);

			boolean resultsEqual;
			if (laxCardinality) {
				resultsEqual = isSubset(queryResultTable, expectedResultTable);
			}
			else {
				resultsEqual = equals(queryResultTable, expectedResultTable);
			}

			if (!resultsEqual) {
				queryResultTable.beforeFirst();
				expectedResultTable.beforeFirst();

				/*
				 * StringBuilder message = new StringBuilder(128);
				 * message.append("\n============ "); message.append(getName());
				 * message.append(" =======================\n");
				 * message.append("Expected result: \n"); while
				 * (expectedResultTable.hasNext()) {
				 * message.append(expectedResultTable.next()); message.append("\n"); }
				 * message.append("============="); StringUtil.appendN('=',
				 * getName().length(), message);
				 * message.append("========================\n"); message.append("Query
				 * result: \n"); while (queryResultTable.hasNext()) {
				 * message.append(queryResultTable.next()); message.append("\n"); }
				 * message.append("============="); StringUtil.appendN('=',
				 * getName().length(), message);
				 * message.append("========================\n");
				 */

				List<BindingSet> queryBindings = Iterations.asList(queryResultTable);

				List<BindingSet> expectedBindings = Iterations.asList(expectedResultTable);

				List<BindingSet> missingBindings = new ArrayList<BindingSet>(expectedBindings);
				missingBindings.removeAll(queryBindings);

				List<BindingSet> unexpectedBindings = new ArrayList<BindingSet>(queryBindings);
				unexpectedBindings.removeAll(expectedBindings);

				StringBuilder message = new StringBuilder(128);
				message.append("\n============ ");
				message.append(getName());
				message.append(" =======================\n");

				if (!missingBindings.isEmpty()) {

					message.append("Missing bindings: \n");
					for (BindingSet bs : missingBindings) {
						message.append(bs);
						message.append("\n");
					}

					message.append("=============");
					StringUtil.appendN('=', getName().length(), message);
					message.append("========================\n");
				}

				if (!unexpectedBindings.isEmpty()) {
					message.append("Unexpected bindings: \n");
					for (BindingSet bs : unexpectedBindings) {
						message.append(bs);
						message.append("\n");
					}

					message.append("=============");
					StringUtil.appendN('=', getName().length(), message);
					message.append("========================\n");
				}
				logger.error(message.toString());
				fail(message.toString());
			}
			/* debugging only: print out result when test succeeds 
			else {
				queryResultTable.beforeFirst();

				List<BindingSet> queryBindings = Iterations.asList(queryResultTable);
				StringBuilder message = new StringBuilder(128);

				message.append("\n============ ");
				message.append(getName());
				message.append(" =======================\n");

				message.append(" =======================\n");
				message.append("query result: \n");
				for (BindingSet bs: queryBindings) {
					message.append(bs);
					message.append("\n");
				}
				
				System.out.print(message.toString());
			}
			*/
		}
	
	/**
	 * Compares the two query results by converting them to graphs and returns
	 * true if they are equal. QueryResults are equal if they contain the same
	 * set of BindingSet and have the headers. Blank nodes identifiers are not
	 * relevant for equality, they are mapped from one model to the other by
	 * using the attached properties. Note that the method consumes both query
	 * results fully.
	 * 
	 * @throws QueryEvaluationException
	 */
	public static boolean equals(TupleQueryResult tqr1, TupleQueryResult tqr2)
		throws QueryEvaluationException
	{
		List<BindingSet> list1 = Iterations.asList(tqr1);
		List<BindingSet> list2 = Iterations.asList(tqr2);

		return matchBindingSets(list1, list2) && matchBindingSets(list2, list1);
	}

	public static boolean isSubset(TupleQueryResult tqr1, TupleQueryResult tqr2)
		throws QueryEvaluationException
	{
		List<BindingSet> list1 = Iterations.asList(tqr1);
		List<BindingSet> list2 = Iterations.asList(tqr2);
		
		return matchBindingSets(list1, list2);
	}

	private static boolean matchBindingSets(List<? extends BindingSet> queryResult1,
			Iterable<? extends BindingSet> queryResult2)
	{
		return matchBindingSets(queryResult1, queryResult2, new HashMap<BNode, BNode>(), 0);
	}

	/**
	 * A recursive method for finding a complete mapping between blank nodes in
	 * queryResult1 and blank nodes in queryResult2. The algorithm does a
	 * depth-first search trying to establish a mapping for each blank node
	 * occurring in queryResult1.
	 * 
	 * @return true if a complete mapping has been found, false otherwise.
	 */
	private static boolean matchBindingSets(List<? extends BindingSet> queryResult1,
			Iterable<? extends BindingSet> queryResult2, Map<BNode, BNode> bNodeMapping, int idx)
	{
		boolean result = false;

		if (idx < queryResult1.size()) {
			BindingSet bs1 = queryResult1.get(idx);

			List<BindingSet> matchingBindingSets = findMatchingBindingSets(bs1, queryResult2, bNodeMapping);

			for (BindingSet bs2 : matchingBindingSets) {
				// Map bNodes in bs1 to bNodes in bs2
				Map<BNode, BNode> newBNodeMapping = new HashMap<BNode, BNode>(bNodeMapping);

				for (Binding binding : bs1) {
					if (binding.getValue() instanceof BNode) {
						newBNodeMapping.put((BNode)binding.getValue(), (BNode)bs2.getValue(binding.getName()));
					}
				}

				// FIXME: this recursive implementation has a high risk of
				// triggering a stack overflow

				// Enter recursion
				result = matchBindingSets(queryResult1, queryResult2, newBNodeMapping, idx + 1);

				if (result == true) {
					// models match, look no further
					break;
				}
			}
		}
		else {
			// All statements have been mapped successfully
			result = true;
		}

		return result;
	}

	private static List<BindingSet> findMatchingBindingSets(BindingSet st,
			Iterable<? extends BindingSet> model, Map<BNode, BNode> bNodeMapping)
	{
		List<BindingSet> result = new ArrayList<BindingSet>();

		for (BindingSet modelSt : model) {
			if (bindingSetsMatch(st, modelSt, bNodeMapping)) {
				// All components possibly match
				result.add(modelSt);
			}
		}

		return result;
	}

	private static boolean bindingSetsMatch(BindingSet bs1, BindingSet bs2, Map<BNode, BNode> bNodeMapping) {

		if (bs1.size() != bs2.size()) {
			return false;
		}

		for (Binding binding1 : bs1) {
			Value value1 = binding1.getValue();
			Value value2 = bs2.getValue(binding1.getName());

			if (value1 instanceof BNode && value2 instanceof BNode) {
				BNode mappedBNode = bNodeMapping.get(value1);

				if (mappedBNode != null) {
					// bNode 'value1' was already mapped to some other bNode
					if (!value2.equals(mappedBNode)) {
						// 'value1' and 'value2' do not match
						return false;
					}
				}
				else {
					// 'value1' was not yet mapped, we need to check if 'value2' is a
					// possible mapping candidate
					if (bNodeMapping.containsValue(value2)) {
						// 'value2' is already mapped to some other value.
						return false;
					}
				}
			}
			else {
				// values are not (both) bNodes
				if (value1 instanceof Literal && value2 instanceof Literal) {
					// do literal value-based comparison for supported datatypes
					Literal leftLit = (Literal)value1;
					Literal rightLit = (Literal)value2;

					URI dt1 = leftLit.getDatatype();
					URI dt2 = rightLit.getDatatype();

					if (dt1 != null && dt2 != null && dt1.equals(dt2)
							&& XMLDatatypeUtil.isValidValue(leftLit.getLabel(), dt1)
							&& XMLDatatypeUtil.isValidValue(rightLit.getLabel(), dt2))
					{
						Integer compareResult = null;
						if (dt1.equals(XMLSchema.DOUBLE)) {
							compareResult = Double.compare(leftLit.doubleValue(), rightLit.doubleValue());
						}
						else if (dt1.equals(XMLSchema.FLOAT)) {
							compareResult = Float.compare(leftLit.floatValue(), rightLit.floatValue());
						}
						else if (dt1.equals(XMLSchema.DECIMAL)) {
							compareResult = leftLit.decimalValue().compareTo(rightLit.decimalValue());
						}
						else if (XMLDatatypeUtil.isIntegerDatatype(dt1)) {
							compareResult = leftLit.integerValue().compareTo(rightLit.integerValue());
						}
						else if (dt1.equals(XMLSchema.BOOLEAN)) {
							Boolean leftBool = Boolean.valueOf(leftLit.booleanValue());
							Boolean rightBool = Boolean.valueOf(rightLit.booleanValue());
							compareResult = leftBool.compareTo(rightBool);
						}
						else if (XMLDatatypeUtil.isCalendarDatatype(dt1)) {
							XMLGregorianCalendar left = leftLit.calendarValue();
							XMLGregorianCalendar right = rightLit.calendarValue();

							compareResult = left.compare(right);
						}

						if (compareResult != null) {
							if (compareResult.intValue() != 0) {
								return false;
							}
						}
						else if (!value1.equals(value2)) {
							return false;
						}
					}
					else if (!value1.equals(value2)) {
						return false;
					}
				}
				else if (!value1.equals(value2)) {
					return false;
				}
			}
		}

		return true;
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
		InputStream stream = new URL(queryFile).openStream();
		try {
			return IOUtil.readString(new InputStreamReader(stream, "UTF-8"));
		} finally {
			stream.close();
		}
	}
	
	private TupleQueryResult readExpectedTupleQueryResult() throws Exception {
		TupleQueryResultFormat tqrFormat = QueryResultIO.getParserFormatForFileName(resultFile);
		if (tqrFormat != null) {
			InputStream in = new URL(resultFile).openStream();
			try {
				TupleQueryResultParser parser = QueryResultIO.createParser(tqrFormat);
				parser.setValueFactory(repository.getValueFactory());

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
		RDFFormat rdfFormat = Rio.getParserFormatForFileName(resultFile);
		if (rdfFormat != null) {
			RDFParser parser = Rio.createParser(rdfFormat);
			parser.setDatatypeHandling(DatatypeHandling.IGNORE);
			parser.setPreserveBNodeIDs(true);
			parser.setValueFactory(repository.getValueFactory());

			Set<Statement> result = new LinkedHashSet<Statement>();
			parser.setRDFHandler(new StatementCollector(result));

			InputStream in = new URL(resultFile).openStream();
			try {
				parser.parse(in, resultFile);
			} finally {
				in.close();
			}
			return result;
		} else {
			throw new RuntimeException("Unable to determine file type of results file");
		}
	}

	private boolean readExpectedBooleanQueryResult() throws Exception {
		BooleanQueryResultFormat bqrFormat = BooleanQueryResultParserRegistry
				.getInstance().getFileFormatForFileName(resultFile);

		if (bqrFormat != null) {
			InputStream in = new URL(resultFile).openStream();
			try {
				return QueryResultIO.parse(in, bqrFormat);
			} finally {
				in.close();
			}
		} else {
			Set<Statement> resultGraph = readExpectedGraphQueryResult();
			return DAWGTestResultSetUtil.toBooleanQueryResult(resultGraph);
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

		CompletenessTestUtils.addTurtle(con, new URL(manifestFileURL), manifestFileURL);

		suite.setName(getManifestName(manifestRep, con, manifestFileURL));

		/* 
		 * Extract test case information from the manifest file. Note that we only
		 * select those test cases that are mentioned in the list.
		 */
		StringBuilder query = new StringBuilder(512);
		query.append("SELECT DISTINCT tid, name, resf, propf, owlf, sparqlf\n");
		query.append("FROM {} rdf:first {tid} \n");
		if (approvedOnly) {
			query.append("   obdat:approval {obdat:Approved};\n");
		}
		query.append("   mf:name {name};\n");
		query.append("   mf:result {resf};\n");
		query.append("   mf:parameters {propf};\n");
		query.append("   mf:action {action} qt:ontology {owlf};\n");
		query.append("                       qt:query {sparqlf}\n");
		query.append("USING NAMESPACE \n");
		query.append("   mf = <http://obda.org/quest/tests/test-manifest#>,\n");
		query.append("   qt = <http://obda.org/quest/tests/test-query#>,\n");
		query.append("   obdat = <http://obda.org/quest/tests/test-scenario#>");
		TupleQuery testCaseQuery = con.prepareTupleQuery(QueryLanguage.SERQL, query.toString());
		
		logger.debug("Evaluating query..");
		TupleQueryResult testCases = testCaseQuery.evaluate();
		while (testCases.hasNext()) {
			BindingSet bindingSet = testCases.next();

			String testId = bindingSet.getValue("tid").toString();
			String testName = bindingSet.getValue("name").toString();
			String resultFile = bindingSet.getValue("resf").toString();
			String parameterFile = bindingSet.getValue("propf").toString();
			String ontologyFile = bindingSet.getValue("owlf").toString();
			String queryFile = bindingSet.getValue("sparqlf").toString();

			logger.debug("Found test case: {}", testName);

			CompletenessParent test = factory.createCompletenessTest(testId, testName, resultFile, parameterFile, ontologyFile, queryFile);
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
		TupleQuery manifestNameQuery = con.prepareTupleQuery(QueryLanguage.SERQL, "SELECT ManifestName FROM {ManifestURL} rdfs:label {ManifestName}");
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
