package it.unibz.krdb.obda.protege4.views;

import it.unibz.krdb.obda.gui.swing.OBDADataQueryAction;
import it.unibz.krdb.obda.gui.swing.OBDASaveQueryResultToFileAction;
import it.unibz.krdb.obda.gui.swing.panel.QueryInterfacePanel;
import it.unibz.krdb.obda.gui.swing.panel.ResultViewTablePanel;
import it.unibz.krdb.obda.gui.swing.panel.SavedQueriesPanelListener;
import it.unibz.krdb.obda.gui.swing.utils.DialogUtils;
import it.unibz.krdb.obda.gui.swing.utils.OBDAProgessMonitor;
import it.unibz.krdb.obda.gui.swing.utils.OBDAProgressListener;
import it.unibz.krdb.obda.gui.swing.utils.TextMessageFrame;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.owlapi3.OWLQueryReasoner;
import it.unibz.krdb.obda.owlapi3.OWLResultSet;
import it.unibz.krdb.obda.owlapi3.OWLResultSetTableModel;
import it.unibz.krdb.obda.owlapi3.OWLResultSetWriter;
import it.unibz.krdb.obda.owlapi3.OWLStatement;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.protege4.core.OBDAModelManagerListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.table.DefaultTableModel;

import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
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
	
	private List<String[]> resultSetTabularData = new ArrayList<String[]>(); // for caching

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
	protected void initialiseOWLView() throws Exception {
		obdaController = (OBDAModelManager) getOWLEditorKit().get(OBDAModelImpl.class.getName());
		obdaController.addListener(this);
		
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
				return 0;
			}
			@Override
			public int getNumberOfRows() {
				return 0;
			}
			@Override
			public void run(String query, QueryInterfacePanel panel) {
				try {
					OBDAProgessMonitor monitor = new OBDAProgessMonitor("Counting tuples...");
					CountDownLatch latch = new CountDownLatch(1);
					CountAllTuplesAction action = new CountAllTuplesAction(latch, query);
					monitor.addProgressListener(action);
					monitor.start();
					action.run();
					latch.await();
					monitor.stop();
					int result = action.getResult();
					if (result != -1) {
						panel.updateStatus(result);
					}
				} catch (Exception e) {
					DialogUtils.showQuickErrorDialog(QueryInterfaceView.this, e);
				}
			}
		});

		
		queryEditorPanel.setExecuteUCQAction(new OBDADataQueryAction() {
			private long time = 0;
			private int rows = 0;

			@Override
			public void run(String query, QueryInterfacePanel panel) {
				try {
					OBDAProgessMonitor monitor = new OBDAProgessMonitor("Executing queries...");
					monitor.start();
					CountDownLatch latch = new CountDownLatch(1);
					ExecuteQueryAction action = new ExecuteQueryAction(latch, query);
					monitor.addProgressListener(action);
					long startTime = System.currentTimeMillis();
					action.run();
					latch.await();
					monitor.stop();
					OWLResultSet result = action.getResult();
					if (result != null) {
						OWLResultSetTableModel model = 
								new OWLResultSetTableModel(result, obdaController.getActiveOBDAModel().getPrefixManager(), 
										queryEditorPanel.isShortURISelect());
						model.addTableModelListener(panel);
						rows = model.getRowCount();
						resultTablePanel.setTableModel(model);
						resultSetTabularData = model.getTabularData(); // cache the result data for exporting
					}
					long end = System.currentTimeMillis();
					time = end - startTime;
				} catch (Exception e) {
					DialogUtils.showQuickErrorDialog(QueryInterfaceView.this, e);
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
			private final int rows = 0;

			@Override
			public void run(String query, QueryInterfacePanel pane) {
				try {
					OBDAProgessMonitor monitor = new OBDAProgessMonitor("Rewriting query...");
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
					if (result != null) {
						TextMessageFrame panel = new TextMessageFrame();
						JFrame protegeFrame = ProtegeManager.getInstance().getFrame(getWorkspace());
						DialogUtils.centerDialogWRTParent(protegeFrame, panel);
						DialogUtils.installEscapeCloseOperation(panel);
						panel.setModal(true);
						panel.displaySQL(result);
						panel.setTitle("Query rewriting");
						double aux = time;
						aux = aux / 1000;
						String msg = "Total unfolding time: " + String.valueOf(aux) + " sec";
						panel.updateStatus(msg);
						panel.setVisible(true);
					}
				} catch (InterruptedException e) {
					DialogUtils.showQuickErrorDialog(QueryInterfaceView.this, e);
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

		queryEditorPanel.setRetrieveUCQUnfoldingAction(new OBDADataQueryAction() {
			private long time = 0;
			private final int rows = 0;

			public void run(String query, QueryInterfacePanel pane) {
				try {
					OBDAProgessMonitor monitor = new OBDAProgessMonitor("Unfolding queries...");
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
					if (result != null) {
						TextMessageFrame panel = new TextMessageFrame();
						JFrame protegeFrame = ProtegeManager.getInstance().getFrame(getWorkspace());
						DialogUtils.centerDialogWRTParent(protegeFrame, panel);
						DialogUtils.installEscapeCloseOperation(panel);
						panel.setModal(true);
						panel.displaySQL(result);
						panel.setTitle("Query Unfolding");
						double aux = time;
						aux = aux / 1000;
						String msg = "Total unfolding time: " + String.valueOf(aux) + " sec";
						panel.updateStatus(msg);
						panel.setVisible(true);
					}
				} catch (InterruptedException e) {
					DialogUtils.showQuickErrorDialog(QueryInterfaceView.this, e);
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

		resultTablePanel.setOBDASaveQueryToFileAction(new OBDASaveQueryResultToFileAction() {
			@Override
			public void run(String fileLocation) {
				try {
					OWLResultSetWriter.writeCSV(resultSetTabularData, fileLocation);
				} catch (Exception e) {
					DialogUtils.showQuickErrorDialog(QueryInterfaceView.this, e);
				}
			}
		});
		log.debug("Query Manager view initialized");
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
		private OWLStatement statement = null;
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
					if (reasoner instanceof OWLQueryReasoner) {
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
								"This feature can only be used in conjunction with an UCQ\nenabled reasoner. " +
								"Please, select a UCQ enabled reasoner and try again.");
					}
				}
			};
			thread.start();
		}

		@Override
		public void actionCanceled() {
			try {
				if (statement != null) {
					statement.close();
				}
				latch.countDown();
			} catch (Exception e) {
				latch.countDown();
				log.error("Error while canceling unfolding action.", e);
				DialogUtils.showQuickErrorDialog(null, e, "Error while canceling unfolding action.");
			}
		}
	}

	private class ExpandQueryAction implements OBDAProgressListener {

		private OWLStatement statement = null;
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
					if (reasoner instanceof OWLQueryReasoner) {
						try {
							QuestOWL dqr = (QuestOWL) reasoner;
							QuestOWLStatement st = (QuestOWLStatement)dqr.getStatement();
							result = st.getRewriting(query);
							latch.countDown();
						} catch (Exception e) {
							latch.countDown();
							log.debug(e.getMessage(), e);
							JOptionPane.showMessageDialog(
									null, 
									"Error while expanding query.\n " + e.getMessage()
									+ "\nPlease refer to the log for more information.");
						}

					} else {
						latch.countDown();
						JOptionPane.showMessageDialog(
								null,
								"This feature can only be used in conjunction with an UCQ\nenabled reasoner. " +
								"Please, select a UCQ enabled reasoner and try again.");
					}
				}
			};
			thread.start();
		}

		@Override
		public void actionCanceled() {
			try {
				if (statement != null) {
					statement.close();
				}
				latch.countDown();
			} catch (Exception e) {
				latch.countDown();
				log.error("Error while counting.", e);
				DialogUtils.showQuickErrorDialog(null, e, "Error while counting.");
			}
		}
	}

	private class ExecuteQueryAction implements OBDAProgressListener {

		private OWLStatement statement = null;
		private CountDownLatch latch = null;
		private Thread thread = null;
		private OWLResultSet result = null;
		private String query = null;

		private ExecuteQueryAction(CountDownLatch latch, String query) {
			this.latch = latch;
			this.query = query;
		}

		public OWLResultSet getResult() {
			return result;
		}

		public void run() {
			thread = new Thread() {
				@Override
				public void run() {
					OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
					if (reasoner instanceof OWLQueryReasoner) {
						try {
							OWLQueryReasoner dqr = (OWLQueryReasoner) reasoner;
							OWLStatement st = dqr.getStatement();
							result = st.execute(query);
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
								"This feature can only be used in conjunction with an UCQ\nenabled reasoner. " +
								"Please, select a UCQ enabled reasoner and try again.");
					}
				}
			};
			thread.start();
		}

		@Override
		public void actionCanceled() {
			try {
				if (statement != null) {
					statement.close();
				}
				latch.countDown();
			} catch (Exception e) {
				latch.countDown();
				log.error("Error while counting.", e);
				DialogUtils.showQuickErrorDialog(null, e, "Error while counting.");
			}
		}

	}

	private class CountAllTuplesAction implements OBDAProgressListener {

		private OWLStatement statement = null;
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
					if (reasoner instanceof OWLQueryReasoner) {
						try {
							QuestOWL dqr = (QuestOWL) reasoner;
							OWLStatement st = dqr.getStatement();
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
								"This feature can only be used in conjunction with an UCQ\nenabled reasoner. " +
								"Please, select a UCQ enabled reasoner and try again.");
					}
				}
			};
			thread.start();
		}

		@Override
		public void actionCanceled() {
			try {
				if (statement != null) {
					statement.close();
				}
				latch.countDown();
			} catch (Exception e) {
				latch.countDown();
				log.error("Error while counting.", e);
				DialogUtils.showQuickErrorDialog(null, e, "Error while counting.");
			}
		}
	}

	@Override
	public void activeOntologyChanged() {
		queryEditorPanel.setOBDAModel(this.obdaController.getActiveOBDAModel());
	}
}
