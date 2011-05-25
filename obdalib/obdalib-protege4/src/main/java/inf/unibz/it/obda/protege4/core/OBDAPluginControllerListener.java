package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.obda.api.controller.APIController;

public interface OBDAPluginControllerListener {

	/***
	 * Triggered when the OBDA Plugin controller 'forgets' about the old obda model and
	 * creates a new one
	 * 
	 * @param oldmodel
	 * @param newmodel
	 */
	public void obdaModelChanged(APIController oldmodel, APIController newmodel);
}
