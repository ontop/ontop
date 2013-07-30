/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.protege4.views;

import java.util.ArrayList;

import org.protege.editor.core.Disposable;

/**
 * A holder for the list of all Query Managers views
 */
public class QueryManagerViewsList extends ArrayList<QueryManagerView> implements Disposable {

	private static final long serialVersionUID = 2986737849606126197L;

	public void dispose() throws Exception {
		// NO-OP
	}
}
