package inf.unibz.it.obda.protege4.gui.view.query;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.inference.reasoner.DataQueryReasoner;
import inf.unibz.it.obda.gui.swing.OBDADataQueryAction;
import inf.unibz.it.obda.gui.swing.OBDASaveQueryResultToFileAction;
import inf.unibz.it.obda.gui.swing.dataquery.panel.QueryInterfacePanel;
import inf.unibz.it.obda.gui.swing.dataquery.panel.ResultViewTablePanel;
import inf.unibz.it.obda.gui.swing.dataquery.panel.SavedQueriesPanelListener;
import inf.unibz.it.obda.gui.swing.queryhistory.QueryhistoryController;
import inf.unibz.it.obda.gui.swing.utils.TextMessageFrame;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;
import inf.unibz.it.obda.queryanswering.QueryResultSet;
import inf.unibz.it.obda.queryanswering.Statement;
import inf.unibz.it.ucq.swing.IncrementalQueryResultSetTableModel;
import inf.unibz.it.utils.io.ResultSetToFileWriter;
import inf.unibz.it.utils.swing.OBDAProgessMonitor;
import inf.unibz.it.utils.swing.OBDAProgressListener;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeListener;

public class QueryInterfaceViewComponent extends AbstractOWLViewComponent implements SavedQueriesPanelListener{
	/**
	 *
	 */
	private static final long			serialVersionUID		= 1L;
	private static final Logger			log						= Logger.getLogger(QueryInterfaceViewComponent.class);

	QueryInterfacePanel					panel_query_interface	= null;

	ResultViewTablePanel				panel_view_results		= null;

	private OWLOntologyChangeListener	ontochange_listener		= null;
	OBDAPluginController				obdaController			= null;
	

	@Override
	protected void disposeOWLView() {
		this.getOWLModelManager().removeOntologyChangeListener(ontochange_listener);

		QueryInterfaceViewsList queryInterfaceViews = (QueryInterfaceViewsList) this.getOWLEditorKit().get(
				QueryInterfaceViewsList.class.getName());
		if ((queryInterfaceViews != null)) {
			queryInterfaceViews.remove(this);
		}

		QueryManagerViewsList queryManagerViews = (QueryManagerViewsList) this.getOWLEditorKit().get(QueryManagerViewsList.class.getName());
		if ((queryManagerViews != null) && (!queryManagerViews.isEmpty())) {
			for (QueryManagerViewComponent queryInterfaceView : queryManagerViews) {
				queryInterfaceView.removeListener(this);
			}
		}
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		obdaController = (OBDAPluginController) getOWLEditorKit().get(APIController.class.getName());
		setLayout(new BorderLayout());

		JPanel panel_right_main = new JPanel();
		JSplitPane split_right_horizontal = new javax.swing.JSplitPane();
		panel_query_interface = new QueryInterfacePanel(obdaController, this.getOWLModelManager().getActiveOntology().getURI());

		// getOWLWorkspace().getEditorKit()
		panel_view_results = new inf.unibz.it.obda.gui.swing.dataquery.panel.ResultViewTablePanel(panel_query_interface);

		ontochange_listener = new OWLOntologyChangeListener() {

			public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
				panel_view_results.setTableModel(new DefaultTableModel());

			}

		};

		this.getOWLModelManager().addOntologyChangeListener(ontochange_listener);

		// QueryController.getInstance().addListener(panel_query_interface);
		
