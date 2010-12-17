/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * InclusionDependencyTreePane.java
 *
 * Created on Aug 12, 2009, 10:09:21 AM
 */
package inf.unibz.it.obda.gui.swing.dependencies.panel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.dependencies.controller.RDBMSInclusionDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSInclusionDependency;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.datasource.panels.ResultSetTableModel;
import inf.unibz.it.obda.gui.swing.dependencies.treemodel.DependencyAssertionTreeCellRenderer;
import inf.unibz.it.obda.gui.swing.dependencies.treemodel.DependencyTreeCellEditor;
import inf.unibz.it.obda.gui.swing.dependencies.treemodel.InclusionDependencyTreeModel;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferenceChangeListener;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferences;
import inf.unibz.it.obda.gui.swing.treemodel.DefaultAssertionTreeNode;
import inf.unibz.it.obda.gui.swing.treemodel.DefaultAssertionTreeNodeRenderer;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.obda.rdbmsgav.validator.SQLQueryValidator;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.obda.query.domain.Term;

/**
 * The tree pane showing all inclusion dependencies associated the selected data source
 *
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
 */
public class InclusionDependencyTreePane extends JPanel implements MappingManagerPreferenceChangeListener{

	/**
	 *
	 */
	private static final long serialVersionUID = 5849742849711210687L;
	/**
	 * the API controller
	 */
	private APIController apic = null;
	/**
	 * the RDBMS inclusion dependency controller
	 */
	private RDBMSInclusionDependencyController incController = null;

	private boolean canceled = false;

	private SQLQueryValidator v = null;

	private	Thread	validatorThread	= null;

	private static InclusionDependencyTreePane instance = null;

	private MappingManagerPreferences pref = null;

