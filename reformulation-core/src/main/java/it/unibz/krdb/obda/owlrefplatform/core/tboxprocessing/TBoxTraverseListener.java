package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataRangeExpression;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ClassExpression;

public interface TBoxTraverseListener {
	
	public void onInclusion(DataPropertyExpression sub, DataPropertyExpression sup);

	public void onInclusion(ObjectPropertyExpression sub, ObjectPropertyExpression sup);
	
	public void onInclusion(ClassExpression sub, ClassExpression sup);

	public void onInclusion(DataRangeExpression sub, DataRangeExpression sup);
	
	
}
