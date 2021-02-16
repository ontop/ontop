package it.unibz.inf.ontop.protege.utils;

import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;

import java.util.Map;
import java.util.Optional;

public class OWL2TurtleTranslator {

	private final PrefixManager prefixManager;
	private final boolean shortenIRIs;

	public OWL2TurtleTranslator(PrefixManager prefixManager, boolean shortenIRIs) {
		// TODO: subclass AbstractPrefixManager
		this.prefixManager = prefixManager;
		this.shortenIRIs = shortenIRIs;
	}

	public Map<String, String> getPrefixMap() { return prefixManager.getPrefixMap(); }

	public Optional<String> render(OWLAxiom axiom) {
		if (axiom instanceof OWLClassAssertionAxiom)
			return Optional.of(renderAxiom((OWLClassAssertionAxiom)axiom));
		if (axiom instanceof OWLObjectPropertyAssertionAxiom)
			return Optional.of(renderAxiom((OWLObjectPropertyAssertionAxiom)axiom));
		if (axiom instanceof OWLDataPropertyAssertionAxiom)
			return Optional.of(renderAxiom((OWLDataPropertyAssertionAxiom)axiom));
		if (axiom instanceof OWLAnnotationAssertionAxiom)
			return Optional.of(renderAxiom((OWLAnnotationAssertionAxiom)axiom));
		return Optional.empty();
	}

	public String render(OWLObject constant) {
		if (constant == null)
			return "";

		return constant instanceof OWLNamedIndividual
				? getIRI(constant.toString())
				: ToStringRenderer.getInstance().getRendering(constant);
	}

	private String renderAxiom(OWLObjectPropertyAssertionAxiom axiom) {
		String subject = getIRI(axiom.getSubject().toString());
		String predicate = getIRI(axiom.getProperty().toString());
		String object = getIRI(axiom.getObject().toString());
		return subject + " " + predicate + " " + object + ". \n";
	}

	private String renderAxiom(OWLClassAssertionAxiom axiom) {
		String subject = getIRI(axiom.getIndividual().toString());
		String object = getIRI(axiom.getClassExpression().toString());
		return subject + " rdf:type " + object + ". \n";
	}

	private String renderAxiom(OWLDataPropertyAssertionAxiom axiom) {
		String subject = getIRI(axiom.getSubject().toString());
		String predicate = getIRI(axiom.getProperty().toString());
		String object = render(axiom.getObject());
		return subject + " " + predicate + " " + object + ". \n";
	}

	private String renderAxiom(OWLAnnotationAssertionAxiom axiom) {
		String subject = getIRI(axiom.getSubject().toString());
		String predicate = getIRI(axiom.getProperty().toString());
		String object = render(axiom.getValue());
		return subject + " " + predicate + " " + object + ". \n";
	}

	private String getIRI(String iri) {
		return shortenIRIs ? prefixManager.getShortForm(iri) : iri;
	}
}
