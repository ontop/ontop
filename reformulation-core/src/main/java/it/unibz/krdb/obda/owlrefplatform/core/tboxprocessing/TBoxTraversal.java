package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataPropertyRangeExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.Datatype;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.PropertyExpression;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public class TBoxTraversal {
	
	public static void traverse(TBoxReasoner reasoner, TBoxTraverseListener listener) {
		
		for (Equivalences<PropertyExpression> nodes : reasoner.getProperties()) {
			PropertyExpression node = nodes.getRepresentative();
			
			for (Equivalences<PropertyExpression> descendants : reasoner.getProperties().getSub(nodes)) {
				PropertyExpression descendant = descendants.getRepresentative();

				//if (!descendant.equals(node))  // exclude trivial inclusions
				if (descendant instanceof ObjectPropertyExpression)
					listener.onInclusion((ObjectPropertyExpression)descendant, (ObjectPropertyExpression)node);
				else
					listener.onInclusion((DataPropertyExpression)descendant, (DataPropertyExpression)node);
			}
			for (PropertyExpression equivalent : nodes) {
				if (!equivalent.equals(node)) {
					if (node instanceof ObjectPropertyExpression) {
						listener.onInclusion((ObjectPropertyExpression)node, (ObjectPropertyExpression)equivalent);
						listener.onInclusion((ObjectPropertyExpression)equivalent, (ObjectPropertyExpression)node);
					}
					else {
						listener.onInclusion((DataPropertyExpression)node, (DataPropertyExpression)equivalent);
						listener.onInclusion((DataPropertyExpression)equivalent, (DataPropertyExpression)node);
					}
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
