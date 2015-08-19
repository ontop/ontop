package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

/**
 * used in one test only
 * 
 * TODO: to be eliminated
 * 
 */

@Deprecated 
public class TBoxReasonerToOntology {
	
	private static final OntologyFactory fac = OntologyFactoryImpl.getInstance();

	public static Ontology getOntology(TBoxReasoner reasoner) {
		OntologyVocabulary voc = fac.createVocabulary();
		final Ontology ont = fac.createOntology(voc);

		TBoxTraversal.traverse(reasoner, new TBoxTraverseListener() {
			
			@Override
			public void onInclusion(ObjectPropertyExpression sub, ObjectPropertyExpression sup) {
				ont.addSubPropertyOfAxiomWithReferencedEntities(sub, sup);
			}
			@Override
			public void onInclusion(DataPropertyExpression sub, DataPropertyExpression sup) {
				ont.addSubPropertyOfAxiomWithReferencedEntities(sub, sup);
			}

			@Override
			public void onInclusion(ClassExpression sub, ClassExpression sup) {
				ont.addSubClassOfAxiomWithReferencedEntities(sub, sup);
			}
			@Override
			public void onInclusion(DataRangeExpression sub, DataRangeExpression sup) {
				ont.addSubClassOfAxiomWithReferencedEntities(sub, sup);
			}
		});
		
		return ont;	
	}

}
