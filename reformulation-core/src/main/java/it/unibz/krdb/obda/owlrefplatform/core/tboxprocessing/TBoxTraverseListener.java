package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.PropertyExpression;

public interface TBoxTraverseListener {
	
	public void onInclusion(PropertyExpression sub, PropertyExpression sup);
	public void onInclusion(BasicClassDescription sub, BasicClassDescription sup);

}
