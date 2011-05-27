package it.unibz.krdb.obda.model;

import java.net.URI;

/**
 * Provides an interface for storing the URI constant.
 */
public interface URIConstant extends Constant {

	/**
	 * Get the URI object from this constant.
	 *
	 * @return the URI object.
	 */
	public URI getURI();
}
