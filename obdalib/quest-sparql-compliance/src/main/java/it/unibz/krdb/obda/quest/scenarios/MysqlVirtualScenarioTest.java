package it.unibz.krdb.obda.quest.scenarios;

import junit.framework.Test;

public class MysqlVirtualScenarioTest extends QuestVirtualScenarioTest {

	public MysqlVirtualScenarioTest(String testURI, String name, String queryFileURL, String resultFileURL, 
			String owlFileURL, String obdaFileURL, String parameterFileURL) {
		super(testURI, name, queryFileURL, resultFileURL, owlFileURL, obdaFileURL, parameterFileURL);
	}

	public static Test suite() throws Exception {
		return ScenarioManifestTest.suite(new Factory() {
			@Override
			public QuestVirtualScenarioTest createQuestScenarioTest(String testURI, String name, String queryFileURL, 
					String resultFileURL, String owlFileURL, String obdaFileURL) {
				return new MysqlVirtualScenarioTest(testURI, name, queryFileURL, resultFileURL, owlFileURL, 
						obdaFileURL, "");
			}
			@Override
			public QuestVirtualScenarioTest createQuestScenarioTest(String testURI, String name, String queryFileURL, 
					String resultFileURL, String owlFileURL, String obdaFileURL, String parameterFileURL) {
				return new MysqlVirtualScenarioTest(testURI, name, queryFileURL, resultFileURL, owlFileURL, 
						obdaFileURL, parameterFileURL);
			}
			@Override
			public String getMainManifestFile() {
				return "/testcases-scenarios/virtual-mode/manifest-scenario-mysql.ttl";
			}
		});
	}
}
