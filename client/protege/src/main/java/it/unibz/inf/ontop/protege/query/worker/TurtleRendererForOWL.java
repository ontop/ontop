package it.unibz.inf.ontop.protege.query.worker;

import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.SimpleRenderer;

import java.util.Optional;

public class TurtleRendererForOWL {

	private final PrefixManager prefixManager;
	private final SimpleRenderer renderer;

	public TurtleRendererForOWL(PrefixManager prefixManager, boolean shortenIRIs) {
		// TODO: subclass AbstractPrefixManager
		this.prefixManager = prefixManager;
		this.renderer = new SimpleRenderer();
		if (shortenIRIs)
			prefixManager.getPrefixMap().forEach(renderer::setPrefix);
	}

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

	public String[] render(OWLBindingSet bs, String[] signature) throws OWLException {
		String[] row = new String[signature.length];
		for (int j = 0; j < signature.length; j++) {
			String variableName = signature[j];
			OWLObject constant = bs.getOWLPropertyAssertionObject(variableName);
			row[j] = render(constant);
		}
		return row;
	}

	public String[] renderPrefixMap() {
		return prefixManager.getPrefixMap().entrySet().stream()
				.map(e -> "@prefix " + e.getKey() + " " + e.getValue() + ".")
				.collect(ImmutableCollectors.toList())
				.toArray(new String[0]);
	}

	private String render(OWLObject constant) {
		return (constant == null) ? "" : renderer.render(constant);
	}

	private String renderAxiom(OWLClassAssertionAxiom axiom) {
		return renderTriple(axiom.getIndividual(), IRI.create(RDF.TYPE.getIRIString()), axiom.getClassExpression());
	}

	private String renderAxiom(OWLObjectPropertyAssertionAxiom axiom) {
		return renderTriple(axiom.getSubject(), axiom.getProperty(), axiom.getObject());
	}

	private String renderAxiom(OWLDataPropertyAssertionAxiom axiom) {
		return renderTriple(axiom.getSubject(), axiom.getProperty(), axiom.getObject());
	}

	private String renderAxiom(OWLAnnotationAssertionAxiom axiom) {
		return renderTriple(axiom.getSubject(), axiom.getProperty(), axiom.getValue());
	}

	private String renderTriple(OWLObject subject, OWLObject predicate, OWLObject object) {
		String subjectString = render(subject);
		String predicateString = render(predicate);
		String objectString = render(object);
		return subjectString + " " + predicateString + " " + objectString + ". \n";
	}
}
