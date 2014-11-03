package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
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
		
		for (Equivalences<BasicClassDescription> nodes : reasoner.getClasses()) {
			BasicClassDescription node = nodes.getRepresentative();
			
			for (Equivalences<BasicClassDescription> descendants : reasoner.getClasses().getSub(nodes)) {
				BasicClassDescription descendant = descendants.getRepresentative();

				//if (!descendant.equals(node))  // exclude trivial inclusions
				if (descendant instanceof ClassExpression)
					listener.onInclusion((ClassExpression)descendant, (ClassExpression)node);
				else
					listener.onInclusion((DataRangeExpression)descendant, (DataRangeExpression)node);
					
			}
			for (BasicClassDescription equivalent : nodes) {
				if (!equivalent.equals(node)) {
					if (node instanceof ClassExpression) {
						listener.onInclusion((ClassExpression)equivalent, (ClassExpression)node);
						listener.onInclusion((ClassExpression)node, (ClassExpression)equivalent);
					}
					else {
						listener.onInclusion((DataRangeExpression)node, (DataRangeExpression)equivalent);
						listener.onInclusion((DataRangeExpression)equivalent, (DataRangeExpression)node);
					}
				}
			}
		}	
	}
}
