/*
 * #%L
 * ontop-rdb2rdf-compliance
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import junit.framework.Test;

public class RDB2RDFGroupScenarioTest extends RDB2RDFScenarioParent {

	public RDB2RDFGroupScenarioTest(String testURI, String name, String sqlFile, String mappingFile, String outputFile) throws FileNotFoundException {
		super(testURI, name, sqlFile, mappingFile, outputFile);
	}

	public static Test suite() throws Exception {
		
		return RDB2RDFManifestUtils.suite(new Factory() {
			
			public RDB2RDFScenarioParent createRDB2RDFScenarioTest(String testURI, String name, String sqlFile, String mappingFile, String outputFile) {
				try {
					return new RDB2RDFGroupScenarioTest(testURI, name,sqlFile, mappingFile, outputFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				return null;
			}
			
			public String getMainManifestFile() {
				return "manifest-h2.ttl";
			}

			public RDB2RDFScenarioParent createRDB2RDFScenarioTest(
					String testURI, String name, String sqlFileURL,
					String mappingFileURL, String outputFileURL,
					String parameterFileURL) {
				try {
					return new RDB2RDFGroupScenarioTest(testURI, name,sqlFileURL, mappingFileURL, outputFileURL);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}
}
