package it.unibz.inf.ontop.rdf4j.completeness;

/*
 * #%L
 * ontop-quest-sesame
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

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import junit.framework.TestCase;
import org.eclipse.rdf4j.repository.Repository;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;

import static it.unibz.inf.ontop.rdf4j.completeness.CompletenessTestUtils.parametersFromSuperManifest;
import static org.junit.Assume.assumeTrue;

/**
 * Test the sound and completeness of Quest reasoner with respect to DL-Lite
 * semantic.
 * 
 * Setting: Without Optimizing Equivalences and Without Using TBox Sigma. 
 */
@RunWith(Parameterized.class)
public class DLLiteCompletenessNoEqNoSigTest extends TestCase {

	private static final ImmutableSet IGNORE = ImmutableSet.of();
	private static final CompletenessRepositoryRegistry REGISTRY = new CompletenessRepositoryRegistry();
	private final String testIRI;
	private final String resultFile;
	private final String parameterFile;
	private final String ontologyFile;
	private final String queryFile;
	private final ImmutableSet<String> ignoredTests;

	public DLLiteCompletenessNoEqNoSigTest(String testIRI, String name, String resultFile, String parameterFile,
										   String ontologyFile, String queryFile,
										   ImmutableSet<String> ignoredTests) {
		super(name);
		this.testIRI = testIRI;
		this.resultFile = resultFile;
		this.parameterFile = parameterFile;
		this.ontologyFile = ontologyFile;
		this.queryFile = queryFile;
		this.ignoredTests = ignoredTests;
	}

	@Parameterized.Parameters(name="{1}")
	public static Collection<Object[]> parameters() throws Exception {
		return parametersFromSuperManifest(
				"/completeness/manifest-noeq-nosig.ttl",
				IGNORE);
	}

	@Test
	@Override
	public void runTest() throws Exception {
		assumeTrue(!ignoredTests.contains(testIRI));

		Repository repository = getRepository();

		CompletenessTestExecutor executor = new CompletenessTestExecutor(testIRI, getName(), resultFile, queryFile, repository);
		executor.runTest();
	}

	@AfterClass
	public static void after() {
		REGISTRY.shutdown();
	}

	protected Repository getRepository() throws Exception {

		// Already existing
		Optional<Repository> optionalRepository = REGISTRY.getRepository(ontologyFile, parameterFile);
		if (optionalRepository.isPresent())
			return optionalRepository.get();

		Properties properties = loadReasonerParameters(parameterFile);
		String ontologyPath = new URL(ontologyFile).getPath();

		try (OntopSemanticIndexLoader loader = OntopSemanticIndexLoader.loadOntologyIndividuals(ontologyPath, properties)) {
			OntopRepository repository = OntopRepository.defaultRepository(loader.getConfiguration());
			repository.initialize();
			return repository;
		}
	}

	protected Properties loadReasonerParameters(String path) throws IOException {
		Properties p = new Properties();
		p.load(new URL(path).openStream());
		return p;
	}
}
