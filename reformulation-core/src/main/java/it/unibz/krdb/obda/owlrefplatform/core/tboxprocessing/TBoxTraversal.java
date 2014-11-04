package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public class TBoxTraversal {
	
	public static void traverse(TBoxReasoner reasoner, TBoxTraverseListener listener) {
		
		for (Equivalences<ObjectPropertyExpression> nodes : reasoner.getObjectProperties()) {
			ObjectPropertyExpression node = nodes.getRepresentative();
			
			for (Equivalences<ObjectPropertyExpression> descendants : reasoner.getObjectProperties().getSub(nodes)) {
				ObjectPropertyExpression descendant = descendants.getRepresentative();
				listener.onInclusion(descendant, node);
			}
			for (ObjectPropertyExpression equivalent : nodes) {
				if (!equivalent.equals(node)) {
					listener.onInclusion(node, equivalent);
					listener.onInclusion(equivalent, node);
				}
			}
		}
		for (Equivalences<DataPropertyExpression> nodes : reasoner.getDataProperties()) {
			DataPropertyExpression node = nodes.getRepresentative();
			
			for (Equivalences<DataPropertyExpression> descendants : reasoner.getDataProperties().getSub(nodes)) {
				DataPropertyExpression descendant = descendants.getRepresentative();

				listener.onInclusion(descendant, node);
			}
			for (DataPropertyExpression equivalent : nodes) {
				if (!equivalent.equals(node)) {
					listener.onInclusion(node, equivalent);
					listener.onInclusion(equivalent, node);
				}
			}
		}
		
		for (Equivalences<ClassExpression> nodes : reasoner.getClasses()) {
			ClassExpression node = nodes.getRepresentative();
			
			for (Equivalences<ClassExpression> descendants : reasoner.getClasses().getSub(nodes)) {
				ClassExpression descendant = descendants.getRepresentative();
				listener.onInclusion(descendant, node);
					
			}
			for (ClassExpression equivalent : nodes) {
				if (!equivalent.equals(node)) {
					listener.onInclusion(equivalent, node);
					listener.onInclusion(node, equivalent);
				}
			}
		}	
		for (Equivalences<DataRangeExpression> nodes : reasoner.getDataRanges()) {
			DataRangeExpression node = nodes.getRepresentative();
			
			for (Equivalences<DataRangeExpression> descendants : reasoner.getDataRanges().getSub(nodes)) {
				DataRangeExpression descendant = descendants.getRepresentative();
				listener.onInclusion(descendant, node);				
			}
			for (DataRangeExpression equivalent : nodes) {
				if (!equivalent.equals(node)) {
					listener.onInclusion(node, equivalent);
					listener.onInclusion(equivalent, node);
				}
			}
		}	
	}
}
