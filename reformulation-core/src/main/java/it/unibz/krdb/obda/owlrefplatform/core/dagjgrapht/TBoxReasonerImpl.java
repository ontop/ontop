package it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht;

import it.unibz.krdb.obda.ontology.Description;

import it.unibz.krdb.obda.ontology.Ontology;

import java.util.Set;

/**
 *  Retrieve all the connection built in our DAG 
 * 
 *
 */

public class TBoxReasonerImpl implements TBoxReasoner{


	public TBoxReasonerImpl(Ontology ontology){

	}

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
