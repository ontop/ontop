package it.unibz.krdb.obda.model;

import java.net.URI;

import com.hp.hpl.jena.iri.IRI;

/**
 * Provides an interface for storing the URI constant.
 */
public interface URIConstant extends ObjectConstant {

	/**
	 * Get the URI object from this constant.
	 *
	 * @return the URI object.
	 */
	public IRI getURI();
}
