package it.unibz.krdb.obda.protege4.core;

import it.unibz.krdb.obda.io.AbstractPrefixManager;

import java.util.Map;

import org.protege.editor.owl.ui.prefix.PrefixMapper;

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
	PrefixMapper	owlmapper;

	public PrefixManagerWrapper(PrefixMapper owlmapper) {
		this.owlmapper = owlmapper;
	}

	@Override
	public void addUri(String uri, String prefix) {
		owlmapper.addPrefixMapping(prefix, uri);

	}

	@Override
	public String getPrefixForURI(String uri) {
		return owlmapper.getPrefix(uri);
	}

	@Override
	public Map<String, String> getPrefixMap() {
		return owlmapper.getPrefixMap();
	}

	@Override
	public String getURIForPrefix(String prefix) {
		return owlmapper.getValue(prefix);
	}

	@Override
	public boolean contains(String prefix) {
		// TODO Auto-generated method stub
		return false;
	}

}
