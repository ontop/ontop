package org.semanticweb.ontop.owlrefplatform.core.tboxprocessing;


import org.semanticweb.ontop.ontology.*;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public class TBoxReasonerToOntology {
	
	private static final OntologyFactory fac = OntologyFactoryImpl.getInstance();

	public static Ontology getOntology(TBoxReasoner reasoner, final boolean excludeExistentials) {
		final Ontology sigma = fac.createOntology();

		TBoxTraversal.traverse(reasoner, new TBoxTraverseListener() {
			
			@Override
			public void onInclusion(ObjectPropertyExpression sub, ObjectPropertyExpression sup) {
				if (sub != sup) {
					sigma.addSubPropertyOfAxiomWithReferencedEntities(sub, sup);
				}
			}
			@Override
			public void onInclusion(DataPropertyExpression sub, DataPropertyExpression sup) {
				if (sub != sup) {
					sigma.addSubPropertyOfAxiomWithReferencedEntities(sub, sup);
				}
			}

			@Override
			public void onInclusion(ClassExpression sub, ClassExpression sup) {
				if (sub != sup) {
					if (!excludeExistentials || !(sup instanceof SomeValuesFrom)) {
						sigma.addSubClassOfAxiomWithReferencedEntities(sub, sup);
					}
				}
			}
			@Override
			public void onInclusion(DataRangeExpression sub, DataRangeExpression sup) {
				if (sub != sup) {
					if (!excludeExistentials || !(sup instanceof SomeValuesFrom)) {
						sigma.addSubClassOfAxiomWithReferencedEntities(sub, sup);
					}
				}
			}
		});
		
		return sigma;	
	}
	
	public static Ontology getOntology(TBoxReasoner reasoner) {
		return getOntology(reasoner, false);
	}

}
