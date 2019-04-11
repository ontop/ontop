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

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.utils.ManifestTestUtils;
import it.unibz.inf.ontop.docker.utils.OntopTestCase;
import it.unibz.inf.ontop.docker.utils.RepositoryRegistry;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class OracleDatatypeTest extends OntopTestCase {

	private static final ImmutableSet<String> IGNORE = ImmutableSet.of();
	private static final RepositoryRegistry REGISTRY = new RepositoryRegistry();

	public OracleDatatypeTest(String name, String queryFileURL, String resultFileURL, String owlFileURL, String obdaFileURL,
							  String parameterFileURL, RepositoryRegistry registry, ImmutableSet<String> ignoredTests) {
		super(name, queryFileURL, resultFileURL, owlFileURL, obdaFileURL, parameterFileURL, registry, ignoredTests);
	}


	@Parameterized.Parameters(name="{0}")
	public static Collection<Object[]> parameters() throws Exception {
		return ManifestTestUtils.parametersFromSuperManifest(
				"/testcases-docker/manifest-datatype-oracle.ttl",
				IGNORE, REGISTRY);
	}


	@AfterClass
	public static void after() {
		REGISTRY.shutdown();
	}
}
