/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FunctionalDepTreePane.java
 *
 * Created on Aug 12, 2009, 10:16:29 AM
 */
package inf.unibz.it.obda.gui.swing.dependencies.panel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.dependencies.controller.RDBMSFunctionalDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSFunctionalDependency;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.dependencies.treemodel.DependencyAssertionTreeCellRenderer;
import inf.unibz.it.obda.gui.swing.dependencies.treemodel.DependencyTreeCellEditor;
import inf.unibz.it.obda.gui.swing.dependencies.treemodel.FunctionalDependenciesTreeModel;
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
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.obda.query.domain.Term;

/**
 * The tree pane showing all functional dependencies associated the selected data source
 *
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
 */
public class FunctionalDepTreePane extends JPanel implements MappingManagerPreferenceChangeListener{

	/**
	 *
	 */
	private static final long serialVersionUID = 7747935375532919184L;
	/**
	 * the API controller
	 */
	private APIController apic = null;
	/**
	 * the RDBMS functional dependency controller
	 */
	private RDBMSFunctionalDependencyController fdController = null;

	private MappingManagerPreferences pref = null;

    /** Creates new form FunctionalDepTreePane */
    public FunctionalDepTreePane(APIController apic) {

    	this.apic = apic;
    	pref = OBDAPreferences.getOBDAPreferences().getMappingsPreference();
    	fdController = (RDBMSFunctionalDependencyController) apic.getController(RDBMSFunctionalDependency.class);
        initComponents();
        addMenu();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Functional Dependencies");
        DefaultAssertionTreeNodeRenderer renderer = new DefaultAssertionTreeNodeRenderer();
        FunctionalDependenciesTreeModel model = new FunctionalDependenciesTreeModel(root, fdController, renderer);
        DependencyAssertionTreeCellRenderer tcr = new DependencyAssertionTreeCellRenderer(apic);
        jTree1.setCellRenderer(tcr);
        jTree1.setModel(model);
        jTree1.setCellEditor(new DependencyTreeCellEditor(apic, RDBMSFunctionalDependency.FUNCTIONALDEPENDENCY));
        jTree1.setEditable(true);
        jTree1.setInvokesStopCellEditing(true);
        jTree1.setRootVisible(false);
        jTree1.setRowHeight(0);
        pref.registerPreferenceChangedListener(this);
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
				addRDBMSFunctionalDependency();

			}

        });
        jButtonWizard.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				TreePath[] paths = Dependency_SelectMappingPane.gestInstance().getSelection();
				Dependency_SelectMappingPane.gestInstance().createDialog("Create Functional Dependency", paths, RDBMSFunctionalDependency.FUNCTIONALDEPENDENCY);
			}

        });
        jButtonMine.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				Dependency_SelectMappingPane.gestInstance().showFunctionalDependencyMiningDialog(jTree1);
			}

        });
    }

    private void addRDBMSFunctionalDependency(){

		apic.getDatasourcesController();
		FunctionalDependenciesTreeModel model =(FunctionalDependenciesTreeModel) jTree1.getModel();
		DefaultAssertionTreeNode<RDBMSFunctionalDependency> node = new DefaultAssertionTreeNode<RDBMSFunctionalDependency>(null);
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
    	validate.setEnabled(false);
    	validate.setText("Validate Dependency");
    	validate.setToolTipText("Check wehter the produced SQL query is valid.");
    	validate.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				TreePath[] paths = jTree1.getSelectionPaths();
				if(paths != null){
					validateRDBMSFunctionalDependencies(paths);
				}
			}

    	});
    	menu.add(validate);
    	jTree1.setComponentPopupMenu(menu);
    }

    /**
     * deletes the selected assertions
     * @param selection current selection in the tree
     */
    private void delete(TreePath[] selection){
    	for(int i=0; i< selection.length;i++){
			TreePath path = selection[i];
			Object o = path.getLastPathComponent();
			if(o instanceof DefaultAssertionTreeNode){
				DefaultAssertionTreeNode<RDBMSFunctionalDependency> node = (DefaultAssertionTreeNode<RDBMSFunctionalDependency>)o;
				RDBMSFunctionalDependency dep = node.getUserObject();
				fdController.removeAssertion(dep);


			}
		}
    }

    /**
     * validates whether the data source fulfills the assertions
     * @param paths assertions to validate
     */
    private void validateRDBMSFunctionalDependencies(TreePath[] paths){

		/*
		 * Sample of an sql query produced by this method:
		 SELECT a1.nid, a1.value FROM (Select id as nid, 2 as value, name from name) as a1
			WHERE ROW(a1.nid, a1.value) NOT IN
		(SELECT nid, value FROM (Select id as nid, 2 as value, name from name) a2)
		 */
    	for (int i=0;i<paths.length;i++){

    		Object o =  paths[i].getLastPathComponent();
    		if(!o.equals(jTree1.getModel().getRoot())){

	    		DefaultAssertionTreeNode<RDBMSFunctionalDependency> node = (DefaultAssertionTreeNode<RDBMSFunctionalDependency>)o;
	    		RDBMSFunctionalDependency dep = node.getUserObject();
	    		RDBMSSQLQuery query1 = (RDBMSSQLQuery) dep.getSourceQueryOne();
	    		RDBMSSQLQuery query2 = (RDBMSSQLQuery) dep.getSourceQueryOne();
	    		List<Term> terms1 = dep.getTermsOfQueryOne();
	    		List<Term> terms2 = dep.getTermsOfQueryTwo();

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
	    			aux2= aux2 + "table1." + it2.next().getName();
	    		}

	    		String query = "SELECT " + aux2 + " FROM(" + query2.toString()+
	    			") table1 WHERE ROW(" + aux2 +") NOT IN (SELECT " + aux1 +
	    			" FROM (" + query1.toString() + ") table2)";

	    		DatasourcesController con = apic.getDatasourcesController();
	    		DataSource ds = con.getCurrentDataSource();
	    		SQLQueryValidator v = new SQLQueryValidator(ds, new RDBMSSQLQuery(query));
				if(v.validate()){
					JOptionPane.showMessageDialog(this, "The Assertion produces a valid SQL query.", "Validation successful", JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(this, "The Assertion produces a non valid SQL query. Please check it again.", "ERROR", JOptionPane.ERROR_MESSAGE);
					v.getReason().printStackTrace();
				}
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
        jLabel1 = new javax.swing.JLabel();
        jButtonWizard = new javax.swing.JButton();
        jButtonAdd = new javax.swing.JButton();
        jButtonMine = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Current Functional Dependencies"));
        setMinimumSize(new java.awt.Dimension(70, 70));
        setPreferredSize(new java.awt.Dimension(700, 700));
        setLayout(new java.awt.GridBagLayout());

        jButton1.setText("Delete");
        jButton1.setMaximumSize(new java.awt.Dimension(95, 23));
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
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jScrollPane1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jLabel1, gridBagConstraints);

        jButtonWizard.setText(" Add Wizard...");
        jButtonWizard.setMaximumSize(new java.awt.Dimension(95, 23));
        jButtonWizard.setMinimumSize(new java.awt.Dimension(95, 23));
        jButtonWizard.setPreferredSize(new java.awt.Dimension(95, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jButtonWizard, gridBagConstraints);

        jButtonAdd.setText("Add");
        jButtonAdd.setMaximumSize(new java.awt.Dimension(95, 23));
        jButtonAdd.setMinimumSize(new java.awt.Dimension(95, 23));
        jButtonAdd.setPreferredSize(new java.awt.Dimension(95, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jButtonAdd, gridBagConstraints);

        jButtonMine.setText("Mine");
        jButtonMine.setMaximumSize(new java.awt.Dimension(95, 23));
        jButtonMine.setMinimumSize(new java.awt.Dimension(95, 23));
        jButtonMine.setPreferredSize(new java.awt.Dimension(95, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jButtonMine, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
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
