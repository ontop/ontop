package it.unibz.inf.ontop.protege.views;

import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

import java.util.Map;
import java.util.stream.Collectors;

public class OWLAxiomToTurtleVisitor extends OWLAxiomVisitorAdapter {

	private final StringBuilder parentBuffer = new StringBuilder();

	private final StringBuilder classAssertionBuffer = new StringBuilder();
	private final StringBuilder objectPropertyAssertionBuffer = new StringBuilder();
	private final StringBuilder dataPropertyAssertionBuffer = new StringBuilder();
	private final StringBuilder annotationPropertyAssertionBuffer = new StringBuilder();

	private final PrefixManager prefixManager;
	private final boolean shotenIRIs;

	public OWLAxiomToTurtleVisitor(PrefixManager prefixManager, boolean shortenIRIs) {
		this.prefixManager = prefixManager;
		this.shotenIRIs = shortenIRIs;
		prefixManager.getPrefixMap().entrySet().stream()
				.map(e -> "@prefix " + e.getKey() + " " + e.getValue() + ".\n")
				.forEach(parentBuffer::append);
		parentBuffer.append("\n");
	}


	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		String subject = prefixManager.getShortForm(axiom.getSubject().toString());
		String predicate = prefixManager.getShortForm(axiom.getProperty().toString());
		String object = prefixManager.getShortForm(axiom.getObject().toString());
		objectPropertyAssertionBuffer.append(subject).append(" ")
				.append(predicate).append(" ")
				.append(object).append(". \n");
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		String subject = prefixManager.getShortForm(axiom.getIndividual().toString());
		String object = prefixManager.getShortForm(axiom.getClassExpression().toString());
		classAssertionBuffer.append(subject)
				.append(" rdf:type ")
				.append(object).append(" .\n");
	}


	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		String subject = prefixManager.getShortForm(axiom.getSubject().toString());
		String predicate = prefixManager.getShortForm(axiom.getProperty().toString());
		String object = ToStringRenderer.getInstance().getRendering(axiom.getObject());
		dataPropertyAssertionBuffer.append(subject).append(" ")
						.append(predicate).append(" ")
						.append(object).append(". \n");
	}

	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {
		String subject = prefixManager.getShortForm(axiom.getSubject().toString());
		String predicate = prefixManager.getShortForm(axiom.getProperty().toString());
		String object = ToStringRenderer.getInstance().getRendering(axiom.getValue());
		annotationPropertyAssertionBuffer.append(subject).append(" ")
				.append(predicate).append(" ")
				.append(object).append(". \n");
	}

	public String getString() {
		parentBuffer.append("# Class assertion axioms\n")
				.append(classAssertionBuffer)
				.append("\n")
				.append("# Object property assertion axioms\n")
				.append(objectPropertyAssertionBuffer)
				.append("\n")
				.append("# Data annotation property assertion axioms\n")
				.append(dataPropertyAssertionBuffer)
				.append("\n")
				.append("# Annotation property assertion axioms\n")
				.append(annotationPropertyAssertionBuffer);

		return parentBuffer.toString();
	}
}
