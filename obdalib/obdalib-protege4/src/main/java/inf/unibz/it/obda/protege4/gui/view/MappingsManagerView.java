package inf.unibz.it.obda.protege4.gui.view;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.datasource.panels.DatasourceSelector;
import inf.unibz.it.obda.gui.swing.mapping.panel.MappingManagerPanel;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;

import java.awt.BorderLayout;
import java.util.Vector;

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
    OBDAPluginController apiController = 
        getOWLEditorKit().get(APIController.class.getName());
    OBDAPreferences preference = (OBDAPreferences)
         getOWLEditorKit().get(OBDAPreferences.class.getName());
    	
    MappingController mapController = apiController.getMappingController();
    DatasourcesController dsController = apiController.getDatasourcesController();
    	
    Vector<DataSource> vecDatasource = 
        new Vector<DataSource>(dsController.getAllSources().values());
 
    MappingManagerPanel mappingPanel = new MappingManagerPanel(apiController, 
        mapController, dsController, preference); 
    DatasourceSelector datasourceSelector = new DatasourceSelector(vecDatasource);
    datasourceSelector.addDatasourceListListener(mappingPanel);
    dsController.addDatasourceControllerListener(datasourceSelector);
    	
    setLayout(new BorderLayout());
    add(mappingPanel, BorderLayout.CENTER);
    add(datasourceSelector, BorderLayout.NORTH);
      
    log.info("Mappings manager initialized");
  }
}
