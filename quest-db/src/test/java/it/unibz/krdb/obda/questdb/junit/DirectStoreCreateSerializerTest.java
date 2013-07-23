package it.unibz.krdb.obda.questdb.junit;

import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBStatement;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.questdb.QuestDBClassicStore;

import java.io.File;

import junit.framework.TestCase;

public class DirectStoreCreateSerializerTest extends TestCase {

	public void testCreateSerialize() throws Exception {
		String owlfile = "src/test/resources/test/stockexchange-classic-unittest.owl";

		QuestPreferences config = new QuestPreferences();
		config.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);

		QuestDBClassicStore store = new QuestDBClassicStore("serializerStoreTest", (new File(owlfile)).toURI(), config);

		QuestDBConnection conn = store.getConnection();
		QuestDBStatement st = conn.createStatement();

		st.add(new File(owlfile).toURI());

		conn.commit();
		
		st = conn.createStatement();

		OBDAResultSet s = st
				.execute("PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT DISTINCT ?x WHERE { ?x a :Person}");
		int i = 0;
		while (s.nextRow()) {
			i += 1;
		}

		s.close();
		st.close();

		QuestDBClassicStore.saveState(".", store);

		store = (QuestDBClassicStore) QuestDBClassicStore.restore("./serializerStoreTest.qst");

		st = store.getConnection().createStatement();

		s = st.execute("PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT DISTINCT ?x WHERE { ?x a :Person}");
		int j = 0;
		while (s.nextRow()) {
			j += 1;
		}
		assertEquals(i, j);

		File storeFile = new File("./serializerStoreTest.qst");
		storeFile.delete();

		System.out.println("Total people: " + i);
	}
}
