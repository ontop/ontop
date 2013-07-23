package inf.unibz.ontop.sesame.tests.general;

import it.unibz.krdb.obda.io.R2RMLWriter;
import it.unibz.krdb.obda.model.OBDAModel;

import java.io.File;
import java.net.URI;

public class R2RMLWriterTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			
			String owlfile = "C:/Project/Test Cases/Book.owl";
			String obdafile = "C:/Project/Test Cases/Book.obda";
			
			URI owlURI =  new File(owlfile).toURI();
			URI obdaURI =  new File(obdafile).toURI();
			it.unibz.krdb.obda.owlrefplatform.questdb.QuestDBVirtualStore store;
			store = new it.unibz.krdb.obda.owlrefplatform.questdb.QuestDBVirtualStore("test", owlURI, obdaURI);
			
			OBDAModel model = store.getObdaModel(obdaURI);
			R2RMLWriter writer = new R2RMLWriter(model, URI.create("simple-h2"));
			File out = new File("C:/Project/Test Cases/mapping1.ttl");
					//"C:/Project/Timi/Workspace/obdalib-parent/quest-rdb2rdf-compliance/src/main/resources/D004/WRr2rmlb.ttl");
			writer.write(out);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

}
