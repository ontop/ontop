package org.semanticweb.ontop.owlapi3;

/*
 * #%L
 * ontop-obdalib-owlapi3
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
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
