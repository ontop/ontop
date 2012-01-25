package it.unibz.krdb.obda.protege4.core;

import it.unibz.krdb.obda.io.AbstractPrefixManager;

import java.util.Map;

import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

/**
 * This PrefixManager is meant to 'wrap' Protege's prefix manager. That way any
 * prefix defined in Protege are transparently passed to all OBDA lib classes.
 * 
 */

public class PrefixManagerWrapper extends AbstractPrefixManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4599514975769563763L;
	PrefixOWLOntologyFormat	owlmapper;

	public PrefixManagerWrapper(PrefixOWLOntologyFormat owlmapper) {
		this.owlmapper = owlmapper;
	}

	@Override
	public void addUri(String uri, String prefix) {
		owlmapper.setPrefix(prefix, uri);

	}

	@Override
	public String getPrefixForURI(String uri) {
		return owlmapper.getPrefix(uri);
	}

	@Override
	public Map<String, String> getPrefixMap() {
		return owlmapper.getPrefixName2PrefixMap();
	}

	@Override
	public String getURIForPrefix(String prefix) {
		return owlmapper.getPrefix(prefix);
	}

}
