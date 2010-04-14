/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CheckConstraintsPanel.java
 *
 * Created on Nov 6, 2009, 11:01:14 AM
 */

package inf.unibz.it.obda.gui.swing.constraints.panel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.constraints.controller.RDBMSCheckConstraintController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSCheckConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSForeignKeyConstraint;
import inf.unibz.it.obda.dependencies.controller.RDBMSDisjointnessDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSFunctionalDependency;
import inf.unibz.it.obda.gui.swing.constraints.treemodel.CheckConstraintTreeModel;
import inf.unibz.it.obda.gui.swing.constraints.treemodel.ConstraintsTreeCellEditor;
import inf.unibz.it.obda.gui.swing.constraints.treemodel.ConstraintsTreeCellRenderer;
import inf.unibz.it.obda.gui.swing.constraints.treemodel.ForeignKeyConstraintTreeModel;
import inf.unibz.it.obda.gui.swing.dependencies.panel.Dependency_SelectMappingPane;
import inf.unibz.it.obda.gui.swing.dependencies.treemodel.DependencyAssertionTreeCellRenderer;
import inf.unibz.it.obda.gui.swing.dependencies.treemodel.DependencyTreeCellEditor;
import inf.unibz.it.obda.gui.swing.dependencies.treemodel.DisjoinednessAssertionTreeModel;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferenceChangeListener;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferences;
import inf.unibz.it.obda.gui.swing.treemodel.DefaultAssertionTreeNode;
import inf.unibz.it.obda.gui.swing.treemodel.DefaultAssertionTreeNodeRenderer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author obda
 */
