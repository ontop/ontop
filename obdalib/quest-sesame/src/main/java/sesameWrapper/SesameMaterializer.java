package sesameWrapper;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.abox.QuestMaterializer;

import java.util.Iterator;

import org.openrdf.model.Statement;

public class SesameMaterializer implements Iterator<Statement> {
	
		private Iterator<Assertion> assertions = null;
		private QuestMaterializer materializer;
		
		public SesameMaterializer(OBDAModel model) throws Exception {
			this(null, model);
		}
		
		public SesameMaterializer(Ontology onto, OBDAModel model) throws Exception {
			 materializer = new QuestMaterializer(onto, model);
			 assertions = materializer.getAssertionIterator();
			setAssertions(assertions);
		}
		
		public SesameMaterializer(Iterator<Assertion> assertions) {
			setAssertions(assertions);
		}
		
		public void setAssertions(Iterator<Assertion> assertions) {
			this.assertions = assertions;
		}
		
		@Override
		public boolean hasNext() {
			return assertions.hasNext();
		}

		@Override
		public Statement next() {
			Assertion assertion = assertions.next();
			Statement individual = new SesameStatement(assertion);
			return individual;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("This iterator is read-only");
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
