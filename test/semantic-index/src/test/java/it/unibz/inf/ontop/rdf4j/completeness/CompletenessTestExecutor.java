package it.unibz.inf.ontop.rdf4j.completeness;

import org.eclipse.rdf4j.common.io.IOUtil;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.common.text.StringUtil;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.dawg.DAWGTestResultSetUtil;
import org.eclipse.rdf4j.query.impl.MutableTupleQueryResult;
import org.eclipse.rdf4j.query.impl.TupleQueryResultBuilder;
import org.eclipse.rdf4j.query.resultio.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CompletenessTestExecutor {

	protected final String testId;
	protected final String queryFile;
	protected final String resultFile;

	protected final boolean laxCardinality = false;

	static final Logger logger = LoggerFactory.getLogger(CompletenessTestExecutor.class);
	private final String name;
	private final Repository repository;

	public CompletenessTestExecutor(String tid, String name, String resf, String sparqlf,
									Repository repository) {
		testId = tid;
		resultFile = resf;
		queryFile = sparqlf;
		this.name = name;
		this.repository = repository;
	}

	public void runTest() throws Exception {
		logger.info("\n\n\n============== " + testId + " ==============\n");
		try (RepositoryConnection con = repository.getConnection()) {
			String queryString = readQueryString();
			Query query = con.prepareQuery(QueryLanguage.SPARQL, queryString, queryFile);
			if (query instanceof TupleQuery) {
				TupleQueryResult queryResult = ((TupleQuery) query).evaluate();
				TupleQueryResult expectedResult = readExpectedTupleQueryResult(repository);
				compareTupleQueryResults(queryResult, expectedResult);
			} else if (query instanceof GraphQuery) {
				GraphQueryResult gqr = ((GraphQuery) query).evaluate();
				Set<Statement> queryResult = Iterations.asSet(gqr);
				Set<Statement> expectedResult = readExpectedGraphQueryResult(repository);
				compareGraphs(queryResult, expectedResult);
			} else if (query instanceof BooleanQuery) {
				boolean queryResult = ((BooleanQuery)query).evaluate();
				boolean expectedResult = readExpectedBooleanQueryResult(repository);
				assertEquals(expectedResult, queryResult);
			} else {
				throw new RuntimeException("Unexpected query type: " + query.getClass());
			}
		}
	}

	private void compareTupleQueryResults(TupleQueryResult queryResult, TupleQueryResult expectedResult)
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

				List<BindingSet> missingBindings = new ArrayList<>(expectedBindings);
				missingBindings.removeAll(queryBindings);

				List<BindingSet> unexpectedBindings = new ArrayList<>(queryBindings);
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

	private String getName() {
		return name;
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
		return matchBindingSets(queryResult1, queryResult2, new HashMap<>(), 0);
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
				Map<BNode, BNode> newBNodeMapping = new HashMap<>(bNodeMapping);

				for (Binding binding : bs1) {
					if (binding.getValue() instanceof BNode) {
						newBNodeMapping.put((BNode)binding.getValue(), (BNode)bs2.getValue(binding.getName()));
					}
				}

				// FIXME: this recursive implementation has a high risk of
				// triggering a stack overflow

				// Enter recursion
				result = matchBindingSets(queryResult1, queryResult2, newBNodeMapping, idx + 1);

				if (result) {
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
		List<BindingSet> result = new ArrayList<>();

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

					IRI dt1 = leftLit.getDatatype();
					IRI dt2 = rightLit.getDatatype();

					if (dt1 != null && dt1.equals(dt2)
							&& XMLDatatypeUtil.isValidValue(leftLit.getLabel(), dt1)
							&& XMLDatatypeUtil.isValidValue(rightLit.getLabel(), dt2))
					{
						Integer compareResult = null;
						if (dt1.equals(XSD.DOUBLE)) {
							compareResult = Double.compare(leftLit.doubleValue(), rightLit.doubleValue());
						}
						else if (dt1.equals(XSD.FLOAT)) {
							compareResult = Float.compare(leftLit.floatValue(), rightLit.floatValue());
						}
						else if (dt1.equals(XSD.DECIMAL)) {
							compareResult = leftLit.decimalValue().compareTo(rightLit.decimalValue());
						}
						else if (XMLDatatypeUtil.isIntegerDatatype(dt1)) {
							compareResult = leftLit.integerValue().compareTo(rightLit.integerValue());
						}
						else if (dt1.equals(XSD.BOOLEAN)) {
							Boolean leftBool = leftLit.booleanValue();
							Boolean rightBool = rightLit.booleanValue();
							compareResult = leftBool.compareTo(rightBool);
						}
						else if (XMLDatatypeUtil.isCalendarDatatype(dt1)) {
							XMLGregorianCalendar left = leftLit.calendarValue();
							XMLGregorianCalendar right = rightLit.calendarValue();

							compareResult = left.compare(right);
						}

						if (compareResult != null) {
							if (compareResult != 0) {
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

	private void compareGraphs(Set<Statement> queryResult, Set<Statement> expectedResult) {
		if (!Models.isomorphic(expectedResult, queryResult)) {
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
		try (InputStream stream = new URL(queryFile).openStream()) {
			return IOUtil.readString(new InputStreamReader(stream, StandardCharsets.UTF_8));
		}
	}

	private TupleQueryResult readExpectedTupleQueryResult(Repository repository) throws Exception {
		Optional<QueryResultFormat> tqrFormat = QueryResultIO.getParserFormatForFileName(resultFile);
		if (tqrFormat.isPresent()) {
			try (InputStream in = new URL(resultFile).openStream()) {
				TupleQueryResultParser parser = QueryResultIO.createTupleParser(tqrFormat.get());
				parser.setValueFactory(repository.getValueFactory());

				TupleQueryResultBuilder qrBuilder = new TupleQueryResultBuilder();
				parser.setTupleQueryResultHandler(qrBuilder);

				parser.parse(in);
				return qrBuilder.getQueryResult();
			}
		} else {
			Set<Statement> resultGraph = readExpectedGraphQueryResult(repository);
			return DAWGTestResultSetUtil.toTupleQueryResult(resultGraph);
		}
	}

	private Set<Statement> readExpectedGraphQueryResult(Repository repository) throws Exception {
		Optional<RDFFormat> rdfFormat = Rio.getParserFormatForFileName(resultFile);
		if (rdfFormat.isPresent()) {
			RDFParser parser = Rio.createParser(rdfFormat.get(), repository.getValueFactory());
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

			try (InputStream in = new URL(resultFile).openStream()) {
				parser.parse(in, resultFile);
			}
			return result;
		} else {
			throw new RuntimeException("Unable to determine file type of results file");
		}
	}

	private boolean readExpectedBooleanQueryResult(Repository repository) throws Exception {
		Optional<QueryResultFormat> bqrFormat = BooleanQueryResultParserRegistry
				.getInstance().getFileFormatForFileName(resultFile);

		if (bqrFormat.isPresent()) {
            try (InputStream in = new URL(resultFile).openStream()) {
                return QueryResultIO.parseBoolean(in, bqrFormat.get());
            }
		} else {
			Set<Statement> resultGraph = readExpectedGraphQueryResult(repository);
			return DAWGTestResultSetUtil.toBooleanQueryResult(resultGraph);
		}
	}


}
