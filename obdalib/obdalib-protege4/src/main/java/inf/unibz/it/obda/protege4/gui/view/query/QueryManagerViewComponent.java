package inf.unibz.it.obda.protege4.gui.view.query;

import inf.unibz.it.obda.gui.swing.panel.SavedQueriesPanel;
import inf.unibz.it.obda.gui.swing.panel.SavedQueriesPanelListener;
import inf.unibz.it.obda.model.OBDAModel;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;

import java.awt.BorderLayout;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryManagerViewComponent extends AbstractOWLViewComponent {
	/**
	 * 
	 */
	private static final long		serialVersionUID	= 1L;
	private static final Logger		log					= LoggerFactory.getLogger(AbstractOWLViewComponent.class.toString());
	private SavedQueriesPanel		panel				= null;
	private OBDAPluginController	obdaController;

	@Override
	protected void disposeOWLView() {
		QueryManagerViewsList queryManagerViews = (QueryManagerViewsList) this.getOWLEditorKit().get(QueryManagerViewsList.class.getName());
		if (queryManagerViews == null)
			return;
		queryManagerViews.remove(this);
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		obdaController = (OBDAPluginController) getOWLEditorKit().get(OBDAModel.class.getName());
		setLayout(new BorderLayout());
		panel = new SavedQueriesPanel(obdaController.getOBDAManager().getQueryController());

		add(panel, BorderLayout.CENTER);

		registerInEditorKit();

		log.debug("Query Manager view initialized");
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
		QueryManagerViewsList queryManagerViews = (QueryManagerViewsList) this.getOWLEditorKit().get(QueryManagerViewsList.class.getName());
		if (queryManagerViews == null) {
			queryManagerViews = new QueryManagerViewsList();
			getOWLEditorKit().put(QueryManagerViewsList.class.getName(), queryManagerViews);
		}

		QueryInterfaceViewsList queryInterfaceViews = (QueryInterfaceViewsList) this.getOWLEditorKit().get(
				QueryInterfaceViewsList.class.getName());
		if ((queryInterfaceViews != null) && (!queryInterfaceViews.isEmpty())) {
			for (QueryInterfaceViewComponent queryInterfaceView : queryInterfaceViews) {
				this.addListener(queryInterfaceView);
			}
		}

		queryManagerViews.add(this);
	}
}
