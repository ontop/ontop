package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3IndividualTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.QuestMaterializer;

import java.util.Iterator;

import org.semanticweb.owlapi.model.OWLIndividualAxiom;

public class OWLAPI3Materializer implements Iterator<OWLIndividualAxiom> {

	private Iterator<Assertion> assertions = null;
	private QuestMaterializer materializer;
	private OWLAPI3IndividualTranslator translator = new OWLAPI3IndividualTranslator();
	
	public OWLAPI3Materializer(OBDAModel model) throws Exception {
		 this(null, model);
	}
	
	public OWLAPI3Materializer(Ontology onto, OBDAModel model) throws Exception {
		 materializer = new QuestMaterializer(onto, model);
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
