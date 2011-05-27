package inf.unibz.it.obda.protege4.gui.view;

import inf.unibz.it.obda.gui.swing.panel.DataSourceSelectionPanel;
import inf.unibz.it.obda.gui.swing.panel.DatasourceParameterEditorPanel;
import inf.unibz.it.obda.model.impl.OBDAModelImpl;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;

import java.awt.BorderLayout;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class DatasourcesManagerViewComponent extends AbstractOWLViewComponent  {
	private static final long		serialVersionUID	= -4515710047558710080L;

	private static final Logger		log					= Logger.getLogger(DatasourcesManagerViewComponent.class);

	DataSourceSelectionPanel		selectionpanel;
	DatasourceParameterEditorPanel	editor;

	@Override
	protected void disposeOWLView() {

	}

	@Override
	protected void initialiseOWLView() throws Exception {
		OBDAPluginController apic = getOWLEditorKit().get(OBDAModelImpl.class.getName());

		setLayout(new BorderLayout());

		selectionpanel = new DataSourceSelectionPanel(apic.getOBDAManager().getDatasourcesController());
		add(selectionpanel, BorderLayout.NORTH);

		editor = new DatasourceParameterEditorPanel(apic.getOBDAManager().getDatasourcesController());
		add(editor, BorderLayout.CENTER);
		selectionpanel.getDataSourceSelector().addDatasourceListListener(editor);
		apic.getOBDAManager().getDatasourcesController().addDatasourceControllerListener(selectionpanel.getDataSourceSelector());

//		apic.addListener(this);
		
		log.debug("Datasource browser initialized");
		
		
	}

//	@Override
//	public void obdaModelChanged(OBDAModel oldmodel, OBDAModel newmodel) {
//		selectionpanel.setDatasourcesController(newmodel.getDatasourcesController());
//		editor.setDatasourcesController(newmodel.getDatasourcesController());
//	}
}
