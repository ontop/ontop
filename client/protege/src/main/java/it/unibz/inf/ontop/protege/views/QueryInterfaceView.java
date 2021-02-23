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
import it.unibz.inf.ontop.owlapi.connection.impl.DefaultOntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.BooleanOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.GraphOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.OBDAModelManagerListener;
import it.unibz.inf.ontop.protege.gui.OWLResultSetTableModel;
import it.unibz.inf.ontop.protege.gui.action.OBDADataQueryAction;
import it.unibz.inf.ontop.protege.panels.QueryInterfacePanel;
import it.unibz.inf.ontop.protege.panels.ResultViewTablePanel;
import it.unibz.inf.ontop.protege.panels.SavedQueriesPanelListener;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OBDAProgressListener;
import it.unibz.inf.ontop.protege.utils.OBDAProgressMonitor;
import it.unibz.inf.ontop.protege.utils.TextMessageFrame;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class QueryInterfaceView extends AbstractOWLViewComponent implements SavedQueriesPanelListener, OBDAModelManagerListener {

    private static final long serialVersionUID = 1L;

    private QueryInterfacePanel queryEditorPanel;

    private ResultViewTablePanel resultTablePanel;

    private OWLOntologyChangeListener ontologyListener;

    private OBDAModelManager obdaController;

    private PrefixManager prefixManager;

    private OWLResultSetTableModel tableModel;

    private static final Logger log = LoggerFactory.getLogger(QueryInterfaceView.class);

    @Override
    protected void disposeOWLView() {
        this.getOWLModelManager().removeOntologyChangeListener(ontologyListener);

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
        obdaController.removeListener(this);
    }


    @Override
    protected void initialiseOWLView() {
        obdaController = (OBDAModelManager) getOWLEditorKit().get(SQLPPMappingImpl.class.getName());
        obdaController.addListener(this);

        prefixManager = obdaController.getActiveOBDAModel().getMutablePrefixManager();

        queryEditorPanel = new QueryInterfacePanel(obdaController.getActiveOBDAModel(), obdaController.getQueryController());
        queryEditorPanel.setPreferredSize(new Dimension(400, 250));
        queryEditorPanel.setMinimumSize(new Dimension(400, 250));

        resultTablePanel = new ResultViewTablePanel(queryEditorPanel);
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
        ontologyListener = changes -> {
            Runnable runner = () -> resultTablePanel.setTableModel(new DefaultTableModel());
            SwingUtilities.invokeLater(runner);
        };

        this.getOWLModelManager().addOntologyChangeListener(ontologyListener);
        setupListeners();

        // Setting up actions for all the buttons of this view.

        //count the tuples in the result table for SELECT queries
        resultTablePanel.setCountAllTuplesActionForUCQ(new OBDADataQueryAction<Long>("Counting tuples...", QueryInterfaceView.this) {
            @Override
            public OWLEditorKit getEditorKit(){
                return getOWLEditorKit();
            }
            @Override
            public int getNumberOfRows() {
                return -1;
            }

            @Override
            public void handleResult(Long result){
                updateTablePanelStatus(result);
            }

            @Override
            public void handleSQLTranslation(String sqlQuery) {
                resultTablePanel.setSQLTranslation(sqlQuery);
            }

            @Override
            public Long executeQuery(OntopOWLStatement st, String query) throws OWLException {
                return st.getTupleCount(query);
            }

            @Override
            public boolean isRunning() {
                return false;
            }
        });

        //action clicking on execute button with a select query
        queryEditorPanel.setExecuteSelectAction(new OBDADataQueryAction<TupleOWLResultSet>("Executing queries...", QueryInterfaceView.this) {

            @Override
            public OWLEditorKit getEditorKit(){
                return getOWLEditorKit();
            }

            @Override
            public void handleResult(TupleOWLResultSet result) throws OWLException{
                createTableModelFromResultSet(result);
                showTupleResultInTablePanel();
            }

            @Override
            public void handleSQLTranslation(String sqlQuery) {
                resultTablePanel.setSQLTranslation(sqlQuery);
            }

            @Override
            public void run(String query){
                removeResultTable();
                super.run(query);
            }

            @Override
            public int getNumberOfRows() {
                OWLResultSetTableModel tm = getTableModel();
                if (tm == null)
                    return 0;
                return getTableModel().getRowCount();
            }

            @Override
            public boolean isRunning() {
                OWLResultSetTableModel tm = getTableModel();
                return tm != null && tm.isFetching();
            }

            @Override
            public TupleOWLResultSet executeQuery(OntopOWLStatement st,
                                                  String queryString) throws OWLException {
                if(queryEditorPanel.isFetchAllSelect()) {
                    return st.executeSelectQuery(queryString);
                }
                else {
                    DefaultOntopOWLStatement defaultOntopOWLStatement = (DefaultOntopOWLStatement) st;
                    defaultOntopOWLStatement.setMaxRows(queryEditorPanel.getFetchSize());
                    return defaultOntopOWLStatement.executeSelectQuery(queryString);
                }
            }
        });


        //action clicking on execute button with an ask query
        queryEditorPanel.setExecuteAskAction(new OBDADataQueryAction<BooleanOWLResultSet>("Executing queries...", QueryInterfaceView.this) {

            @Override
            public OWLEditorKit getEditorKit(){
                return getOWLEditorKit();
            }

            @Override
            public void handleResult(BooleanOWLResultSet result) throws OWLException{
                queryEditorPanel.showBooleanActionResultInTextInfo("Result:", result);
            }

            @Override
            public void handleSQLTranslation(String sqlQuery) {
                resultTablePanel.setSQLTranslation(sqlQuery);
            }

            @Override
            public int getNumberOfRows() {
                return -1;
            }

            @Override
            public boolean isRunning() {
                OWLResultSetTableModel tm = getTableModel();
                return tm != null && tm.isFetching();
            }
            @Override
            public BooleanOWLResultSet executeQuery(OntopOWLStatement st,
                                                    String queryString) throws OWLException {
                removeResultTable();
                if(queryEditorPanel.isFetchAllSelect())
                    return st.executeAskQuery(queryString);

                DefaultOntopOWLStatement defaultOntopOWLStatement = (DefaultOntopOWLStatement) st;
                defaultOntopOWLStatement.setMaxRows(queryEditorPanel.getFetchSize());
                return defaultOntopOWLStatement.executeAskQuery(queryString);
            }

        });

        //action clicking on execute button with an graph query (describe or construct)
        queryEditorPanel.setExecuteGraphQueryAction(
                new OBDADataQueryAction<GraphOWLResultSet>("Executing queries...", QueryInterfaceView.this) {

                    @Override
                    public OWLEditorKit getEditorKit(){
                        return getOWLEditorKit();
                    }

                    @Override
                    public GraphOWLResultSet executeQuery(OntopOWLStatement st, String queryString) throws OWLException {
                        removeResultTable();
                        if(queryEditorPanel.isFetchAllSelect())
                            return st.executeGraphQuery(queryString);

                        DefaultOntopOWLStatement defaultOntopOWLStatement = (DefaultOntopOWLStatement) st;
                        defaultOntopOWLStatement.setMaxRows(queryEditorPanel.getFetchSize());
                        return defaultOntopOWLStatement.executeGraphQuery(queryString);
                    }

                    @Override
                    public void handleResult(GraphOWLResultSet result) throws OWLException{
                        OWLAxiomToTurtleVisitor owlVisitor = new OWLAxiomToTurtleVisitor(prefixManager);
                        populateResultUsingVisitor(result, owlVisitor);
                        showGraphResultInTextPanel(owlVisitor);
                    }

                    @Override
                    public void handleSQLTranslation(String sqlQuery) {
                        resultTablePanel.setSQLTranslation(sqlQuery);
                    }

                    @Override
                    public int getNumberOfRows() {
                        return 0;
                    }

                    @Override
                    public boolean isRunning() {
                        OWLResultSetTableModel tm = getTableModel();
                        return tm != null && tm.isFetching();
                    }


                });

        //action after right click on the query for UCQ expansion
        queryEditorPanel.setRetrieveUCQExpansionAction(new OBDADataQueryAction<String>("Rewriting query...", QueryInterfaceView.this) {

            @Override
            public String executeQuery(OntopOWLStatement st, String query) throws OWLException {
                removeResultTable();
                return st.getRewritingRendering(query);
            }

            @Override
            public OWLEditorKit getEditorKit(){
                return getOWLEditorKit();
            }

            @Override
            public void handleResult(String result){
                showActionResultInTextPanel("UCQ Expansion Result", result);
            }

            @Override
            public void handleSQLTranslation(String sqlQuery) {
                resultTablePanel.setSQLTranslation(sqlQuery);
            }

            @Override
            public int getNumberOfRows() {
                return -1;
            }
            @Override
            public boolean isRunning() {
                return false;
            }
        });

        //action after right click on the query for UCQ Unfolding
        queryEditorPanel.setRetrieveUCQUnfoldingAction(new OBDADataQueryAction<String>("Unfolding queries...", QueryInterfaceView.this) {
            @Override
            public String executeQuery(OntopOWLStatement st, String query) throws OWLException{
                removeResultTable();
				// TODO: should we show the SQL query only?
				return st.getExecutableQuery(query).toString();
            }

            @Override
            public OWLEditorKit getEditorKit(){
                return getOWLEditorKit();
            }

            @Override
            public void handleResult(String result){
                showActionResultInTextPanel("SQL Translation", result);
            }

            @Override
            public void handleSQLTranslation(String sqlQuery) {
                resultTablePanel.setSQLTranslation(sqlQuery);
            }

            @Override
            public int getNumberOfRows() {
                return -1;
            }
            @Override
            public boolean isRunning() {
                return false;
            }
        });

        //action to save the query on a file
        resultTablePanel.setOBDASaveQueryToFileAction(fileLocation -> {
            OBDAProgressMonitor monitor = null;
            try {
                monitor = new OBDAProgressMonitor("Writing output files...", getOWLWorkspace());
                monitor.start();
                CountDownLatch latch = new CountDownLatch(1);
                List<String[]> data = tableModel.getTabularData();
                if(monitor.isCanceled())
                    return;
                File output = new File(fileLocation);
                BufferedWriter writer = new BufferedWriter(new FileWriter(output, false));
                SaveQueryToFileAction action = new SaveQueryToFileAction(latch, data, writer);
                monitor.addProgressListener(action);
                action.run();
                latch.await();
                monitor.stop();
            } catch (Exception e) {
                DialogUtils.showQuickErrorDialog(QueryInterfaceView.this, e);
            }
        });
        log.debug("Query Manager view initialized");
    }



    private class UCQExpansionPanel implements Runnable{
        String title;
        String result;
        OBDADataQueryAction<?> action;
        UCQExpansionPanel(String title, String result, OBDADataQueryAction<?> action){
            this.title = title;
            this.result = result;
            this.action = action;
        }
        @Override
        public void run(){
            TextMessageFrame panel = new TextMessageFrame(title);
            JFrame protegeFrame = ProtegeManager.getInstance().getFrame(getWorkspace());
            DialogUtils.centerDialogWRTParent(protegeFrame, panel);
            DialogUtils.installEscapeCloseOperation(panel);
            panel.setTextMessage(result);

            panel.setTimeProcessingMessage(String.format("Amount of processing time: %s sec", action.getExecutionTime()/1000));
            panel.setVisible(true);
        }
    }

    private void showActionResultInTextPanel(String title, String result) {
        if (result == null) {
            return;
        }
        OBDADataQueryAction<?> action = queryEditorPanel.getRetrieveUCQExpansionAction();
        UCQExpansionPanel alter_result_panel = new UCQExpansionPanel(title, result, action);
        SwingUtilities.invokeLater(alter_result_panel);
    }


    private class QueryStatusUpdater implements Runnable{
        long result;
        QueryStatusUpdater(long result){
            this.result = result;
        }

        @Override
        public void run(){
            queryEditorPanel.updateStatus(result);
        }
    }

    protected void updateTablePanelStatus(Long result) {
        if (result != -1) {
            Runnable status_updater = new QueryStatusUpdater(result);
            SwingUtilities.invokeLater(status_updater);
        }
    }


    private class TableModelSetter implements Runnable{
        OWLResultSetTableModel currentTableModel;
        TableModelSetter(OWLResultSetTableModel currentTableModel){
            this.currentTableModel = currentTableModel;
        }

        @Override
        public void run(){
            resultTablePanel.setTableModel(currentTableModel);
        }
    }

    private int showTupleResultInTablePanel() {
        OWLResultSetTableModel currentTableModel = getTableModel();
        if (currentTableModel != null) {
            SwingUtilities.invokeLater(new TableModelSetter(currentTableModel));
            return currentTableModel.getRowCount();
        } else {
            return 0;
        }
    }


    private synchronized void createTableModelFromResultSet(TupleOWLResultSet result) throws OWLException {
        if (result == null)
            throw new NullPointerException("An error occurred. createTableModelFromResultSet cannot use a null QuestOWLResultSet");
        tableModel = new OWLResultSetTableModel(result, prefixManager,
                queryEditorPanel.isShortURISelect(),
                queryEditorPanel.isFetchAllSelect(),
                queryEditorPanel.getFetchSize());
        tableModel.addTableModelListener(queryEditorPanel);
    }

    /**
     * removes the result table.
     * Could be called at data query execution, or at cancelling
     * Not necessary when replacing with a new result, just to remove old
     * results that are outdated
     */
    private synchronized void removeResultTable(){
        OWLResultSetTableModel tm = getTableModel();
        if (tm != null){
            tm.close();
        }
        resultTablePanel.setTableModel(new DefaultTableModel());
    }

    private OWLResultSetTableModel getTableModel() {
        return tableModel;
    }


    private class ResultUpdater implements Runnable {
        OWLAxiomToTurtleVisitor visitor;

        ResultUpdater(OWLAxiomToTurtleVisitor visitor){
            super();
            this.visitor = visitor;
        }

        @Override
        public void run(){
            TextMessageFrame panel = new TextMessageFrame("SPARQL Graph Query (CONSTRUCT/DESCRIBE) Result");
            JFrame protegeFrame = ProtegeManager.getInstance().getFrame(getWorkspace());
            DialogUtils.centerDialogWRTParent(protegeFrame, panel);
            DialogUtils.installEscapeCloseOperation(panel);
            panel.setTextMessage(visitor.getString());
            panel.setVisible(true);
        }
    }

    private synchronized void showGraphResultInTextPanel(OWLAxiomToTurtleVisitor visitor) {
        try {
            ResultUpdater result_updater = new ResultUpdater(visitor);
            SwingUtilities.invokeLater(result_updater);
        } catch (Exception e) {
            DialogUtils.showQuickErrorDialog(QueryInterfaceView.this, e);
        }

    }

    private void populateResultUsingVisitor(GraphOWLResultSet resultSet, OWLAxiomToTurtleVisitor visitor) throws OWLException {
        if (resultSet != null) {
            while (resultSet.hasNext()) {
                resultSet.next().accept(visitor);
            }
            resultSet.close();
        }
    }

    @Override
    public synchronized void selectedQueryChanged(String new_group, String new_query, String new_id) {
        this.queryEditorPanel.selectedQueryChanged(new_group, new_query, new_id);
    }

    /**
     * On creation of a new view, we register it globally and make sure that its selector is listened
     * by all other instances of query view in this editor kit. Also, we make this new instance listen
     * to the selection of all other query selectors in the views.
     */
    public void setupListeners() {

        // Getting the list of views
        QueryInterfaceViewsList queryInterfaceViews = (QueryInterfaceViewsList) this.getOWLEditorKit().get(QueryInterfaceViewsList.class.getName());
        if ((queryInterfaceViews == null)) {
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


    private class SaveQueryToFileAction implements OBDAProgressListener {

        private final CountDownLatch latch;
        private Thread thread;
        private final List<String[]> rawData;
        private final Writer writer;
        private boolean isCancelled;
        private boolean errorShown;

        private SaveQueryToFileAction(CountDownLatch latch, List<String[]> rawData, Writer writer) {
            this.latch = latch;
            this.rawData = rawData;
            this.writer = writer;
            this.errorShown = false;
            this.isCancelled = false;
        }

        public void run() {
            thread = new Thread() {
                @Override
                public void run() {
                    try {
                        writeCSV(rawData, writer);
                        latch.countDown();
                    } catch (Exception e) {
                        if(!isCancelled()){
                            errorShown = true;
                            latch.countDown();
                            log.error(e.getMessage());
                            DialogUtils.showQuickErrorDialog(null, e, "Error while writing output file.");
                        }
                    }
                }
            };
            thread.start();
        }

        @Override
        public void actionCanceled() throws Exception {
            this.isCancelled = true;
            try {
                writer.flush();
                writer.close();
                latch.countDown();
                log.info("Writing operation cancelled by users.");
            } catch (Exception e) {
                latch.countDown();
                DialogUtils.showQuickErrorDialog(null, e, "Error during cancel action.");
            }
        }

        @Override
        public boolean isCancelled() {
            return this.isCancelled;
        }

        @Override
        public boolean isErrorShown() {
            return this.errorShown;
        }
    }


    @Override
    public void activeOntologyChanged() {
        queryEditorPanel.setOBDAModel(this.obdaController.getActiveOBDAModel());
    }

    private static void writeCSV(List<String[]> tabularData, Writer writer) throws IOException {

        // Print the CSV content
        for (String[] rows : tabularData) {
            StringBuilder line = new StringBuilder();
            boolean needComma = false;
            for (String row : rows) {
                if (needComma) {
                    line.append(",");
                }
                line.append(row);
                needComma = true;
            }
            line.append("\n");
            writer.write(line.toString());
            writer.flush();
        }
        writer.close();
    }
}
