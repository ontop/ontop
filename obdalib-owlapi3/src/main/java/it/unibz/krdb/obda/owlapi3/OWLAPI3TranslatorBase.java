package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.ontology.Ontology;

import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLOntology;

public abstract class OWLAPI3TranslatorBase implements OWLAxiomVisitor {

	public abstract Ontology getOntology();
	
	public abstract void prepare(OWLOntology owlont);

	public static class TranslationException extends Exception {

		private static final long serialVersionUID = 7917688953760608030L;

		public TranslationException() {
		}
		
		public TranslationException(String msg) {
			super(msg);
		}

	}
}
