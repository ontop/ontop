/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
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
