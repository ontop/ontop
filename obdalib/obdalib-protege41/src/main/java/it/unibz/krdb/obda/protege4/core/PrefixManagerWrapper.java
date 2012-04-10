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
		String questprefix = owlmapper.getPrefix("quest");
		
	}
	
	@Override
	public String getDefaultNamespace() {
		return super.getDefaultNamespace();
	}

	@Override
	public void addUri(String uri, String prefix) {
		owlmapper.setPrefix(prefix, uri);

	}

	@Override
	public String getPrefixForURI(String uri) {
		String prefix = owlmapper.getPrefixIRI(IRI.create(uri));
		String p2 = owlmapper.getPrefix(uri);
		
		for (String key: owlmapper.getPrefixName2PrefixMap().keySet()) {
			if (owlmapper.getPrefixName2PrefixMap().get(key).equals(uri)) {
				prefix = key;
			}
		}
		if (prefix == null && owlmapper.getDefaultPrefix().equals(uri))
			prefix = ":";
		
		//we use -1 since OWL API return colon with prefxies, e.g., test:
//		System.out.println(uri);
//		System.out.println(prefix);
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

	@Override
	public void setPrefix(String name, String uri) {
		owlmapper.setPrefix(name, uri);
	}
}