    /** Creates new form InclusionDependencyTreePane */
    public InclusionDependencyTreePane(APIController apic) {
    	instance = this;
    	this.apic = apic;
    	incController = (RDBMSInclusionDependencyController) apic.getController(RDBMSInclusionDependency.class);
    	pref = OBDAPreferences.getOBDAPreferences().getMappingsPreference();
        initComponents();
        addMenu();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Inclusion Dependencies");
        DefaultAssertionTreeNodeRenderer renderer = new DefaultAssertionTreeNodeRenderer();
        InclusionDependencyTreeModel model = new InclusionDependencyTreeModel(root, incController, renderer);
        DependencyAssertionTreeCellRenderer tcr = new DependencyAssertionTreeCellRenderer(apic);
        jTree1.setCellRenderer(tcr);
        jTree1.setModel(model);
        jTree1.setEditable(true);
        jTree1.setCellEditor(new DependencyTreeCellEditor(apic, RDBMSInclusionDependency.INCLUSIONDEPENDENCY));
        jTree1.setInvokesStopCellEditing(true);
        jTree1.setRootVisible(false);
        jTree1.setRowHeight(0);
        pref.registerPreferenceChangedListener(this);
//        incController.addControllerListener(model);

        jButton1.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				TreePath[] selection =jTree1.getSelectionPaths();
				if(selection != null){
					delete(selection);
				}
			}

        });

        jButtonAdd.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				addRDBMSInclusionDependency();
			}

        });

        jButtonWizard.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				TreePath[] paths = Dependency_SelectMappingPane.gestInstance().getSelection();
				Dependency_SelectMappingPane.gestInstance().createDialog("Create Inclusion Dependency", paths, RDBMSInclusionDependency.INCLUSIONDEPENDENCY);
			}

        });

        jButtonMine.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				Dependency_SelectMappingPane.gestInstance().showInclusionMiningDialog(jTree1);
			}

        });
    }

    private void addRDBMSInclusionDependency(){
		DatasourcesController dscon = apic.getDatasourcesController();
		if(dscon.getCurrentDataSource() == null){
			JOptionPane.showMessageDialog(null, "Please select a data source.", "ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		}
		InclusionDependencyTreeModel model =(InclusionDependencyTreeModel) jTree1.getModel();
		DefaultAssertionTreeNode<RDBMSInclusionDependency> node = new DefaultAssertionTreeNode<RDBMSInclusionDependency>(null);
		MutableTreeNode root = (MutableTreeNode) model.getRoot();
		int index = ((DefaultMutableTreeNode)model.getRoot()).getChildCount();
		root.insert(node, index);
		model.nodesWereInserted(root, new int[]{index});
		model.nodeStructureChanged(root);
		jTree1.setVisibleRowCount(index+1);
//		model.insertNodeInto(node, (MutableTreeNode) model.getRoot(), ((DefaultMutableTreeNode)model.getRoot()).getChildCount());
		TreePath path = jTree1.getPathForRow(index);
		if(path == null){
			root.remove(index);
			model.nodesWereRemoved(root, new int[] {index}, new Object[]{node});
		}
		jTree1.setSelectionPath(path);
		jTree1.startEditingAtPath(path);
    }

    public static InclusionDependencyTreePane getInstance(){
    	return instance;
    }

    public JTree getInclusionDependencyTree(){
    	return jTree1;
    }
    /**
     * adds a popup menu to the tree
     */
    private void addMenu(){
    	JPopupMenu menu = new JPopupMenu();

    	JMenuItem del = new JMenuItem();
    	del.setText("delete");
    	del.setToolTipText("deletes all selected Assertions");
    	del.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				TreePath[] selection =jTree1.getSelectionPaths();
				if(selection != null){
					delete(selection);
				}
			}
    	});

    	menu.add(del);
    	menu.addSeparator();

    	JMenuItem validate = new JMenuItem();
    	validate.setText("Validate Dependency");
    	validate.setToolTipText("Check wehter the produced SQL query is valid.");
    	validate.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				TreePath[] paths = jTree1.getSelectionPaths();
				if (paths != null){
					validateRDBMSInclusionDependencies(paths);
				}
			}

    	});
    	menu.add(validate);
    	jTree1.setComponentPopupMenu(menu);
    }

    /**
     * validates whether the data source fulfills the selected assertions
     * @param paths selected assertions
     */
    private void validateRDBMSInclusionDependencies(TreePath[] paths){

    	final ValidateDepenencyDialog dialog = new ValidateDepenencyDialog(jTree1);
    	Runnable action = new Runnable() {
			public void run() {
				canceled = false;
				final TreePath path[] = jTree1.getSelectionPaths();

				dialog.setVisible(true);

				if (path == null) {
					return;
				}
				dialog.addText("Validating " + path.length + " inclusion dependencies.\n", dialog.NORMAL);
				for (int i = 0; i < path.length; i++) {
					final int index = i;
					boolean error = false;
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path[i].getLastPathComponent();
            		RDBMSInclusionDependency inc = (RDBMSInclusionDependency)node.getUserObject();
        			RDBMSSQLQuery query1 = (RDBMSSQLQuery) inc.getSourceQueryOne();
            		RDBMSSQLQuery query2 = (RDBMSSQLQuery) inc.getSourceQueryTwo();
            		List<Term> terms1 = inc.getTermsOfQueryOne();
            		List<Term> terms2 = inc.getTermsOfQueryTwo();
            		dialog.addText(inc.toString() +"... ", dialog.NORMAL);
            		String aux1 = "";
            		Iterator<Term> it1 = terms1.iterator();
            		while(it1.hasNext()){
            			if(aux1.length() > 0){
            				aux1 = aux1 + ",";
            			}
            			aux1= aux1 + "table1." + it1.next().getName();
            		}
            		String aux2 = "";
            		Iterator<Term> it2 = terms2.iterator();
            		while(it2.hasNext()){
            			if(aux2.length() > 0){
            				aux2 = aux2 + ",";
            			}
            			aux2= aux2 + "table2." + it2.next().getName();
            		}

            		String query = "SELECT " + aux1 +" FROM (" + query1.toString() + ") table1 WHERE ROW("+
        			aux1+") NOT IN (SELECT " + aux2 + " FROM (" + query2.toString() +") table2)";

            		DatasourcesController con = apic.getDatasourcesController();
            		DataSource ds = con.getCurrentDataSource();
            		v = new SQLQueryValidator(ds, new RDBMSSQLQuery(query));
					if (canceled)
						return;
					if(!error){
						if(validateAssertion(v)){
							String output = "VALID \n";
							dialog.addText(output, dialog.VALID);
						}else{
							Exception e = v.getReason();
							String output = "";
							if(e == null){
								output = "INVALID - Reason: Datasource violates assertion\n";;
							}else{
								output = "INVALID - Reason: "+e.getMessage()+"\n";
							}
							dialog.addText(output, dialog.ERROR);
						}
						v.dispose();

						if (canceled)
							return;
					}
				}
			}
		};
		validatorThread = new Thread(action);
		validatorThread.start();

		Thread cancelThread = new Thread(new Runnable() {
			public void run() {
				while (!dialog.closed) {
					try {
						Thread.currentThread().sleep(100);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
				}
				if (validatorThread.isAlive()) {
					try {
						Thread.currentThread().sleep(250);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
					try {
						canceled = true;
						v.cancelValidation();
					} catch (SQLException e) {
						e.printStackTrace();
					}

				}
			}
		});
		cancelThread.start();
    }

    private void delete(TreePath[] selection){
    	for(int i=0; i< selection.length;i++){
			TreePath path = selection[i];
			Object o = path.getLastPathComponent();
			if(o instanceof DefaultAssertionTreeNode){
				DefaultAssertionTreeNode<RDBMSInclusionDependency> node = (DefaultAssertionTreeNode<RDBMSInclusionDependency>)o;
				RDBMSInclusionDependency dep = node.getUserObject();
				incController.removeAssertion(dep);


			}
		}
    	jTree1.setSelectionPath(null);
    }

    private boolean validateAssertion(SQLQueryValidator v){

		ResultSetTableModel model = (ResultSetTableModel) v.execute();
		if (model == null){
			v.getReason().printStackTrace();
			return false;
		}else{
			if(model.getRowCount() == 0){
				return true;
			}else{
				return false;
			}
		}

	}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jButtonAdd = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButtonWizard = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButtonMine = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Current Inclusion Dependencies")));
        setMinimumSize(new java.awt.Dimension(70, 70));
        setLayout(new java.awt.GridBagLayout());

        jButton1.setText("Delete");
        jButton1.setMaximumSize(null);
        jButton1.setMinimumSize(new java.awt.Dimension(95, 23));
        jButton1.setPreferredSize(new java.awt.Dimension(95, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jButton1, gridBagConstraints);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(700, 600));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(700, 600));
        jScrollPane1.setViewportView(jTree1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jScrollPane1, gridBagConstraints);

        jButtonAdd.setText("Add");
        jButtonAdd.setMaximumSize(null);
        jButtonAdd.setMinimumSize(new java.awt.Dimension(95, 23));
        jButtonAdd.setPreferredSize(new java.awt.Dimension(95, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jButtonAdd, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jLabel1, gridBagConstraints);

        jButtonWizard.setText("Add Wizard...");
        jButtonWizard.setMaximumSize(null);
        jButtonWizard.setMinimumSize(new java.awt.Dimension(95, 23));
        jButtonWizard.setPreferredSize(new java.awt.Dimension(95, 23));
        jButtonWizard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWizardActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jButtonWizard, gridBagConstraints);

        jButton2.setText("Validate");
        jButton2.setToolTipText("validates wether the selected dependencies are satisfied by the database");
        jButton2.setMaximumSize(new java.awt.Dimension(95, 23));
        jButton2.setMinimumSize(new java.awt.Dimension(95, 23));
        jButton2.setPreferredSize(new java.awt.Dimension(95, 23));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jButton2, gridBagConstraints);

        jButtonMine.setText("Mine");
        jButtonMine.setMaximumSize(new java.awt.Dimension(95, 23));
        jButtonMine.setMinimumSize(new java.awt.Dimension(95, 23));
        jButtonMine.setPreferredSize(new java.awt.Dimension(95, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jButtonMine, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonWizardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWizardActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonWizardActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        TreePath[] paths = jTree1.getSelectionPaths();
		if (paths != null){
			validateRDBMSInclusionDependencies(paths);
		}
    }//GEN-LAST:event_jButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonMine;
    private javax.swing.JButton jButtonWizard;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

	public void colorPeferenceChanged(String preference, Color col) {
		DefaultTreeModel model = (DefaultTreeModel)jTree1.getModel();
		model.reload();
	}

	public void fontFamilyPreferenceChanged(String preference, String font) {
		DefaultTreeModel model = (DefaultTreeModel)jTree1.getModel();
		model.reload();
	}

	public void fontSizePreferenceChanged(String preference, int size) {
		DefaultTreeModel model = (DefaultTreeModel)jTree1.getModel();
		model.reload();

	}

	public void isBoldPreferenceChanged(String preference, Boolean isBold) {
		DefaultTreeModel model = (DefaultTreeModel)jTree1.getModel();
		model.reload();
	}

	public void shortCutChanged(String preference, String shortcut) {
		DefaultTreeModel model = (DefaultTreeModel)jTree1.getModel();
		model.reload();
	}

}
