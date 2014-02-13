package it.unibz.krdb.obda.parser;

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

import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.io.PrefixManager;

import java.util.Map;
import java.util.Set;

/**
 * Provides additional strings for constructing a proper datalog query.
 */
public class DatalogQueryHelper {

	private final PrefixManager prefixManager;

	public DatalogQueryHelper(PrefixManager prefixManager) {
		this.prefixManager = prefixManager;
	}

	public String getDefaultHead() {
		return OBDALibConstants.OBDA_PREFIX_MAPPING_PREDICATE + ":" + OBDALibConstants.OBDA_QUERY_PREDICATE + "(*)";
	}

	public String getPrefixes() {
		String prefixString = "";
		String baseString = "";

		if (prefixManager.getDefaultPrefix() != null) {
			prefixString += "BASE <"+ prefixManager.getDefaultPrefix() +">\n";
			prefixString += "PREFIX : <"+ prefixManager.getDefaultPrefix() +">\n";
		}
		Map<String, String> prefixMapping = prefixManager.getPrefixMap();
		Set<String> prefixes = prefixMapping.keySet();
		for (String prefix : prefixes) {
			if (prefix.trim().equals(":")) {
				continue;
			}
			if (prefix.trim().endsWith(":")) {
				prefixString +=
					"PREFIX " + prefix + " <" + prefixMapping.get(prefix) + ">\n";
			} else {
				prefixString +=
						"PREFIX " + prefix + ": <" + prefixMapping.get(prefix) + ">\n";
			}
		}
		prefixString = baseString + prefixString; // the base prefix should always on top.
		prefixString = prefixString+ "PREFIX "+ OBDALibConstants.OBDA_PREFIX_MAPPING_PREDICATE + ": <" + OBDALibConstants.OBDA_URI_MAPPING_PREDICATE +">\n";
		return prefixString;
	}
}
