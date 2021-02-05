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
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;

public abstract class AbstractPrefixManager implements PrefixManager {

	protected abstract Optional<String> getIriDefinition(String prefix);

	protected abstract ImmutableList<Map.Entry<String, String>> getOrderedMap();

	protected static ImmutableList<Map.Entry<String, String>> orderMap(Map<String, String> map) {
		Comparator<Map.Entry<String, String>> comparator =
				Map.Entry.<String, String>comparingByValue()
						.thenComparing(Map.Entry.comparingByKey());
		return map.entrySet().stream()
				.sorted(comparator)
				.collect(ImmutableCollectors.toList());
	}

	@Override
	public String getShortForm(String stringIri) {
		// Clean the IRI string from <...> signs, if they exist.
		// <http://www.example.org/library#Book> --> http://www.example.org/library#Book
		String fullIri = stringIri.startsWith("<") && stringIri.endsWith(">")
				? stringIri.substring(1, stringIri.length() - 1)
				: stringIri;
		
		// Check if the URI string has a matched prefix
		for (Map.Entry<String, String> e : getOrderedMap()) {
			String iri = e.getValue();
			if (fullIri.startsWith(iri)) {
				// Replace the IRI with the corresponding prefix.
				return e.getKey() + fullIri.substring(iri.length());
			}
		}
		return stringIri; // return the original IRI if no prefix definition was found
	}
	
	@Override
	public String getExpandForm(String prefixedName) {
		int index = prefixedName.indexOf(":") + 1;
		if (index == 0)
			throw new InvalidPrefixWritingException();

		// extract the whole prefix, e.g., "ex:Book" --> "ex:"
		String prefix = prefixedName.substring(0, index);
		String iri = getIriDefinition(prefix)
				.orElseThrow(() -> new InvalidPrefixWritingException("The prefix name is unknown: " + prefix));

		return iri + prefixedName.substring(index);
	}
	
	@Override
	public String getDefaultIriPrefix() {
		return getIriDefinition(DEFAULT_PREFIX)
				.orElseThrow(() -> new InvalidPrefixWritingException("No default prefix"));
	}
}
