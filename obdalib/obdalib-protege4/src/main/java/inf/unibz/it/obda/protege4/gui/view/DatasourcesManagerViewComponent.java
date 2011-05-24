package inf.unibz.it.obda.protege4.gui.view;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.gui.swing.datasource.panels.DataSourceSelectionPanel;
import inf.unibz.it.obda.gui.swing.datasource.panels.DatasourceParameterEditorPanel;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;

import java.awt.BorderLayout;

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
    	
        setLayout(new BorderLayout());
        
        DataSourceSelectionPanel selectionpanel = new DataSourceSelectionPanel(apic.getOBDAManager());
        add(selectionpanel, BorderLayout.NORTH);
        
        DatasourceParameterEditorPanel editor = new DatasourceParameterEditorPanel(apic.getOBDAManager());
        add(editor,BorderLayout.CENTER);
        selectionpanel.getDataSourceSelector().addDatasourceListListener(editor);
        apic.getOBDAManager().getDatasourcesController().addDatasourceControllerListener(selectionpanel.getDataSourceSelector());
        
        
        log.info("Datasource browser initialized");
    }

}
