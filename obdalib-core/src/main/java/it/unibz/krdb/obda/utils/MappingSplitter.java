package it.unibz.krdb.obda.utils;

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

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class split the mappings
 * 
 *  <pre> q1, q2, ... qn <- SQL </pre>
 *  
 *   into n mappings
 *   
 *  <pre> q1 <-SQL , ..., qn <- SQL </pre>
 * 
 * 
 * @author xiao
 *
 */
public class MappingSplitter {

	public static Collection<OBDAMappingAxiom> splitMappings(Collection<OBDAMappingAxiom> mappings) {

		List<OBDAMappingAxiom> newMappings = new LinkedList<>();
		
		OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

		for (OBDAMappingAxiom mapping : mappings) {
			List<Function> targetQuery = mapping.getTargetQuery();

			if (targetQuery.size() == 1) {
				// For mappings with only one body atom, we do not need to change it
				newMappings.add(mapping);
			} 
			else {
				String id = mapping.getId();
				OBDASQLQuery sourceQuery = mapping.getSourceQuery();

				for (Function bodyAtom : targetQuery) {
					String newId = IDGenerator.getNextUniqueID(id + "#");
					OBDAMappingAxiom newMapping = dfac.getRDBMSMappingAxiom(newId, sourceQuery, Collections.singletonList(bodyAtom));
					newMappings.add(newMapping);
				}
			}
		}

		return newMappings;
	}
}
