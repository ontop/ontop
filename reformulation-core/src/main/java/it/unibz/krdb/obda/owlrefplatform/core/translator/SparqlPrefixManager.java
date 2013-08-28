/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.translator;

import it.unibz.krdb.obda.io.AbstractPrefixManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * A read-only prefix manager that wraps <code>PrefixMapping</code> from Jena-API
 * 
 * @see com.hp.hpl.jena.shared.PrefixMapping
 */
public class SparqlPrefixManager extends AbstractPrefixManager {

	private PrefixMapping prefixMapping;

	public SparqlPrefixManager(PrefixMapping prefixMapping) {
		this.prefixMapping = prefixMapping;
	}

	@Override
	public void addPrefix(String prefix, String uri) {
		throw new UnsupportedOperationException("This is a read-only prefix manager. Addition operation is not permitted");
	}

	@Override
	public String getURIDefinition(String prefix) {
		if (prefix.equals(":")) {
			prefix = ""; // to conform with Ontop prefix manager
		}
		return prefixMapping.getNsPrefixURI(prefix);
	}

	@Override
	public String getPrefix(String uri) {
		String prefix = prefixMapping.getNsURIPrefix(uri);
		if (prefix.equals("")) {
			prefix = ":"; // to conform with Ontop prefix manager
		}
		return prefix;
	}

	@Override
	public Map<String, String> getPrefixMap() {
		Map<String, String> newPrefixMap = new HashMap<String, String>();
		Map<String, String> jenaPrefixMap = prefixMapping.getNsPrefixMap();
		for (String prefix : jenaPrefixMap.keySet()) {
			if (prefix.isEmpty()) {
				newPrefixMap.put(":", jenaPrefixMap.get(prefix));
			} else {
				newPrefixMap.put(prefix, jenaPrefixMap.get(prefix));
			}
		}
		return newPrefixMap;
	}

	@Override
	public boolean contains(String prefix) {
		if (prefix.equals(":")) {
			prefix = ""; // to conform with Ontop prefix manager
		}
		return getPrefixMap().containsKey(prefix);
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("This is a read-only prefix manager. Clearing operation is not permitted");
	}

	@Override
	public List<String> getNamespaceList() {
		return new ArrayList<String>(getPrefixMap().values());
	}
}
