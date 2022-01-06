/*
 * #%L
 * ontop-rdb2rdf-compliance
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

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration.Builder;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import it.unibz.inf.ontop.spec.mapping.bootstrap.DirectMappingBootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.DirectMappingBootstrapper.BootstrappingResults;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Test suite for RDB2RDF tests.
 *
 * @author Evren Sirin (evren@complexible.com)
 */
@RunWith(Parameterized.class)
public class RDB2RDFTest {
	/**
	 * Following tests are failing due to various different reasons and bugs and are excluded manually.
	 */
	private static final Set<String> IGNORE = ImmutableSet.of(
			// Should reject an undefined SQL version
			"tc0003a",
			// Limitation of bnode isomorphism detection + xsd:double encoding (engineering notation was expected)
			"dg0005",
			// Limitation of bnode isomorphism detection
			"dg0005-modified",
			// Different XSD.DOUBLE lexical form; was expecting the engineering notation. Modified version added.
			"tc0005a",
			// Different XSD.DOUBLE lexical form; was expecting the engineering notation. Modified version added.
			"tc0005b",
			// Modified (different XSD.DOUBLE lexical form)
			"dg0012",
			// Direct mapping and bnodes: row unique ids are not considered, leadinq to incomplete results
			// (e.g. Bob-London should appear twice). TODO: fix it
			"dg0012-modified",
			// Modified (different XSD.DOUBLE lexical form)
			"tc0012a",
			// Modified (different XSD.DOUBLE lexical form)
			"tc0012e",
			// Double + timezone was not expected to be added. Same for milliseconds.
			"dg0016",
			// Different XSD.DOUBLE lexical form. Modified version added.
			"tc0016b",
			// Timezone was not expected to be added. Same for milliseconds (not so relevant test)
			"tc0016c",
			// H2 returns varbinary in lower case while upper case was expected. Modified test added.
			"tc0016e",
			// H2 does not store the implicit trailing spaces in CHAR(15) and does not output them.
			"dg0018",
			// H2 does not store the implicit trailing spaces in CHAR(15) and does not output them.
			"tc0018a",
			// Should create an IRI based on a column and the base IRI. TODO: support the base IRI in R2RML
			"tc0019a"
	);

	private static List<String> FAILURES = Lists.newArrayList();

	private static final String BASE_IRI = "http://example.com/base/";

	private static String LAST_SQL_SCRIPT = null;

	private static Connection SQL_CONN;

	private static ValueFactory FACTORY = SimpleValueFactory.getInstance();

	private static Properties PROPERTIES;

	private static final String JDBC_URL = "jdbc:h2:mem:questrepository";
	private static final String DB_USER = "sa";
	private static final String DB_PASSWORD = "";
	private static final String JDBC_DRIVER = "org.h2.Driver";

	/**
	 * Terms used in the manifest files of RDB2RDF test suite
	 */
	private static class TestVocabulary  {
		public static final String NS = "http://purl.org/NET/rdb2rdf-test#";

		public static final IRI SQL_SCRIPT_FILE = FACTORY.createIRI(NS, "sqlScriptFile");

		public static final IRI DIRECT_MAPPING = FACTORY.createIRI(NS, "DirectMapping");

		public static final IRI R2RML = FACTORY.createIRI(NS, "R2RML");

		public static final IRI MAPPING_DOCUMENT = FACTORY.createIRI(NS, "mappingDocument");

		public static final IRI OUTPUT = FACTORY.createIRI(NS, "output");
	}

	/**
	 * Returns the list of parameters created automatically from RDB2RDF manifest files
	 */
	@Parameterized.Parameters(name="{0}")
	public static Collection<Object[]> parameters() throws Exception {
		final List<Object[]> params = Lists.newArrayList();

		// There are 25 directories in the test suite so we'll iterate them all
		for (int i = 0; i < 26; i++) {
			final String dir = String.format("D%03d/", i);
			final String manifest = dir + "manifest.ttl";

			// simpler handler for manifest files that takes advantage of the fact that triples in
			// manifest files are ordered in a certain way (otherwise we'll get an explicit error)
            RDFHandler manifestHandler = new AbstractRDFHandler() {
				protected String name;
				protected String sqlFile;
				protected String mappingFile;
				protected String outputFile;

				@Override
				public void handleStatement(final Statement st) throws RDFHandlerException {
					IRI pred = st.getPredicate();
					// the first thing we'll see in the file is the SQL script file
					if (pred.equals(TestVocabulary.SQL_SCRIPT_FILE)) {
						// make sure there is a single SQL script in each manifest
						Preconditions.checkState(sqlFile == null, "Multiple SQL files defined");
						sqlFile = dir + st.getObject().stringValue();
					}
					else if (pred.equals(RDF.TYPE)) {
						Value aType = st.getObject();
						// definition for a new test is beginning
						if (aType.equals(TestVocabulary.DIRECT_MAPPING) || aType.equals(TestVocabulary.R2RML)) {
							// create parameter for the previous test case
							createTestCase();
							// reset state
							name = ((IRI) st.getSubject()).getLocalName();
							mappingFile = outputFile = null;
						}
					}
					else if (pred.equals(TestVocabulary.MAPPING_DOCUMENT)) {
						// record the mapping file
						mappingFile = dir + st.getObject().stringValue();
					}
					else if (pred.equals(TestVocabulary.OUTPUT)) {
						// record the output file
						outputFile = dir + st.getObject().stringValue();
					}
				}

				@Override
				public void endRDF() throws RDFHandlerException {
					// generate the parameter corresponding to the last test case
					createTestCase();
				}

				private void createTestCase() {
					if (name != null) {
						Preconditions.checkState(sqlFile != null, "SQL file not defined");
						params.add(new Object[] { name, sqlFile, mappingFile, outputFile });
					}
				}
			};

			// parse the manifest file
			RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
			parser.setRDFHandler(manifestHandler);
			parser.parse(stream(manifest), TestVocabulary.NS);
		}

		return params;
	}

