package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import java.util.Iterator;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.owlapi3.OWLAPI3IndividualTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.QuestMaterializer;

import org.semanticweb.owlapi.model.OWLIndividualAxiom;

public class OWLAPI3Materializer implements Iterator<OWLIndividualAxiom> {

	private Iterator<Assertion> assertions = null;

	private OWLAPI3IndividualTranslator translator = new OWLAPI3IndividualTranslator();
	
	public OWLAPI3Materializer(OBDAModel model) throws Exception {
		QuestMaterializer materializer = new QuestMaterializer(model);
		 assertions = materializer.getAssertionIterator();
		setAssertions(assertions);
	}
	
	public OWLAPI3Materializer(Iterator<Assertion> assertions) {
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
	public OWLIndividualAxiom next() {
		Assertion assertion = assertions.next();
		OWLIndividualAxiom individual = translator.translate(assertion);
		return individual;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("This iterator is read-only");
	}
}
