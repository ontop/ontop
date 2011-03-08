package inf.unibz.it.obda.protege4.gui.view;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.datasource.panels.DatasourceSelector;
import inf.unibz.it.obda.gui.swing.datasource.panels.SQLSchemaInspectorPanel;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;
import inf.unibz.it.obda.protege4.gui.constants.GUIConstants;

import java.awt.BorderLayout;
import java.util.Vector;

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
  	
    setLayout(new BorderLayout());
    add(inspectorPanel, BorderLayout.CENTER);
    add(datasourceSelector, BorderLayout.SOUTH);
    
    log.info("RDBMS schema inspector initialized");
  }
}
