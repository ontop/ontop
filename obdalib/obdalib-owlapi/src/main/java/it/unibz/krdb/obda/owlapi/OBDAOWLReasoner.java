package it.unibz.krdb.obda.owlapi;

import it.unibz.krdb.obda.model.OBDAModel;

import org.semanticweb.owl.inference.OWLReasoner;

public interface OBDAOWLReasoner extends OWLReasoner {
	
	public void loadOBDAModel(OBDAModel model);

}
