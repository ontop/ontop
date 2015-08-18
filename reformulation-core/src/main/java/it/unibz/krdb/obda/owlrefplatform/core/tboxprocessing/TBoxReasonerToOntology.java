package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public class TBoxReasonerToOntology {
	
	private static final OntologyFactory fac = OntologyFactoryImpl.getInstance();

	public static Ontology getOntology(TBoxReasoner reasoner, final boolean excludeExistentials) {
		OntologyVocabulary voc = fac.createVocabulary();
		final Ontology sigma = fac.createOntology(voc);

		TBoxTraversal.traverse(reasoner, new TBoxTraverseListener() {
			
			@Override
			public void onInclusion(ObjectPropertyExpression sub, ObjectPropertyExpression sup) {
				if (sub != sup) 
					sigma.addSubPropertyOfAxiomWithReferencedEntities(sub, sup);
			}
			@Override
			public void onInclusion(DataPropertyExpression sub, DataPropertyExpression sup) {
				if (sub != sup) 
					sigma.addSubPropertyOfAxiomWithReferencedEntities(sub, sup);
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
				if (sub != sup) 
					sigma.addSubClassOfAxiomWithReferencedEntities(sub, sup);
			}
		});
		
		return sigma;	
	}
	
	public static Ontology getOntology(TBoxReasoner reasoner) {
		return getOntology(reasoner, false);
	}

}
