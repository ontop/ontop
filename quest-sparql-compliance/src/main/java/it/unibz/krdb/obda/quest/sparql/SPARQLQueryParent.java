package it.unibz.krdb.obda.quest.sparql;

/*
 * #%L
 * ontop-sparql-compliance
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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
import info.aduna.iteration.Iterations;
import info.aduna.text.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.dawg.DAWGTestResultSetUtil;
import org.openrdf.query.impl.DatasetImpl;
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
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SPARQLQueryParent extends TestCase {

	/*-----------*
	 * Constants *
	 *-----------*/

	static final Logger logger = LoggerFactory.getLogger(SPARQLQueryParent.class);

	protected final String testURI;

	protected final String queryFileURL;

	protected final String resultFileURL;

	protected final Dataset dataset;

	protected final boolean laxCardinality;

	protected final boolean checkOrder;

	/*-----------*
	 * Variables *
	 *-----------*/

	protected Repository dataRep;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SPARQLQueryParent(String testURI, String name, String queryFileURL, String resultFileURL,
			Dataset dataSet, boolean laxCardinality)
	{
		this(testURI, name, queryFileURL, resultFileURL, dataSet, laxCardinality, false);
	}

	public SPARQLQueryParent(String testURI, String name, String queryFileURL, String resultFileURL,
			Dataset dataSet, boolean laxCardinality, boolean checkOrder)
	{
		super(name);

		this.testURI = testURI;
		this.queryFileURL = queryFileURL;
		this.resultFileURL = resultFileURL;
		this.dataset = dataSet;
		this.laxCardinality = laxCardinality;
		this.checkOrder = checkOrder;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void setUp()
		throws Exception
	{
		dataRep = createRepository(dataset);

		if (dataset != null) {
			try {
				uploadDataset(dataset);
			}
			catch (Exception exc) {
				try {
					dataRep.shutDown();
					dataRep = null;
				}
				catch (Exception e2) {
					logger.error(e2.toString(), e2);
				}
				throw exc;
			}
		}
	}

	protected Repository createRepository(Dataset data)
		throws Exception
	{
		Repository repo = newRepository();
//		RepositoryConnection con = repo.getConnection();
//		try {
//			con.clear();
//			con.clearNamespaces();
//		}
//		finally {
//			con.close();
//		}
		return repo;
	}

	protected abstract Repository newRepository()
		throws Exception;

	@Override
	protected void tearDown()
		throws Exception
	{
		if (dataRep != null) {
			dataRep.shutDown();
			dataRep = null;
		}
	}

	@Override
	protected void runTest()
		throws Exception
	{
		RepositoryConnection con = dataRep.getConnection();
		try {
			String queryString = readQueryString();
			Query query = con.prepareQuery(QueryLanguage.SPARQL, queryString, queryFileURL);
			if (dataset != null) {
				query.setDataset(dataset);
			}
			if (query instanceof TupleQuery) {
				TupleQueryResult queryResult = ((TupleQuery)query).evaluate();

				TupleQueryResult expectedResult = readExpectedTupleQueryResult();

				compareTupleQueryResults(queryResult, expectedResult);

				// Graph queryGraph = RepositoryUtil.asGraph(queryResult);
				// Graph expectedGraph = readExpectedTupleQueryResult();
				// compareGraphs(queryGraph, expectedGraph);
			}
			else if (query instanceof GraphQuery) {
				GraphQueryResult gqr = ((GraphQuery)query).evaluate();
				Set<Statement> queryResult = Iterations.asSet(gqr);
				Set<Statement> expectedResult = readExpectedGraphQueryResult();
				compareGraphs(queryResult, expectedResult);
			}
			else if (query instanceof BooleanQuery) {
				boolean queryResult = ((BooleanQuery)query).evaluate();
				boolean expectedResult = readExpectedBooleanQueryResult();
				assertEquals(expectedResult, queryResult);
			}
			else {
				throw new RuntimeException("Unexpected query type: " + query.getClass());
			}
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

			if (checkOrder) {
				// also check the order in which solutions occur.
				queryResultTable.beforeFirst();
				expectedResultTable.beforeFirst();

				while (queryResultTable.hasNext()) {
					BindingSet bs = queryResultTable.next();
					BindingSet expectedBs = expectedResultTable.next();

					if (!bs.equals(expectedBs)) {
						resultsEqual = false;
						break;
					}
				}
			}
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

			if (checkOrder && missingBindings.isEmpty() && unexpectedBindings.isEmpty()) {
				message.append("Results are not in expected order.\n");
				message.append(" =======================\n");
				message.append("query result: \n");
				for (BindingSet bs : queryBindings) {
					message.append(bs);
					message.append("\n");
				}
				message.append(" =======================\n");
				message.append("expected result: \n");
				for (BindingSet bs : expectedBindings) {
					message.append(bs);
					message.append("\n");
				}
				message.append(" =======================\n");

			//	System.out.print(message.toString());
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

	private void compareGraphs(Set<Statement> queryResult, Set<Statement> expectedResult)
		throws Exception
	{
		if (!ModelUtil.equals(expectedResult, queryResult)) {
			// Don't use RepositoryUtil.difference, it reports incorrect diffs
			/*
			 * Collection<? extends Statement> unexpectedStatements =
			 * RepositoryUtil.difference(queryResult, expectedResult); Collection<?
			 * extends Statement> missingStatements =
			 * RepositoryUtil.difference(expectedResult, queryResult);
			 * StringBuilder message = new StringBuilder(128);
			 * message.append("\n=======Diff: "); message.append(getName());
			 * message.append("========================\n"); if
			 * (!unexpectedStatements.isEmpty()) { message.append("Unexpected
			 * statements in result: \n"); for (Statement st :
			 * unexpectedStatements) { message.append(st.toString());
			 * message.append("\n"); } message.append("============="); for (int i =
			 * 0; i < getName().length(); i++) { message.append("="); }
			 * message.append("========================\n"); } if
			 * (!missingStatements.isEmpty()) { message.append("Statements missing
			 * in result: \n"); for (Statement st : missingStatements) {
			 * message.append(st.toString()); message.append("\n"); }
			 * message.append("============="); for (int i = 0; i <
			 * getName().length(); i++) { message.append("="); }
			 * message.append("========================\n"); }
			 */
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

	private void uploadDataset(Dataset dataset)
		throws Exception
	{
		RepositoryConnection con = dataRep.getConnection();
		try {
			// Merge default and named graphs to filter duplicates
			Set<URI> graphURIs = new HashSet<URI>();
			graphURIs.addAll(dataset.getDefaultGraphs());
			graphURIs.addAll(dataset.getNamedGraphs());

			for (Resource graphURI : graphURIs) {
				upload(((URI)graphURI), graphURI);
			}
		}
		finally {
			con.close();
		}
	}

	private void upload(URI graphURI, Resource context)
		throws Exception
	{
		RepositoryConnection con = dataRep.getConnection();
		con.begin();
		try {
			RDFFormat rdfFormat = Rio.getParserFormatForFileName(graphURI.toString(), RDFFormat.TURTLE);
			RDFParser rdfParser = Rio.createParser(rdfFormat, dataRep.getValueFactory());
			ParserConfig config = rdfParser.getParserConfig();
			// To emulate DatatypeHandling.IGNORE 
			config.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
			config.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
			config.addNonFatalError(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);
//			config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
			
//			rdfParser.setVerifyData(false);
//			rdfParser.setDatatypeHandling(DatatypeHandling.IGNORE);
			// rdfParser.setPreserveBNodeIDs(true);

			RDFInserter rdfInserter = new RDFInserter(con);
			rdfInserter.enforceContext(context);
			rdfParser.setRDFHandler(rdfInserter);

			URL graphURL = new URL(graphURI.toString());
			InputStream in = graphURL.openStream();
			try {
				rdfParser.parse(in, graphURI.toString());
			}
			finally {
				in.close();
			}

			con.setAutoCommit(true);
		}
		finally {
			con.close();
		}
	}

	private String readQueryString()
		throws IOException
	{
		InputStream stream = new URL(queryFileURL).openStream();
		try {
			return IOUtil.readString(new InputStreamReader(stream, "UTF-8"));
		}
		finally {
			stream.close();
		}
	}

	private TupleQueryResult readExpectedTupleQueryResult()
		throws Exception
	{
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
			}
			finally {
				in.close();
			}
		}
		else {
			Set<Statement> resultGraph = readExpectedGraphQueryResult();
			return DAWGTestResultSetUtil.toTupleQueryResult(resultGraph);
		}
	}

	private boolean readExpectedBooleanQueryResult()
		throws Exception
	{
		BooleanQueryResultFormat bqrFormat = BooleanQueryResultParserRegistry.getInstance().getFileFormatForFileName(
				resultFileURL);

		if (bqrFormat != null) {
			InputStream in = new URL(resultFileURL).openStream();
			try {
				return QueryResultIO.parse(in, bqrFormat);
			}
			finally {
				in.close();
			}
		}
		else {
			Set<Statement> resultGraph = readExpectedGraphQueryResult();
			return DAWGTestResultSetUtil.toBooleanQueryResult(resultGraph);
		}
	}

	private Set<Statement> readExpectedGraphQueryResult()
		throws Exception
	{
		RDFFormat rdfFormat = Rio.getParserFormatForFileName(resultFileURL);

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

			Set<Statement> result = new LinkedHashSet<Statement>();
			parser.setRDFHandler(new StatementCollector(result));

			InputStream in = new URL(resultFileURL).openStream();
			try {
				parser.parse(in, resultFileURL);
			}
			finally {
				in.close();
			}

			return result;
		}
		else {
			throw new RuntimeException("Unable to determine file type of results file");
		}
	}

	public interface Factory {

		SPARQLQueryParent createSPARQLQueryTest(String testURI, String name, String queryFileURL,
				String resultFileURL, Dataset dataSet, boolean laxCardinality);

		SPARQLQueryParent createSPARQLQueryTest(String testURI, String name, String queryFileURL,
				String resultFileURL, Dataset dataSet, boolean laxCardinality, boolean checkOrder);
	}

	public static TestSuite suite(String manifestFileURL, Factory factory)
		throws Exception
	{
		return suite(manifestFileURL, factory, true);
	}

	public static TestSuite suite(String manifestFileURL, Factory factory, boolean approvedOnly)
		throws Exception
	{
		logger.info("Building test suite for {}", manifestFileURL);

		TestSuite suite = new TestSuite(factory.getClass().getName());

		// Read manifest and create declared test cases
		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.initialize();
		RepositoryConnection con = manifestRep.getConnection();

		QuestManifestTestUtils.addTurtle(con, new URL(manifestFileURL), manifestFileURL);

		suite.setName(getManifestName(manifestRep, con, manifestFileURL));

		// Extract test case information from the manifest file. Note that we only
		// select those test cases that are mentioned in the list.
		StringBuilder query = new StringBuilder(512);
		query.append(" SELECT DISTINCT testURI, testName, resultFile, action, queryFile, defaultGraph, ordered ");
		query.append(" FROM {} rdf:first {testURI} ");
		if (approvedOnly) {
			query.append("                          dawgt:approval {dawgt:Approved}; ");
		}
		query.append("                             mf:name {testName}; ");
		query.append("                             mf:result {resultFile}; ");
		query.append("                             [ mf:checkOrder {ordered} ]; ");
		query.append("                             [ mf:requires {Requirement} ];");
		query.append("                             mf:action {action} qt:query {queryFile}; ");
		query.append("                                               [qt:data {defaultGraph}]; ");
		query.append("                                               [sd:entailmentRegime {Regime} ]");

		// skip tests involving CSV result files, these are not query tests
		query.append(" WHERE NOT resultFile LIKE \"*.csv\" ");
		// skip tests involving JSON, sesame currently does not have a SPARQL/JSON
		// parser.
		query.append(" AND NOT resultFile LIKE \"*.srj\" ");
		// skip tests involving entailment regimes
		query.append(" AND NOT BOUND(Regime) ");
		// skip test involving basic federation, these are tested separately.
		query.append(" AND (NOT BOUND(Requirement) OR (Requirement != mf:BasicFederation)) ");
		query.append(" USING NAMESPACE ");
		query.append("  mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>, ");
		query.append("  dawgt = <http://www.w3.org/2001/sw/DataAccess/tests/test-dawg#>, ");
		query.append("  qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>, ");
		query.append("  sd = <http://www.w3.org/ns/sparql-service-description#>, ");
		query.append("  ent = <http://www.w3.org/ns/entailment/> ");
		TupleQuery testCaseQuery = con.prepareTupleQuery(QueryLanguage.SERQL, query.toString());

		query.setLength(0);
		query.append(" SELECT graph ");
		query.append(" FROM {action} qt:graphData {graph} ");
		query.append(" USING NAMESPACE ");
		query.append(" qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>");
		TupleQuery namedGraphsQuery = con.prepareTupleQuery(QueryLanguage.SERQL, query.toString());

		query.setLength(0);
		query.append("SELECT 1 ");
		query.append(" FROM {testURI} mf:resultCardinality {mf:LaxCardinality}");
		query.append(" USING NAMESPACE mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>");
		TupleQuery laxCardinalityQuery = con.prepareTupleQuery(QueryLanguage.SERQL, query.toString());

		logger.debug("evaluating query..");
		TupleQueryResult testCases = testCaseQuery.evaluate();
		while (testCases.hasNext()) {
			BindingSet bindingSet = testCases.next();

			URI testURI = (URI)bindingSet.getValue("testURI");
			String testName = bindingSet.getValue("testName").toString();
			String resultFile = bindingSet.getValue("resultFile").toString();
			String queryFile = bindingSet.getValue("queryFile").toString();
			URI defaultGraphURI = (URI)bindingSet.getValue("defaultGraph");
			Value action = bindingSet.getValue("action");
			Value ordered = bindingSet.getValue("ordered");

			logger.debug("found test case : {}", testName);

			// Query named graphs
			namedGraphsQuery.setBinding("action", action);
			TupleQueryResult namedGraphs = namedGraphsQuery.evaluate();

			DatasetImpl dataset = null;

			if (defaultGraphURI != null || namedGraphs.hasNext()) {
				dataset = new DatasetImpl();

				if (defaultGraphURI != null) {
					dataset.addDefaultGraph(defaultGraphURI);
				}

				while (namedGraphs.hasNext()) {
					BindingSet graphBindings = namedGraphs.next();
					URI namedGraphURI = (URI)graphBindings.getValue("graph");
					logger.debug(" adding named graph : {}", namedGraphURI);
					dataset.addNamedGraph(namedGraphURI);
				}
			}

			// Check for lax-cardinality conditions
			boolean laxCardinality = false;
			laxCardinalityQuery.setBinding("testURI", testURI);
			TupleQueryResult laxCardinalityResult = laxCardinalityQuery.evaluate();
			try {
				laxCardinality = laxCardinalityResult.hasNext();
			}
			finally {
				laxCardinalityResult.close();
			}

			// if this is enabled, Sesame passes all tests, showing that the only
			// difference is the semantics of arbitrary-length
			// paths
			/*
			if (!laxCardinality) {
				// property-path tests always with lax cardinality because Sesame filters out duplicates by design
				if (testURI.stringValue().contains("property-path")) {
					laxCardinality = true;
				}
			}
			*/

			// check if we should test for query result ordering
			boolean checkOrder = false;
			if (ordered != null) {
				checkOrder = Boolean.parseBoolean(ordered.stringValue());
			}

			SPARQLQueryParent test = factory.createSPARQLQueryTest(testURI.toString(), testName, queryFile,
					resultFile, dataset, laxCardinality, checkOrder);
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

	protected static String getManifestName(Repository manifestRep, RepositoryConnection con,
			String manifestFileURL)
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
