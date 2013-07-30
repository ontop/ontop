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
 * This is a holder for all QueryView objects instantiated in protege. We keep
 * them to coordinate their query selectors.
 */
public class QueryInterfaceViewsList extends ArrayList<QueryInterfaceView> implements Disposable {

	private static final long serialVersionUID = -7082548696764069555L;

	public void dispose() throws Exception {
		// NO-OP
	}
}
