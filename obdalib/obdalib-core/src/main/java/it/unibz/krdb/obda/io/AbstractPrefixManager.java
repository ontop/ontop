package it.unibz.krdb.obda.io;

import java.util.Map;

public abstract class AbstractPrefixManager implements PrefixManager {

	public String getShortForm(String uri) {
		return getShortForm(uri, false);
	}
	
	public String getShortForm(String uri, boolean insideQuotes) {
		final Map<String, String> prefixMap = getPrefixMap();
		
		/* Clean the URI string from <...> signs, if they exist.
		 * <http://www.example.org/library#Book> --> http://www.example.org/library#Book
		 */
		if (uri.contains("<") && uri.contains(">")) {
			uri = uri.replace("<", "").replace(">", "");
		}
		
		// Initial value for the short URI
		String shortUri = uri;
		
		String subUri = "";
		if (uri.contains("#")) {
			/* 
			 * Obtain the URI definition that contains a hash sign
			 * http://www.example.org/library#Book --> http://www.example.org/library#
			 */
			subUri = uri.substring(0, uri.lastIndexOf("#")+1);
		} else {
			/* 
			 * Obtain the URI definition that contains a slash sign
			 * http://www.example.org/library/Book --> http://www.example.org/library/
			 */
			subUri = uri.substring(0, uri.lastIndexOf("/")+1);
		}
		
		// Check if the URI string has a matched prefix
		for (String prefixUriDefinition : prefixMap.values()) {
			if (subUri.equals(prefixUriDefinition)) {
				String prefix = getPrefix(prefixUriDefinition);
				if (insideQuotes) {
					prefix = String.format("&%s;", removeColon(prefix));
				}
				// Replace the URI with the corresponding prefix.
				shortUri = uri.replace(prefixUriDefinition, prefix);
				
				return shortUri;
			}
		}
		return shortUri;
	}
	
	public String getDefaultPrefix() {
		return getPrefixMap().get(DEFAULT_PREFIX);
	}
		
	/**
	 * A utility method to ensure a proper naming for prefix URI
	 */
	protected String getProperPrefixURI(String prefixUri) {
		if (!prefixUri.endsWith("/")) {
			if (!prefixUri.endsWith("#")) {
				prefixUri += "#";
			}
		}
		return prefixUri;
	} 
	
	private String removeColon(String prefix) {
		if (prefix.equals(PrefixManager.DEFAULT_PREFIX)) {
			return prefix; // TODO Remove this code in the future.
		}
		return prefix.replace(":", "");
	}
}
