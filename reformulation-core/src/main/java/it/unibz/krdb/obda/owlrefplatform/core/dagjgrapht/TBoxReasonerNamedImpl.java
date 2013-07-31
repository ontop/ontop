/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;

import java.util.Set;

/**    Retrieve all the connection built in our DAG it provides only the NamedDescription
 * 
 * */
public class TBoxReasonerNamedImpl implements TBoxReasoner {

	//	starting from a dug
	//	TBoxReasonerNamedImpl(DAG){
	//		
	//	}

	@Override
	public Set<Set<Description>> getDirectChildren(Description desc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<Description>> getDirectParents(Description desc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<Description>> getDescendants(Description desc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<Description>> getAncestors(Description desc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Description> getEquivalences(Description description) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getNode() {
		// TODO Auto-generated method stub

	}

}
