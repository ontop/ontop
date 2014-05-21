package org.semanticweb.ontop.owlrefplatform.core.tboxprocessing;

import org.semanticweb.ontop.ontology.BasicClassDescription;
import org.semanticweb.ontop.ontology.Property;

public interface TBoxTraverseListener {
	
	public void onInclusion(Property sub, Property sup);
	public void onInclusion(BasicClassDescription sub, BasicClassDescription sup);

}
