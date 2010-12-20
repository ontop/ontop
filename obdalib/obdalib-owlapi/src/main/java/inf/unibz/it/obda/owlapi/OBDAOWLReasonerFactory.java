package inf.unibz.it.obda.owlapi;

import inf.unibz.it.obda.api.controller.APIController;

import org.semanticweb.owl.inference.OWLReasonerFactory;

public interface OBDAOWLReasonerFactory extends OWLReasonerFactory {

	public abstract void setOBDAController(APIController controller);

}