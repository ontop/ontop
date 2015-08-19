package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public class TBoxTraversal {
	
	public static void traverse(TBoxReasoner reasoner, TBoxTraverseListener listener) {
		
		// note: these are nodes of the same DAG and so, we can compare them with ==
		
		for (Equivalences<ObjectPropertyExpression> nodes : reasoner.getObjectPropertyDAG()) {
			ObjectPropertyExpression node = nodes.getRepresentative();
			
			for (Equivalences<ObjectPropertyExpression> descendants : reasoner.getObjectPropertyDAG().getSub(nodes)) {
				ObjectPropertyExpression descendant = descendants.getRepresentative();
				if (descendant != node)
					listener.onInclusion(descendant, node);
			}
			for (ObjectPropertyExpression equivalent : nodes) {
				if (equivalent != node) {
					listener.onInclusion(node, equivalent);
					listener.onInclusion(equivalent, node);
				}
			}
		}
		for (Equivalences<DataPropertyExpression> nodes : reasoner.getDataPropertyDAG()) {
			DataPropertyExpression node = nodes.getRepresentative();
			
			for (Equivalences<DataPropertyExpression> descendants : reasoner.getDataPropertyDAG().getSub(nodes)) {
				DataPropertyExpression descendant = descendants.getRepresentative();
				if (descendant != node)
					listener.onInclusion(descendant, node);
			}
			for (DataPropertyExpression equivalent : nodes) {
				if (equivalent != node) {
					listener.onInclusion(node, equivalent);
					listener.onInclusion(equivalent, node);
				}
			}
		}
		
		for (Equivalences<ClassExpression> nodes : reasoner.getClassDAG()) {
			ClassExpression node = nodes.getRepresentative();
			
			for (Equivalences<ClassExpression> descendants : reasoner.getClassDAG().getSub(nodes)) {
				ClassExpression descendant = descendants.getRepresentative();
				if (descendant != node)
					listener.onInclusion(descendant, node);
			}
			for (ClassExpression equivalent : nodes) {
				if (equivalent != node) {
					listener.onInclusion(equivalent, node);
					listener.onInclusion(node, equivalent);
				}
			}
		}	
		for (Equivalences<DataRangeExpression> nodes : reasoner.getDataRangeDAG()) {
			DataRangeExpression node = nodes.getRepresentative();
			
			for (Equivalences<DataRangeExpression> descendants : reasoner.getDataRangeDAG().getSub(nodes)) {
				DataRangeExpression descendant = descendants.getRepresentative();
				if (descendant != node)
					listener.onInclusion(descendant, node);				
			}
			for (DataRangeExpression equivalent : nodes) {
				if (equivalent != node) {
					listener.onInclusion(node, equivalent);
					listener.onInclusion(equivalent, node);
				}
			}
		}	
	}
}
