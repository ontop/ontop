package it.unibz.krdb.obda.protege4.gui.treemodels;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
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
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.impl.CQIEImpl;
import it.unibz.krdb.obda.protege4.gui.treemodels.MappingHeadVariableTreeModelFilter;
import it.unibz.krdb.obda.protege4.gui.treemodels.MappingIDTreeModelFilter;
import it.unibz.krdb.obda.protege4.gui.treemodels.MappingSQLStringTreeModelFilter;
import it.unibz.krdb.obda.protege4.gui.treemodels.TreeModelFilter;

import java.util.List;

/**
 * This filter receives a string in the constructor and returns true if accepts
 * any mapping containing the string in the head or body
 */
public class MappingStringTreeModelFilter extends TreeModelFilter<OBDAMappingAxiom> {

	public MappingStringTreeModelFilter() {
		super.bNegation = false;
	}

	@Override
	public boolean match(OBDAMappingAxiom object) {
		boolean isMatch = false;
		for (String keyword : vecKeyword) {
			// Check in the Mapping ID
			final String mappingId = object.getId();
			isMatch = MappingIDTreeModelFilter.match(keyword.trim(), mappingId);
			if (isMatch) {
				break; // end loop if a match is found!
			}

			// Check in the Mapping Target Query
			final CQIE headquery = (CQIEImpl) object.getTargetQuery();
			final List<Function> atoms = headquery.getBody();
			for (int i = 0; i < atoms.size(); i++) {
				Function predicate = (Function) atoms.get(i);
				isMatch = isMatch || MappingHeadVariableTreeModelFilter.match(keyword.trim(), predicate);
			}
			if (isMatch) {
				break; // end loop if a match is found!
			}

			// Check in the Mapping Source Query
			final OBDASQLQuery query = (OBDASQLQuery) object.getSourceQuery();
			isMatch = MappingSQLStringTreeModelFilter.match(keyword.trim(), query.toString());
			if (isMatch) {
				break; // end loop if a match is found!
			}
		}
		// no match found!
		return (bNegation ? !isMatch : isMatch);
	}
}
