package it.unibz.krdb.obda.io;

import java.util.Map;

public abstract class AbstractPrefixManager implements PrefixManager {

	public String getShortForm(String uri) {
		return getShortForm(uri, false);
	}
	
	public String getShortForm(String uri, boolean insideQuotes) {
		final Map<String, String> prefixMap = getPrefixMap();
		String shortUri = uri;
		for (String prefixUriDefinition : prefixMap.values()) {
			if (uri.contains(prefixUriDefinition)) {
				String prefix = getPrefix(prefixUriDefinition);
				if (insideQuotes) {
					prefix = String.format("&%s;", removeColon(prefix));
				}
				// Replace the URI with the corresponding prefix.
				shortUri = uri.replace(prefixUriDefinition, prefix);
				
				// Clean the URI string from <> signs, if exist
				if (shortUri.contains("<") && shortUri.contains(">")) {
					shortUri = shortUri.replace("<", "").replace(">", "");
				}
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
