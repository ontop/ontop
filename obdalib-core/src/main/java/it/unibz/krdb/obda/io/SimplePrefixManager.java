package it.unibz.krdb.obda.io;

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

import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimplePrefixManager extends AbstractPrefixManager {

	/**
	 * A simple map containing for each ontology URI the correponding prefix
	 */
	private HashMap<String, String> uriToPrefixMap;

	/**
	 * A simple map containing for each prefix the correponding ontology URI
	 */
	private HashMap<String, String> prefixToURIMap;

	/**
	 * The default constructor. It creates a new instance of the prefix manager
	 */
	public SimplePrefixManager() {
		uriToPrefixMap = new HashMap<String, String>();
		prefixToURIMap = new HashMap<String, String>();
		initKnownPrefixes();
	}

	private void initKnownPrefixes() {
		addPrefix(OBDAVocabulary.PREFIX_RDF, OBDAVocabulary.NS_RDF);
		addPrefix(OBDAVocabulary.PREFIX_RDFS, OBDAVocabulary.NS_RDFS);
		addPrefix(OBDAVocabulary.PREFIX_OWL, OBDAVocabulary.NS_OWL);
		addPrefix(OBDAVocabulary.PREFIX_XSD, OBDAVocabulary.NS_XSD);
		addPrefix(OBDAVocabulary.PREFIX_QUEST, OBDAVocabulary.NS_QUEST);
	}

	@Override
	public void addPrefix(String prefix, String uri) {
		
		if (uri == null) {
			throw new NullPointerException("Prefix name must not be null");
		}
		if (!prefix.endsWith(":")) {
			throw new IllegalArgumentException("Prefix names must end with a colon (:)");
		}
		
		prefixToURIMap.put(prefix, getProperPrefixURI(uri));
		uriToPrefixMap.put(getProperPrefixURI(uri), prefix);
	}

	/**
	 * Returns the corresponding URI definition for the given prefix
	 * 
	 * @param prefix
	 *            the prefix name
	 * @return the corresponding URI definition or null if the prefix is not
	 *         registered
	 */
	public String getURIDefinition(String prefix) {
		return prefixToURIMap.get(prefix);
	}

	/**
	 * Returns the corresponding prefix for the given URI.
	 * 
	 * @param prefix
	 *            the prefix name
	 * @return the corresponding prefix or null if the URI is not registered
	 */
	public String getPrefix(String uri) {
		return uriToPrefixMap.get(uri);
	}

	/**
	 * Returns a map with all registered prefixes and the corresponding URI
	 * 
	 * @return a hash map
	 */
	public Map<String, String> getPrefixMap() {
		return Collections.unmodifiableMap(prefixToURIMap);
	}

	/**
	 * Checks if the prefix manager stores the prefix name.
	 * 
	 * @param prefix
	 *            The prefix name to check.
	 */
	public boolean contains(String prefix) {
		Set<String> prefixes = prefixToURIMap.keySet();
		return prefixes.contains(prefix);
	}

	@Override
	public void clear() {
		prefixToURIMap.clear();
		uriToPrefixMap.clear();
	}

	@Override
	public List<String> getNamespaceList() {
		ArrayList<String> namespaceList = new ArrayList<String>();
		for (String uri : getPrefixMap().values()) {
			namespaceList.add(uri);
		}
		Collections.sort(namespaceList, Collections.reverseOrder());
		return namespaceList;
	}
}
