package it.unibz.inf.ontop.test.sparql;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.eclipse.rdf4j.OpenRDFUtil;
import org.eclipse.rdf4j.common.io.FileUtil;
import org.eclipse.rdf4j.common.io.ZipUtil;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.RDFInserter;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarFile;

public class ManifestTestUtils {

	static final Logger LOGGER = LoggerFactory.getLogger(ManifestTestUtils.class);

	public static Collection<Object[]> parametersFromSuperManifest(String manifestFilePath,
																   ImmutableSet<String> ignoredTests) throws Exception {
		URL url = ManifestTestUtils.class.getResource(manifestFilePath);

		if (url == null)
			throw new RuntimeException("Could not find the resource file " + manifestFilePath
					+ ".\nPlease make sure resources have been generated");

		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.initialize();
		RepositoryConnection con = manifestRep.getConnection();

		String manifestFile = url.toString();
		addTurtle(con, url, manifestFile);

		String query = "SELECT DISTINCT manifestFile FROM {x} rdf:first {manifestFile} "
				+ "USING NAMESPACE mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>, "
				+ "  qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>";

		TupleQueryResult manifestResults = con.prepareTupleQuery(QueryLanguage.SERQL, query, manifestFile).evaluate();
		List<Object[]> testCaseParameters = Lists.newArrayList();
		while (manifestResults.hasNext()) {
			BindingSet bindingSet = manifestResults.next();
			String subManifestFile = bindingSet.getValue("manifestFile").toString();
			testCaseParameters.addAll(parametersFromSubManifest(subManifestFile, true, ignoredTests));
		}

		manifestResults.close();
		con.close();
		manifestRep.shutDown();

		LOGGER.info("Created aggregated test suite with " + testCaseParameters.size() + " test cases.");
		return testCaseParameters;
	}

	public static Collection<Object[]> parametersFromSubManifest(String manifestFileURL, boolean approvedOnly, ImmutableSet<String> ignoredTests)
			throws Exception
	{
		LOGGER.info("Building test suite for {}", manifestFileURL);

		List<Object[]> testCaseParameters = Lists.newArrayList();

		// Read manifest and create declared test cases
		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.initialize();
		RepositoryConnection con = manifestRep.getConnection();

		ManifestTestUtils.addTurtle(con, new URL(manifestFileURL), manifestFileURL);

		String manifestName = getManifestName(manifestRep, con, manifestFileURL);

		// Extract test case information from the manifest file. Note that we only
		// select those test cases that are mentioned in the list.
		StringBuilder query = new StringBuilder(512);
		query.append(" SELECT DISTINCT testIRI, testName, resultFile, action, queryFile, defaultGraph, ordered ");
		query.append(" FROM {} rdf:first {testIRI} ");
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
		query.append(" FROM {testIRI} mf:resultCardinality {mf:LaxCardinality}");
		query.append(" USING NAMESPACE mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>");
		TupleQuery laxCardinalityQuery = con.prepareTupleQuery(QueryLanguage.SERQL, query.toString());

		LOGGER.debug("evaluating query..");
		TupleQueryResult testCases = testCaseQuery.evaluate();
		while (testCases.hasNext()) {
			BindingSet bindingSet = testCases.next();

			IRI testIRI = (IRI)bindingSet.getValue("testIRI");
			String testLocalName =  bindingSet.getValue("testName").toString();
			String testName = manifestName + "-" + testLocalName;
			String resultFile = bindingSet.getValue("resultFile").toString();
			String queryFile = bindingSet.getValue("queryFile").toString();
			IRI defaultGraphIRI = (IRI)bindingSet.getValue("defaultGraph");
			Value action = bindingSet.getValue("action");
			Value ordered = bindingSet.getValue("ordered");

			LOGGER.debug("found test case : {}", testName);

			// Query named graphs
			namedGraphsQuery.setBinding("action", action);
			TupleQueryResult namedGraphs = namedGraphsQuery.evaluate();

			SimpleDataset dataset = null;

			if (defaultGraphIRI != null || namedGraphs.hasNext()) {
				dataset = new SimpleDataset();

				if (defaultGraphIRI != null) {
					dataset.addDefaultGraph(defaultGraphIRI);
				}

				while (namedGraphs.hasNext()) {
					BindingSet graphBindings = namedGraphs.next();
					IRI namedGraphIRI = (IRI)graphBindings.getValue("graph");
					LOGGER.debug(" adding named graph : {}", namedGraphIRI);
					dataset.addNamedGraph(namedGraphIRI);
				}
			}

			// Check for lax-cardinality conditions
			boolean laxCardinality ;
			laxCardinalityQuery.setBinding("testIRI", testIRI);
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
				if (testIRI.stringValue().contains("property-path")) {
					laxCardinality = true;
				}
			}
			*/

			// check if we should test for query result ordering
			boolean checkOrder = false;
			if (ordered != null) {
				checkOrder = Boolean.parseBoolean(ordered.stringValue());
			}

			testCaseParameters.add(
					new Object[] {testIRI.toString(), testName, queryFile,
							resultFile, dataset, laxCardinality, checkOrder, ignoredTests});
		}

		testCases.close();
		con.close();

		manifestRep.shutDown();
		return testCaseParameters;
	}

	protected static String getManifestName(Repository manifestRep, RepositoryConnection con,
											String manifestFileURL)
			throws QueryEvaluationException, RepositoryException, MalformedQueryException
	{
		// Try to extract suite name from manifest file
		TupleQuery manifestNameQuery = con.prepareTupleQuery(QueryLanguage.SERQL,
				"SELECT ManifestName FROM {ManifestURL} rdfs:label {ManifestName}");
		manifestNameQuery.setBinding("ManifestURL", manifestRep.getValueFactory().createIRI(manifestFileURL));
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

	static void addTurtle(RepositoryConnection con, URL url, String baseURI, Resource... contexts)
		throws IOException, RepositoryException, RDFParseException
	{
		if (baseURI == null) {
			baseURI = url.toExternalForm();
		}

		InputStream in = url.openStream();

		try {
			OpenRDFUtil.verifyContextNotNull(contexts);
			final ValueFactory vf = con.getRepository().getValueFactory();
			RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE, vf);

			ParserConfig config = rdfParser.getParserConfig();
			// To emulate DatatypeHandling.IGNORE 
			config.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
			config.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
			config.addNonFatalError(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);
//			config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
//			rdfParser.setVerifyData(false);
//			rdfParser.setStopAtFirstError(true);
//			rdfParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

			RDFInserter rdfInserter = new RDFInserter(con);
			rdfInserter.enforceContext(contexts);
			rdfParser.setRDFHandler(rdfInserter);

			con.begin();

			try {
				rdfParser.parse(in, baseURI);
			}
			catch (RDFHandlerException e) {
					con.rollback();
				// RDFInserter only throws wrapped RepositoryExceptions
				throw (RepositoryException)e.getCause();
			}
			catch (RuntimeException e) {
					con.rollback();
				throw e;
			}
			finally {
				con.commit();
			}
		}
		finally {
			in.close();
		}
	}
}
