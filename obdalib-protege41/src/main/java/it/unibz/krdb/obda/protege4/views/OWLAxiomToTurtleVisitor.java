/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.protege4.views;

import it.unibz.krdb.obda.io.PrefixManager;

import java.util.Map;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

public class OWLAxiomToTurtleVisitor implements OWLAxiomVisitor {

	private StringBuilder parentBuffer = new StringBuilder();

	private StringBuilder classAssertionBuffer = new StringBuilder();
	private StringBuilder objectPropertyAssertionBuffer = new StringBuilder();
	private StringBuilder dataPropertyAssertionBuffer = new StringBuilder();

	private PrefixManager prefixManager;

	public OWLAxiomToTurtleVisitor(PrefixManager prefixManager) {
		this.prefixManager = prefixManager;
		Map<String, String> namespaces = prefixManager.getPrefixMap();
		for (String prefix : namespaces.keySet()) {
			parentBuffer.append(String.format("@prefix %s <%s> .\n", prefix,
					namespaces.get(prefix)));
		}
		parentBuffer.append("\n");
	}

	@Override
	public void visit(OWLDeclarationAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		String subject = prefixManager.getShortForm(axiom.getSubject()
				.toString());
		String predicate = prefixManager.getShortForm(axiom.getProperty()
				.toString());
		String object = prefixManager
				.getShortForm(axiom.getObject().toString());
		objectPropertyAssertionBuffer.append(String.format("%s %s %s .\n",
				subject, predicate, object));
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		String subject = prefixManager.getShortForm(axiom.getIndividual()
				.toString());
		String object = prefixManager.getShortForm(axiom.getClassExpression()
				.toString());
		classAssertionBuffer.append(String.format("%s rdf:type %s .\n",
				subject, object));
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		String subject = prefixManager.getShortForm(axiom.getSubject()
				.toString());
		String predicate = prefixManager.getShortForm(axiom.getProperty()
				.toString());
		String object = axiom.getObject().toString();
		dataPropertyAssertionBuffer.append(String.format("%s %s %s .\n",
				subject, predicate, object));
	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLHasKeyAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLDatatypeDefinitionAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(SWRLRule rule) {
		// NO-OP
	}

	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
		// NO-OP
	}

	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
		// NO-OP
	}

	public String getString() {
		parentBuffer.append("# Class assertion axioms\n");
		parentBuffer.append(classAssertionBuffer);
		parentBuffer.append("\n");

		parentBuffer.append("# Object property assertion axioms\n");
		parentBuffer.append(objectPropertyAssertionBuffer);
		parentBuffer.append("\n");

		parentBuffer.append("# Data property assertion axioms\n");
		parentBuffer.append(dataPropertyAssertionBuffer);

		return parentBuffer.toString();
	}
}
