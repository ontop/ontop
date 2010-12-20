/*
 * @(#)DatalogQueryHelper 16/12/2010
 *
 * Copyright 2010 OBDA-API. All rights reserved.
 * Use is subject to license terms.
 */
package org.obda.query.tools.parser;

import inf.unibz.it.obda.api.io.PrefixManager;

import java.net.URI;
import java.util.HashMap;
import java.util.Set;

/**
 * Provides additional strings for constructing a proper datalog query.
 *
 * @author Josef Hardi <josef.hardi@gmail.com>
 */
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

		HashMap<String, URI> prefixMapping = prefixManager.getPrefixMap();
		Set<String> prefixes = prefixMapping.keySet();
		for (String prefix : prefixes) {
			if (prefix.equals("version"))  continue;

			if (prefix.equals("xml:base"))
				baseString =
					"BASE <" + prefixMapping.get(prefix).toString() + ">\n";
			else if (prefix.equals("xmlns"))
				prefixString +=
					"PREFIX : <" + prefixMapping.get(prefix).toString() + ">\n";
			else
				prefixString +=
					"PREFIX " + prefix +
					": <" + prefixMapping.get(prefix).toString() + ">\n";
		}
		prefixString = baseString + prefixString; // the base prefix should always on top.
		return prefixString;
	}
}
