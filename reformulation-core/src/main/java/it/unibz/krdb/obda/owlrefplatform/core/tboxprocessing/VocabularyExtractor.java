/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.SubDescriptionAxiom;

import java.util.HashSet;
import java.util.Set;

/***
 * Extracts the vocabulary of an ontology.
 * 
 * Warning, it only suports subclass and subproperty axioms and descriptions of
 * the form.
 * 
 * R, R-, A, \exists R, \exists R-
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class VocabularyExtractor {

	public static Set<Predicate> getVocabulary(Ontology ontology) {
		Set<Predicate> result = new HashSet<Predicate>();

		for (Axiom axiom : ontology.getAssertions()) {
			if (axiom instanceof SubDescriptionAxiom) {
				SubDescriptionAxiom subClass = (SubDescriptionAxiom) axiom;
				result.add(getPredicate(subClass.getSub()));
				result.add(getPredicate(subClass.getSuper()));
			}
		}

		return result;
	}

	public static Predicate getPredicate(Description e) {
		if (e instanceof OClass) {
			return ((OClass) e).getPredicate();
		}
		if (e instanceof PropertySomeRestriction) {
			return ((PropertySomeRestriction) e).getPredicate();
		}
		if (e instanceof Property) {
			return ((Property) e).getPredicate();
		}
		return null;
	}
}
