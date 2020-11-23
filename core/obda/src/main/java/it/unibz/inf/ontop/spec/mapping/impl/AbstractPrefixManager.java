package it.unibz.inf.ontop.spec.mapping.impl;

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

import it.unibz.inf.ontop.exception.InvalidPrefixWritingException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;

import java.util.List;

public abstract class AbstractPrefixManager implements PrefixManager {

	public abstract List<String> getOrderedNamespaces();
	
	@Override
	public String getShortForm(String uri) {
		return getShortForm(uri, false);
	}
	
	@Override
	public String getShortForm(String originalUri, boolean insideQuotes) {
		final List<String> namespaceList = getOrderedNamespaces();
		
		/* Clean the URI string from <...> signs, if they exist.
		 * <http://www.example.org/library#Book> --> http://www.example.org/library#Book
		 */
		String cleanUri = originalUri;
		if (originalUri.contains("<") && originalUri.contains(">")) {
			cleanUri = originalUri.replace("<", "").replace(">", "");
		}
		
		// Check if the URI string has a matched prefix
		for (String prefixUriDefinition : namespaceList) {
			if (cleanUri.startsWith(prefixUriDefinition)) {
				String prefix = getPrefix(prefixUriDefinition)
						.orElseThrow(() -> new PrefixManagerException("A prefix is expected"));
				if (insideQuotes) {
					prefix = String.format("&%s;", removeColon(prefix));
				}
				// Replace the URI with the corresponding prefix.
				return cleanUri.replace(prefixUriDefinition, prefix);
			}
		}
		return originalUri; // return the original URI if no prefix definition was found
	}
	
	@Override
	public String getExpandForm(String prefixedName) {
		return getExpandForm(prefixedName, false);
	}
	
	@Override
	public String getExpandForm(String prefixedName, boolean insideQuotes) {
		String prefix;

		try {
			/* Clean the URI string from <"..."> signs, if they exist.
			 * e.g., <"&ex;Book"> --> &ex;Book
			 */
			if (prefixedName.contains("<\"") && prefixedName.contains("\">")) {
				prefixedName = prefixedName.replace("<\"", "").replace("\">", "");
			}
			
			if (insideQuotes) {
				// &ex;Book
				int start = prefixedName.indexOf("&");
				int end = prefixedName.indexOf(";");
	
				// extract the whole prefix placeholder, e.g., "&ex;Book" --> "&ex;"
				String prefixPlaceHolder = prefixedName.substring(start, end + 1);
	
				// extract the prefix name, e.g., "&ex;" --> "ex:"
				prefix = prefixPlaceHolder.substring(1, prefixPlaceHolder.length() - 1);
				if (!prefix.equals(":")) {
					prefix = prefix + ":"; // add a colon
				}
			} else {
				// ex:Book
				int index = prefixedName.indexOf(":");
				
				// extract the whole prefix placeholder, e.g., "ex:Book" --> "ex:"
				String prefixPlaceHolder = prefixedName.substring(0, index);
				
				// extract the prefix name
				prefix = prefixPlaceHolder + ":";
			}
		} catch (StringIndexOutOfBoundsException e) {
			throw new InvalidPrefixWritingException();
		}
		
		String uri = getURIDefinition(prefix);
		if (uri == null) {
			throw new InvalidPrefixWritingException("The prefix name is unknown: " + prefix); // the prefix is unknown.
		}
		return prefixedName.replaceFirst(prefix, uri);
	}
	
	@Override
	public String getDefaultPrefix() {
		return getPrefixMap().get(DEFAULT_PREFIX);
	}
	
	private String removeColon(String prefix) {
		if (prefix.equals(DEFAULT_PREFIX)) {
			return prefix; // TODO Remove this code in the future.
		}
		return prefix.replace(":", "");
	}

	private static class PrefixManagerException extends OntopInternalBugException {
		private PrefixManagerException(String msg) {
			super(msg);
		}
	}
}
