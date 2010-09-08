package inf.unibz.it.obda.protege4.gui.view.query;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.datasource.JDBCConnectionManager;
import inf.unibz.it.obda.api.inference.reasoner.UCQReasoner;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.action.OBDADataQueryAction;
import inf.unibz.it.obda.gui.swing.dataquery.panel.QueryInterfacePanel;
import inf.unibz.it.obda.gui.swing.dataquery.panel.ResultViewTablePanel;
import inf.unibz.it.obda.gui.swing.dataquery.panel.SavedQueriesPanelListener;
import inf.unibz.it.obda.gui.swing.queryhistory.QueryhistoryController;
import inf.unibz.it.obda.gui.swing.utils.TextMessageFrame;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;
import inf.unibz.it.obda.protege4.gui.action.query.P4GetDefaultSPARQLPrefixAction;
import inf.unibz.it.ucq.domain.QueryResult;
import inf.unibz.it.ucq.domain.UnionOfConjunctiveQueries;
import inf.unibz.it.ucq.parser.sparql.UCQTranslator;
import inf.unibz.it.ucq.renderer.UCQDatalogStringRenderer;
import inf.unibz.it.ucq.swing.IncrementalQueryResultTableModel;
import inf.unibz.it.utils.swing.TextMessageDialog;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.util.List;

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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;

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
		panel_query_interface = new QueryInterfacePanel(obdaController.getQueryController());

		// getOWLWorkspace().getEditorKit()
		panel_query_interface.setGetSPARQLDefaultPrefixAction(new P4GetDefaultSPARQLPrefixAction(getOWLModelManager()));
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
				OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
				if (reasoner instanceof UCQReasoner) {
					try {
						P4GetDefaultSPARQLPrefixAction prefixAction = new P4GetDefaultSPARQLPrefixAction(getOWLEditorKit()
								.getModelManager());
						prefixAction.run();
						String prefix = (String) prefixAction.getResult();
						Query sparqlQuery = QueryFactory.create(prefix + "\n" + query);
						UCQTranslator translator = new UCQTranslator();
						UnionOfConjunctiveQueries ucq = translator.getUCQ(obdaController, sparqlQuery);
						String result = ((UCQReasoner) reasoner).getUnfolding(ucq);
						String newsql = "SELECT count(*) FROM (" +result+") t1";
						JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();
						DataSource ds = obdaController.getDatasourcesController().getCurrentDataSource();
						if(ds == null){
							JOptionPane.showMessageDialog(null, "Error: \n No data source selected");
						}else{
							ResultSet set = man.executeQuery(ds, newsql);
							if(set.next()){
								int o =Integer.parseInt(set.getObject(1).toString());
								panel.updateStatus(o);
							}
							set.getStatement().close();
							set.close();
						}
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, "Error: \n" + e.getMessage());
						e.printStackTrace();
					}
				} else {
					JOptionPane
							.showMessageDialog(null,
									"This feature can only be used in conjunction with an UCQ\nenabled reasoner. Please, select a UCQ enabled reasoner and try again.");
				}
			}
			
		});
		
		panel_view_results.setCountAllTuplesActionForEQL(new OBDADataQueryAction(){

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

				OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
				if (reasoner instanceof UCQReasoner) {
					
					try {
						String sql = ((UCQReasoner) reasoner).unfoldEQL(query);
						String newsql = "SELECT count(*) FROM (" +sql+") t1";
						JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();
						DataSource ds = obdaController.getDatasourcesController().getCurrentDataSource();
						if(ds == null){
							JOptionPane.showMessageDialog(null, "Error: \n No data source selected");
						}else{
							ResultSet set = man.executeQuery(ds, newsql);
							if(set.next()){
								int o =Integer.parseInt(set.getObject(1).toString());
								panel.updateStatus(o);
							}
							set.getStatement().close();
							set.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "Error while unfolding the query. Reasoner's message:\n" + e.getMessage());
					}
				} else {
					JOptionPane
					.showMessageDialog(null,
							"This feature can only be used in conjunction with an UCQ\nenabled reasoner. Please, select a UCQ enabled reasoner and try again.");
				}
			}
			
		});
		
		panel_query_interface.setExecuteUCQAction(new OBDADataQueryAction() {
			
			private long time =0;
			private int rows =0;
			
			public void run(String query, QueryInterfacePanel panel) {
				OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
				if (reasoner instanceof UCQReasoner) {
					try {
						long startTime = System.currentTimeMillis();
						QueryhistoryController.getInstance().addQuery(query);
						P4GetDefaultSPARQLPrefixAction prefixAction = new P4GetDefaultSPARQLPrefixAction(getOWLEditorKit()
								.getModelManager());
						prefixAction.run();
						String prefix = (String) prefixAction.getResult();
						Query sparqlQuery = QueryFactory.create(prefix + "\n" + query);
						UCQTranslator translator = new UCQTranslator();
						UnionOfConjunctiveQueries ucq = translator.getUCQ(obdaController, sparqlQuery);
						QueryResult result = ((UCQReasoner) reasoner).answerUCQ(ucq);
						IncrementalQueryResultTableModel model = new IncrementalQueryResultTableModel(result);
						model.addTableModelListener(panel);
						rows = model.getRowCount();
//						JOptionPane.showMessageDialog(null, "Number of tuples retrieved: " + rows);
						panel_view_results.setTableModel(model);
						long end = System.currentTimeMillis();
						time = end - startTime;
					} catch (QueryParseException e) {
						JOptionPane.showMessageDialog(null, "Syntax error in the SPARQL query. Parser's message:\n" + e.getMessage());
						// e.printStackTrace(System.err);
					} catch (Exception e) {
						TextMessageDialog dialog = new TextMessageDialog(null, true);
						StringWriter st = new StringWriter();
						e.printStackTrace(new PrintWriter(st));
						dialog.setText(st.toString());
						dialog.setVisible(true);
					}
				} else {
					JOptionPane
							.showMessageDialog(null,
									"This feature can only be used in conjunction with an UCQ\nenabled reasoner. Please, select a UCQ enabled reasoner and try again.");
				}
			}

			public long getExecutionTime() {
				return time;
			}

			public int getNumberOfRows() {
				return rows;
			}

		});
		
		panel_query_interface.setRetrieveEQLUnfoldingAction(new OBDADataQueryAction() {
			
			private long time = 0;
			
			public long getExecutionTime() {
				return time;
			}

			public int getNumberOfRows() {
				return 0;
			}

			public void run(String query, QueryInterfacePanel pane) {
				OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
				if (reasoner instanceof UCQReasoner) {
					
					try {
						long startTime = System.currentTimeMillis();
						String sql = ((UCQReasoner) reasoner).unfoldEQL(query);
						
//						JTextArea textArea = new JTextArea("\n\nQuery expansion:\n\n" + sql);
//						textArea.setFont(new Font("Helvetica", Font.PLAIN, 12));
//						JScrollPane scroll = new JScrollPane(textArea);
//
//						JFrame protegeFrame = ProtegeManager.getInstance().getFrame(getWorkspace());
//						final JDialog dialog = new JDialog(protegeFrame);
//						dialog.setLocation((protegeFrame.getLocation().x + protegeFrame.getSize().width) / 2 - 400, (protegeFrame
//								.getLocation().y + protegeFrame.getSize().height) / 2 - 300);
//						dialog.setTitle("Query Expansion");
//						dialog.getContentPane().setLayout(new BorderLayout());
//						dialog.getContentPane().add(scroll, BorderLayout.CENTER);
//						JButton closeButton = new JButton("Close");
//						closeButton.addActionListener(new ActionListener() {
//							public void actionPerformed(ActionEvent e) {
//								dialog.dispose();
//							}
//						});
//
//						JPanel south = new JPanel();
//						south.add(closeButton);
//						dialog.getContentPane().add(south, BorderLayout.SOUTH);
//						dialog.setSize(800, 600);
//
//						dialog.setVisible(true);
//						closeButton.requestFocus();
						
						long end = System.currentTimeMillis();
						time = end - startTime;
						TextMessageFrame panel = new TextMessageFrame();
						JFrame protegeFrame = ProtegeManager.getInstance().getFrame(getWorkspace());
						panel.setLocation((protegeFrame.getLocation().x + protegeFrame.getSize().width) / 2 - 400, (protegeFrame
								.getLocation().y + protegeFrame.getSize().height) / 2 - 300);
						panel.displaySQL(sql);
						panel.setTitle("Query Unfolding");
						double aux = time;
						aux = aux/1000;
						String msg = "Total unfolding time: " + String.valueOf(aux) + " sec";
						panel.updateStatus(msg);
						panel.setVisible(true);
						
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "Error while unfolding the query. Reasoner's message:\n" + e.getMessage());
					}
					
				} else {
					JOptionPane
					.showMessageDialog(null,
							"This feature can only be used in conjunction with an UCQ\nenabled reasoner. Please, select a UCQ enabled reasoner and try again.");
		}
			}
		});
		
		panel_query_interface.setExecuteEQLAction(new OBDADataQueryAction() {
			
			private long time =0;
			private int rows =0;
			
			public void run(String query, QueryInterfacePanel panel) {
				OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
				if (reasoner instanceof UCQReasoner) {
					try {
						long startTime = System.currentTimeMillis();
						QueryResult result = ((UCQReasoner) reasoner).answerEQL(query);
						IncrementalQueryResultTableModel model = new IncrementalQueryResultTableModel(result);
						model.addTableModelListener(panel);
//						JOptionPane.showMessageDialog(null, "Tuples retrieved: " + model.getRowCount());
						panel_view_results.setTableModel(model);
						long end = System.currentTimeMillis();
						time = end - startTime;
						rows = model.getRowCount();
					} catch (QueryParseException e) {
						JOptionPane.showMessageDialog(null, "Syntax error in the SPARSQL query. Parser's message:\n" + e.getMessage());
						// e.printStackTrace(System.err);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, "Error while executing the query. Reasoner's message:\n" + e.getMessage());
						e.printStackTrace(System.err);
					}
				} else {
					JOptionPane
							.showMessageDialog(null,
									"This feature can only be used in conjunction with an UCQ\nenabled reasoner. Please, select a UCQ enabled reasoner and try again.");
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
			private int rows =0;
			
			public void run(String query,QueryInterfacePanel pane) {
				OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
				if (reasoner instanceof UCQReasoner) {
					try {
						long startTime = System.currentTimeMillis();
						P4GetDefaultSPARQLPrefixAction prefixAction = new P4GetDefaultSPARQLPrefixAction(getOWLEditorKit()
								.getModelManager());
						prefixAction.run();
						String prefix = (String) prefixAction.getResult();
						Query sparqlQuery = QueryFactory.create(prefix + "\n" + query);
						UCQTranslator translator = new UCQTranslator();
						UnionOfConjunctiveQueries ucq = translator.getUCQ(obdaController, sparqlQuery);
						UnionOfConjunctiveQueries result = ((UCQReasoner) reasoner).getRewritting(ucq);
						String s = "";
						if(result != null){
							UCQDatalogStringRenderer ren = new UCQDatalogStringRenderer(obdaController);
							s = ren.encode(result);
						}
//						JTextArea textArea = new JTextArea("\n\nQuery expansion:\n\n" + result);
//						textArea.setFont(new Font("Helvetica", Font.PLAIN, 12));
//						JScrollPane scroll = new JScrollPane(textArea);
//
//						JFrame protegeFrame = ProtegeManager.getInstance().getFrame(getWorkspace());
//						final JDialog dialog = new JDialog(protegeFrame);
//						dialog.setLocation((protegeFrame.getLocation().x + protegeFrame.getSize().width) / 2 - 400, (protegeFrame
//								.getLocation().y + protegeFrame.getSize().height) / 2 - 300);
//						dialog.setTitle("Query Expansion");
//						dialog.getContentPane().setLayout(new BorderLayout());
//						dialog.getContentPane().add(scroll, BorderLayout.CENTER);
//						JButton closeButton = new JButton("Close");
//						closeButton.addActionListener(new ActionListener() {
//							public void actionPerformed(ActionEvent e) {
//								dialog.dispose();
//							}
//						});
//
//						JPanel south = new JPanel();
//						south.add(closeButton);
//						dialog.getContentPane().add(south, BorderLayout.SOUTH);
//						dialog.setSize(800, 600);
//
//						dialog.setVisible(true);
//						closeButton.requestFocus();
						
						long end = System.currentTimeMillis();
						time = end - startTime;
						TextMessageFrame panel = new TextMessageFrame();
						JFrame protegeFrame = ProtegeManager.getInstance().getFrame(getWorkspace());
						panel.setLocation((protegeFrame.getLocation().x + protegeFrame.getSize().width) / 2 - 400, (protegeFrame
								.getLocation().y + protegeFrame.getSize().height) / 2 - 300);
						panel.displaySQL(s);
						panel.setTitle("Query Expansion");
						panel.updateBoderTitel("Query Expansion");
						double aux = time;
						aux = aux/1000;
						String msg = "Total expansion time: " + String.valueOf(aux) + " sec";
						panel.updateStatus(msg);
						panel.setVisible(true);
						
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, "Error: \n" + e.getMessage());
					}
				} else {
					JOptionPane
							.showMessageDialog(null,
									"This feature can only be used in conjunction with an UCQ\nenabled reasoner. Please, select a UCQ enabled reasoner and try again.");
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
			private int rows =0;
			
			public void run(String query, QueryInterfacePanel pane) {
				OWLReasoner reasoner = getOWLEditorKit().getModelManager().getOWLReasonerManager().getCurrentReasoner();
				if (reasoner instanceof UCQReasoner) {
					try {
						long startTime = System.currentTimeMillis();
						P4GetDefaultSPARQLPrefixAction prefixAction = new P4GetDefaultSPARQLPrefixAction(getOWLEditorKit()
								.getModelManager());
						prefixAction.run();
						String prefix = (String) prefixAction.getResult();
						Query sparqlQuery = QueryFactory.create(prefix + "\n" + query);
						UCQTranslator translator = new UCQTranslator();
						UnionOfConjunctiveQueries ucq = translator.getUCQ(obdaController, sparqlQuery);
						String result = ((UCQReasoner) reasoner).getUnfolding(ucq);

//						JTextArea textArea = new JTextArea("\n\nSQL Unfolding:\n\n" + result);
//						textArea.setFont(new Font("Helvetica", Font.PLAIN, 12));
//						JScrollPane scroll = new JScrollPane(textArea);
//
//						JFrame protegeFrame = ProtegeManager.getInstance().getFrame(getWorkspace());
//						final JDialog dialog = new JDialog(protegeFrame);
//						dialog.setLocation((protegeFrame.getLocation().x + protegeFrame.getSize().width) / 2 - 400, (protegeFrame
//								.getLocation().y + protegeFrame.getSize().height) / 2 - 300);
//						dialog.setTitle("Query Unfolding");
//						dialog.getContentPane().setLayout(new BorderLayout());
//						dialog.getContentPane().add(scroll, BorderLayout.CENTER);
//						JButton closeButton = new JButton("Close");
//						closeButton.addActionListener(new ActionListener() {
//							public void actionPerformed(ActionEvent e) {
//								dialog.dispose();
//							}
//						});
//
//						JPanel south = new JPanel();
//						south.add(closeButton);
//						dialog.getContentPane().add(south, BorderLayout.SOUTH);
//						dialog.setSize(800, 600);
//
//						dialog.setVisible(true);
//						closeButton.requestFocus();
						long end = System.currentTimeMillis();
						time = end - startTime;
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
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, "Error: \n" + e.getMessage());
					}
				} else {
					JOptionPane
							.showMessageDialog(null,
									"This feature can only be used in conjunction with an UCQ\nenabled reasoner. Please, select a UCQ enabled reasoner and try again.");
				}
			}

			public long getExecutionTime() {
				return time;
			}

			public int getNumberOfRows() {
				return rows;
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
	
	public class TextDialog extends JDialog {
		
		JTextArea textArea = new JTextArea("");
		
		public TextDialog() {
			
			textArea.setFont(new Font("Helvetica", Font.PLAIN, 12));
			JScrollPane scroll = new JScrollPane(textArea);

			JFrame protegeFrame = ProtegeManager.getInstance().getFrame(getWorkspace());
			
			setLocation((protegeFrame.getLocation().x + protegeFrame.getSize().width) / 2 - 400, (protegeFrame
					.getLocation().y + protegeFrame.getSize().height) / 2 - 300);
			getContentPane().setLayout(new GridBagLayout());
			GridBagConstraints c1 = new GridBagConstraints();
			c1.anchor = GridBagConstraints.CENTER;
			c1.weightx = 1;
			c1.weighty = 1;
			c1.fill = GridBagConstraints.BOTH;
			
			getContentPane().add(scroll, c1);
			
			c1.anchor = GridBagConstraints.CENTER;
			c1.weightx = 1;
			c1.weighty = 1;
			c1.fill = GridBagConstraints.HORIZONTAL;
			
			
			JButton closeButton = new JButton("Close");
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});

			JPanel south = new JPanel();
			south.add(closeButton);
			
			
			getContentPane().add(south, c1);
			setSize(800, 600);
			
		}
		
		public void setContent(String content) {
			textArea.setText(content);
		}
	}
}
