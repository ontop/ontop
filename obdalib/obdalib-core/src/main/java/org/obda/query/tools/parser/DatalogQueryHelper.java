/*
 * @(#)DatalogQueryHelper 16/12/2010
 *
 * Copyright 2010 OBDA-API. All rights reserved.
 * Use is subject to license terms.
 */
package org.obda.query.tools.parser;

import inf.unibz.it.obda.api.io.PrefixManager;

import java.util.Map;
import java.util.Set;

/**
 * Provides additional strings for constructing a proper datalog query.
 *
 * @author Josef Hardi <josef.hardi@gmail.com>
 */

@Deprecated
public class DatalogQueryHelper {

	public static final String OBDA_PREFIX_MAPPING_PREDICATE = "obdap";
	public static final String OBDA_URI_MAPPING_PREDICATE =
		"http://obda.org/mapping/predicates/";
	public static final String DATALOG_IMPLY_SYMBOL = ":-";

	private final PrefixManager prefixManager;

	public DatalogQueryHelper(PrefixManager prefixManager) {
		this.prefixManager = prefixManager;
	}

	public String getDefaultHead() {
		return OBDA_PREFIX_MAPPING_PREDICATE + ":q(*)";
	}

	public String getPrefixes() {
		String prefixString = "";
		String baseString = "";

		if (prefixManager.getDefaultNamespace() != null) {
			prefixString += "BASE <"+ prefixManager.getDefaultNamespace() +">\n";
			prefixString += "PREFIX : <"+ prefixManager.getDefaultNamespace() +">\n";
		}
		Map<String, String> prefixMapping = prefixManager.getPrefixMap();
		Set<String> prefixes = prefixMapping.keySet();
		for (String prefix : prefixes) {
//			if (prefix.equals("version"))  continue;

//			if (prefix.equals("xml:base"))
//				baseString =
//					"BASE <" + prefixMapping.get(prefix) + ">\n";
//			else if (prefix.equals("xmlns"))
//				prefixString +=
//					"PREFIX : <" + prefixMapping.get(prefix) + ">\n";
//			else
				prefixString +=
					"PREFIX " + prefix + ": <" + prefixMapping.get(prefix) + ">\n";
		}
		prefixString = baseString + prefixString; // the base prefix should always on top.
		prefixString = prefixString+ "PREFIX "+ OBDA_PREFIX_MAPPING_PREDICATE + ": <" + OBDA_URI_MAPPING_PREDICATE +">\n";
		return prefixString;
	}
}
