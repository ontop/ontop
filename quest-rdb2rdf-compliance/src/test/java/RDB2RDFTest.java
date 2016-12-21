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

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import it.unibz.inf.ontop.injection.OBDAProperties;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.RDBMSourceParameterConstants;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCorePreferences;
import it.unibz.inf.ontop.sesame.SesameVirtualRepo;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.model.util.ModelUtil;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.RDFHandlerBase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
	private static Set<String> IGNORE = ImmutableSet.of(
		"tc0002f", "tc0002h", "tc0003a", "dg0005", "tc0007h", "tc0009a", "tc0009b", "tc0009d", "dg0010", "tc0010c", "dg0012",
		"dg0014", "tc0014b", "tc0014c", "tc0015b", "dg0016", "tc0016c", "tc0016e", "dg0017", "dg0018", "tc0018a", "tc0019a",
		"tc0019b", "tc0020b", "dg0025"
	);

	private static List<String> FAILURES = Lists.newArrayList();

	private static final String BASE_IRI = "http://example.com/base/";

	private static String LAST_SQL_SCRIPT = null;

	private static Connection SQL_CONN;

	private static ValueFactory FACTORY = ValueFactoryImpl.getInstance();

	private static OWLOntology EMPTY_ONT;
	private static Properties PROPERTIES;
	private static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

	private static final String JDBC_URL = "jdbc:h2:mem:questrepository";
	private static final String DB_USER = "sa";
	private static final String DB_PASSWORD = "";
	private static final String JDBC_DRIVER = "org.h2.Driver";

	/**
	 * Terms used in the manifest files of RDB2RDF test suite
	 */
	private static class TestVocabulary  {
		public static final String NS = "http://purl.org/NET/rdb2rdf-test#";

		public static final URI SQL_SCRIPT_FILE = FACTORY.createURI(NS, "sqlScriptFile");

		public static final URI DIRECT_MAPPING = FACTORY.createURI(NS, "DirectMapping");

		public static final URI R2RML = FACTORY.createURI(NS, "R2RML");

		public static final URI MAPPING_DOCUMENT = FACTORY.createURI(NS, "mappingDocument");

		public static final URI OUTPUT = FACTORY.createURI(NS, "output");
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
			RDFHandlerBase manifestHandler = new RDFHandlerBase() {
				protected String name;
				protected String sqlFile;
				protected String mappingFile;
				protected String outputFile;

				@Override
				public void handleStatement(final Statement st) throws RDFHandlerException {
					URI pred = st.getPredicate();
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
							name = ((URI) st.getSubject()).getLocalName();
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

	private static URL url(String path) throws IOException {
		return path == null ? null : RDB2RDFTest.class.getResource(path);
	}

	private static InputStream stream(String path) throws IOException {
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

		EMPTY_ONT = OWLManager.createOWLOntologyManager().createOntology();

		PROPERTIES = new Properties();

		PROPERTIES.setProperty(OBDAProperties.DB_NAME, "h2");
		PROPERTIES.setProperty(OBDAProperties.DB_USER, DB_USER);
		PROPERTIES.setProperty(OBDAProperties.DB_PASSWORD, DB_PASSWORD);
		PROPERTIES.setProperty(OBDAProperties.JDBC_URL, JDBC_URL);
		PROPERTIES.setProperty(OBDAProperties.JDBC_DRIVER, JDBC_DRIVER);
		PROPERTIES.setProperty(QuestCorePreferences.BASE_IRI, BASE_IRI);
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

		QuestConfiguration.Builder configBuilder = QuestConfiguration.defaultBuilder()
				.properties(PROPERTIES)
				.ontology(EMPTY_ONT);
		if (mappingFile != null) {
			String absoluteFilePath = Optional.ofNullable(getClass().getResource(mappingFile))
					.map(URL::getFile)
					.orElseThrow(() -> new IllegalArgumentException("The mappingFile " + mappingFile
							+ " has not been found"));
			configBuilder.r2rmlMappingFile(absoluteFilePath);
		}
		else {
			configBuilder.bootstrapMapping(getMemOBDADataSource(), BASE_IRI);
		}
		SesameVirtualRepo repo = new SesameVirtualRepo(name, configBuilder.build());
		repo.initialize();
		return repo;
	}

	private static OBDADataSource getMemOBDADataSource() {

		OBDADataSource obdaSource = DATA_FACTORY.getDataSource(java.net.URI.create("http://www.obda.org/ABOXDUMP" + System.currentTimeMillis()));
		obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, JDBC_DRIVER);
		obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, DB_PASSWORD);
		obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, JDBC_URL);
		obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, DB_USER);
		obdaSource.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		obdaSource.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		return obdaSource;
	}

	protected static void clearDB() throws Exception {
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


		RepositoryConnection con = null;
		try {
			con = dataRep.getConnection();
			String graphq = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
			GraphQuery gquery = con.prepareGraphQuery(QueryLanguage.SPARQL, graphq);

			Set<Statement> actual = QueryResults.asSet(gquery.evaluate());

			Set<Statement> expected = ImmutableSet.of();
			if (outputExpected) {
				expected = stripNamedGraphs(Rio.parse(stream(outputFile), BASE_IRI, Rio.getParserFormatForFileName(outputFile).get()));
			}

			if (!ModelUtil.equals(expected, actual)) {
				String msg = failureMessage(expected, actual);
				System.out.println(msg);

				fail(msg);
			}
		}
		finally {
			if (con != null) {
				con.close();
			}
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

	/**
	 * Remove named graphs from expected answers since they are not supported
	 */
	private Set<Statement> stripNamedGraphs(Iterable<Statement> statements) {
		Set<Statement> model = Sets.newHashSet();
		for (Statement stmt : statements) {
			if (stmt.getContext() != null) {
				stmt = FACTORY.createStatement(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
			}

			model.add(stmt);
		}
		return model;
	}
}

