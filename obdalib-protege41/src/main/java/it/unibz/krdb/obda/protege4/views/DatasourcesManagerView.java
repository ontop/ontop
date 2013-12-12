package it.unibz.krdb.obda.protege4.views;

import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.protege4.core.OBDAModelManagerListener;
import it.unibz.krdb.obda.protege4.panels.DatasourceParameterEditorPanel;

import java.awt.BorderLayout;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasourcesManagerView extends AbstractOWLViewComponent implements OBDAModelManagerListener {

	private static final long serialVersionUID = -4515710047558710080L;

	private static final Logger log = LoggerFactory.getLogger(DatasourcesManagerView.class);

	DatasourceParameterEditorPanel editor;

	OBDAModelManager apic = null;

	@Override
	protected void disposeOWLView() {
		apic.removeListener(this);
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		
		apic = (OBDAModelManager) getOWLEditorKit().get(OBDAModelImpl.class.getName());
		apic.addListener(this);

		setLayout(new BorderLayout());

		editor = new DatasourceParameterEditorPanel(apic.getActiveOBDAModel());
		add(editor, BorderLayout.NORTH);

		log.debug("Datasource browser initialized");
	}

	@Override
	public void activeOntologyChanged() {
		editor.setDatasourcesController(apic.getActiveOBDAModel());
	}
}
