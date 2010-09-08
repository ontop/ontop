package inf.unibz.it.obda.protege4.gui.view;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.gui.swing.datasource.panels.SQLSchemaInspectorPanel;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;
import inf.unibz.it.obda.protege4.gui.constants.GUIConstants;

import java.awt.BorderLayout;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class RDBMSInspectorView  extends AbstractOWLViewComponent {
    
    /**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private static final Logger log = Logger.getLogger(RDBMSInspectorView.class);
    
    
    @Override
    protected void disposeOWLView() {
        
    }

    @Override
    protected void initialiseOWLView() throws Exception {
    	
//    	List<Bundle> plugins = ProtegeApplication.getBundleManager().getPlugins();
//    	Bundle obdaBundle = null;
//    	for(Bundle plugin: plugins) {
//    		String name = plugin.getSymbolicName();
//    		if (name.equals("inf.unibz.it.obda.protege4")) {
//    			obdaBundle =  plugin;
//    		}
//    	}
//    	if (obdaBundle == null)
//    		throw new Exception("Error initializing SQLQuery interface view, couldnt find OBDA Bundle");
//    	
//    	ServiceReference dscServiceReference = obdaBundle.getBundleContext().getServiceReference(DatasourcesController.class.getName());
//    	DatasourcesController dsc = (DatasourcesController)obdaBundle.getBundleContext().getService(dscServiceReference);
    	OBDAPluginController apic = getOWLEditorKit().get(APIController.class.getName());
    	
    	setBackground(GUIConstants.COLOR_DATASOURCE);
        setLayout(new BorderLayout());
        add(new SQLSchemaInspectorPanel(apic.getDatasourcesController()), BorderLayout.CENTER);
        log.info("RDBMS schema inspector initialized");
    }
}
