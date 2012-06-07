package it.unibz.krdb.obda.io;

import java.util.HashMap;
import java.util.Set;

public class SimplePrefixManager extends AbstractPrefixManager {

	/**
	 * A simple map containing for each ontology URI the correponding prefix
	 */
	private HashMap<String, String> uriToPrefixMap;

	/**
	 * A simple map containing for each prefix the correponding ontology URI
	 */
	private HashMap<String, String> prefixToURIMap;

	/**
	 * The default constructor. It creates a new instance of the prefix manager
	 */
	public SimplePrefixManager() {
		uriToPrefixMap = new HashMap<String, String>();
		prefixToURIMap = new HashMap<String, String>();
	}

	@Override
	public void addPrefix(String prefix, String uri) {
		prefixToURIMap.put(prefix, getProperPrefixURI(uri));
		uriToPrefixMap.put(getProperPrefixURI(uri), prefix);
	}

	/**
	 * Returns the corresponding URI definition for the given prefix
	 * 
	 * @param prefix
	 *            the prefix name
	 * @return the corresponding URI definition or null if the prefix is not
	 *         registered
	 */
	public String getURIDefinition(String prefix) {
		return prefixToURIMap.get(prefix);
	}

	/**
	 * Returns the corresponding prefix for the given URI.
	 * 
	 * @param prefix
	 *            the prefix name
	 * @return the corresponding prefix or null if the URI is not registered
	 */
	public String getPrefix(String uri) {
		return uriToPrefixMap.get(uri);
	}

	/**
	 * Returns a map with all registered prefixes and the corresponding URI
	 * 
	 * @return a hash map
	 */
	public HashMap<String, String> getPrefixMap() {
		return prefixToURIMap;
	}

	/**
	 * Checks if the prefix manager stores the prefix name.
	 * 
	 * @param prefix
	 *            The prefix name to check.
	 */
	public boolean contains(String prefix) {
		Set<String> prefixes = prefixToURIMap.keySet();
		return prefixes.contains(prefix);
	}
}
