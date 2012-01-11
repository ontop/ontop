package it.unibz.krdb.obda.owlapi2;

import it.unibz.krdb.obda.model.OBDAModel;

import org.semanticweb.owlapi.reasoner.OWLReasoner;

public interface OBDAOWLReasoner extends OWLReasoner {
	
	public void loadOBDAModel(OBDAModel model);

}
