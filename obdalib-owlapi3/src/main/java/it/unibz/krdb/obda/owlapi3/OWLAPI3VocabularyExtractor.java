/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlapi3;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

/***
 * Extracts all declared Classes, Object and Data properties and translate them
 * into obdalib Predicate objects.
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class OWLAPI3VocabularyExtractor {

	OBDADataFactory obdaFac = OBDADataFactoryImpl.getInstance();

	/***
	 * Returns the vocabulary of classes and properties of a set of ontologies
	 * 
	 * @param ontologies
	 * @return
	 */
	public Set<Predicate> getVocabulary(Collection<OWLOntology> ontologies) {

		Set<Predicate> predicates = new HashSet<Predicate>();
		for (OWLOntology ontology : ontologies) {
			predicates.addAll(getVocabulary(ontology));
		}
		return predicates;
	}

	/***
	 * Returns the vocabulary of classes and properties of an Ontology
	 * 
	 * @param ontologies
	 * @return
	 */
	public Set<Predicate> getVocabulary(OWLOntology ontology) {
		Set<OWLEntity> vocabulary = new HashSet<OWLEntity>();
		for (OWLAxiom axiom : ontology.getAxioms()) {
			vocabulary.addAll(axiom.getClassesInSignature());
			vocabulary.addAll(axiom.getDataPropertiesInSignature());
			vocabulary.addAll(axiom.getObjectPropertiesInSignature());
		}
		return getVocabulary(vocabulary);
	}

	/***
	 * Returns the vocabulary of classes and properties of an Iterator of
	 * declaration axioms
	 * 
	 * @param ontologies
	 * @return
	 */
	public Set<Predicate> getVocabulary(Iterable<OWLEntity> declarations) {
		return getVocabulary(declarations.iterator());
	}

	/***
	 * Returns the vocabulary of classes and properties of an Iterator of
	 * declaration axioms
	 * 
	 * @param ontologies
	 * @return
	 */
	public Set<Predicate> getVocabulary(Iterator<OWLEntity> declarations) {
		Set<Predicate> predicates = new HashSet<Predicate>();
		while (declarations.hasNext()) {
			// OWLDeclarationAxiom axiom = declarations.next();
			OWLEntity entity = declarations.next();
			Predicate predicate = getPredicate(entity);
			if (predicate != null)
				predicates.add(predicate);
		}
		return predicates;
	}

	/***
	 * Returns a predicate for Classes, Object and Data Properties
	 * 
	 * @param entity
	 * @return
	 */
	private Predicate getPredicate(OWLEntity entity) {

		Predicate predicate = null;
		if (entity instanceof OWLClass) {
			OWLClass c = (OWLClass) entity;
			if (c.isOWLThing() || c.isOWLNothing())
				return null;
			predicate = obdaFac.getPredicate(entity.getIRI().toString(), 1, new Predicate.COL_TYPE[] { COL_TYPE.OBJECT });
		} else if (entity instanceof OWLObjectProperty) {
			predicate = obdaFac.getPredicate(entity.getIRI().toString(), 2, new Predicate.COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
		} else if (entity instanceof OWLDataProperty) {
			predicate = obdaFac.getPredicate(entity.getIRI().toString(), 2, new Predicate.COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		}
		return predicate;
	}

}
