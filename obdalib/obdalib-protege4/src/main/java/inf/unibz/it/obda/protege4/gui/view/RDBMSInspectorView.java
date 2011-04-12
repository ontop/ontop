package inf.unibz.it.obda.protege4.gui.view;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.datasource.panels.DatasourceSelector;
import inf.unibz.it.obda.gui.swing.datasource.panels.SQLSchemaInspectorPanel;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;
import inf.unibz.it.obda.protege4.gui.constants.GUIConstants;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class RDBMSInspectorView  extends AbstractOWLViewComponent {
    
	private static final Logger log = Logger.getLogger(RDBMSInspectorView.class);   
    
  @Override
  protected void disposeOWLView() {
    // Do nothing.
  }

  @Override
  protected void initialiseOWLView() throws Exception {
  	
  	OBDAPluginController apic = 
  	    getOWLEditorKit().get(APIController.class.getName());
  	
  	DatasourcesController dsController = apic.getDatasourcesController();
  	Vector<DataSource> vecDatasource = 
        new Vector<DataSource>(dsController.getAllSources().values());
  	
  	SQLSchemaInspectorPanel inspectorPanel = new SQLSchemaInspectorPanel(dsController);
  	DatasourceSelector datasourceSelector = new DatasourceSelector(vecDatasource);
  	datasourceSelector.addDatasourceListListener(inspectorPanel);
    dsController.addDatasourceControllerListener(datasourceSelector);

  	setBackground(GUIConstants.COLOR_DATASOURCE);
  	
  	JPanel selectorPanel = new JPanel();
    selectorPanel.setLayout(new GridBagLayout());
    
    JLabel label = new JLabel("Select datasource: ");
    label.setBackground(new java.awt.Color(153, 153, 153));
    label.setFont(new java.awt.Font("Arial", 1, 11));
    label.setForeground(new java.awt.Color(153, 153, 153));
    label.setPreferredSize(new Dimension(119,14));
    
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
    inspectorPanel.setBorder(new TitledBorder("SQL Schema Inspector"));
  	
    setLayout(new BorderLayout());
    add(inspectorPanel, BorderLayout.CENTER);
    add(selectorPanel, BorderLayout.NORTH);
    
    log.info("RDBMS schema inspector initialized");
  }
}
