package it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing;

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
import it.unibz.krdb.obda.owlrefplatform.core.EquivalenceMap;

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
			EquivalenceMap equivalencesMap) {
		Collection<OBDAMappingAxiom> result = new LinkedList<OBDAMappingAxiom>();
		for (OBDAMappingAxiom mapping : originalMappings) {
			
			CQIE targetQuery = (CQIE) mapping.getTargetQuery();
			List<Function> body = targetQuery.getBody();
			List<Function> newbody = new LinkedList<Function>();

			for (Function atom : body) {
				Function newatom = equivalencesMap.getNormal(atom);
				newbody.add(newatom);
			}
			CQIE newTargetQuery = dfac.getCQIE(targetQuery.getHead(), newbody);
			result.add(dfac.getRDBMSMappingAxiom(mapping.getId(),((OBDASQLQuery) mapping.getSourceQuery()).toString(), newTargetQuery));

		}
		return result;

	}
}
