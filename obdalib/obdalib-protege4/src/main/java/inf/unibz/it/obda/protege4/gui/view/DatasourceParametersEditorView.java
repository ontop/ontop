package inf.unibz.it.obda.protege4.gui.view;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.gui.swing.datasource.panels.DatasourceParameterEditorPanel;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;
import inf.unibz.it.obda.protege4.core.OBDAPluginControllerListener;

import java.awt.BorderLayout;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class DatasourceParametersEditorView extends AbstractOWLViewComponent implements OBDAPluginControllerListener {
	/**
	 * 
	 */
	private static final long		serialVersionUID	= 1L;
	private static final Logger		log					= Logger.getLogger(DatasourceParametersEditorView.class);

	DatasourceParameterEditorPanel	panel				= null;

	@Override
	protected void disposeOWLView() {
	}

	@Override
	protected void initialiseOWLView() throws Exception {

		OBDAPluginController apic = getOWLEditorKit().get(APIController.class.getName());

		panel = new DatasourceParameterEditorPanel(apic.getOBDAManager().getDatasourcesController());
		add(panel, BorderLayout.CENTER);
		log.debug("Datasource parameter view Component initialized");

	}

	@Override
	public void obdaModelChanged(APIController oldmodel, APIController newmodel) {
		OBDAPluginController apic = getOWLEditorKit().get(APIController.class.getName());
		panel.setDatasourcesController(newmodel.getDatasourcesController());
	}
}
