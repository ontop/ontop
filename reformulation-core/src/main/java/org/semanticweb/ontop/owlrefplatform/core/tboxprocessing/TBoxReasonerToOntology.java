package org.semanticweb.ontop.owlrefplatform.core.tboxprocessing;

import org.semanticweb.ontop.ontology.DataPropertyExpression;
import org.semanticweb.ontop.ontology.DataRangeExpression;
import org.semanticweb.ontop.ontology.DataSomeValuesFrom;
import org.semanticweb.ontop.ontology.ObjectPropertyExpression;
import org.semanticweb.ontop.ontology.ObjectSomeValuesFrom;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.ontology.OntologyFactory;
import org.semanticweb.ontop.ontology.ClassExpression;
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
					if (!excludeExistentials || (!(sup instanceof ObjectSomeValuesFrom) && !(sup instanceof DataSomeValuesFrom))) {
						sigma.addSubClassOfAxiomWithReferencedEntities(sub, sup);
					}
				}
			}
			@Override
			public void onInclusion(DataRangeExpression sub, DataRangeExpression sup) {
				if (sub != sup) {
					// TODO: to be cleaned
					if (!excludeExistentials || (!(sup instanceof ObjectSomeValuesFrom) && !(sup instanceof DataSomeValuesFrom))) {
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
