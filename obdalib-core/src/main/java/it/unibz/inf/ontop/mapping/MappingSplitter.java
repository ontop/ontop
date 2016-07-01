package it.unibz.inf.ontop.mapping;

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

import java.util.*;

import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.model.*;

import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.utils.IDGenerator;

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

	public static List<OBDAMappingAxiom> splitMappings(Collection<OBDAMappingAxiom> mappingAxioms,
														NativeQueryLanguageComponentFactory nativeQLFactory) {

		List<OBDAMappingAxiom> newMappings = new ArrayList<>();
		
		OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

		for (OBDAMappingAxiom mapping : mappingAxioms) {
			List<Function> bodyAtoms = mapping.getTargetQuery();

			if(bodyAtoms.size() == 1){
				// For mappings with only one body atom, we do not need to change it
				newMappings.add(mapping);
			}
			else {
				String id = mapping.getId();

				for (Function bodyAtom : bodyAtoms) {
					String newId = IDGenerator.getNextUniqueID(id + "#");

					OBDAMappingAxiom newMapping = nativeQLFactory.create(newId, mapping.getSourceQuery(),
							Collections.singletonList(bodyAtom));
					newMappings.add(newMapping);
				}
			}
		}

		return newMappings;
	}
}
