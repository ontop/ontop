package org.semanticweb.ontop.owlrefplatform.core.tboxprocessing;

import org.semanticweb.ontop.ontology.BasicClassDescription;
import org.semanticweb.ontop.ontology.Property;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.Equivalences;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public class TBoxTraversal {
	
	public static void traverse(TBoxReasoner reasoner, TBoxTraverseListener listener) {
		
		for (Equivalences<Property> nodes : reasoner.getProperties()) {
			Property node = nodes.getRepresentative();
			
			for (Equivalences<Property> descendants : reasoner.getProperties().getSub(nodes)) {
				Property descendant = descendants.getRepresentative();

				if (!descendant.equals(node))  // exclude trivial inclusions
					listener.onInclusion(descendant, node);
			}
			for (Property equivalent : nodes) {
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

				if (!descendant.equals(node))  // exclude trivial inclusions
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