	private static URL url(String path)  {
		return path == null ? null : RDB2RDFTest.class.getResource(path);
	}

	private static InputStream stream(String path) {
		return RDB2RDFTest.class.getResourceAsStream(path);
	}

	protected final String sqlFile;
	protected final String mappingFile;
	protected final String outputFile;
	protected final String name;
	static final Logger logger = LoggerFactory.getLogger(RDB2RDFTest.class);
	
	public RDB2RDFTest(String name, String sqlFile, String mappingFile, String outputFile) throws FileNotFoundException {
		this.name = name;
		this.sqlFile = sqlFile;
		this.mappingFile = mappingFile;
		this.outputFile =  outputFile;
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		SQL_CONN = DriverManager.getConnection("jdbc:h2:mem:questrepository","sa", "");
		//Server.startWebServer(SQL_CONN);

		PROPERTIES = new Properties();

		PROPERTIES.setProperty(OntopSQLCredentialSettings.JDBC_USER, DB_USER);
		PROPERTIES.setProperty(OntopSQLCredentialSettings.JDBC_PASSWORD, DB_PASSWORD);
		PROPERTIES.setProperty(OntopSQLCoreSettings.JDBC_URL, JDBC_URL);
		PROPERTIES.setProperty(OntopSQLCoreSettings.JDBC_DRIVER, JDBC_DRIVER);
		PROPERTIES.setProperty(OntopMappingSettings.BASE_IRI, BASE_IRI);
		PROPERTIES.setProperty(OntopOBDASettings.ALLOW_RETRIEVING_BLACK_BOX_VIEW_METADATA_FROM_DB, "true");
	}

	@Before
	public void beforeTest() throws Exception {
		// Several tests use the same backing database so no need to recreate the database if the previous test already did so
		if (Objects.equal(LAST_SQL_SCRIPT, sqlFile)) {
			return;
		}

		// new test so clear the database first
		if (LAST_SQL_SCRIPT != null) {
			clearDB();
		}

		LAST_SQL_SCRIPT = sqlFile;

        try (java.sql.Statement s = SQL_CONN.createStatement()) {
            String text = Resources.toString(url(sqlFile), Charsets.UTF_8);
            s.execute(text);
        } catch (SQLException sqle) {
            System.out.println("Exception in creating db from script");
        }
	}

	protected Repository createRepository() throws Exception {
		logger.info("RDB2RDFTest " + name + " " + mappingFile);

		OntopSQLOWLAPIConfiguration configuration;
		if (mappingFile != null) {
			String absoluteFilePath = Optional.ofNullable(getClass().getResource(mappingFile))
					.map(URL::getFile)
					.orElseThrow(() -> new IllegalArgumentException("The mappingFile " + mappingFile
							+ " has not been found"));
			configuration = createStandardConfigurationBuilder()
					.r2rmlMappingFile(absoluteFilePath)
					.build();
		}
		else {
			configuration = bootstrapDMConfiguration();
		}
		OntopRepository repo = OntopRepository.defaultRepository(configuration);
		repo.initialize();
		return repo;
	}

	Builder<? extends Builder<?>> createStandardConfigurationBuilder() {
		  return OntopSQLOWLAPIConfiguration.defaultBuilder()
				 .properties(PROPERTIES)
				  .enableDefaultDatatypeInference(true);
	}

	Builder<? extends Builder<?>> createInMemoryBuilder() {
		return createStandardConfigurationBuilder()
				.jdbcUrl(JDBC_URL)
				.jdbcDriver(JDBC_DRIVER)
				.jdbcUser(DB_USER)
				.jdbcPassword(DB_PASSWORD)
				.enableDefaultDatatypeInference(true)
				.enableTestMode();
	}

