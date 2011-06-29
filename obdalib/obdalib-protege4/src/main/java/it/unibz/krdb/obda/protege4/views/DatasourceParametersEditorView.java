package it.unibz.krdb.obda.protege4.views;

import it.unibz.krdb.obda.gui.swing.panel.DatasourceParameterEditorPanel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.protege4.core.OBDAModelManagerListener;

import java.awt.BorderLayout;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class DatasourceParametersEditorView extends AbstractOWLViewComponent implements OBDAModelManagerListener {
	/**
	 * 
	 */
	private static final long		serialVersionUID	= 1L;
	private static final Logger		log					= Logger.getLogger(DatasourceParametersEditorView.class);

	DatasourceParameterEditorPanel	panel				= null;

	OBDAModelManager				apic				= null;

	@Override
	protected void disposeOWLView() {
		 apic.removeListener(this);
	}

	@Override
	protected void initialiseOWLView() throws Exception {

		apic = getOWLEditorKit().get(OBDAModelImpl.class.getName());
		apic.addListener(this);

		panel = new DatasourceParameterEditorPanel(apic.getActiveOBDAModel().getDatasourcesController());

		add(panel, BorderLayout.CENTER);
		log.debug("Datasource parameter view Component initialized");

	}

	@Override
	public void activeOntologyChanged() {
		panel.setDatasourcesController(apic.getActiveOBDAModel().getDatasourcesController());

	}

}
