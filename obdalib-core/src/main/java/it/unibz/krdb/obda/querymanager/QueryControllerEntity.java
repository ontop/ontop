/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.querymanager;

import java.io.Serializable;

public abstract class QueryControllerEntity implements Serializable {

	private static final long serialVersionUID = -5241238894055210463L;

	public abstract String getNodeName();

	public abstract String getID();
}
