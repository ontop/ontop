package it.unibz.inf.ontop.docker.datatypes;

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

import it.unibz.inf.ontop.docker.ScenarioManifestTestUtils;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.eclipse.rdf4j.common.io.FileUtil;
import org.eclipse.rdf4j.common.io.ZipUtil;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
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
import java.nio.file.Files;
import java.util.jar.JarFile;

public class QuestDatatypeTestUtils {

	static final Logger logger = LoggerFactory.getLogger(QuestDatatypeTestUtils.class);

	public static TestSuite suite(QuestDatatypeParent.Factory factory) throws Exception
	{
		final String manifestFile;
		final File tmpDir;
		
		URL url = ScenarioManifestTestUtils.class.getResource(factory.getMainManifestFile());
			
		if ("jar".equals(url.getProtocol())) {
			// Extract manifest files to a temporary directory
			try {
				tmpDir = Files.createTempDirectory("datatype-evaluation").toFile();

				JarURLConnection con = (JarURLConnection) url.openConnection();
				JarFile jar = con.getJarFile();

				ZipUtil.extract(jar, tmpDir);

				File localFile = new File(tmpDir, con.getEntryName());
				manifestFile = localFile.toURI().toURL().toString();
			} catch (IOException e) {
				throw new AssertionError(e);
			}
		} else {
			manifestFile = url.toString();
			tmpDir = null;
		}		
		
		TestSuite suite = new TestSuite(factory.getClass().getName()) {

			@Override
			public void run(TestResult result) {
				try {
					super.run(result);
				}
				finally {
					if (tmpDir != null) {
						try {
							FileUtil.deleteDir(tmpDir);
						}
						catch (IOException e) {
							System.err.println("Unable to clean up temporary directory '" + tmpDir + "': " + e.getMessage());
						}
					}
				}
			}
		};

		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.init();
		RepositoryConnection con = manifestRep.getConnection();

		addTurtle(con, new URL(manifestFile), manifestFile);

		String query = "PREFIX mf: <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> \n"
				+ "PREFIX qt: <http://www.w3.org/2001/sw/DataAccess/tests/test-query#> \n"
				+ "SELECT DISTINCT ?manifestFile WHERE { ?x rdf:first ?manifestFile . } ";

		TupleQueryResult manifestResults = con.prepareTupleQuery(QueryLanguage.SPARQL, query, manifestFile).evaluate();

		while (manifestResults.hasNext()) {
			BindingSet bindingSet = manifestResults.next();
			String subManifestFile = bindingSet.getValue("manifestFile").toString();
			suite.addTest(QuestDatatypeParent.suite(subManifestFile, factory));
		}

		manifestResults.close();
		con.close();
		manifestRep.shutDown();

		logger.info("Created aggregated test suite with " + suite.countTestCases() + " test cases.");
		return suite;
	}

	static void addTurtle(RepositoryConnection con, URL url, String baseURI, Resource... contexts)
		throws IOException, RepositoryException, RDFParseException
	{
		if (baseURI == null) {
			baseURI = url.toExternalForm();
		}

		try (InputStream in = url.openStream()) {
			final ValueFactory vf = con.getRepository().getValueFactory();
			RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE, vf);
			ParserConfig config = rdfParser.getParserConfig();
			// To emulate DatatypeHandling.IGNORE
			config.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
			config.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
			config.addNonFatalError(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);
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
		}
	}
}
