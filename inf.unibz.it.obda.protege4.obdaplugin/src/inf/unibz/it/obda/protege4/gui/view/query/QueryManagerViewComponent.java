package inf.unibz.it.obda.protege4.gui.view.query;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.gui.swing.dataquery.panel.SavedQueriesPanel;
import inf.unibz.it.obda.gui.swing.dataquery.panel.SavedQueriesPanelListener;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;

import java.awt.BorderLayout;
import java.util.logging.Logger;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class QueryManagerViewComponent extends AbstractOWLViewComponent {
    /**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private static final Logger log = Logger.getLogger(AbstractOWLViewComponent.class.toString());
    private SavedQueriesPanel panel = null;
	private OBDAPluginController	obdaController;
    
    @Override
    protected void disposeOWLView() {
    	QueryManagerViewsList queryManagerViews= (QueryManagerViewsList)this.getOWLEditorKit().get(QueryManagerViewsList.class.getName());
    	if (queryManagerViews == null) 
    		return;
    	queryManagerViews.remove(this);
    }

    @Override
    protected void initialiseOWLView() throws Exception {
    	obdaController = (OBDAPluginController)getOWLEditorKit().get(APIController.class.getName());
    	setLayout(new BorderLayout());
    	panel = new SavedQueriesPanel(obdaController.getQueryController());
//    	panel.add
//    	panel.add
        add(panel, BorderLayout.CENTER);
        
        registerInEditorKit();
        
        log.info("Query Manager view initialized");
    }
    
    public void addListener(SavedQueriesPanelListener listener) {
    	panel.addQueryManagerListener(listener);
    }
    
    public void removeListener(SavedQueriesPanelListener listener) {
    	panel.removeQueryManagerListener(listener);
    }
    
    /***
     *
     * TODO: Document
     *
     */
    public void registerInEditorKit() {
    	QueryManagerViewsList queryManagerViews= (QueryManagerViewsList)this.getOWLEditorKit().get(QueryManagerViewsList.class.getName());
    	if (queryManagerViews == null) {
    		queryManagerViews = new QueryManagerViewsList();
    		getOWLEditorKit().put(QueryManagerViewsList.class.getName(), queryManagerViews);
    	}
    	
    	QueryInterfaceViewsList queryInterfaceViews= (QueryInterfaceViewsList)this.getOWLEditorKit().get(QueryInterfaceViewsList.class.getName());
    	if ((queryInterfaceViews != null)&&(!queryInterfaceViews.isEmpty())) {
    		for (QueryInterfaceViewComponent queryInterfaceView : queryInterfaceViews) {
    			this.addListener(queryInterfaceView);
			}
    	}
    	
    	queryManagerViews.add(this);
    }
}
