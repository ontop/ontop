package it.unibz.krdb.obda.ontology.impl;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.DisjointDescriptionAxiom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.PropertyFunctionalAxiom;
import it.unibz.krdb.obda.ontology.SubClassOfAxiom;
import it.unibz.krdb.obda.ontology.SubPropertyOfAxiom;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class OntologyImpl implements Ontology {

	private static final long serialVersionUID = 758424053258299151L;

	private final String ontouri;

	private final Set<Predicate> concepts = new HashSet<Predicate>();

	private final Set<Predicate> roles = new HashSet<Predicate>();

	private final static Set<Predicate> reserved = new ReservedPredicate();

	private final Set<Axiom> assertions = new LinkedHashSet<Axiom>();

	private final Set<PropertyFunctionalAxiom> functionalAxioms = new HashSet<PropertyFunctionalAxiom>();
	
	private final Set<DisjointDescriptionAxiom> disjointAxioms = new HashSet<DisjointDescriptionAxiom>();

	public static final String AUXROLEURI = "ER.A-AUXROLE";
	
	private final Set<Assertion> aboxAssertions = new HashSet<Assertion>();


	static final class ReservedPredicate extends HashSet<Predicate> {

		private static final long serialVersionUID = -4693542870632449462L;

		public ReservedPredicate() {
			add(OBDAVocabulary.RDFS_LITERAL);
			add(OBDAVocabulary.XSD_STRING);
			add(OBDAVocabulary.XSD_INTEGER);
            add(OBDAVocabulary.XSD_NEGATIVE_INTEGER);
            add(OBDAVocabulary.XSD_NON_NEGATIVE_INTEGER);
            add(OBDAVocabulary.XSD_POSITIVE_INTEGER);
            add(OBDAVocabulary.XSD_NON_POSITIVE_INTEGER);
            add(OBDAVocabulary.XSD_INT);
            add(OBDAVocabulary.XSD_UNSIGNED_INT);
            add(OBDAVocabulary.XSD_FLOAT);
            add(OBDAVocabulary.XSD_LONG);
			add(OBDAVocabulary.XSD_DECIMAL);
			add(OBDAVocabulary.XSD_DOUBLE);
			add(OBDAVocabulary.XSD_DATETIME);
			add(OBDAVocabulary.XSD_BOOLEAN);
		}
	}

	OntologyImpl(String uri) {
		this.ontouri = uri;
	}

	@Override
	public OntologyImpl clone() {
		OntologyImpl clone = new OntologyImpl(ontouri);
		clone.assertions.addAll(assertions);
		clone.concepts.addAll(concepts);
		clone.roles.addAll(roles);
		return clone;
	}

	
	private boolean referencesPredicates(Collection<Predicate> preds) {
		for (Predicate pred : preds) {
			
			// Make sure we never validate against auxiliary roles introduced by
			// the translation of the OWL ontology
			if (preds.toString().contains(AUXROLEURI)) {
				continue;
			}
			if (!(concepts.contains(pred) || roles.contains(pred) || reserved.contains(pred))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void addAssertion(Axiom assertion) {
		if (assertions.contains(assertion)) {
			return;
		}
		Set<Predicate> referencedEntities = assertion.getReferencedEntities();
		if (!referencesPredicates(referencedEntities)) {
			IllegalArgumentException ex = new IllegalArgumentException("At least one of these predicates is unknown: "
					+ referencedEntities.toString());
			throw ex;
		}

		if (assertion instanceof SubClassOfAxiom) {
			SubClassOfAxiom axiom = (SubClassOfAxiom) assertion;
			// We avoid redundant axioms
			if (axiom.getSub().equals(axiom.getSuper())) 
				return;

			assertions.add(assertion);
		} 
		else if (assertion instanceof SubPropertyOfAxiom) {
			SubPropertyOfAxiom axiom = (SubPropertyOfAxiom) assertion;
			 // We avoid redundant axioms
			if (axiom.getSub().equals(axiom.getSuper())) 
				return;
			
			assertions.add(assertion);
		}
		else if (assertion instanceof PropertyFunctionalAxiom) {
			functionalAxioms.add((PropertyFunctionalAxiom) assertion);
		} 
		else if (assertion instanceof DisjointDescriptionAxiom) {
			disjointAxioms.add((DisjointDescriptionAxiom) assertion);
		} else if (assertion instanceof Assertion) {
			/*ABox assertions */
			aboxAssertions.add((Assertion)assertion);
		}
	}
	
	@Override 
	public Set<Assertion> getABox() {
		return aboxAssertions;
	}

	@Override
	public Set<Axiom> getAssertions() {
		return assertions;
	}
	
	@Override 
	public Set<PropertyFunctionalAxiom> getFunctionalPropertyAxioms() {
		return functionalAxioms;
	}
	
	@Override 
	public Set<DisjointDescriptionAxiom> getDisjointDescriptionAxioms() {
		return disjointAxioms;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("[Ontology info.");
		if (assertions != null) {
			str.append(String.format(" Axioms: %d", assertions.size()));
		}
		if (concepts != null) {
			str.append(String.format(" Classes: %d", concepts.size()));
		}
		if (roles != null) {
			str.append(String.format(" Properties: %d]", roles.size()));
		}
		return str.toString();
	}

	@Override
	public void addConcept(Predicate cd) {
		concepts.add(cd);
	}

	@Override
	public void addRole(Predicate rd) {
		roles.add(rd);
	}

	@Override
	public Set<Predicate> getConcepts() {
		return concepts;
	}

	@Override
	public Set<Predicate> getRoles() {
		return roles;
	}

	@Override
	public String getUri() {
		return ontouri;
	}

	@Override
	public void addAssertionWithEntities(Axiom assertion) {
		for (Predicate pred : assertion.getReferencedEntities()) {
			if (pred.getArity() == 1) {
				addConcept(pred);
			} else {
				addRole(pred);
			}
			
		}
		addAssertion(assertion);
	}
}
