package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.QuestOWLIndividualIterator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.QuestMaterializer;

import java.util.Iterator;

public class OWLAPI3Materializer {

	private Iterator<Assertion> assertions = null;
	private QuestMaterializer materializer;
	
	public OWLAPI3Materializer(OBDAModel model) throws Exception {
		 this(model, null);
	}
	
	public OWLAPI3Materializer(OBDAModel model, Ontology onto) throws Exception {
		 materializer = new QuestMaterializer(model, onto);
		 assertions = materializer.getAssertionIterator();
	}
	
	public QuestOWLIndividualIterator getIterator() {
		return new QuestOWLIndividualIterator(assertions);
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
