package it.unibz.ontop.sesame.completeness.test;

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

import junit.framework.Test;

/**
 * Test the sound and completeness of Quest reasoner with respect to DL-Lite
 * semantic.
 * 
 * Setting: With Optimizing Equivalences and With Using TBox Sigma. 
 */
public class DLLiteCompletenessEqSigTest extends CompletenessParent {

	public DLLiteCompletenessEqSigTest(String tid, String name, String resf,
			String propf, String owlf, String sparqlf) throws Exception {
		super(tid, name, resf, propf, owlf, sparqlf);
	}

	public static Test suite() throws Exception {
		return CompletenessTestUtils.suite(new Factory() {
			@Override
			public CompletenessParent createCompletenessTest(String testId,
					String testName, String testResultPath,
					String testParameterPath, String testOntologyPath,
					String testQueryPath) throws Exception {
				return new DLLiteCompletenessEqSigTest(testId, testName,
						testResultPath, testParameterPath, testOntologyPath,
						testQueryPath);
			}

			@Override
			public String getMainManifestFile() {
				return "/completeness/manifest-eq-sig.ttl";
			}
		});
	}
}