		panel_view_results.setCountAllTuplesActionForUCQ(new OBDADataQueryAction(){

//			@Override
			public long getExecutionTime() {
				return 0;
			}

//			@Override
			public int getNumberOfRows() {
				return 0;
			}

//			@Override
			public void run(String query, QueryInterfacePanel panel) {
				
				try {
					OBDAProgessMonitor monitor = new OBDAProgessMonitor();
					CountDownLatch latch = new CountDownLatch(1);
					CountAllTuplesAction action = new CountAllTuplesAction(latch, query);
					monitor.addProgressListener(action);
					monitor.start();
					action.run();
					latch.await();
					monitor.stop();
					int result = action.getResult();
					if(result != -1){
						panel.updateStatus(result);
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error while counting tuples.\n Please refer to the log file for more information.");
					log.error("Error while counting tuples.",e);
				}
			}

		});

		

		panel_query_interface.setExecuteUCQAction(new OBDADataQueryAction() {

			private long time =0;
			private int rows =0;

			public void run(String query, QueryInterfacePanel panel) {
				
				try {
					OBDAProgessMonitor monitor = new OBDAProgessMonitor();
					CountDownLatch latch = new CountDownLatch(1);
					ExecuteQueryAction action = new ExecuteQueryAction(latch, query);
					monitor.addProgressListener(action);
					monitor.start();
					long startTime = System.currentTimeMillis();
					action.run();
					latch.await();
					monitor.stop();
					QueryResultSet result = action.getResult();
					if(result !=null){
						IncrementalQueryResultSetTableModel model = new IncrementalQueryResultSetTableModel(result);
						model.addTableModelListener(panel);
						rows = model.getRowCount();
						panel_view_results.setTableModel(model);
					}
					long end = System.currentTimeMillis();
					time = end - startTime;
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error while executing query.\n Please refer to the log file for more information.");
					log.error("Error while executing query.",e);
				}

			}

			public long getExecutionTime() {
				return time;
			}

			public int getNumberOfRows() {
				return rows;
			}

		});

		

		

		panel_query_interface.setRetrieveUCQExpansionAction(new OBDADataQueryAction() {

			private long time =0;
			private final int rows =0;

			public void run(String query,QueryInterfacePanel pane) {
				
				try {
					OBDAProgessMonitor monitor = new OBDAProgessMonitor();
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
					if(result != null){
						TextMessageFrame panel = new TextMessageFrame();
						JFrame protegeFrame = ProtegeManager.getInstance().getFrame(getWorkspace());
						panel.setLocation((protegeFrame.getLocation().x + protegeFrame.getSize().width) / 2 - 400, (protegeFrame
										.getLocation().y + protegeFrame.getSize().height) / 2 - 300);
						panel.displaySQL(result);
						panel.setTitle("Query Unfolding");
						double aux = time;
						aux = aux/1000;
						String msg = "Total unfolding time: " + String.valueOf(aux) + " sec";
						panel.updateStatus(msg);
						panel.setVisible(true);
					}
				} catch (InterruptedException e) {
					JOptionPane.showMessageDialog(null, "Error while expanding query.\n Please refer to the log file for more information.");
					log.error("Error while expanding query.",e);
				}
			}

			public long getExecutionTime() {
				return time;
			}

			public int getNumberOfRows() {
				return rows;
			}

		});

		panel_query_interface.setRetrieveUCQUnfoldingAction(new OBDADataQueryAction() {


			private long time =0;
			private final int rows =0;

			public void run(String query, QueryInterfacePanel pane) {
				
				try {
					OBDAProgessMonitor monitor = new OBDAProgessMonitor();
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
					if(result != null){
						TextMessageFrame panel = new TextMessageFrame();
						JFrame protegeFrame = ProtegeManager.getInstance().getFrame(getWorkspace());
						panel.setLocation((protegeFrame.getLocation().x + protegeFrame.getSize().width) / 2 - 400, (protegeFrame
										.getLocation().y + protegeFrame.getSize().height) / 2 - 300);
						panel.displaySQL(result);
						panel.setTitle("Query Unfolding");
						double aux = time;
						aux = aux/1000;
						String msg = "Total unfolding time: " + String.valueOf(aux) + " sec";
						panel.updateStatus(msg);
						panel.setVisible(true);
					}
				} catch (InterruptedException e) {
					JOptionPane.showMessageDialog(null, "Error while expanding query.\n Please refer to the log file for more information.");
					log.error("Error while unfolding query.",e);
				}

			}

			public long getExecutionTime() {
				return time;
			}

			public int getNumberOfRows() {
				return rows;
			}

		});

		panel_view_results.setOBDASaveQueryToFileAction(new OBDASaveQueryResultToFileAction() {
			
			@Override
			public void run(String query, File file) {
				
				try {
					OBDAProgessMonitor monitor = new OBDAProgessMonitor();
					CountDownLatch latch = new CountDownLatch(1);
					ExecuteQueryAction action = new ExecuteQueryAction(latch, query);
					monitor.addProgressListener(action);
					monitor.start();
					action.run();
					latch.await();
					monitor.stop();
					QueryResultSet result = action.getResult();
					if(result !=null){
						ResultSetToFileWriter.saveResultSet(result, file);	
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error while saving query results.\n Please refer to the log file for more information.");
					log.error("Error while saving query results.",e);
				}
			}
		});
		
		panel_right_main.setLayout(new java.awt.BorderLayout());

		split_right_horizontal.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		split_right_horizontal.setResizeWeight(0.25);
		split_right_horizontal.setOneTouchExpandable(true);
		split_right_horizontal.setTopComponent(panel_query_interface);
		panel_view_results.setMinimumSize(new java.awt.Dimension(400, 250));
		panel_view_results.setPreferredSize(new java.awt.Dimension(400, 250));
		split_right_horizontal.setBottomComponent(panel_view_results);

		panel_right_main.add(split_right_horizontal, java.awt.BorderLayout.CENTER);

		add(panel_right_main, BorderLayout.CENTER);
		setupListeners();

		log.info("Query Manager view initialized");

		// getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();

	}

	public void selectedQuerychanged(String new_group,String new_query,String new_id) {
		this.panel_query_interface.selectedQuerychanged(new_group,new_query,new_id);
	}

	public void setupListeners() {

		QueryInterfaceViewsList queryInterfaceViews = (QueryInterfaceViewsList) this.getOWLEditorKit().get(
				QueryInterfaceViewsList.class.getName());
		if ((queryInterfaceViews == null)) {
			queryInterfaceViews = new QueryInterfaceViewsList();
			getOWLEditorKit().put(QueryInterfaceViewsList.class.getName(), queryInterfaceViews);
		}
		queryInterfaceViews.add(this);

		QueryManagerViewsList queryManagerViews = (QueryManagerViewsList) this.getOWLEditorKit().get(QueryManagerViewsList.class.getName());
		if ((queryManagerViews != null) && (!queryManagerViews.isEmpty())) {
			for (QueryManagerViewComponent queryInterfaceView : queryManagerViews) {
				queryInterfaceView.addListener(this);
			}
		}
	}
	
	private class UnfoldQueryAction implements OBDAProgressListener{
		private Statement statement = null;
		private CountDownLatch latch = null;
		private Thread thread = null;
		private String result = null;
		private String query = null;
		
		private UnfoldQueryAction(CountDownLatch latch,String query){
			this.latch = latch;
			this.query = query;
		}

		public String getResult(){
			return result;
		}
		
		public void run(){
			thread = new Thread(){
				public void run(){
					OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
					if(reasoner instanceof DataQueryReasoner){
		
						try {
							DataQueryReasoner dqr = (DataQueryReasoner) reasoner;
							Statement st =  dqr.getStatement(query);
							result = st.getUnfolding();
							latch.countDown();
						} catch (Exception e) {
							latch.countDown();
							log.error(e.getMessage(), e);
							JOptionPane
							.showMessageDialog(null,
									"Error while unfolding query.\n Please refer to the log for more information.");
						}
		
					}else {
						latch.countDown();
						JOptionPane
								.showMessageDialog(null,
										"This feature can only be used in conjunction with an UCQ\nenabled reasoner. Please, select a UCQ enabled reasoner and try again.");
					}
				}
			};
			thread.start();
		}
		
		@Override
		public void actionCanceled() {
			try {
				if(statement != null){
					statement.close();
				}
				latch.countDown();
			} catch (Exception e) {
				latch.countDown();
				JOptionPane.showMessageDialog(null, "Error while canceling unfolding action.\n Please refer to the log file for more information.");
				log.error("Error while canceling unfolding action.",e);
			}
		}
	}
	
	private class ExpandQueryAction implements OBDAProgressListener{
	
		private Statement statement = null;
		private CountDownLatch latch = null;
		private Thread thread = null;
		private String result = null;
		private String query = null;
		
		private ExpandQueryAction(CountDownLatch latch,String query){
			this.latch = latch;
			this.query = query;
		}

		public String getResult(){
			return result;
		}
		
		public void run(){
			thread = new Thread(){
				public void run(){
					OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
					if(reasoner instanceof DataQueryReasoner){
		
						try {
							DataQueryReasoner dqr = (DataQueryReasoner) reasoner;
							Statement st =  dqr.getStatement(query);
							result = st.getRewriting();
							latch.countDown();
						} catch (Exception e) {
							latch.countDown();
							log.error(e.getMessage(), e);
							JOptionPane
							.showMessageDialog(null,
									"Error while expanding query.\n Please refer to the log for more information.");
						}
		
					}else {
						latch.countDown();
						JOptionPane
								.showMessageDialog(null,
										"This feature can only be used in conjunction with an UCQ\nenabled reasoner. Please, select a UCQ enabled reasoner and try again.");
					}
				}
			};
			thread.start();
		}
		
		@Override
		public void actionCanceled() {
			try {
				if(statement != null){
					statement.close();
				}
				latch.countDown();
			} catch (Exception e) {
				latch.countDown();
				JOptionPane.showMessageDialog(null, "Error while counting.\n Please refer to the log file for more information.");
				log.error("Error while counting.",e);
			}
		}
	}
	
	private class ExecuteQueryAction implements OBDAProgressListener{

		private Statement statement = null;
		private CountDownLatch latch = null;
		private Thread thread = null;
		private QueryResultSet result = null;
		private String query = null;
		
		private ExecuteQueryAction(CountDownLatch latch,String query){
			this.latch = latch;
			this.query = query;
		}

		public QueryResultSet getResult(){
			return result;
		}
		
		public void run(){
			thread = new Thread(){
				public void run(){
					OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
					if(reasoner instanceof DataQueryReasoner){
		
						try {
							DataQueryReasoner dqr = (DataQueryReasoner) reasoner;
							Statement st =  dqr.getStatement(query);
							result = st.getResultSet();
							latch.countDown();
						} catch (Exception e) {
							latch.countDown();
							log.error(e.getMessage(), e);
							JOptionPane
							.showMessageDialog(null,
									"Error while executing query.\n Please refer to the log for more information.");
						}
		
					}else {
						latch.countDown();
						JOptionPane
								.showMessageDialog(null,
										"This feature can only be used in conjunction with an UCQ\nenabled reasoner. Please, select a UCQ enabled reasoner and try again.");
					}
				}
			};
			thread.start();
		}
		
		@Override
		public void actionCanceled() {
			try {
				if(statement != null){
					statement.close();
				}
				latch.countDown();
			} catch (Exception e) {
				latch.countDown();
				JOptionPane.showMessageDialog(null, "Error while counting.\n Please refer to the log file for more information.");
				log.error("Error while counting.",e);
			}
		}
		
	}
	
	private class CountAllTuplesAction implements OBDAProgressListener{
		
		private Statement statement = null;
		private CountDownLatch latch = null;
		private Thread thread = null;
		private int result = -1;
		private String query = null;
		
		private CountAllTuplesAction(CountDownLatch latch,String query){
			this.latch = latch;
			this.query = query;
		}

		public int getResult(){
			return result;
		}
		
		public void run(){
			thread = new Thread(){
				public void run(){
					OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
					if(reasoner instanceof DataQueryReasoner){
		
						try {
							DataQueryReasoner dqr = (DataQueryReasoner) reasoner;
							Statement st =  dqr.getStatement(query);
							result = st.getTupleCount();
							latch.countDown();
						} catch (Exception e) {
							latch.countDown();
							log.error(e.getMessage(), e);
							JOptionPane
							.showMessageDialog(null,
									"Error while counting tuples.\n Please refer to the log for more information.");
						}
		
					}else {
						latch.countDown();
						JOptionPane
								.showMessageDialog(null,
										"This feature can only be used in conjunction with an UCQ\nenabled reasoner. Please, select a UCQ enabled reasoner and try again.");
					}
				}
			};
			thread.start();
		}
		
		@Override
		public void actionCanceled() {
			try {
				if(statement != null){
					statement.close();
				}
				latch.countDown();
			} catch (Exception e) {
				latch.countDown();
				JOptionPane.showMessageDialog(null, "Error while counting.\n Please refer to the log file for more information.");
				log.error("Error while counting.",e);
			}
		}
	}
}
