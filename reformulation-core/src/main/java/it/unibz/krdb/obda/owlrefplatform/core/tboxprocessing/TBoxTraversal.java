package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public class TBoxTraversal {
	
	static boolean equalityNodes=false;
	
	public static void traverse(TBoxReasoner reasoner, TBoxTraverseListener listener, boolean... equality) {
		if(equality.length==1)
			equalityNodes= equality[0]; 
		
		for (Equivalences<Property> nodes : reasoner.getProperties()) {
			Property node = nodes.getRepresentative();
			
			for (Equivalences<Property> descendants : reasoner.getProperties().getSub(nodes)) {
				Property descendant = descendants.getRepresentative();

				if (equalityNodes || !descendant.equals(node) )  // exclude trivial inclusions
					listener.onInclusion(descendant, node);
			}
			for (Property equivalent : nodes) {
				if (equalityNodes || !equivalent.equals(node)) {
					listener.onInclusion(node, equivalent);					
					listener.onInclusion(equivalent, node);
				}
			}
		}
		
		for (Equivalences<BasicClassDescription> nodes : reasoner.getClasses()) {
			BasicClassDescription node = nodes.getRepresentative();
			
			for (Equivalences<BasicClassDescription> descendants : reasoner.getClasses().getSub(nodes)) {
				BasicClassDescription descendant = descendants.getRepresentative();

				if (equalityNodes || !descendant.equals(node))  // exclude trivial inclusions
					listener.onInclusion(descendant, node);
			}
			for (BasicClassDescription equivalent : nodes) {
				if (equalityNodes || !equivalent.equals(node)) {
					listener.onInclusion(node, equivalent);					
					listener.onInclusion(equivalent, node);
				}
			}
		}	
	}
}
