package it.unibz.krdb.obda.ontology;

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

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

public interface Ontology extends Cloneable, Serializable {

	public void addAssertion(Axiom assertion);

	public void addAssertions(Collection<Axiom> assertion);

	public void addEntity(Predicate c);
	
	public void addConcept(Predicate c);

	public void addRole(Predicate role);

	public void addConcepts(Collection<Predicate> cd);

	public void addRoles(Collection<Predicate> rd);

	public Set<Predicate> getRoles();

	public Set<Predicate> getConcepts();
	
	public Set<Predicate> getVocabulary();

	public Set<Axiom> getAssertions();

//	public boolean referencesPredicate(Predicate pred);

//	public boolean referencesPredicates(Collection<Predicate> preds);

	/***
	 * This will retrun all the assertions whose right side concept description
	 * refers to the predicate 'pred'
	 */
//	public Set<SubDescriptionAxiom> getByIncluding(Predicate pred);

	/***
	 * As before but it will only return assetions where the right side is an
	 * existential role concept description
	 */
	public Set<SubDescriptionAxiom> getByIncludingExistOnly(Predicate pred);

	public Set<SubDescriptionAxiom> getByIncludingNoExist(Predicate pred);

//	public Set<SubDescriptionAxiom> getByIncluded(Predicate pred);

	public String getUri();

	/**
	 * This will saturate the ontology, i.e. it will make sure that all axioms
	 * implied by this ontology are asserted in the ontology and accessible
	 * through the methods of the ontology.
	 */
//	public void saturate();

	public Ontology clone();

	public void addEntities(Set<Predicate> referencedEntities);

	/**
	 * @return
	 */
	public Set<Assertion> getABox();
	
	public Set<PropertyFunctionalAxiom> getFunctionalPropertyAxioms();
	
	public Set<DisjointDescriptionAxiom> getDisjointDescriptionAxioms();
}
