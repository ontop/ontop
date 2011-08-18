package it.unibz.krdb.obda.protege4.views;

import it.unibz.krdb.obda.gui.swing.panel.DataSourceSelectionPanel;
import it.unibz.krdb.obda.gui.swing.panel.DatasourceParameterEditorPanel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.protege4.core.OBDAModelManagerListener;

import java.awt.BorderLayout;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class DatasourcesManagerView extends AbstractOWLViewComponent implements OBDAModelManagerListener {
	private static final long		serialVersionUID	= -4515710047558710080L;

	private static final Logger		log					= Logger.getLogger(DatasourcesManagerView.class);

	DataSourceSelectionPanel		selectionpanel;
	DatasourceParameterEditorPanel	editor;

	OBDAModelManager				apic				= null;

	@Override
	protected void disposeOWLView() {
		apic.removeListener(this);
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		apic = getOWLEditorKit().get(OBDAModelImpl.class.getName());
		apic.addListener(this);

		setLayout(new BorderLayout());

		selectionpanel = new DataSourceSelectionPanel(apic.getActiveOBDAModel());
		add(selectionpanel, BorderLayout.NORTH);

		editor = new DatasourceParameterEditorPanel(apic.getActiveOBDAModel());
		add(editor, BorderLayout.CENTER);
		selectionpanel.getDataSourceSelector().addDatasourceListListener(editor);
//		apic.getActiveOBDAModel().addDatasourceControllerListener(selectionpanel.getDataSourceSelector());

		// apic.addListener(this);

		log.debug("Datasource browser initialized");

	}

	@Override
	public void activeOntologyChanged() {
		editor.setDatasourcesController(apic.getActiveOBDAModel());
		selectionpanel.setDatasourcesController(apic.getActiveOBDAModel());
		
	}

}
