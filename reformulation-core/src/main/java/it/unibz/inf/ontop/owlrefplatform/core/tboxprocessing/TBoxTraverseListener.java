package it.unibz.inf.ontop.owlrefplatform.core.tboxprocessing;

import it.unibz.inf.ontop.ontology.DataPropertyExpression;
import it.unibz.inf.ontop.ontology.DataRangeExpression;
import it.unibz.inf.ontop.ontology.ObjectPropertyExpression;
import it.unibz.inf.ontop.ontology.ClassExpression;

public interface TBoxTraverseListener {
	
	public void onInclusion(DataPropertyExpression sub, DataPropertyExpression sup);

	public void onInclusion(ObjectPropertyExpression sub, ObjectPropertyExpression sup);
	
	public void onInclusion(ClassExpression sub, ClassExpression sup);

	public void onInclusion(DataRangeExpression sub, DataRangeExpression sup);
	
	
}
