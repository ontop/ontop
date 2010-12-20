/*
 * @(#)URIConstantImpl 3/11/2010
 *
 * Copyright 2010 OBDA-API. All rights reserved.
 * Use is subject to license terms.
 */
package org.obda.query.domain.imp;

import java.net.URI;

import org.obda.query.domain.Term;
import org.obda.query.domain.URIConstant;

/**
 * Provides a storage to put the URI constant.
 *
 * @author Josef Hardi <josef.hardi@gmail.com>
 */
public class URIConstantImpl implements URIConstant {

	private final URI uri;

	/**
	 * The default constructor.
	 *
	 * @param uri URI from a term.
	 */
	public URIConstantImpl(URI uri) {
		this.uri = uri;
	}

	@Override
	public URI getURI() {
		return uri;
	}

	@Override
	public String getName() {
		return uri.toString();
	}

	@Override
	public Term copy() {
		return null;
	}
}
