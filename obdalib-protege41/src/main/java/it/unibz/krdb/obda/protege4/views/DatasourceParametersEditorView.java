package it.unibz.krdb.obda.protege4.views;

import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.protege4.core.OBDAModelManagerListener;
import it.unibz.krdb.obda.protege4.panels.DatasourceParameterEditorPanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasourceParametersEditorView extends AbstractOWLViewComponent implements OBDAModelManagerListener {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(DatasourceParametersEditorView.class);

	private DatasourceParameterEditorPanel panel;

	private OBDAModelManager apic;

	@Override
	protected void disposeOWLView() {
		apic.removeListener(this);
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		
		
		
		
		apic = (OBDAModelManager) getOWLEditorKit().get(OBDAModelImpl.class.getName());
		apic.addListener(this);

		panel = new DatasourceParameterEditorPanel(apic.getActiveOBDAModel());
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		
		setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        add(panel, gridBagConstraints);
        
//		add(panel, BorderLayout.CENTER);
		
		log.debug("Datasource parameter view Component initialized");
	}

	@Override
	public void activeOntologyChanged() {
		panel.setDatasourcesController(apic.getActiveOBDAModel());
	}
}
