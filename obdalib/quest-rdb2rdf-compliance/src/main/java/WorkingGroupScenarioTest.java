import java.io.FileNotFoundException;

import junit.framework.Test;


public class WorkingGroupScenarioTest extends RDB2RDFScenarioTest{

	public WorkingGroupScenarioTest(String testURI, String name, String sqlFile, String mappingFile, String outputFile) throws FileNotFoundException {
		super(testURI, name, sqlFile, mappingFile, outputFile);
	}

	public static Test suite() throws Exception {
		return RDB2RDFManifestTest.suite(new Factory() {
			
			public RDB2RDFScenarioTest createRDB2RDFScenarioTest(String testURI, String name, String sqlFile, String mappingFile, String outputFile) {
				try {
					return new WorkingGroupScenarioTest(testURI, name,sqlFile, mappingFile, outputFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				return null;
			}
			
			
			public String getMainManifestFile() {
				return "../../manifest-h2.ttl";
						//"/C:/Project/Timi/Workspace/obdalib-parent/quest-rdb2rdf-compliance/src/main/resources/manifest-h2.ttl";
						//"../../src/main/resources/manifest-h2.ttl";
			}


			public RDB2RDFScenarioTest createRDB2RDFScenarioTest(
					String testURI, String name, String sqlFileURL,
					String mappingFileURL, String outputFileURL,
					String parameterFileURL) {
				try {
					return new WorkingGroupScenarioTest(testURI, name,sqlFileURL, mappingFileURL, outputFileURL);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				return null;
			}

			
		});
	}
}
