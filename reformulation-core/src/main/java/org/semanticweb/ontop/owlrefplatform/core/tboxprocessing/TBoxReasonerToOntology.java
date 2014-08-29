package org.semanticweb.ontop.owlrefplatform.core.tboxprocessing;


import org.semanticweb.ontop.ontology.*;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public class TBoxReasonerToOntology {
	
	private static final OntologyFactory fac = OntologyFactoryImpl.getInstance();

	public static Ontology getOntology(TBoxReasoner reasoner, final boolean excludeExistentials) {
		final Ontology sigma = fac.createOntology("ontology");

		TBoxTraversal.traverse(reasoner, new TBoxTraverseListener() {
			
			@Override
			public void onInclusion(Property sub, Property sup) {
				Axiom ax = fac.createSubPropertyAxiom(sub, sup);
				sigma.addEntities(ax.getReferencedEntities());
				sigma.addAssertion(ax);						
			}

			@Override
			public void onInclusion(BasicClassDescription sub, BasicClassDescription sup) {
				if (!excludeExistentials || !(sup instanceof PropertySomeRestriction)) {
					Axiom ax = fac.createSubClassAxiom(sub, sup);
					sigma.addEntities(ax.getReferencedEntities());
					sigma.addAssertion(ax);						
				}
			}
		});
		
		return sigma;	
	}
	
	public static Ontology getOntology(TBoxReasoner reasoner) {
		return getOntology(reasoner, false);
	}

}
