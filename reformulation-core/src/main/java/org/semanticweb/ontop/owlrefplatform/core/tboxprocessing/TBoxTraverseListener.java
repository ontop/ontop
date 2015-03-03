package org.semanticweb.ontop.owlrefplatform.core.tboxprocessing;

import org.semanticweb.ontop.ontology.DataPropertyExpression;
import org.semanticweb.ontop.ontology.DataRangeExpression;
import org.semanticweb.ontop.ontology.ObjectPropertyExpression;
import org.semanticweb.ontop.ontology.ClassExpression;

public interface TBoxTraverseListener {
	
	public void onInclusion(DataPropertyExpression sub, DataPropertyExpression sup);

	public void onInclusion(ObjectPropertyExpression sub, ObjectPropertyExpression sup);
	
	public void onInclusion(ClassExpression sub, ClassExpression sup);

	public void onInclusion(DataRangeExpression sub, DataRangeExpression sup);
	
	
}
