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
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.model.vocabulary.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.Stream;

public class SimplePrefixManager extends AbstractPrefixManager {

	private final ImmutableMap<String, String> prefixToIriMap;

    @Inject
	private SimplePrefixManager(@Assisted ImmutableMap<String, String> prefixToIriMap) {
    	if (prefixToIriMap.containsValue(null))
			throw new NullPointerException("Prefix name must not be null");

    	if (!prefixToIriMap.keySet().stream().allMatch(p -> p.endsWith(":")))
			throw new IllegalArgumentException("Prefix names must end with a colon (:)");

        this.prefixToIriMap = Stream.concat(
        			prefixToIriMap.entrySet().stream(),
					standardIriPrefixes.entrySet().stream())
				.distinct()
				.collect(ImmutableCollectors.toMap());
	}

    private static final ImmutableMap<String, String> standardIriPrefixes = ImmutableMap.of(
        		OntopInternal.PREFIX_RDF, RDF.PREFIX,
        		OntopInternal.PREFIX_RDFS, RDFS.PREFIX,
        		OntopInternal.PREFIX_OWL, OWL.PREFIX,
        		OntopInternal.PREFIX_XSD, XSD.PREFIX,
        		OntopInternal.PREFIX_OBDA, Ontop.PREFIX);

	/**
	 * Returns the corresponding IRI definition for the given prefix
	 * 
	 * @param prefix
	 *            the prefix name
	 * @return the corresponding IRI definition or null if the prefix is not
	 *         registered
	 */
    @Override
	protected Optional<String> getIriDefinition(String prefix) {
		return Optional.ofNullable(prefixToIriMap.get(prefix));
	}

	private ImmutableList<Map.Entry<String, String>> orderedMap; // lazy instantiation

	@Override
	protected ImmutableList<Map.Entry<String, String>> getOrderedMap() {
		if (orderedMap == null)
			orderedMap = orderMap(prefixToIriMap);
		return orderedMap;
	}

	/**
	 * Returns a map with all registered prefixes and the corresponding IRI
	 * 
	 * @return an immutable map
	 */
    @Override
	public ImmutableMap<String, String> getPrefixMap() {
		return prefixToIriMap;
	}
}
