package it.unibz.krdb.obda.quest.scenarios;

import org.openrdf.repository.Repository;

import sesameWrapper.SesameVirtualRepo;

public abstract class QuestVirtualScenarioTest extends QuestScenarioTest {

	public QuestVirtualScenarioTest(String testURI, String name,
			String queryFileURL, String resultFileURL, String owlFileURL,
			String obdaFileURL, String parameterFileURL) {
		super(testURI, name, queryFileURL, resultFileURL, owlFileURL, obdaFileURL, parameterFileURL);
	}
	
	@Override
	protected Repository createRepository() throws Exception {
		try {
			SesameVirtualRepo repo = new SesameVirtualRepo(getName(), owlFileURL, obdaFileURL, parameterFileURL);
			repo.initialize();
			return repo;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}