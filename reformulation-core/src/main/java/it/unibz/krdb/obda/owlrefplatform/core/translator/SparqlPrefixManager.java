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
		return prefixMapping.getNsPrefixURI(prefix);
	}

	@Override
	public String getPrefix(String uri) {
		return prefixMapping.getNsURIPrefix(uri);
	}

	@Override
	public Map<String, String> getPrefixMap() {
		return prefixMapping.getNsPrefixMap();
	}

	@Override
	public boolean contains(String prefix) {
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
