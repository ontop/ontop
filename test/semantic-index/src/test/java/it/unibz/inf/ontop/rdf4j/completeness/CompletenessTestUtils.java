package it.unibz.inf.ontop.rdf4j.completeness;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.eclipse.rdf4j.common.io.FileUtil;
import org.eclipse.rdf4j.common.io.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarFile;

import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.eclipse.rdf4j.OpenRDFUtil;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.RDFInserter;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompletenessTestUtils {
	
	static final Logger LOGGER = LoggerFactory.getLogger(CompletenessTestUtils.class);

	public static Collection<Object[]> parametersFromSuperManifest(String manifestFilePath,
																   ImmutableSet<String> ignoredTests) throws Exception {
		URL url = CompletenessTestUtils.class.getResource(manifestFilePath);

		if (url == null)
			throw new RuntimeException("Could not find the resource file " + manifestFilePath
					+ ".\nPlease make sure resources have been generated");

		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.initialize();
		RepositoryConnection con = manifestRep.getConnection();

		String manifestFile = url.toString();
		addTurtle(con, url, manifestFile);

		String query = "SELECT DISTINCT manifestFile FROM {x} rdf:first {manifestFile} "
				+ "USING NAMESPACE mf = <http://obda.org/quest/tests/test-manifest#>, "
				+ "  qt = <http://obda.org/quest/tests/test-query#>";

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

	public static Collection<Object[]> parametersFromSubManifest(String manifestFileURL,
																 boolean approvedOnly,
																 ImmutableSet<String> ignoredTests) throws Exception {
		LOGGER.info("Building test suite for {}", manifestFileURL);

		// Read manifest and create declared test cases
		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.initialize();
		RepositoryConnection con = manifestRep.getConnection();

		CompletenessTestUtils.addTurtle(con, new URL(manifestFileURL), manifestFileURL);

		String manifestName = getManifestName(manifestRep, con, manifestFileURL);

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

		LOGGER.debug("Evaluating query..");
		TupleQueryResult testCases = testCaseQuery.evaluate();
		List<Object[]> testCaseParameters = Lists.newArrayList();
		while (testCases.hasNext()) {
			BindingSet bindingSet = testCases.next();

			String testId = bindingSet.getValue("tid").toString();
			String testLocalName = bindingSet.getValue("name").toString();
			String resultFile = bindingSet.getValue("resf").toString();
			String parameterFile = bindingSet.getValue("propf").toString();
			String ontologyFile = bindingSet.getValue("owlf").toString();
			String queryFile = bindingSet.getValue("sparqlf").toString();

			String testName = manifestName + "-" + testLocalName;

			LOGGER.debug("Found test case: {}", testName);

			testCaseParameters.add(
					new Object[] {testId, testName, resultFile, parameterFile, ontologyFile, queryFile, ignoredTests});
		}

		testCases.close();
		con.close();

		manifestRep.shutDown();
		return testCaseParameters;
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

	static void addTurtle(RepositoryConnection con, URL url, String baseURI, Resource... contexts)
			throws IOException, RepositoryException, RDFParseException {
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
			} catch (RDFHandlerException e) {
					con.rollback();
				// RDFInserter only throws wrapped RepositoryExceptions
				throw (RepositoryException) e.getCause();
			} catch (RuntimeException e) {
					con.rollback();
				throw e;
			} finally {
				con.commit();
			}
		} finally {
			in.close();
		}
	}
}
