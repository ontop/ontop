package it.unibz.krdb.obda.owlrefplatform.core.store;

import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.questdb.QuestDBClassicStore;

import java.io.File;

import junit.framework.TestCase;

public class DirectStoreCreateSerializerTest extends TestCase {

	public void disabledtestCreateSerialize() throws Exception {
		String owlfile = "src/test/resources/test/stockexchange-unittest.owl";

		ReformulationPlatformPreferences config = new ReformulationPlatformPreferences();
		config.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);

		QuestDBClassicStore store = new QuestDBClassicStore("name",(new File(owlfile)).toURI(), config);

		OBDAResultSet s = store
				.executeQuery("PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE { ?x a :Person}");
		int i = 0;
		while (s.nextRow()) {
			i += 1;
		}
		System.out.println("Count " + i);

		QuestDBClassicStore.saveState("./", store);

		store = (QuestDBClassicStore)QuestDBClassicStore.restore("./");
		store.connect();

		s = store
				.executeQuery("PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE { ?x a :Person}");
		i = 0;
		while (s.nextRow()) {
			i += 1;
		}
		System.out.println("Count " + i);
	}
}
