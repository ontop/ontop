package org.semanticweb.ontop.owlapi3;


import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLDataProperty;

public abstract class OWLAPI3TranslatorBase implements OWLAxiomVisitor {

	public abstract Ontology getOntology();
	
	public abstract void declare(OWLClass entity);
	
	public abstract void declare(OWLObjectProperty prop);
	
	public abstract void declare(OWLDataProperty prop);

	public static class TranslationException extends Exception {

		private static final long serialVersionUID = 7917688953760608030L;

		public TranslationException() {
		}
		
		public TranslationException(String msg) {
			super(msg);
		}

	}
}
