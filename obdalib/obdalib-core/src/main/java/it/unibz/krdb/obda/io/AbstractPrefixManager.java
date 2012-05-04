package it.unibz.krdb.obda.io;

import java.util.Collection;
import java.util.Iterator;

/**
 * The prefix manager is administrating the prefixes for ontolgyie. It allows to
 * register and unregister prefixes for ontolgies and to query them.
 * 
 * @author Manfred Gerstgrasser
 * 
 */

public abstract class AbstractPrefixManager implements PrefixManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2034866002642539028L;
	private String defaultNamespace = null;

	@Override
	public String getDefaultNamespace() {
		return defaultNamespace;
	}

	@Override
	public void setDefaultNamespace(String uri) {
		if ((uri.charAt(uri.length() - 1) == '#') || (uri.charAt(uri.length() - 1) == '/')) {
			defaultNamespace = uri;
		} else if (uri.charAt(uri.length() - 1) != '/') {
			defaultNamespace = uri + "#";
		} else {
			defaultNamespace = uri;
		}
		this.setPrefix(":", defaultNamespace);
	}

	public String getShortForm(String uri) {
		return getShortForm(uri, true, false);
	}

	public String getShortForm(String uri, boolean useDefaultPrefix) {
		return getShortForm(uri, useDefaultPrefix, false);
	}

	public String getShortForm(String uri, boolean useDefaultPrefix, boolean isLiteral) {
		StringBuilder result = new StringBuilder(uri);
		if (getDefaultNamespace() != null && useDefaultPrefix) {
			if (uri.length() > getDefaultNamespace().length()) {
				if (uri.contains(getDefaultNamespace())) {
					int index = uri.indexOf(getDefaultNamespace());
					result.replace(index, getDefaultNamespace().length()+index, "");
					if (isLiteral) {
						result.insert(0, "&:;");
					} else {
						// TODO: BAD CODE! The precondition for the input URI should not include any symbols. Only a valid URI string!
						if (uri.charAt(0) == '<') { 
							// If the URI is enclosed by <...> brackets then put the colon after the opening bracket.
							result.insert(1, ":");
						}
						else {
							result.insert(0, ":");
						}
					}
					return result.toString();
				}
			}
		}

		Collection<String> longnamespacesset = this.getPrefixMap().values();
		Iterator<String> longnamespaces = longnamespacesset.iterator();
		while (longnamespaces.hasNext()) {
			String longnamespace = longnamespaces.next();
			if (uri.length() > longnamespace.length()) {
				if (uri.substring(0, longnamespace.length()).equals(longnamespace)) {
					if (isLiteral) {
						result.replace(0, longnamespace.length(), "&" + getPrefixForURI(longnamespace) + ":;");
					} else {
						result.replace(0, longnamespace.length(), getPrefixForURI(longnamespace) + ":");
					}
					break;
				}
			}
		}
		return result.toString();
	}
}
