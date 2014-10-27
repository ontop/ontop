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
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataType;
import it.unibz.krdb.obda.ontology.DisjointClassesAxiom;
import it.unibz.krdb.obda.ontology.DisjointPropertiesAxiom;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.FunctionalPropertyAxiom;
import it.unibz.krdb.obda.ontology.PropertyAssertion;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
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

	private final Set<FunctionalPropertyAxiom> functionalAxioms = new HashSet<FunctionalPropertyAxiom>();
	
	private final Set<DisjointClassesAxiom> disjointClassesAxioms = new HashSet<DisjointClassesAxiom>();

	private final Set<DisjointPropertiesAxiom> disjointPropertiesAxioms = new HashSet<DisjointPropertiesAxiom>();
	
	public static final String AUXROLEURI = "ER.A-AUXROLE";
	
	private final Set<ClassAssertion> classAssertions = new HashSet<ClassAssertion>();

	private final Set<PropertyAssertion> propertyAssertions = new HashSet<PropertyAssertion>();
	

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

	private Predicate getReferencedEntities(BasicClassDescription desc) {
		if (desc instanceof OClass) 
			return (((OClass) desc).getPredicate());
		else if (desc instanceof PropertySomeRestriction) 
			return (((PropertySomeRestriction) desc).getPredicate());
		else if (desc instanceof DataType) 
			return (((DataType) desc).getPredicate());
		else 
			throw new UnsupportedOperationException("Cant understand: " + desc.toString());
	}
	
	private Set<Predicate> getReferencedEntities(Axiom assertion) {
		Set<Predicate> refs = new HashSet<Predicate>();
		if (assertion instanceof SubClassOfAxiom) {
			SubClassOfAxiom ax = (SubClassOfAxiom)assertion;
			refs.add(getReferencedEntities(ax.getSub()));
			refs.add(getReferencedEntities(ax.getSuper()));			
		}
		else if (assertion instanceof DisjointClassesAxiom) {
			DisjointClassesAxiom ax = (DisjointClassesAxiom)assertion;
			refs.add(getReferencedEntities(ax.getFirst()));
			refs.add(getReferencedEntities(ax.getSecond()));	
		}
		else if (assertion instanceof ClassAssertion) 
			refs.add(((ClassAssertion)assertion).getConcept().getPredicate());
		else if (assertion instanceof PropertyAssertion) 
			refs.add(((PropertyAssertion)assertion).getProperty().getPredicate());
		else if (assertion instanceof SubPropertyOfAxiom) {
			SubPropertyOfAxiom ax = (SubPropertyOfAxiom)assertion;
			refs.add(ax.getSub().getPredicate());
			refs.add(ax.getSuper().getPredicate());			
		}
		else if (assertion instanceof DisjointPropertiesAxiom) {
			DisjointPropertiesAxiom ax = (DisjointPropertiesAxiom)assertion;
			refs.add(ax.getFirst().getPredicate());
			refs.add(ax.getSecond().getPredicate());	
		}
		else if (assertion instanceof FunctionalPropertyAxiom) {
			FunctionalPropertyAxiom ax = (FunctionalPropertyAxiom)assertion;
			refs.add(ax.getProperty().getPredicate());
		}
		return refs;
	}
	
	@Override
	public void addAssertion(Axiom assertion) {
		if (assertions.contains(assertion)) {
			return;
		}
		Set<Predicate> referencedEntities = getReferencedEntities(assertion);
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
		else if (assertion instanceof FunctionalPropertyAxiom) {
			functionalAxioms.add((FunctionalPropertyAxiom) assertion);
		} 
		else if (assertion instanceof DisjointClassesAxiom) {
			disjointClassesAxioms.add((DisjointClassesAxiom) assertion);
		}
		else if (assertion instanceof DisjointPropertiesAxiom) {
			disjointPropertiesAxioms.add((DisjointPropertiesAxiom) assertion);
		} 
		else if (assertion instanceof ClassAssertion) {
			/*ABox assertions */
			classAssertions.add((ClassAssertion)assertion);
		}
		else if (assertion instanceof PropertyAssertion) {
			/*ABox assertions */
			propertyAssertions.add((PropertyAssertion)assertion);
		}
	}
	
	@Override 
	public Set<ClassAssertion> getClassAssertions() {
		return classAssertions;
	}
	
	@Override 
	public Set<PropertyAssertion> getPropertyAssertions() {
		return propertyAssertions;
	}

	@Override
	public Set<Axiom> getAssertions() {
		return assertions;
	}
	
	@Override 
	public Set<FunctionalPropertyAxiom> getFunctionalPropertyAxioms() {
		return functionalAxioms;
	}
	
	@Override 
	public Set<DisjointClassesAxiom> getDisjointClassesAxioms() {
		return disjointClassesAxioms;
	}
	
	@Override 
	public Set<DisjointPropertiesAxiom> getDisjointPropertiesAxioms() {
		return disjointPropertiesAxioms;
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
		for (Predicate pred : getReferencedEntities(assertion)) {
			if (pred.getArity() == 1) {
				addConcept(pred);
			} else {
				addRole(pred);
			}
			
		}
		addAssertion(assertion);
	}
}
