package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.DisjointDataPropertyAxiom;

public class DisjointDataPropertyAxiomImpl extends DisjointPropertyAxiomImpl implements DisjointDataPropertyAxiom{

	private static final long serialVersionUID = 2049346032304523558L;

	DisjointDataPropertyAxiomImpl(Predicate p1, Predicate p2) {
	//	if (p1.isDataProperty() && p2.isDataProperty())
		super(p1, p2);
		
	}

}
