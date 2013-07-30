/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.ontology;

public enum LanguageProfile {
	
	RDFS(1), OWL2QL(2), DLLITEA(3);

	private final int	order;

	LanguageProfile(int order) {
		this.order = order;
	}

	public int order() {
		return this.order;
	}
}
