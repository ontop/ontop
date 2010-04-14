package inf.unibz.it.obda.owlapi;

import org.semanticweb.owl.inference.OWLReasonerFactory;

import inf.unibz.it.obda.api.controller.APIController;

public interface OBDAOWLReasonerFactory extends OWLReasonerFactory {

	public abstract void setOBDAController(APIController controller);

}