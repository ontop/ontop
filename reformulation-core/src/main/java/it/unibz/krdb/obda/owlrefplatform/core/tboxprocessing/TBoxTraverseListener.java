package it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing;

import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.Property;

public interface TBoxTraverseListener {
	
	public void onInclusion(Property sub, Property sup);
	public void onInclusion(BasicClassDescription sub, BasicClassDescription sup);

}
