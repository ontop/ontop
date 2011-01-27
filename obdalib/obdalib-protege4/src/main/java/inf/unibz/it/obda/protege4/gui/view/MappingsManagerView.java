package inf.unibz.it.obda.protege4.gui.view;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.gui.swing.mapping.panel.MappingManagerPanel;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;

import java.awt.BorderLayout;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class MappingsManagerView extends AbstractOWLViewComponent {

	private static final Logger log = Logger.getLogger(MappingsManagerView.class);
       
    @Override
    protected void disposeOWLView() {
        // Do nothing.
    }

    @Override
    protected void initialiseOWLView() throws Exception {
    	OBDAPluginController apic = getOWLEditorKit().get(APIController.class.getName());
    	MappingController mapc = apic.getMappingController();
    	DatasourcesController dsc = apic.getDatasourcesController();
    	OBDAPreferences pref = (OBDAPreferences)
    			getOWLEditorKit().get(OBDAPreferences.class.getName());
 
    	MappingManagerPanel mappingPanel = new MappingManagerPanel(apic, mapc, dsc, pref); 
    								
        setLayout(new BorderLayout());
        add(mappingPanel, BorderLayout.CENTER);
        log.info("Mappings manager initialized");
    }
}
