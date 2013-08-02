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

public interface QueryControllerListener extends Serializable {

	public void elementAdded(QueryControllerEntity element);

	public void elementAdded(QueryControllerQuery query, QueryControllerGroup group);

	public void elementRemoved(QueryControllerEntity element);

	public void elementRemoved(QueryControllerQuery query, QueryControllerGroup group);

	public void elementChanged(QueryControllerQuery query);

	public void elementChanged(QueryControllerQuery query, QueryControllerGroup group);
}
