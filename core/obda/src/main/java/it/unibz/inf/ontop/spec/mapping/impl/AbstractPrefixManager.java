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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.InvalidPrefixWritingException;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;

import java.util.*;

public abstract class AbstractPrefixManager implements PrefixManager {

	protected abstract Optional<String> getIriDefinition(String prefix);

	private ImmutableList<Map.Entry<String, String>> orderedNamespaces; // lazy evaluation

	private ImmutableList<Map.Entry<String, String>> getOrderedNamespaces() {
		if (orderedNamespaces == null) {
			List<Map.Entry<String, String>> namespaceList = new ArrayList<>(getPrefixMap().entrySet());
			Comparator<Map.Entry<String, String>> comparator =
					Map.Entry.<String, String>comparingByValue()
							.thenComparing(Map.Entry.comparingByKey());
			namespaceList.sort(comparator);
			orderedNamespaces = ImmutableList.copyOf(namespaceList);
		}
		return orderedNamespaces;
	}

	@Override
	public String getShortForm(String originalUri) {
		ImmutableList<Map.Entry<String, String>> namespaceList = getOrderedNamespaces();
		
		// Clean the URI string from <...> signs, if they exist.
		// <http://www.example.org/library#Book> --> http://www.example.org/library#Book
		String cleanUri = originalUri.startsWith("<") && originalUri.endsWith(">")
				? originalUri.substring(1, originalUri.length() - 1)
				: originalUri;
		
		// Check if the URI string has a matched prefix
		for (Map.Entry<String, String> e : namespaceList) {
			String iri = e.getValue();
			if (cleanUri.startsWith(iri)) {
				return cleanUri.replace(iri, e.getKey()); // Replace the URI with the corresponding prefix.
			}
		}
		return originalUri; // return the original URI if no prefix definition was found
	}
	
	@Override
	public String getExpandForm(String prefixedName) {
		int index = prefixedName.indexOf(":") + 1;
		if (index == 0)
			throw new InvalidPrefixWritingException();

		// extract the whole prefix, e.g., "ex:Book" --> "ex:"
		String prefix = prefixedName.substring(0, index);
		String uri = getIriDefinition(prefix)
				.orElseThrow(() -> new InvalidPrefixWritingException("The prefix name is unknown: " + prefix));

		return uri + prefixedName.substring(index);
	}
	
	@Override
	public String getDefaultIriPrefix() {
		return getIriDefinition(DEFAULT_PREFIX)
				.orElseThrow(() -> new InvalidPrefixWritingException("No default prefix"));
	}
}
