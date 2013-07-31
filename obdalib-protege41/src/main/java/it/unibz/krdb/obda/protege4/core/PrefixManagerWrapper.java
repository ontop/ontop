/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.protege4.core;

import it.unibz.krdb.obda.io.AbstractPrefixManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

/**
 * This PrefixManager is meant to 'wrap' Protege's prefix manager. That way any
 * prefix defined in Protege are transparently passed to all OBDA lib classes.
 */
public class PrefixManagerWrapper extends AbstractPrefixManager {

	PrefixOWLOntologyFormat owlmapper;

	public PrefixManagerWrapper(PrefixOWLOntologyFormat owlmapper) {
		this.owlmapper = owlmapper;
	}
	
	@Override
	public String getDefaultPrefix() {
		return super.getDefaultPrefix();
	}

	@Override
	public String getPrefix(String uri) {
		for (String prefix : owlmapper.getPrefixName2PrefixMap().keySet()) {
			if (owlmapper.getPrefixName2PrefixMap().get(prefix).contains(uri)) {
				return prefix;
			}
		}
		return null;
	}

	@Override
	public Map<String, String> getPrefixMap() {
		return owlmapper.getPrefixName2PrefixMap();
	}

	@Override
	public String getURIDefinition(String prefix) {
		return owlmapper.getPrefix(prefix);
	}

	@Override
	public boolean contains(String prefix) {
		return owlmapper.containsPrefixMapping(prefix);
	}

	@Override
	public void addPrefix(String name, String uri) {
		owlmapper.setPrefix(name, getProperPrefixURI(uri));
	}

	@Override
	public void clear() {
		owlmapper.clearPrefixes();
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
