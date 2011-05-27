package inf.unibz.it.obda.owlapi;

import inf.unibz.it.obda.model.APIController;

import org.semanticweb.owl.model.OWLOntology;

public class OWLAPIController extends APIController {

	OWLOntology					currentOntology	= null;

	private boolean				loadingData;

	public OWLAPIController() {
		super();
	}



}
