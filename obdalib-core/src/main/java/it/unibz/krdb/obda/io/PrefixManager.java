package it.unibz.krdb.obda.io;

import java.util.Map;

/**
 * Abstracts the prefix mapping mechanism.
 */
public interface PrefixManager {
	
	public static final String DEFAULT_PREFIX = ":";
	
	/**
	 * Registers a prefix. Leave blank for BASE prefix.
	 * 	 
	 * @param name
	 * 			The prefix name (without the colon).
	 * @param uri
	 * 			The URI definition for the given prefix.
	 */
	public void addPrefix(String prefix, String uri);

	public String getURIDefinition(String prefix);

	public String getPrefix(String uri);

	public String getDefaultPrefix();
	
	public Map<String, String> getPrefixMap();

	public String getShortForm(String uri);

	public String getShortForm(String uri, boolean insideQuotes);

	public String getExpandForm(String prefixedName);
	
	public String getExpandForm(String prefixedName, boolean insideQuotes);
	
	public boolean contains(String prefix);
	
	public void clear();
}
