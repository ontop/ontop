package inf.unibz.it.obda.protege4.gui.view;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.datasource.panels.DatasourceSelector;
import inf.unibz.it.obda.gui.swing.dependencies.panel.DependencyTabPane;
import inf.unibz.it.obda.gui.swing.dependencies.panel.Dependency_SelectMappingPane;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class DependencyInterfaceViewComponent extends AbstractOWLViewComponent {

	private static final Logger log = Logger.getLogger(DependencyInterfaceViewComponent.class);
	
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
		
    DatasourcesController dsController = apiController.getDatasourcesController();
    MappingController mapController = apiController.getMappingController();
    
    Vector<DataSource> vecDatasource = 
        new Vector<DataSource>(dsController.getAllSources().values());

		DependencyTabPane dependencyPanel = 
		    new DependencyTabPane(apiController, preference);
		Dependency_SelectMappingPane mappingPanel = 
		    new Dependency_SelectMappingPane(apiController, mapController, 
		        dsController, preference);
		DatasourceSelector datasourceSelector = new DatasourceSelector(vecDatasource);
    datasourceSelector.addDatasourceListListener(dependencyPanel);
    datasourceSelector.addDatasourceListListener(mappingPanel);
    dsController.addDatasourceControllerListener(datasourceSelector);
		
		JSplitPane splDependencyMapping = new javax.swing.JSplitPane();
		splDependencyMapping.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		splDependencyMapping.setTopComponent(dependencyPanel);
		splDependencyMapping.setBottomComponent(mappingPanel);
		splDependencyMapping.setContinuousLayout(true);
		
		setLayout(new BorderLayout());
    add(splDependencyMapping, BorderLayout.CENTER);
    add(datasourceSelector, BorderLayout.SOUTH);
		
    log.info("Dependency interface view initialized");
	}
}