public class CheckConstraintsPanel extends javax.swing.JPanel implements MappingManagerPreferenceChangeListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6257834399693621686L;

	/**
	 * the API controller
	 */
	private APIController apic = null;
	
	private MappingManagerPreferences pref = null;
	
	private RDBMSCheckConstraintController ccController = null;
	
    /** Creates new form CheckConstraintsPanel */
    public CheckConstraintsPanel(APIController apic) {
    	this.apic = apic;
        initComponents();
        addListener();
        addMenu();
        pref = OBDAPreferences.getOBDAPreferences().getMappingsPreference();
        ccController =(RDBMSCheckConstraintController) apic.getController(RDBMSCheckConstraint.class);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Check Constraints");
        DefaultAssertionTreeNodeRenderer renderer = new DefaultAssertionTreeNodeRenderer();
        CheckConstraintTreeModel model = new CheckConstraintTreeModel(root, ccController, renderer);
        ConstraintsTreeCellRenderer tcr = new ConstraintsTreeCellRenderer(apic);
        jTreeCheckConstraints.setCellRenderer(tcr);
        jTreeCheckConstraints.setModel(model);
        jTreeCheckConstraints.setCellEditor(new ConstraintsTreeCellEditor(apic, RDBMSCheckConstraint.RDBMSCHECKSONSTRAINT));
        jTreeCheckConstraints.setEditable(true);
        jTreeCheckConstraints.setInvokesStopCellEditing(true);
        jTreeCheckConstraints.setRootVisible(false);
        jTreeCheckConstraints.setRowHeight(0);
        pref.registerPreferenceChangedListener(this);
    }

    
    private void addListener(){
    	
        jButtonAdd.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(apic.getDatasourcesController().getCurrentDataSource()!= null){
					addRDBMSCheckConstraint();
				}else{
					JOptionPane.showMessageDialog(null, "Please select a data source first.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
        	
        });
        jButtonWizard.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
			}
        	
        });
        
        jButtonDelete.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				

				if(apic.getDatasourcesController().getCurrentDataSource()!= null){
					TreePath[] selection =jTreeCheckConstraints.getSelectionPaths();
					if(selection != null){
						delete(selection);
					}
				}else{
					JOptionPane.showMessageDialog(null, "Please select a data source first.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
        	
        });
    }
    
    private void addMenu(){
    	JPopupMenu menu = new JPopupMenu();
    	
    	JMenuItem del = new JMenuItem();
    	del.setText("delete");
    	del.setToolTipText("deletes all selected Assertions");
    	del.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				TreePath[] selection =jTreeCheckConstraints.getSelectionPaths();
				if(selection != null){
					
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
				
				TreePath[] paths = jTreeCheckConstraints.getSelectionPaths();
				if(paths != null){
					
				}
			}
    		
    	});
    	menu.add(validate);
    	jTreeCheckConstraints.setComponentPopupMenu(menu);
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

        jButtonDelete = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTreeCheckConstraints = new javax.swing.JTree();
        jLabel1 = new javax.swing.JLabel();
        jButtonWizard = new javax.swing.JButton();
        jButtonAdd = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Check Constraints")));
        setLayout(new java.awt.GridBagLayout());

        jButtonDelete.setText("Delete");
        jButtonDelete.setMaximumSize(new java.awt.Dimension(95, 23));
        jButtonDelete.setMinimumSize(new java.awt.Dimension(95, 23));
        jButtonDelete.setPreferredSize(new java.awt.Dimension(95, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jButtonDelete, gridBagConstraints);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(700, 600));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(700, 600));
        jScrollPane1.setViewportView(jTreeCheckConstraints);

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
    }// </editor-fold>//GEN-END:initComponents

    
	public void colorPeferenceChanged(String preference, Color col) {
		
		DefaultTreeModel model = (DefaultTreeModel)jTreeCheckConstraints.getModel();
		model.reload();
	}

	public void fontFamilyPreferenceChanged(String preference, String font) {
		DefaultTreeModel model = (DefaultTreeModel)jTreeCheckConstraints.getModel();
		model.reload();
		
	}

	public void fontSizePreferenceChanged(String preference, int size) {
		DefaultTreeModel model = (DefaultTreeModel)jTreeCheckConstraints.getModel();
		model.reload();
		
	}

	public void isBoldPreferenceChanged(String preference, Boolean isBold) {
		DefaultTreeModel model = (DefaultTreeModel)jTreeCheckConstraints.getModel();
		model.reload();
		
	}

	public void shortCutChanged(String preference, String shortcut) {
		DefaultTreeModel model = (DefaultTreeModel)jTreeCheckConstraints.getModel();
		model.reload();
		
	}
    
	/**
     * Removes the selected assertions
     * @param selection
     */
    private void delete(TreePath[] selection){
    	for(int i=0; i< selection.length;i++){
			TreePath path = selection[i];
			Object o = path.getLastPathComponent();
			if(o instanceof DefaultAssertionTreeNode){
				DefaultAssertionTreeNode<RDBMSCheckConstraint> node = (DefaultAssertionTreeNode<RDBMSCheckConstraint>)o;
				RDBMSCheckConstraint con = (RDBMSCheckConstraint) node.getUserObject();
				ccController.removeAssertion(con);
				
				
			}
		}
    }
    
    private void addRDBMSCheckConstraint(){
		apic.getDatasourcesController(); 
		CheckConstraintTreeModel model =(CheckConstraintTreeModel) jTreeCheckConstraints.getModel();
		DefaultAssertionTreeNode<RDBMSCheckConstraint> node = new DefaultAssertionTreeNode<RDBMSCheckConstraint>(null);
		MutableTreeNode root = (MutableTreeNode) model.getRoot();
		int index = ((DefaultMutableTreeNode)model.getRoot()).getChildCount();
		root.insert(node, index);
		model.nodesWereInserted(root, new int[]{index});
		model.nodeStructureChanged(root);
		jTreeCheckConstraints.setVisibleRowCount(index+1);
//		model.insertNodeInto(node, (MutableTreeNode) model.getRoot(), ((DefaultMutableTreeNode)model.getRoot()).getChildCount());
		TreePath path = jTreeCheckConstraints.getPathForRow(index);
		if(path == null){
			root.remove(index);
			model.nodesWereRemoved(root, new int[] {index}, new Object[]{node});
		}
		jTreeCheckConstraints.setSelectionPath(path);
		jTreeCheckConstraints.startEditingAtPath(path);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonWizard;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTreeCheckConstraints;
    // End of variables declaration//GEN-END:variables

}
