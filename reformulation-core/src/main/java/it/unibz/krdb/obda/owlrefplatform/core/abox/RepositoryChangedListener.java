/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.abox;

import java.util.EventObject;

public interface RepositoryChangedListener {

	//Clear cache if repository changed - after addition or removal of data
	public void repositoryChanged();
}
