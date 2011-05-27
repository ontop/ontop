package inf.unibz.it.obda.protege4.gui.view;

import inf.unibz.it.obda.protege4.core.OBDAPluginController;
import it.unibz.krdb.obda.gui.swing.panel.DatasourceSelector;
import it.unibz.krdb.obda.gui.swing.panel.SQLSchemaInspectorPanel;
import it.unibz.krdb.obda.model.DataSource;
import it.unibz.krdb.obda.model.DatasourcesController;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;

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
    
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -1168890804596237449L;
	private static final Logger log = Logger.getLogger(RDBMSInspectorView.class);   
    
  @Override
  protected void disposeOWLView() {
    // Do nothing.
  }

  @Override
  protected void initialiseOWLView() throws Exception {
  	
  	OBDAPluginController apic = 
  	    getOWLEditorKit().get(OBDAModelImpl.class.getName());
  	
  	DatasourcesController dsController = apic.getOBDAManager().getDatasourcesController();
  	Vector<DataSource> vecDatasource = 
        new Vector<DataSource>(dsController.getAllSources());
  	
  	SQLSchemaInspectorPanel inspectorPanel = new SQLSchemaInspectorPanel(dsController);
  	DatasourceSelector datasourceSelector = new DatasourceSelector(vecDatasource);
  	datasourceSelector.addDatasourceListListener(inspectorPanel);
    dsController.addDatasourceControllerListener(datasourceSelector);

  	setBackground(new java.awt.Color(153, 0, 0));
  	
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
    
    log.debug("RDBMS schema inspector initialized");
  }
}
