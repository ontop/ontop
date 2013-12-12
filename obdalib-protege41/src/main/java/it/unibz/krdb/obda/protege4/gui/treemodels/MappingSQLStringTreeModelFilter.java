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

import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.protege4.gui.treemodels.TreeModelFilter;

/**
 * This filter receives a string in the constructor and returns true if any mapping contains the string in the body.
 */
public class MappingSQLStringTreeModelFilter extends TreeModelFilter<OBDAMappingAxiom> {

	public MappingSQLStringTreeModelFilter() {
		super.bNegation = false;
	}

	@Override
	public boolean match(OBDAMappingAxiom object) {
		final OBDASQLQuery bodyquery = (OBDASQLQuery) object.getSourceQuery();

		boolean isMatch = false;
		for (String keyword : vecKeyword) {
			isMatch = match(keyword.trim(), bodyquery.toString());
			if (isMatch) {
				break; // end loop if a match is found!
			}
		}
		// no match found!
		return (bNegation ? !isMatch : isMatch);
	}

	/** A helper method to check a match */
	public static boolean match(String keyword, String query) {
		if (query.indexOf(keyword) != -1) { // match found!
			return true;
		}
		return false;
	}
}
