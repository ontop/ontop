package it.unibz.krdb.obda.protege4.views;

import it.unibz.krdb.obda.gui.swing.panel.DatasourceSelector;
import it.unibz.krdb.obda.gui.swing.panel.SQLQueryPanel;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.protege4.core.OBDAModelManagerListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class SQLQueryInterfaceView extends AbstractOWLViewComponent implements OBDAModelManagerListener {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 993255482453828915L;
	private static final Logger	log					= Logger.getLogger(SQLQueryInterfaceView.class);

	DatasourceSelector			datasourceSelector	= null;
	OBDAModelManager			apic				= null;

	@Override
	protected void disposeOWLView() {
		apic.removeListener(this);
	}

	@Override
	protected void initialiseOWLView() throws Exception {

		apic = (OBDAModelManager)getOWLEditorKit().get(OBDAModelImpl.class.getName());
		apic.addListener(this);

		OBDAModel dsController = apic.getActiveOBDAModel();

		// Vector<DataSource> vecDatasource =
		// new Vector<DataSource>(dsController.getAllSources());

		SQLQueryPanel queryPanel = new SQLQueryPanel();
		datasourceSelector = new DatasourceSelector(dsController);
		datasourceSelector.addDatasourceListListener(queryPanel);

		JPanel selectorPanel = new JPanel();
		selectorPanel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Select datasource: ");
		label.setBackground(new java.awt.Color(153, 153, 153));
		label.setFont(new java.awt.Font("Arial", 1, 11));
		label.setForeground(new java.awt.Color(153, 153, 153));
		label.setPreferredSize(new Dimension(119, 14));

		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		selectorPanel.add(label, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		selectorPanel.add(datasourceSelector, gridBagConstraints);

		selectorPanel.setBorder(new TitledBorder("Datasource Selection"));
		queryPanel.setBorder(new TitledBorder("SQL Query"));

		setLayout(new BorderLayout());
		add(queryPanel, BorderLayout.CENTER);
		add(selectorPanel, BorderLayout.NORTH);

		log.debug("SQL Query view initialized");
	}

	@Override
	public void activeOntologyChanged() {
		datasourceSelector.setDatasourceController(apic.getActiveOBDAModel());

	}
}
