package unibz.inf.ontop.owlrefplatform.core.tboxprocessing;

import unibz.inf.ontop.ontology.DataSomeValuesFrom;
import unibz.inf.ontop.ontology.ObjectPropertyExpression;
import unibz.inf.ontop.ontology.Ontology;
import unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import unibz.inf.ontop.ontology.DataPropertyExpression;
import unibz.inf.ontop.ontology.DataRangeExpression;
import unibz.inf.ontop.ontology.ObjectSomeValuesFrom;
import unibz.inf.ontop.ontology.OntologyFactory;
import unibz.inf.ontop.ontology.ClassExpression;
import unibz.inf.ontop.ontology.impl.OntologyFactoryImpl;

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
