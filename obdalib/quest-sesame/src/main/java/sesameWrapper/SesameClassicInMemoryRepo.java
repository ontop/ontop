package sesameWrapper;

import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

import org.openrdf.query.Dataset;

public class SesameClassicInMemoryRepo extends SesameClassicRepo {
	
	private static QuestPreferences p = new QuestPreferences();

	public SesameClassicInMemoryRepo(String name, String tboxFile, boolean existential, String rewriting) throws Exception {
		super();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "false");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC); 
		p.setCurrentValueOf(QuestPreferences.STORAGE_LOCATION, QuestConstants.INMEMORY);
		if (existential) {
			p.setCurrentValueOf(QuestPreferences.REWRITE, "true");
		} else {
			p.setCurrentValueOf(QuestPreferences.REWRITE, "false");
		}
		if (rewriting.equals("TreeWitness")) {
			p.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
		} else if (rewriting.equals("Default")) {
			p.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		}
		createStore(name, tboxFile, p); 
	}
	
	public SesameClassicInMemoryRepo(String name, Dataset data) throws Exception {
		super();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "false");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC); 
		p.setCurrentValueOf(QuestPreferences.STORAGE_LOCATION, QuestConstants.INMEMORY);
		
		createStore(name, data, p); 
	}
	
	public SesameClassicInMemoryRepo(String name, String tboxFilePath, String configFilePath) throws Exception {
		super();
		File configFile = new File(URI.create(configFilePath));
		p.readDefaultPropertiesFile(new FileInputStream(configFile));
		createStore(name, tboxFilePath, p);
	}
}
