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
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

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

		CompletenessTestExecutor executor = new CompletenessTestExecutor(testIRI, getName(), resultFile, parameterFile, ontologyFile, queryFile);
		executor.runTest();
	}
}
