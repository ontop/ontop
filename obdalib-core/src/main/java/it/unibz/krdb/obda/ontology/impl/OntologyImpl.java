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
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertyFunctionalAxiom;
import it.unibz.krdb.obda.ontology.SubDescriptionAxiom;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class OntologyImpl implements Ontology {

	private static final long serialVersionUID = 758424053258299151L;

	private Set<SubDescriptionAxiom> assertions = null;

	private Set<Predicate> concepts = null;

	private Set<Predicate> roles = null;

	private Set<Predicate> reserved = null;

	private String ontouri = null;

	private Set<Axiom> originalassertions = null;

	// Assertions indexed by right side predicate
//	private Map<Predicate, Set<SubDescriptionAxiom>> rightAssertionIndex = null;

	private Map<Predicate, Set<SubDescriptionAxiom>> rightNonExistentialIndex = null;

	private Map<Predicate, Set<SubDescriptionAxiom>> rightExistentialIndex = null;

	// Assertions indexed by left side predicate
//	private Map<Predicate, Set<SubDescriptionAxiom>> leftAssertionIndex = null;

//	public boolean isSaturated = false;

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	private Set<PropertyFunctionalAxiom> functionalAxioms = new HashSet<PropertyFunctionalAxiom>();
	
	private Set<DisjointDescriptionAxiom> disjointAxioms = new HashSet<DisjointDescriptionAxiom>();

	public static final String AUXROLEURI = "ER.A-AUXROLE";
	
	private Set<Assertion> aboxAssertions = new HashSet<Assertion>();

	class ReservedPredicate extends HashSet<Predicate> {

		private static final long serialVersionUID = -4693542870632449462L;

		public ReservedPredicate() {
			add(OBDAVocabulary.RDFS_LITERAL);
			add(OBDAVocabulary.XSD_STRING);
			add(OBDAVocabulary.XSD_INTEGER);
			add(OBDAVocabulary.XSD_DECIMAL);
			add(OBDAVocabulary.XSD_DOUBLE);
			add(OBDAVocabulary.XSD_DATETIME);
			add(OBDAVocabulary.XSD_BOOLEAN);
		}
	}

	OntologyImpl(String uri) {
		ontouri = uri;
		originalassertions = new LinkedHashSet<Axiom>();
		concepts = new HashSet<Predicate>();
		roles = new HashSet<Predicate>();
		reserved = new ReservedPredicate();
	}

	@Override
	public OntologyImpl clone() {
		OntologyImpl clone = null;
		if (ontouri != null) {
			clone = (OntologyImpl) ofac.createOntology(ontouri.toString());
		}
		else {
			clone = (OntologyImpl) ofac.createOntology();
		}
		clone.originalassertions.addAll(originalassertions);
		clone.concepts.addAll(concepts);
		clone.roles.addAll(roles);
//		clone.isSaturated = isSaturated;

		if (assertions != null) {
			clone.assertions = new HashSet<SubDescriptionAxiom>();
			clone.assertions.addAll(assertions);
		}
/*		
		if (rightAssertionIndex != null) {
			clone.rightAssertionIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();
			clone.rightAssertionIndex.putAll(rightAssertionIndex);
		}
*/		
		if (rightNonExistentialIndex != null) {
			clone.rightNonExistentialIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();
			clone.rightNonExistentialIndex.putAll(rightNonExistentialIndex);
		}
		if (rightExistentialIndex != null) {
			clone.rightExistentialIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();
			clone.rightExistentialIndex.putAll(rightExistentialIndex);
		}
/*		
		if (leftAssertionIndex != null) {
			clone.leftAssertionIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();
			clone.leftAssertionIndex.putAll(leftAssertionIndex);
		}
*/		
		return clone;
	}

	@Override
	public boolean referencesPredicate(Predicate pred) {
		return concepts.contains(pred) || roles.contains(pred);
	}

	@Override
	public boolean referencesPredicates(Collection<Predicate> preds) {
		for (Predicate pred : preds) {
			/*
			 * Make sure we never validate against auxiliary roles introduced by
			 * the translation of the OWL ontology
			 */
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
		if (originalassertions.contains(assertion)) {
			return;
		}
		Set<Predicate> referencedEntities = assertion.getReferencedEntities();
		if (!referencesPredicates(referencedEntities)) {
			IllegalArgumentException ex = new IllegalArgumentException("At least one of these predicates is unknown: "
					+ referencedEntities.toString());
			throw ex;
		}
//		isSaturated = false;

		if (assertion instanceof SubDescriptionAxiom) {
			SubDescriptionAxiom axiom = (SubDescriptionAxiom) assertion;
			/*
			 * We avoid redundant axioms
			 */
			if (axiom.getSub().equals(axiom.getSuper())) {
				return;
			}
			originalassertions.add(assertion);
		} else if (assertion instanceof PropertyFunctionalAxiom) {
			functionalAxioms.add((PropertyFunctionalAxiom) assertion);
		} else if (assertion instanceof DisjointDescriptionAxiom) {
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
		return originalassertions;
	}
	
	@Override 
	public Set<PropertyFunctionalAxiom> getFunctionalPropertyAxioms() {
		return functionalAxioms;
	}
	
	@Override 
	public Set<DisjointDescriptionAxiom> getDisjointDescriptionAxioms() {
		return disjointAxioms;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("[Ontology info.");
		if (originalassertions != null) {
			str.append(String.format(" Axioms: %d", originalassertions.size()));
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
	public void addAssertions(Collection<Axiom> ass) {
//		isSaturated = false;
		for (Axiom axiom : ass) {
			addAssertion(axiom);
		}
	}

	@Override
	public void addConcept(Predicate cd) {
		concepts.add(cd);
	}

	@Override
	public void addConcepts(Collection<Predicate> cd) {
//		isSaturated = false;
		for (Predicate p : cd) {
			concepts.add(p);
		}
	}

	@Override
	public void addRole(Predicate rd) {
//		isSaturated = false;
		roles.add(rd);
	}

	@Override
	public void addRoles(Collection<Predicate> rd) {
//		isSaturated = false;
		roles.addAll(rd);
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
	public Set<Predicate> getVocabulary() {
		HashSet<Predicate> set = new HashSet<Predicate>();
		set.addAll(getConcepts());
		set.addAll(getRoles());
		return set;
	}

	@Override
	public String getUri() {
		return ontouri;
	}

	/***
	 * This will return all the assertions whose right side concept description
	 * refers to the predicate 'pred'
	 */
/*	
	public Set<SubDescriptionAxiom> getByIncluding(Predicate pred) {
		return rightAssertionIndex.get(pred);
	}
*/
	/***
	 * As before but it will only return assertions where the right side is an
	 * existential role concept description
	 */
	public Set<SubDescriptionAxiom> getByIncludingExistOnly(Predicate pred) {
		return rightExistentialIndex.get(pred);
	}

	public Set<SubDescriptionAxiom> getByIncludingNoExist(Predicate pred) {
		return rightNonExistentialIndex.get(pred);
	}
/*
	@Override
	public Set<SubDescriptionAxiom> getByIncluded(Predicate pred) {
		return leftAssertionIndex.get(pred);
	}
*/
	/*
	@Override
	public void saturate() {
		if (isSaturated) {
			return;
		}
//		log.debug("Given assertions: {}", originalassertions);
		
		// Our strategy requires that for every aciom R ISA S, we also have the
		// axioms \exists R ISA \exist S and \exists R- ISA \exists S- this
		// allows us to keep the cycles to a minimum
		//
		originalassertions.addAll(computeExistentials());
		saturateAssertions();
//		log.debug("Computed assertions: {}", this.originalassertions);
		isSaturated = true;
	}
*/
	/***
	 * Saturates the set of assertions and creates the indexes for these based
	 * on their predicates. It only takes into account positive inclusions. PIs
	 * with qualififed existntital concepts are ignored.
	 * 
	 * To saturate, we do
	 * 
	 * For each pair of assertions C1 ISA C2, C2 ISA C3, we compute C1 ISA C3.
	 */
/*	
	private void saturateAssertions() {
		assertions = new HashSet<SubDescriptionAxiom>();
		leftAssertionIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();
		rightAssertionIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();
		rightNonExistentialIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();
		rightExistentialIndex = new HashMap<Predicate, Set<SubDescriptionAxiom>>();

		
		// Loading the initial assertions, filtering postive inlusions and
		// indexing
		
		for (Axiom assertion : originalassertions) {
			if (assertion instanceof SubDescriptionAxiom) {
				SubDescriptionAxiom pi = (SubDescriptionAxiom) assertion;
				assertions.add(pi);
				index(pi);
			}
		}

		// Saturating is-a hierachy loop 
		boolean loop = true;
		while (loop) {
			loop = false;
			HashSet<SubDescriptionAxiom> newInclusions = new HashSet<SubDescriptionAxiom>();
			for (SubDescriptionAxiom pi1 : assertions) {
				for (SubDescriptionAxiom pi2 : assertions) {
					if ((pi1 instanceof SubClassAxiomImpl) && (pi2 instanceof SubClassAxiomImpl)) {
						SubClassAxiomImpl ci1 = (SubClassAxiomImpl) pi1;
						SubClassAxiomImpl ci2 = (SubClassAxiomImpl) pi2;
						if (ci1.getSuper().equals(ci2.getSub())) {
							SubClassAxiomImpl newinclusion = (SubClassAxiomImpl) ofac.createSubClassAxiom(ci1.getSub(), ci2.getSuper());
							newInclusions.add(newinclusion);
						} else if (ci1.getSub().equals(ci2.getSuper())) {
							SubClassAxiomImpl newinclusion = (SubClassAxiomImpl) ofac.createSubClassAxiom(ci2.getSub(), ci1.getSuper());
							newInclusions.add(newinclusion);
						}
					} else if ((pi1 instanceof SubPropertyAxiomImpl) && (pi2 instanceof SubPropertyAxiomImpl)) {
						SubPropertyAxiomImpl ci1 = (SubPropertyAxiomImpl) pi1;
						SubPropertyAxiomImpl ci2 = (SubPropertyAxiomImpl) pi2;
						if (ci1.getSuper().equals(ci2.getSub())) {
							SubPropertyAxiomImpl newinclusion = (SubPropertyAxiomImpl) ofac.createSubPropertyAxiom(ci1.getSub(),
									ci2.getSuper());
							newInclusions.add(newinclusion);
						} else if (ci1.getSub().equals(ci2.getSuper())) {
							SubPropertyAxiomImpl newinclusion = (SubPropertyAxiomImpl) ofac.createSubPropertyAxiom(ci2.getSub(),
									ci1.getSuper());
							newInclusions.add(newinclusion);
						}
					}
				}
			}

			loop = loop || assertions.addAll(newInclusions);
			if (loop) {
				indexAll(newInclusions);
			}
		}
	}
*/
/*	
	private void indexAll(Collection<SubDescriptionAxiom> pis) {
		for (SubDescriptionAxiom pi : pis) {
			index(pi);
		}
	}
*/
/*
	private void index(SubDescriptionAxiom pi) {
		if (pi instanceof SubClassAxiomImpl) {
			SubClassAxiomImpl cpi = (SubClassAxiomImpl) pi;
			ClassDescription description1 = cpi.getSub();
			ClassDescription description2 = cpi.getSuper();

			// Processing right side 
			if (description2 instanceof ClassImpl) {
				ClassImpl acd = (ClassImpl) description2;
				Set<SubDescriptionAxiom> rightAssertion = getRight(acd.getPredicate());
				rightAssertion.add(pi);
				Set<SubDescriptionAxiom> rightNonExistential = getRightNotExistential(acd.getPredicate());
				rightNonExistential.add(pi);
			} else if (description2 instanceof DataType) {
				DataType acd = (DataType) description2;
				Set<SubDescriptionAxiom> rightAssertion = getRight(acd.getPredicate());
				rightAssertion.add(pi);
				Set<SubDescriptionAxiom> rightNonExistential = getRightNotExistential(acd.getPredicate());
				rightNonExistential.add(pi);
			} else if (description2 instanceof PropertySomeRestrictionImpl) {
				PropertySomeRestrictionImpl ecd = (PropertySomeRestrictionImpl) description2;
				Set<SubDescriptionAxiom> rightAssertion = getRight(ecd.getPredicate());
				rightAssertion.add(pi);
				Set<SubDescriptionAxiom> rightExistential = getRightExistential(ecd.getPredicate());
				rightExistential.add(pi);
			}

			// Processing left side 
			if (description1 instanceof ClassImpl) {
				ClassImpl acd = (ClassImpl) description1;
				Set<SubDescriptionAxiom> leftAssertion = getLeft(acd.getPredicate());
				leftAssertion.add(pi);
			} else if (description1 instanceof DataType) {
				DataType acd = (DataType) description1;
				Set<SubDescriptionAxiom> leftAssertion = getLeft(acd.getPredicate());
				leftAssertion.add(pi);
			} else if (description1 instanceof PropertySomeRestrictionImpl) {
				PropertySomeRestrictionImpl ecd = (PropertySomeRestrictionImpl) description1;
				Set<SubDescriptionAxiom> leftAssertion = getLeft(ecd.getPredicate());
				leftAssertion.add(pi);
			}
		} else if (pi instanceof SubPropertyAxiomImpl) {
			SubPropertyAxiomImpl cpi = (SubPropertyAxiomImpl) pi;
			Property description1 = cpi.getSub();
			Property description2 = cpi.getSuper();

			// Processing right side 
			if (description2 instanceof PropertyImpl) {
				PropertyImpl acd = (PropertyImpl) description2;
				Set<SubDescriptionAxiom> rightAssertion = getRight(acd.getPredicate());
				rightAssertion.add(pi);
				Set<SubDescriptionAxiom> rightNonExistential = getRightNotExistential(acd.getPredicate());
				rightNonExistential.add(pi);
			}
			// Processing left side 
			if (description1 instanceof PropertyImpl) {
				PropertyImpl acd = (PropertyImpl) description1;
				Set<SubDescriptionAxiom> leftAssertion = getLeft(acd.getPredicate());
				leftAssertion.add(pi);

			}

		}
	}
*/
/*	
	private Set<SubDescriptionAxiom> getRight(Predicate pred) {
		Set<SubDescriptionAxiom> assertions = rightAssertionIndex.get(pred);
		if (assertions == null) {
			assertions = new LinkedHashSet<SubDescriptionAxiom>();
			rightAssertionIndex.put(pred, assertions);
		}
		return assertions;
	}

	private Set<SubDescriptionAxiom> getLeft(Predicate pred) {
		Set<SubDescriptionAxiom> assertions = leftAssertionIndex.get(pred);
		if (assertions == null) {
			assertions = new LinkedHashSet<SubDescriptionAxiom>();
			leftAssertionIndex.put(pred, assertions);
		}
		return assertions;
	}
*/
/*	
	private Set<SubDescriptionAxiom> getRightNotExistential(Predicate pred) {
		Set<SubDescriptionAxiom> assertions = rightNonExistentialIndex.get(pred);
		if (assertions == null) {
			assertions = new LinkedHashSet<SubDescriptionAxiom>();
			rightNonExistentialIndex.put(pred, assertions);
		}
		return assertions;
	}

	private Set<SubDescriptionAxiom> getRightExistential(Predicate pred) {
		Set<SubDescriptionAxiom> assertions = rightExistentialIndex.get(pred);
		if (assertions == null) {
			assertions = new LinkedHashSet<SubDescriptionAxiom>();
			rightExistentialIndex.put(pred, assertions);
		}
		return assertions;
	}
*/
	/***
	 * This method adds to the TBox a pair of axioms ER ISA ES and ER- ISA ES-
	 * for each role inclusion R ISA S found in the ontology.
	 * 
	 * @return The set of extra existential assertions that need to be added to
	 *         the ontology to account for the semantics of role inclusions
	 *         w.r.t. their domains and ranges.
	 */
/*	
	private Set<Axiom> computeExistentials() {
		HashSet<Axiom> newassertion = new HashSet<Axiom>(1000);
		for (Axiom assertion : originalassertions) {
			if (assertion instanceof SubPropertyAxiomImpl) {
				SubPropertyAxiomImpl rinclusion = (SubPropertyAxiomImpl) assertion;
				Property r1 = rinclusion.getSub();
				Property r2 = rinclusion.getSuper();

				PropertySomeRestriction e11 = ofac.createPropertySomeRestriction(r1.getPredicate(), r1.isInverse());
				PropertySomeRestriction e12 = ofac.createPropertySomeRestriction(r2.getPredicate(), r2.isInverse());
				PropertySomeRestriction e21 = ofac.createPropertySomeRestriction(r1.getPredicate(), !r1.isInverse());
				PropertySomeRestriction e22 = ofac.createPropertySomeRestriction(r2.getPredicate(), !r2.isInverse());

				SubClassAxiomImpl inc1 = (SubClassAxiomImpl) ofac.createSubClassAxiom(e11, e12);
				SubClassAxiomImpl inc2 = (SubClassAxiomImpl) ofac.createSubClassAxiom(e21, e22);
				newassertion.add(inc1);
				newassertion.add(inc2);
			}
		}
		return newassertion;
	}
*/
	@Override
	public void addEntities(Set<Predicate> referencedEntities) {
		for (Predicate pred : referencedEntities) {
			addEntity(pred);
		}
	}
	
	@Override
	public void addEntity(Predicate pred) {
		if (pred.getArity() == 1) {
			addConcept(pred);
		} else {
			addRole(pred);
		}
	}
}
