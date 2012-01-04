package it.unibz.krdb.obda.owlrefplatform.core.store;

import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.owlapi2.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;
import it.unibz.krdb.obda.owlrefplatform.questdb.QuestDBClassicStore;

import java.io.File;

import junit.framework.TestCase;

public class DirectStoreCreateSerializerTest extends TestCase {

	public void disabledtestCreateSerialize() throws Exception {
		String owlfile = "src/test/resources/test/stockexchange-unittest.owl";

		ReformulationPlatformPreferences config = new ReformulationPlatformPreferences();
		config.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.CLASSIC);

		QuestDBClassicStore store = new QuestDBClassicStore("name", (new File(owlfile)).toURI(), config);
		QuestStatement st = store.getConnection().createStatement();
		OBDAResultSet s = st.execute("PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE { ?x a :Person}");
		int i = 0;
		while (s.nextRow()) {
			i += 1;
		}
		System.out.println("Count " + i);

		s.close();
		st.close();

		QuestDBClassicStore.saveState("./", store);

		store = (QuestDBClassicStore) QuestDBClassicStore.restore("./");
		store.connect();

		st = store.getConnection().createStatement();

		s = st.execute("PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE { ?x a :Person}");
		i = 0;
		while (s.nextRow()) {
			i += 1;
		}
		System.out.println("Count " + i);
	}
}
