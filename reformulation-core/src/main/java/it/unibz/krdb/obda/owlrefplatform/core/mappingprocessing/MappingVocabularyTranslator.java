/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MappingVocabularyTranslator {

	private static OntologyFactory	fac		= OntologyFactoryImpl.getInstance();
	private static OBDADataFactory	dfac	= OBDADataFactoryImpl.getInstance();

	/***
	 * Given a collection of mappings and an equivalence map for classes and
	 * properties, it returns a new collection in which all references to
	 * class/properties with equivalents has been removed and replaced by the
	 * equivalents.
	 * 
	 * For example, given the map hasFather -> inverse(hasChild)
	 * 
	 * If there is a mapping:
	 * 
	 * q(x,y):- hasFather(x,y) <- SELECT x, y FROM t
	 * 
	 * This will be replaced by the mapping
	 * 
	 * q(x,y):- hasChild(y,x) <- SELECT x, y FROM t
	 * 
	 * The same is done for classes.
	 * 
	 * @param originalMappings
	 * @param equivalencesMap
	 * @return
	 */
	public Collection<OBDAMappingAxiom> translateMappings(Collection<OBDAMappingAxiom> originalMappings,
			Map<Predicate, Description> equivalencesMap) {
		Collection<OBDAMappingAxiom> result = new LinkedList<OBDAMappingAxiom>();
		for (OBDAMappingAxiom mapping : originalMappings) {
			
			CQIE targetQuery = (CQIE) mapping.getTargetQuery();
			List<Function> body = targetQuery.getBody();
			List<Function> newbody = new LinkedList<Function>();

			for (Function atom : body) {
				Predicate p = atom.getPredicate();
				Function newatom = null;
				if (p.getArity() == 1) {
//					Description description = fac.createClass(p);
					Description equivalent = equivalencesMap.get(p);
					if (equivalent == null)
						newatom = atom;
					else {
						newatom = dfac.getFunction(((OClass) equivalent).getPredicate(), atom.getTerms());

					}
				} else {
//					Description description = fac.createProperty(p);
					Description equivalent = equivalencesMap.get(p);
					if (equivalent == null)
						newatom = atom;
					else {
						Property equiprop = (Property) equivalent;
						if (!equiprop.isInverse()) {
							newatom = dfac.getFunction(equiprop.getPredicate(), atom.getTerms());
						} else {
							newatom = dfac.getFunction(equiprop.getPredicate(), atom.getTerms().get(1), atom.getTerm(0));
						}
					}
				}
				newbody.add(newatom);
			}
			CQIE newTargetQuery = dfac.getCQIE(targetQuery.getHead(), newbody);
			result.add(dfac.getRDBMSMappingAxiom(mapping.getId(),((OBDASQLQuery) mapping.getSourceQuery()).toString(), newTargetQuery));

		}
		return result;

	}
}
