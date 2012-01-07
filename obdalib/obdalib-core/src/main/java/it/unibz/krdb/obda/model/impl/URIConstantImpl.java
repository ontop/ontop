package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.URIConstant;

import java.net.URI;

/**
 * Provides a storage to put the URI constant.
 */
public class URIConstantImpl implements URIConstant {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1263974895010238519L;

	private final URI uri;

	private final int identifier;

	/**
	 * The default constructor.
	 * 
	 * @param uri
	 *            URI from a term.
	 */
	protected URIConstantImpl(URI uri) {
		this.uri = uri;
		this.identifier = uri.hashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null || !(obj instanceof URIConstantImpl))
			return false;

		URIConstantImpl uri2 = (URIConstantImpl) obj;
		return this.identifier == uri2.identifier;
	}

	@Override
	public int hashCode() {
		return identifier;
	}

	@Override
	public URI getURI() {
		return uri;
	}

	// @Override
	// public String getName() {
	// return uri.toString();
	// }

	@Override
	public URIConstant clone() {
		return this;
		// URIConstantImpl clone = new URIConstantImpl(uri);
		// clone.identifier = identifier;
		// return clone;
	}

	@Override
	public String toString() {
		return uri.toString();
	}
}
