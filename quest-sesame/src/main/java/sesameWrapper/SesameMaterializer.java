package sesameWrapper;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.abox.QuestMaterializer;
import it.unibz.krdb.obda.sesame.SesameStatementIterator;

import java.util.Iterator;

public class SesameMaterializer {
	
		private Iterator<Assertion> assertions = null;
		private QuestMaterializer materializer;
		
		public SesameMaterializer(OBDAModel model) throws Exception {
			this(model, null);
		}
		
		public SesameMaterializer(OBDAModel model, Ontology onto) throws Exception {
			 materializer = new QuestMaterializer(model, onto);
			 assertions = materializer.getAssertionIterator();
		}
		
		public SesameStatementIterator getIterator() {
			return new SesameStatementIterator(assertions);
		}
		
		public void disconnect() {
			materializer.disconnect();
		}
		
		public long getTriplesCount()
		{ try {
			return materializer.getTriplesCount();
		} catch (Exception e) {
			e.printStackTrace();
		}return -1;
		}
	
		public int getVocabularySize() {
			return materializer.getVocabSize();
		}
}
