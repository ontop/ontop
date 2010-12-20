/*
 * @(#)URIConstant 3/11/2010
 *
 * Copyright 2010 OBDA-API. All rights reserved.
 * Use is subject to license terms.
 */
package org.obda.query.domain;

import java.net.URI;

/**
 * Provides an interface for storing the URI constant.
 *
 * @author Josef Hardi <josef.hardi@gmail.com>
 */
public interface URIConstant extends Constant {

	public URI getURI();
}
