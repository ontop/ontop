package it.unibz.krdb.obda.protege4.core;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
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
