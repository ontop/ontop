package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.PropertyExpression;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public class TBoxTraversal {
	
	public static void traverse(TBoxReasoner reasoner, TBoxTraverseListener listener) {
		
		for (Equivalences<PropertyExpression> nodes : reasoner.getProperties()) {
			PropertyExpression node = nodes.getRepresentative();
			
			for (Equivalences<PropertyExpression> descendants : reasoner.getProperties().getSub(nodes)) {
				PropertyExpression descendant = descendants.getRepresentative();

				//if (!descendant.equals(node))  // exclude trivial inclusions
					listener.onInclusion(descendant, node);
			}
			for (PropertyExpression equivalent : nodes) {
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
					listener.onInclusion(descendant, node);
			}
			for (BasicClassDescription equivalent : nodes) {
				if (!equivalent.equals(node)) {
					listener.onInclusion(node, equivalent);					
					listener.onInclusion(equivalent, node);
				}
			}
		}	
	}
}
