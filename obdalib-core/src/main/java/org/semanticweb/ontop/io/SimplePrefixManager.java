package org.semanticweb.ontop.io;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;

public class SimplePrefixManager extends AbstractPrefixManager {

	/**
	 * A simple map containing for each ontology URI the corresponding prefix.
     * Immutable.
	 */
	private final ImmutableMap<String, String> uriToPrefixMap;

	/**
	 * A simple map containing for each prefix the corresponding ontology URI.
     * Immutable.
	 */
	private final ImmutableMap<String, String> prefixToURIMap;

	/**
	 * The default constructor. It creates a new instance of the prefix manager
     *
     * TODO: make it private (again).
	 */
    @Inject
	public SimplePrefixManager(@Assisted Map<String, String> prefixToURIMap) {
        checkPrefixToURIMap(prefixToURIMap);
        Map<String, String> newPrefixToURIMap = new HashMap<>(prefixToURIMap);
		newPrefixToURIMap.putAll(initKnownPrefixes());
        this.prefixToURIMap = ImmutableMap.copyOf(newPrefixToURIMap);
        this.uriToPrefixMap = reversePrefixToURI(newPrefixToURIMap);
	}

    private static ImmutableMap<String, String> reversePrefixToURI(Map<String, String> prefixToURIMap) {
        Map<String, String> uriToPrefixMap = new HashMap<>();
        for (Map.Entry<String, String> entry : prefixToURIMap.entrySet()) {
            uriToPrefixMap.put(entry.getValue(), entry.getKey());
        }
        return ImmutableMap.copyOf(uriToPrefixMap);
    }

    private static void checkPrefixToURIMap(Map<String, String> prefixToURIMap) {
        for (Map.Entry<String, String> entry : prefixToURIMap.entrySet()) {
            String prefix = entry.getKey();
            String uri = entry.getValue();

            if (uri == null) {
                throw new NullPointerException("Prefix name must not be null");
            }
            if (!prefix.endsWith(":")) {
                throw new IllegalArgumentException("Prefix names must end with a colon (:)");
            }
        }
    }

    private static Map<String, String> initKnownPrefixes() {
        Map<String, String> prefixToURIMap = new HashMap<>();
        prefixToURIMap.put(OBDAVocabulary.PREFIX_RDF, OBDAVocabulary.NS_RDF);
        prefixToURIMap.put(OBDAVocabulary.PREFIX_RDFS, OBDAVocabulary.NS_RDFS);
        prefixToURIMap.put(OBDAVocabulary.PREFIX_OWL, OBDAVocabulary.NS_OWL);
        prefixToURIMap.put(OBDAVocabulary.PREFIX_XSD, OBDAVocabulary.NS_XSD);
        prefixToURIMap.put(OBDAVocabulary.PREFIX_QUEST, OBDAVocabulary.NS_QUEST);
        return prefixToURIMap;
	}

	/**
	 * Returns the corresponding URI definition for the given prefix
	 * 
	 * @param prefix
	 *            the prefix name
	 * @return the corresponding URI definition or null if the prefix is not
	 *         registered
	 */
    @Override
	public String getURIDefinition(String prefix) {
		return prefixToURIMap.get(prefix);
	}

	/**
	 * Returns the corresponding prefix for the given URI.
	 * 
	 * @param uri
     *
	 * @return the corresponding prefix or null if the URI is not registered
	 */
    @Override
	public String getPrefix(String uri) {
		return uriToPrefixMap.get(uri);
	}

	/**
	 * Returns a map with all registered prefixes and the corresponding URI
	 * 
	 * @return an immutable map
	 */
    @Override
	public ImmutableMap<String, String> getPrefixMap() {
		return prefixToURIMap;
	}

	/**
	 * Checks if the prefix manager stores the prefix name.
	 * 
	 * @param prefix
	 *            The prefix name to check.
	 */
    @Override
	public boolean contains(String prefix) {
		Set<String> prefixes = prefixToURIMap.keySet();
		return prefixes.contains(prefix);
	}

	@Override
	public List<String> getNamespaceList() {
		List<String> namespaceList = new ArrayList<>();
		for (String uri : getPrefixMap().values()) {
			namespaceList.add(uri);
		}
		Collections.sort(namespaceList, Collections.reverseOrder());
		return namespaceList;
	}
}
