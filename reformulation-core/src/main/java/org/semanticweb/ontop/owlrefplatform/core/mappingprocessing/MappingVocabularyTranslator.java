package org.semanticweb.ontop.owlrefplatform.core.mappingprocessing;

/*
 * #%L
 * ontop-reformulation-core
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDASQLQuery;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.Description;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.OntologyFactory;
import org.semanticweb.ontop.ontology.Property;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;

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
