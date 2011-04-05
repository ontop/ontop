package inf.unibz.it.obda.protege4.gui.view;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.gui.swing.datasource.panels.DataSourceSelectionPanel;
import inf.unibz.it.obda.gui.swing.datasource.panels.DatasourceParameterEditorPanel;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class DatasourcesManagerViewComponent extends AbstractOWLViewComponent {
    private static final long serialVersionUID = -4515710047558710080L;
    
    private static final Logger log = Logger.getLogger(DatasourcesManagerViewComponent.class);
    
    
    @Override
    protected void disposeOWLView() {
        
    }

    @Override
    protected void initialiseOWLView() throws Exception {
    	OBDAPluginController apic = getOWLEditorKit().get(APIController.class.getName());
    	
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        DataSourceSelectionPanel selectionpanel = new DataSourceSelectionPanel(apic);
        add(selectionpanel, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.weightx = 1.0;
        DatasourceParameterEditorPanel editor = new DatasourceParameterEditorPanel(apic);
        add(editor,gridBagConstraints);
        selectionpanel.getDataSourceSelector().addDatasourceListListener(editor);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.weightx = 1.0;
        JPanel newPanel = new JPanel();
        newPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("User Settings"));
        newPanel.setMaximumSize(new Dimension(80, 50));
        add(newPanel,gridBagConstraints);
        
        log.info("Datasource browser initialized");
    }

}
