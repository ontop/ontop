package it.unibz.krdb.obda.io;

import java.util.List;

public abstract class AbstractPrefixManager implements PrefixManager {

	public abstract List<String> getNamespaceList();
	
	public String getShortForm(String uri) {
		return getShortForm(uri, false);
	}
	
	public String getShortForm(String uri, boolean insideQuotes) {
		final List<String> namespaceList = getNamespaceList();
		
		/* Clean the URI string from <...> signs, if they exist.
		 * <http://www.example.org/library#Book> --> http://www.example.org/library#Book
		 */
		if (uri.contains("<") && uri.contains(">")) {
			uri = uri.replace("<", "").replace(">", "");
		}
		
		// Initial value for the short URI
		String shortUri = uri;
		
		// Check if the URI string has a matched prefix
		for (String prefixUriDefinition : namespaceList) {
			if (uri.contains(prefixUriDefinition)) {
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
