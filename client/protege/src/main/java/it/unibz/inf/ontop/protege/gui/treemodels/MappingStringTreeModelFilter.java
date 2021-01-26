package it.unibz.inf.ontop.protege.gui.treemodels;

/*
 * #%L
 * ontop-protege
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

import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;

/**
 * This filter receives a string in the constructor and returns true if accepts
 * any mapping containing the string in the head or body
 */
public class MappingStringTreeModelFilter extends TreeModelFilter<SQLPPTriplesMap> {

	public MappingStringTreeModelFilter() {
		super.bNegation = false;
	}

	@Override
	public boolean match(SQLPPTriplesMap object) {
		boolean isMatch = false;
		for (String keyword : vecKeyword) {
			// Check in the Mapping ID
			String mappingId = object.getId();
			isMatch = mappingId.contains(keyword.trim());
			if (isMatch) {
				break; // end loop if a match is found!
			}

			// Check in the Mapping Target Query
			for (TargetAtom targetAtom : object.getTargetAtoms()) {
				isMatch = isMatch || TreeModelTools.match(keyword.trim(), targetAtom);
			}
			if (isMatch) {
				break; // end loop if a match is found!
			}

			// Check in the Mapping Source Query
			SQLPPSourceQuery query = object.getSourceQuery();
			isMatch = query.getSQL().contains(keyword.trim());
			if (isMatch) {
				break; // end loop if a match is found!
			}
		}
		// no match found!
		return bNegation != isMatch;
	}
}
