package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public class TBoxReasonerToOntology {
	
	private static final OntologyFactory fac = OntologyFactoryImpl.getInstance();

	public static Ontology getOntology(TBoxReasoner reasoner, final boolean excludeExistentials) {
		final Ontology sigma = fac.createOntology("ontology");

		TBoxTraversal.traverse(reasoner, new TBoxTraverseListener() {
			
			@Override
			public void onInclusion(Property sub, Property sup) {
				Axiom ax = fac.createSubPropertyAxiom(sub, sup);
				sigma.addAssertionWithEntities(ax);						
			}

			@Override
			public void onInclusion(BasicClassDescription sub, BasicClassDescription sup) {
				if (!excludeExistentials || !(sup instanceof PropertySomeRestriction)) {
					Axiom ax = fac.createSubClassAxiom(sub, sup);
					sigma.addAssertionWithEntities(ax);						
				}
			}
		});
		
		return sigma;	
	}
	
	public static Ontology getOntology(TBoxReasoner reasoner) {
		return getOntology(reasoner, false);
	}

}