	/**
	 * Bootstraps the mapping and returns a new configuration
	 */
	OntopSQLOWLAPIConfiguration bootstrapDMConfiguration()
			throws OWLOntologyCreationException, MappingException, MappingBootstrappingException {

		OntopSQLOWLAPIConfiguration initialConfiguration = createInMemoryBuilder().build();
		DirectMappingBootstrapper bootstrapper = DirectMappingBootstrapper.defaultBootstrapper();
		BootstrappingResults results = bootstrapper.bootstrap(initialConfiguration, BASE_IRI);

		SQLPPMapping bootstrappedMapping = results.getPPMapping();

		return createInMemoryBuilder()
				.ppMapping(bootstrappedMapping)
				.build();
	}

	protected static void clearDB()  {
        try (java.sql.Statement s = SQL_CONN.createStatement()) {
            s.execute("DROP ALL OBJECTS DELETE FILES");
        } catch (SQLException sqle) {
            System.out.println("Table not found, not dropping");
        }
	}

	@AfterClass
	public static void afterClass() throws Exception {
		System.out.println("RDB2RDF Summary");
		System.out.println("IGNORED " + IGNORE.size() + " " + IGNORE);
		System.out.println("FAILED " + FAILURES.size() + " " + FAILURES);

		try {
			clearDB();
		}
		finally {
			SQL_CONN.close();
		}
	}

	@Test
	public void runTest() throws Exception {
		assumeTrue(!IGNORE.contains(name));

		try {
 			runTestWithoutIgnores();
		}
		catch (Throwable e) {
			FAILURES.add('"' + name + '"');
			throw e;
		}
	}

	private void runTestWithoutIgnores() throws Exception {
		boolean outputExpected = (outputFile != null);

		Repository dataRep;

		try {
			dataRep = createRepository();
		}
		catch (Exception e) {
			// if the test is for invalid mappings then there won't be any expected output and exception here is fine
			// otherwise it is a test failure
			if (outputExpected) {
				e.printStackTrace();
				fail("Error creating repository: " + e.getMessage());
			}
			return;
		}


		try (RepositoryConnection con = dataRep.getConnection()) {
			String tripleQuery = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
			GraphQuery gquery = con.prepareGraphQuery(QueryLanguage.SPARQL, tripleQuery);
			Set<Statement> triples = QueryResults.asSet(gquery.evaluate());

			TupleQuery namedGraphQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
					"SELECT DISTINCT ?g WHERE { GRAPH ?g {?s ?p ?o } }");
			ImmutableSet<Resource> namedGraphs = QueryResults.asSet(namedGraphQuery.evaluate()).stream()
					.map(bs -> bs.getBinding("g"))
					.map(Binding::getValue)
					.map(v -> (Resource) v)
					.collect(ImmutableCollectors.toSet());

			String quadQuery = "CONSTRUCT {?s ?p ?o} WHERE { GRAPH ?g {?s ?p ?o} }";
			Set<Statement> actual = new HashSet<>(triples);
			for (Resource namedGraph : namedGraphs) {
				GraphQuery query = con.prepareGraphQuery(quadQuery);
				query.setBinding("g", namedGraph);
				QueryResults.asSet(query.evaluate()).stream()
						.map(s -> FACTORY.createStatement(s.getSubject(), s.getPredicate(), s.getObject(), namedGraph))
						.forEach(actual::add);
			}

			Set<Statement> expected = ImmutableSet.of();
			if (outputExpected) {
				expected = Rio.parse(stream(outputFile), BASE_IRI, Rio.getParserFormatForFileName(outputFile).get());
			}

			if (!Models.isomorphic(expected, actual)) {
				String msg = failureMessage(expected, actual);
				System.out.println(msg);

				fail(msg);
			}
		}
		catch (QueryEvaluationException e) {
			if (e.getCause() != null && e.getCause() instanceof OntopResultConversionException) {
				if (outputExpected) {
					e.printStackTrace();
					fail("Unexpected result conversion exception: " + e.getMessage());
				}
			}
			else {
				fail("Unexpected exception: " + e.getMessage());
			}
		}
		finally {
			dataRep.shutDown();
		}
	}

	/**
	 * Pretty print expected and actual results
	 */
	private String failureMessage(Set<Statement> expected, Set<Statement> actual) throws RDFHandlerException {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Failed Test: " + name);
		pw.println("========== Expected =============");
		Rio.write(expected, pw, RDFFormat.NQUADS);
		pw.println("========== Actual ===============");
		Rio.write(actual, pw, RDFFormat.NQUADS);
		pw.flush();
		return sw.toString();
	}
}

