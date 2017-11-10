package it.unibz.inf.ontop.protege.views;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

import java.util.Map;

public class OWLAxiomToTurtleVisitor extends OWLAxiomVisitorAdapter {

	private final StringBuilder parentBuffer = new StringBuilder();

	private final StringBuilder classAssertionBuffer = new StringBuilder();
	private final StringBuilder objectPropertyAssertionBuffer = new StringBuilder();
	private final StringBuilder dataPropertyAssertionBuffer = new StringBuilder();

	private final PrefixManager prefixManager;

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
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		String subject = prefixManager.getShortForm(axiom.getSubject().toString());
		String predicate = prefixManager.getShortForm(axiom.getProperty().toString());
		String object = prefixManager.getShortForm(axiom.getObject().toString());
		objectPropertyAssertionBuffer.append(String.format("%s %s %s .\n",
				subject, predicate, object));
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		String subject = prefixManager.getShortForm(axiom.getIndividual().toString());
		String object = prefixManager.getShortForm(axiom.getClassExpression().toString());
		classAssertionBuffer.append(String.format("%s rdf:type %s .\n", subject, object));
	}


	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		String subject = prefixManager.getShortForm(axiom.getSubject().toString());
		String predicate = prefixManager.getShortForm(axiom.getProperty().toString());
		String object = ToStringRenderer.getInstance().getRendering(axiom.getObject());
		dataPropertyAssertionBuffer.append(String.format("%s %s %s .\n",
				subject, predicate, object));
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
