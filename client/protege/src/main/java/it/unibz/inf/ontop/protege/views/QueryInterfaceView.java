package it.unibz.inf.ontop.protege.views;

/*
 * #%L
 * ontop-protege
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen-Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.panels.QueryInterfacePanel;
import it.unibz.inf.ontop.protege.panels.ResultViewTablePanel;
import it.unibz.inf.ontop.protege.panels.SavedQueriesPanelListener;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.gui.dialogs.TextQueryResultsDialog;
import it.unibz.inf.ontop.protege.workers.OntopQuerySwingWorker;
import it.unibz.inf.ontop.protege.workers.OntopQuerySwingWorkerFactory;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class QueryInterfaceView extends AbstractOWLViewComponent implements SavedQueriesPanelListener {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryInterfaceView.class);


    private QueryInterfacePanel queryEditorPanel;
    private ResultViewTablePanel resultTablePanel;

    private OWLOntologyChangeListener ontologyListener;

    private OBDAModelManager obdaModelManager;

    @Override
    protected void disposeOWLView() {
        getOWLModelManager().removeOntologyChangeListener(ontologyListener);

        QueryInterfaceViewsList queryInterfaceViews = (QueryInterfaceViewsList) this.getOWLEditorKit().get(QueryInterfaceViewsList.class.getName());
        if ((queryInterfaceViews != null)) {
            queryInterfaceViews.remove(this);
        }

        QueryManagerViewsList queryManagerViews = (QueryManagerViewsList) this.getOWLEditorKit().get(QueryManagerViewsList.class.getName());
        if ((queryManagerViews != null) && (!queryManagerViews.isEmpty())) {
            for (QueryManagerView queryInterfaceView : queryManagerViews) {
                queryInterfaceView.removeListener(this);
            }
        }
    }

    private final OntopQuerySwingWorkerFactory<String, Void> retrieveUCQExpansionAction =
            (ontop, query) -> new OntopQuerySwingWorker<String, Void>(ontop, query, this, "Rewriting query") {

        @Override
        protected String runQuery(OntopOWLStatement statement, String query) throws Exception {
            return statement.getRewritingRendering(query);
        }

        @Override
        protected void onCompletion(String result, String sqlQuery) {
            setSQLTranslation(sqlQuery);
            TextQueryResultsDialog dialog = new TextQueryResultsDialog(getWorkspace(),
                    "Intermediate Query",
                    result,
                    "Processing time: " + DialogUtils.renderElapsedTime(elapsedTimeMillis()));
            dialog.setVisible(true);
        }
    };

    private final OntopQuerySwingWorkerFactory<String, Void> retrieveUCQUnfoldingAction =
            (ontop, query) -> new OntopQuerySwingWorker<String, Void>(ontop, query, this, "Rewriting query") {

        @Override
        protected String runQuery(OntopOWLStatement statement, String query) throws Exception {
            // TODO: should we show the SQL query only?
            return statement.getExecutableQuery(query).toString();
        }

        @Override
        protected void onCompletion(String result, String sqlQuery) {
            setSQLTranslation(sqlQuery);
            TextQueryResultsDialog dialog = new TextQueryResultsDialog(getWorkspace(),
                    "SQL Translation",
                    result,
                    "Processing time: " + DialogUtils.renderElapsedTime(elapsedTimeMillis()));
            dialog.setVisible(true);
        }
    };

    @Override
    protected void initialiseOWLView() {
        obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(getOWLEditorKit());

        queryEditorPanel = new QueryInterfacePanel(getOWLEditorKit(), obdaModelManager, retrieveUCQExpansionAction, retrieveUCQUnfoldingAction);
        queryEditorPanel.setPreferredSize(new Dimension(400, 250));
        queryEditorPanel.setMinimumSize(new Dimension(400, 250));

        resultTablePanel = new ResultViewTablePanel(getOWLEditorKit(), queryEditorPanel);
        resultTablePanel.setMinimumSize(new java.awt.Dimension(400, 250));
        resultTablePanel.setPreferredSize(new java.awt.Dimension(400, 250));

        JSplitPane splQueryInterface = new JSplitPane();
        splQueryInterface.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splQueryInterface.setResizeWeight(0.5);
        splQueryInterface.setDividerLocation(0.5);
        splQueryInterface.setOneTouchExpandable(true);
        splQueryInterface.setTopComponent(queryEditorPanel);
        splQueryInterface.setBottomComponent(resultTablePanel);
        JPanel pnlQueryInterfacePane = new JPanel();
        pnlQueryInterfacePane.setLayout(new BorderLayout());
        pnlQueryInterfacePane.add(splQueryInterface, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(pnlQueryInterfacePane, BorderLayout.CENTER);


        // Setting up model listeners
        ontologyListener = changes ->
                SwingUtilities.invokeLater(() -> resultTablePanel.setTableModel(new DefaultTableModel()));

        this.getOWLModelManager().addOntologyChangeListener(ontologyListener);
        setupListeners();

        queryEditorPanel.setExecuteSelectAction(() -> {
            resultTablePanel.runSelectQuery(obdaModelManager, queryEditorPanel.getQuery());
        });

        queryEditorPanel.setExecuteAskAction(() -> {
            resultTablePanel.runAskQuery(queryEditorPanel.getQuery());
        });

        queryEditorPanel.setExecuteGraphQueryAction(() -> {
            resultTablePanel.runGraphQuery(obdaModelManager, queryEditorPanel.getQuery());
        });

        LOGGER.debug("Query Manager view initialized");
    }



    private void setSQLTranslation(String s) {
        resultTablePanel.setSQLTranslation(s);
    }


    @Override
    public void selectedQueryChanged(String groupId, String queryId, String query) {
        queryEditorPanel.selectedQueryChanged(groupId, queryId, query);
    }

    /**
     * On creation of a new view, we register it globally and make sure that its selector is listened
     * by all other instances of query view in this editor kit. Also, we make this new instance listen
     * to the selection of all other query selectors in the views.
     */
    public void setupListeners() {

        // Getting the list of views
        QueryInterfaceViewsList queryInterfaceViews = (QueryInterfaceViewsList) getOWLEditorKit().get(QueryInterfaceViewsList.class.getName());
        if (queryInterfaceViews == null) {
            queryInterfaceViews = new QueryInterfaceViewsList();
            getOWLEditorKit().put(QueryInterfaceViewsList.class.getName(), queryInterfaceViews);
        }

        // Adding the new instance (this)
        queryInterfaceViews.add(this);

        // Registering the current query view with all existing query manager views
        QueryManagerViewsList queryManagerViews = (QueryManagerViewsList) this.getOWLEditorKit().get(QueryManagerViewsList.class.getName());
        if ((queryManagerViews != null) && (!queryManagerViews.isEmpty())) {
            for (QueryManagerView queryInterfaceView : queryManagerViews) {
                queryInterfaceView.addListener(this);
            }
        }
    }
}
