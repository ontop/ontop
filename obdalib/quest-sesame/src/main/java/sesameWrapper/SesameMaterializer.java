package sesameWrapper;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.abox.QuestMaterializer;

import java.util.Iterator;

import org.openrdf.model.Statement;

public class SesameMaterializer implements Iterator<Statement> {
	
		private Iterator<Assertion> assertions = null;

		
		public SesameMaterializer(OBDAModel model) throws Exception {
			QuestMaterializer materializer = new QuestMaterializer(model);
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
	

}
