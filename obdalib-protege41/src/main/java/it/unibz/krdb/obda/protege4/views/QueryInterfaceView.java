package it.unibz.krdb.obda.protege4.views;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.owlapi3.OWLResultSetWriter;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SPARQLQueryUtility;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.protege4.core.OBDAModelManagerListener;
import it.unibz.krdb.obda.protege4.gui.OWLResultSetTableModel;
import it.unibz.krdb.obda.protege4.gui.action.OBDADataQueryAction;
import it.unibz.krdb.obda.protege4.gui.action.OBDASaveQueryResultToFileAction;
import it.unibz.krdb.obda.protege4.panels.QueryInterfacePanel;
import it.unibz.krdb.obda.protege4.panels.ResultViewTablePanel;
import it.unibz.krdb.obda.protege4.panels.SavedQueriesPanelListener;
import it.unibz.krdb.obda.protege4.utils.DialogUtils;
import it.unibz.krdb.obda.protege4.utils.OBDAProgessMonitor;
import it.unibz.krdb.obda.protege4.utils.OBDAProgressListener;
import it.unibz.krdb.obda.protege4.utils.TextMessageFrame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.table.DefaultTableModel;

import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryInterfaceView extends AbstractOWLViewComponent implements SavedQueriesPanelListener, OBDAModelManagerListener {

	private static final long serialVersionUID = 1L;

	private QueryInterfacePanel queryEditorPanel;

	private ResultViewTablePanel resultTablePanel;

	private OWLOntologyChangeListener ontologyListener;
	
	private OBDAModelManager obdaController;
	
	private PrefixManager prefixManager;
	
	private OWLResultSetTableModel tableModel;

	private static final Logger log = LoggerFactory.getLogger(QueryInterfaceView.class);
	
	private static String QUEST_START_MESSAGE = "Quest must be started before using this feature. To proceed \n * select Quest in the \"Reasoners\" menu and \n * click \"Start reasoner\" in the same menu.";
	
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
	protected void initialiseOWLView() throws Exception {
		obdaController = (OBDAModelManager) getOWLEditorKit().get(OBDAModelImpl.class.getName());
		obdaController.addListener(this);
		
		prefixManager = obdaController.getActiveOBDAModel().getPrefixManager();
		
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
		ontologyListener = new OWLOntologyChangeListener() {
			@Override
			public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
				resultTablePanel.setTableModel(new DefaultTableModel());
			}
		};
		this.getOWLModelManager().addOntologyChangeListener(ontologyListener);
		setupListeners();
		
		// Setting up actions for all the buttons of this view.
		resultTablePanel.setCountAllTuplesActionForUCQ(new OBDADataQueryAction() {
			@Override
			public long getExecutionTime() {
				return -1;
			}
			@Override
			public int getNumberOfRows() {
				return -1;
			}
			@Override
			public void run(String query) {
				OBDAProgessMonitor monitor = null;
				try {
					monitor = new OBDAProgessMonitor("Counting tuples...");
					CountDownLatch latch = new CountDownLatch(1);
					CountAllTuplesAction action = new CountAllTuplesAction(latch, query);
					monitor.addProgressListener(action);
					monitor.start();
					action.run();
					latch.await();
					monitor.stop();
					int result = action.getResult();
					updateTablePanelStatus(result);
				} catch (Exception e) {
					DialogUtils.showQuickErrorDialog(QueryInterfaceView.this, e);
				} finally {
					monitor.stop();
				}
			}
		});
		
		queryEditorPanel.setExecuteUCQAction(new OBDADataQueryAction() {
			private long time = 0;
			private int rows = 0;
			@Override
			public void run(String query) {
				OBDAProgessMonitor monitor = null;
				try {
					monitor = new OBDAProgessMonitor("Executing queries...");
					monitor.start();
					CountDownLatch latch = new CountDownLatch(1);
					SPARQLQueryUtility internalQuery = new SPARQLQueryUtility(query);
					ExecuteQueryAction action = new ExecuteQueryAction(latch, internalQuery);
					monitor.addProgressListener(action);
					long startTime = System.currentTimeMillis();
					action.run();
					latch.await();
					monitor.stop();
					if (internalQuery.isSelectQuery() || internalQuery.isAskQuery()) {
						QuestOWLResultSet result = action.getResult();
						long end = System.currentTimeMillis();
						time = end - startTime;
						createTableModelFromResultSet(result);
						rows = showTupleResultInTablePanel();
					} else if (internalQuery.isConstructQuery()) {
						List<OWLAxiom> result = action.getGraphResult();
						OWLAxiomToTurtleVisitor owlVisitor = new OWLAxiomToTurtleVisitor(prefixManager);
						populateResultUsingVisitor(result, owlVisitor);
						showGraphResultInTextPanel(owlVisitor);
						long end = System.currentTimeMillis();
						time = end - startTime;
						rows = result.size();
					} else if (internalQuery.isDescribeQuery()) {
						List<OWLAxiom> result = action.getGraphResult();
						OWLAxiomToTurtleVisitor owlVisitor = new OWLAxiomToTurtleVisitor(prefixManager);
						populateResultUsingVisitor(result, owlVisitor);
						showGraphResultInTextPanel(owlVisitor);
						long end = System.currentTimeMillis();
						time = end - startTime;
						rows = result.size();
					}
				} catch (Exception e) {
					DialogUtils.showQuickErrorDialog(QueryInterfaceView.this, e);
				} finally {
					monitor.stop();
				}
			}
			@Override
			public long getExecutionTime() {
				return time;
			}
			@Override
			public int getNumberOfRows() {
				return rows;
			}
		});
		
		queryEditorPanel.setRetrieveUCQExpansionAction(new OBDADataQueryAction() {
			private long time = 0;
			@Override
			public void run(String query) {
				OBDAProgessMonitor monitor = null;
				try {
					monitor = new OBDAProgessMonitor("Rewriting query...");
					CountDownLatch latch = new CountDownLatch(1);
					ExpandQueryAction action = new ExpandQueryAction(latch, query);
					monitor.addProgressListener(action);
					monitor.start();
					long startTime = System.currentTimeMillis();
					action.run();
					latch.await();
					monitor.stop();
					String result = action.getResult();
					long end = System.currentTimeMillis();
					time = end - startTime;
					showActionResultInTextPanel("UCQ Expansion Result", result);
				} catch (InterruptedException e) {
					DialogUtils.showQuickErrorDialog(QueryInterfaceView.this, e);
				}finally {
					monitor.stop();
				}
			}
			@Override
			public long getExecutionTime() {
				return time;
			}
			@Override
			public int getNumberOfRows() {
				return -1;
			}
		});
		
		queryEditorPanel.setRetrieveUCQUnfoldingAction(new OBDADataQueryAction() {
			private long time = 0;
			@Override
			public void run(String query) {
				OBDAProgessMonitor monitor = null;
				try {
					monitor = new OBDAProgessMonitor("Unfolding queries...");
					CountDownLatch latch = new CountDownLatch(1);
					UnfoldQueryAction action = new UnfoldQueryAction(latch, query);
					monitor.addProgressListener(action);
					monitor.start();
					long startTime = System.currentTimeMillis();
					action.run();
					latch.await();
					monitor.stop();
					String result = action.getResult();
					long end = System.currentTimeMillis();
					time = end - startTime;
					showActionResultInTextPanel("UCQ Unfolding Result", result);
				} catch (InterruptedException e) {
					DialogUtils.showQuickErrorDialog(QueryInterfaceView.this, e);
				}finally {
					monitor.stop();
				}
			}
			@Override
			public long getExecutionTime() {
				return time;
			}
			@Override
			public int getNumberOfRows() {
				return -1;
			}
		});
		
		resultTablePanel.setOBDASaveQueryToFileAction(new OBDASaveQueryResultToFileAction() {
			@Override
			public void run(String fileLocation) {
				OBDAProgessMonitor monitor = null;
				try {
					monitor = new OBDAProgessMonitor("Writing output files...");
					monitor.start();
					CountDownLatch latch = new CountDownLatch(1);
					File output = new File(fileLocation);
					BufferedWriter writer = new BufferedWriter(new FileWriter(output, false));
					SaveQueryToFileAction action = new SaveQueryToFileAction(latch, tableModel.getTabularData(), writer);
					monitor.addProgressListener(action);
					action.run();
					latch.await();
					monitor.stop();
				} catch (Exception e) {
					DialogUtils.showQuickErrorDialog(QueryInterfaceView.this, e);
				}
			}
		});
		log.debug("Query Manager view initialized");
	}

	private void showActionResultInTextPanel(String title, String result) {
		
		if (result == null) {
			return;
		}
		TextMessageFrame panel = new TextMessageFrame(title);
		JFrame protegeFrame = ProtegeManager.getInstance().getFrame(getWorkspace());
		DialogUtils.centerDialogWRTParent(protegeFrame, panel);
		DialogUtils.installEscapeCloseOperation(panel);
		OBDADataQueryAction action = queryEditorPanel.getRetrieveUCQExpansionAction();
		panel.setTextMessage(result);
		panel.setTimeProcessingMessage(String.format("Amount of processing time: %s sec", action.getExecutionTime()/1000));
		panel.setVisible(true);
	}

	protected void updateTablePanelStatus(int result) {
		if (result != -1) {
			queryEditorPanel.updateStatus(result);
		}		
	}

	private int showTupleResultInTablePanel() throws OWLException {
		OWLResultSetTableModel currentTableModel = getTableModel();
		if (currentTableModel != null) {
			resultTablePanel.setTableModel(currentTableModel);
			return currentTableModel.getRowCount();
		} else {
			return 0;
		}
	}

	private void createTableModelFromResultSet(QuestOWLResultSet result) throws OWLException {
		if (result != null) {
			tableModel = new OWLResultSetTableModel(result, prefixManager, 
					queryEditorPanel.isShortURISelect(),
					queryEditorPanel.isFetchAllSelect(),
					queryEditorPanel.getFetchSize());
			tableModel.addTableModelListener(queryEditorPanel);
		}
	}
	
	private OWLResultSetTableModel getTableModel() {
		return tableModel;
	}
	
	private void showGraphResultInTextPanel(OWLAxiomToTurtleVisitor visitor) {
		try {
			TextMessageFrame panel = new TextMessageFrame("Query Result");
			JFrame protegeFrame = ProtegeManager.getInstance().getFrame(getWorkspace());
			DialogUtils.centerDialogWRTParent(protegeFrame, panel);
			DialogUtils.installEscapeCloseOperation(panel);
			panel.setTextMessage(visitor.getString());
			panel.setVisible(true);
		} catch (Exception e) {
			DialogUtils.showQuickErrorDialog(QueryInterfaceView.this, e);
		}
		
	}
	
	private void populateResultUsingVisitor(List<OWLAxiom> result, OWLAxiomToTurtleVisitor visitor) {
		if (result != null) {
			for (OWLAxiom axiom : result) {
				axiom.accept(visitor);
			}
		}
	}

	public void selectedQuerychanged(String new_group, String new_query, String new_id) {
		this.queryEditorPanel.selectedQuerychanged(new_group, new_query, new_id);
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
		
		// Registring the current query view with all existing query manager views
		QueryManagerViewsList queryManagerViews = (QueryManagerViewsList) this.getOWLEditorKit().get(QueryManagerViewsList.class.getName());
		if ((queryManagerViews != null) && (!queryManagerViews.isEmpty())) {
			for (QueryManagerView queryInterfaceView : queryManagerViews) {
				queryInterfaceView.addListener(this);
			}
		}
	}

	private class UnfoldQueryAction implements OBDAProgressListener {
		private QuestOWLStatement statement = null;
		private CountDownLatch latch = null;
		private Thread thread = null;
		private String result = null;
		private String query = null;

		private UnfoldQueryAction(CountDownLatch latch, String query) {
			this.latch = latch;
			this.query = query;
		}

		public String getResult() {
			return result;
		}

		public void run() {
			thread = new Thread() {
				@Override
				public void run() {
					OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
					if (reasoner instanceof QuestOWL) {
						try {
							QuestOWL dqr = (QuestOWL) reasoner;
							QuestOWLStatement st = (QuestOWLStatement)dqr.getStatement();
							result = st.getUnfolding(query);
							latch.countDown();
						} catch (Exception e) {
							latch.countDown();
							log.error(e.getMessage(), e);
							DialogUtils.showQuickErrorDialog(null, e, "Error while unfolding query.");
						}
					} else {
						latch.countDown();
						JOptionPane.showMessageDialog(
								null,
								QUEST_START_MESSAGE);
					}
				}
			};
			thread.start();
		}

		@Override
		public void actionCanceled() {
			try {
				closeConnection();
				latch.countDown();
			} catch (Exception e) {
				latch.countDown();
				log.error("Error while canceling unfolding action.", e);
				DialogUtils.showQuickErrorDialog(null, e, "Error while canceling unfolding action.");
			}
		}
		
		public void closeConnection() throws OWLException {
			if (statement != null) {
				statement.close();
			}
		}
	}

	private class ExpandQueryAction implements OBDAProgressListener {

		private QuestOWLStatement statement = null;
		private CountDownLatch latch = null;
		private Thread thread = null;
		private String result = null;
		private String query = null;

		private ExpandQueryAction(CountDownLatch latch, String query) {
			this.latch = latch;
			this.query = query;
		}

		public String getResult() {
			return result;
		}

		public void run() {
			thread = new Thread() {
				@Override
				public void run() {
					OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
					if (reasoner instanceof QuestOWL) {
						try {
							QuestOWL dqr = (QuestOWL) reasoner;
							QuestOWLStatement st = (QuestOWLStatement)dqr.getStatement();
							result = st.getRewriting(query);
							latch.countDown();
						} catch (Exception e) {
							latch.countDown();
							DialogUtils.showQuickErrorDialog(null, e, "Error computing query rewriting");
						}
					} else {
						latch.countDown();
						JOptionPane.showMessageDialog(
								null,
								QUEST_START_MESSAGE);
					}
				}
			};
			thread.start();
		}

		@Override
		public void actionCanceled() {
			try {
				closeConnection();
				latch.countDown();
			} catch (Exception e) {
				latch.countDown();
				log.error("Error while counting.", e);
				DialogUtils.showQuickErrorDialog(null, e, "Error while counting.");
			}
		}
		
		public void closeConnection() throws OWLException {
			if (statement != null) {
				statement.close();
			}
		}
	}

	private class ExecuteQueryAction implements OBDAProgressListener {

		private QuestOWLStatement statement = null;
		private CountDownLatch latch = null;
		private Thread thread = null;
		private QuestOWLResultSet result = null;
		private List<OWLAxiom> graphResult = null;
		private SPARQLQueryUtility query = null;

		private ExecuteQueryAction(CountDownLatch latch, SPARQLQueryUtility query) {
			this.latch = latch;
			this.query = query;
		}

		/**
		 * Returns results from executing SELECT query.
		 */
		public QuestOWLResultSet getResult() {
			return result;
		}
		
		/**
		 * Returns results from executing CONSTRUCT and DESCRIBE query.
		 */
		public List<OWLAxiom> getGraphResult() {
			return graphResult;
		}

		public void run() {
			thread = new Thread() {
				@Override
				public void run() {
					OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
					if (reasoner instanceof QuestOWL) {
						try {
							QuestOWL dqr = (QuestOWL) reasoner;
							statement = dqr.getStatement();
							String queryString = query.getQueryString();
							if (query.isSelectQuery() || query.isAskQuery()) {
								result = statement.executeTuple(queryString);
							} else  {
								graphResult = statement.executeGraph(queryString);
							} 
							latch.countDown();
						} catch (Exception e) {
							latch.countDown();
							log.error(e.getMessage(), e);
							DialogUtils.showQuickErrorDialog(null, e);
						}
					} else {
						latch.countDown();
						JOptionPane.showMessageDialog(
								null,
								QUEST_START_MESSAGE);
					}
				}
			};
			thread.start();
		}

		@Override
		public void actionCanceled() {
			try {
				closeConnection();
				latch.countDown();
			} catch (Exception e) {
				latch.countDown();
				DialogUtils.showQuickErrorDialog(null, e, "Error executing query.");
			}
		}
		
		public void closeConnection() throws OWLException {
			if (statement != null) {
				statement.close();
			}
		}
	}

	private class CountAllTuplesAction implements OBDAProgressListener {

		private QuestOWLStatement statement = null;
		private CountDownLatch latch = null;
		private Thread thread = null;
		private int result = -1;
		private String query = null;

		private CountAllTuplesAction(CountDownLatch latch, String query) {
			this.latch = latch;
			this.query = query;
		}

		public int getResult() {
			return result;
		}

		public void run() {
			thread = new Thread() {
				@Override
				public void run() {
					OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
					if (reasoner instanceof QuestOWL) {
						try {
							QuestOWL dqr = (QuestOWL) reasoner;
							QuestOWLStatement st = dqr.getStatement();
							result = st.getTupleCount(query);
							latch.countDown();
						} catch (Exception e) {
							latch.countDown();
							log.debug(e.getMessage());
							JOptionPane.showMessageDialog(
									null, 
									"Error while counting tuples.\n " + e.getMessage()
									+ "\nPlease refer to the log for more information.");
						}
					} else {
						latch.countDown();
						JOptionPane.showMessageDialog(
								null,
								QUEST_START_MESSAGE);
					}
				}
			};
			thread.start();
		}

		@Override
		public void actionCanceled() {
			try {
				closeConnection();
				latch.countDown();
			} catch (Exception e) {
				latch.countDown();
				log.error("Error while counting.", e);
				DialogUtils.showQuickErrorDialog(null, e, "Error while counting.");
			}
		}
		
		public void closeConnection() throws OWLException {
			if (statement != null) {
				statement.close();
			}
		}
	}

	private class SaveQueryToFileAction implements OBDAProgressListener {

		private CountDownLatch latch;
		private Thread thread;
		private List<String[]> rawData;
		private Writer writer;
		
		private SaveQueryToFileAction(CountDownLatch latch, List<String[]> rawData, Writer writer) {
			this.latch = latch;
			this.rawData = rawData;
			this.writer = writer;
		}
		
		public void run() {
			thread = new Thread() {
				@Override
				public void run() {
					try {
						OWLResultSetWriter.writeCSV(rawData, writer);
						latch.countDown();
					} catch (Exception e) {
						latch.countDown();
						log.error(e.getMessage());
						DialogUtils.showQuickErrorDialog(null, e, "Error while writing output file.");
					}
				}
			};
			thread.start();
		}
		
		@Override
		public void actionCanceled() throws Exception {
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
	}
	
	@Override
	public void activeOntologyChanged() {
		queryEditorPanel.setOBDAModel(this.obdaController.getActiveOBDAModel());
	}
}
