package it.unibz.krdb.obda.protege4.core;

import it.unibz.krdb.obda.io.AbstractPrefixManager;

import java.util.Map;

import org.semanticweb.owlapi.model.IRI;
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
		String prefix = owlmapper.getPrefixIRI(IRI.create(uri));
		//we use -1 since OWL API return colon with prefxies, e.g., test:
		return prefix.substring(0,prefix.length()-1);
	}

	@Override
	public Map<String, String> getPrefixMap() {
		return owlmapper.getPrefixName2PrefixMap();
	}

	@Override
	public String getURIForPrefix(String prefix) {
		return owlmapper.getPrefix(prefix);
	}

	@Override
	public boolean contains(String prefix) {
		return owlmapper.containsPrefixMapping(prefix);
	}
}
