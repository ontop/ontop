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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sesameWrapper.SesameVirtualRepo;

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
	private static Set<String> IGNORE = Sets.newHashSet(
       "dg0001", "tc0001b", "dg0002", "tc0002b", "tc0002d", "tc0002f", "tc0002h", "dg0003", "tc0003a", "dg0004", "dg0005", "tc0005a", "tc0005b", "dg0006", "dg0007", "tc0007h",
       "dg0008", "dg0009", "tc0009a", "tc0009b", "tc0009d", "dg0010", "tc0010c", "dg0011", "dg0012", "tc0012a", "tc0012b", "tc0012e", "dg0013", "dg0014", "tc0014a", "tc0014b",
       "tc0014c", "dg0015", "tc0015b", "dg0016", "tc0016b", "tc0016c", "tc0016e", "dg0017", "dg0018", "tc0018a", "tc0019a", "tc0019b", "tc0020b", "dg0021", "dg0022", "dg0023",
       "dg0024", "dg0025"
	);

	private static List<String> FAILURES = Lists.newArrayList();

	private static final String BASE_URI = "http://example.com/base/";

	private static String LAST_SQL_SCRIPT = null;

	private static Connection SQL_CONN;

	private static ValueFactory FACTORY = ValueFactoryImpl.getInstance();

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

		java.sql.Statement s = SQL_CONN.createStatement();

		try {
			String text = Resources.toString(url(sqlFile), Charsets.UTF_8);
			s.execute(text);
		}
		catch (SQLException sqle) {
			System.out.println("Exception in creating db from script");
		}
		finally {
			s.close();
		}
	}

	protected Repository createRepository() throws Exception {
		String mappingURL = mappingFile == null ? null : url(mappingFile).toString();
		logger.info("RDB2RDFTest " + name + " " + mappingURL);
		// FIXME We should pass BASE_URI to the repo so it knows what base URI we want to use for mappings
		SesameVirtualRepo repo = new SesameVirtualRepo(name, mappingURL, false, "TreeWitness");
		repo.initialize();
		return repo;
	}

	protected static void clearDB() throws Exception {
		java.sql.Statement s = SQL_CONN.createStatement();
		try {
			s.execute("DROP ALL OBJECTS DELETE FILES");
		} catch (SQLException sqle) {
			System.out.println("Table not found, not dropping");
		} finally {
			s.close();
		}
	}

	@AfterClass
	public static void afterClass() throws Exception {
		System.out.println("RDB2RDF Summary");
		System.out.println("IGNORED " + IGNORE.size());
		System.out.println("FAILED " + FAILURES.size());
		if (!FAILURES.isEmpty()) {
			System.out.println("FAILURES: " + FAILURES);
		}

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
				expected = normalizeTypedLiterals(Rio.parse(stream(outputFile), BASE_URI, RDFFormat.NQUADS));
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
	 * RDB2RDF test suite expects "3.0E1"^^xsD:double in results but Quest returns "30"^^xsd:double which is
	 * semantically equivalent but syntactically different. We normalize those types here so they won't trigger
	 * a bigus failure.
	 */
	private Set<Statement> normalizeTypedLiterals(Iterable<Statement> statements) {
		TreeModel model = new TreeModel();
		for (Statement stmt : statements) {
			if (stmt.getObject() instanceof Literal) {
				Literal lit = (Literal) stmt.getObject();
				if (XMLSchema.DOUBLE.equals(lit.getDatatype())) {
					lit = FACTORY.createLiteral(lit.doubleValue());
					stmt = FACTORY.createStatement(stmt.getSubject(), stmt.getPredicate(), lit, stmt.getContext());
				}
			}

			model.add(stmt);
		}
		return model;
	}
}

