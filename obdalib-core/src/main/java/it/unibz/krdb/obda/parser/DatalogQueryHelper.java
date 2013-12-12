package it.unibz.krdb.obda.parser;

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
