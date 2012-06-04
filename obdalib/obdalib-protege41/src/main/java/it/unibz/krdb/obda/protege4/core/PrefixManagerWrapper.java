package it.unibz.krdb.obda.protege4.core;

import it.unibz.krdb.obda.io.AbstractPrefixManager;

import java.util.Map;

import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

/**
 * This PrefixManager is meant to 'wrap' Protege's prefix manager. That way any
 * prefix defined in Protege are transparently passed to all OBDA lib classes.
 */
public class PrefixManagerWrapper extends AbstractPrefixManager {

	PrefixOWLOntologyFormat	owlmapper;

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
			if (owlmapper.getPrefixName2PrefixMap().get(prefix).equals(uri)) {
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
		owlmapper.setPrefix(name, uri);
	}
}
