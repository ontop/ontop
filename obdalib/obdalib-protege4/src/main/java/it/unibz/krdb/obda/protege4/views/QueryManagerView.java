package it.unibz.krdb.obda.protege4.views;

import it.unibz.krdb.obda.gui.swing.panel.SavedQueriesPanel;
import it.unibz.krdb.obda.gui.swing.panel.SavedQueriesPanelListener;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.protege4.core.OBDAModelManagerListener;

import java.awt.BorderLayout;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryManagerView extends AbstractOWLViewComponent implements OBDAModelManagerListener {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private static final Logger	log					= LoggerFactory.getLogger(AbstractOWLViewComponent.class.toString());
	private SavedQueriesPanel	panel				= null;
	private OBDAModelManager	obdaController;

	@Override
	protected void disposeOWLView() {
		QueryManagerViewsList queryManagerViews = (QueryManagerViewsList) this.getOWLEditorKit().get(QueryManagerViewsList.class.getName());
		if (queryManagerViews == null)
			return;
		queryManagerViews.remove(this);
		obdaController.removeListener(this);
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		obdaController = (OBDAModelManager) getOWLEditorKit().get(OBDAModelImpl.class.getName());
		obdaController.addListener(this);
		
		setLayout(new BorderLayout());
		panel = new SavedQueriesPanel(obdaController.getActiveOBDAModel().getQueryController());

		add(panel, BorderLayout.CENTER);

		registerInEditorKit();
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
			for (QueryInterfaceView queryInterfaceView : queryInterfaceViews) {
				this.addListener(queryInterfaceView);
			}
		}

		queryManagerViews.add(this);
	}

	@Override
	public void activeOntologyChanged() {
		panel.setQueryController(obdaController.getActiveOBDAModel().getQueryController());
	}
}
