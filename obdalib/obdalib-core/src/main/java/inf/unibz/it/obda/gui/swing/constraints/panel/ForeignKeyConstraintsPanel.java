/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ForeignKeyConstraintsPanel.java
 *
 * Created on Nov 6, 2009, 10:58:15 AM
 */

package inf.unibz.it.obda.gui.swing.constraints.panel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.constraints.controller.RDBMSForeignKeyConstraintController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSForeignKeyConstraint;
import inf.unibz.it.obda.constraints.miner.AbstractAssertionMiner;
import inf.unibz.it.obda.gui.swing.constraints.treemodel.ConstraintsTreeCellEditor;
import inf.unibz.it.obda.gui.swing.constraints.treemodel.ConstraintsTreeCellRenderer;
import inf.unibz.it.obda.gui.swing.constraints.treemodel.ForeignKeyConstraintTreeModel;
import inf.unibz.it.obda.gui.swing.dependencies.panel.ProgressMonitorDialog;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferenceChangeListener;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferences;
import inf.unibz.it.obda.gui.swing.treemodel.DefaultAssertionTreeNode;
import inf.unibz.it.obda.gui.swing.treemodel.DefaultAssertionTreeNodeRenderer;

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

/**
 *
 * @author obda
 */
public class ForeignKeyConstraintsPanel extends javax.swing.JPanel implements MappingManagerPreferenceChangeListener{

	/**
	 * the API controller
	 */
	private APIController apic = null;
	
	private MappingManagerPreferences pref = null;
	
	private RDBMSForeignKeyConstraintController fkController = null;
	
	private JPanel myself = null;
	
    /** Creates new form ForeignKeyConstraintsPanel */
    public ForeignKeyConstraintsPanel(APIController apic, OBDAPreferences preference) {
    	this.apic = apic;
    	myself = this;
        initComponents();
        addListener();
        addMenu();
    	pref = preference.getMappingsPreference();
        fkController =(RDBMSForeignKeyConstraintController) apic.getController(RDBMSForeignKeyConstraint.class);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Disjoinedness Constraints");
        DefaultAssertionTreeNodeRenderer renderer = new DefaultAssertionTreeNodeRenderer();
        ForeignKeyConstraintTreeModel model = new ForeignKeyConstraintTreeModel(root, fkController, renderer);
        ConstraintsTreeCellRenderer tcr = new ConstraintsTreeCellRenderer(apic, preference);
        jTreeFKConstraints.setCellRenderer(tcr);
        jTreeFKConstraints.setModel(model);
        jTreeFKConstraints.setCellEditor(new ConstraintsTreeCellEditor(apic, RDBMSForeignKeyConstraint.RDBMSFOREIGNKEYCONSTRAINT));
        jTreeFKConstraints.setEditable(true);
        jTreeFKConstraints.setInvokesStopCellEditing(true);
        jTreeFKConstraints.setRootVisible(false);
        jTreeFKConstraints.setRowHeight(0);
        pref.registerPreferenceChangedListener(this);
    }

    private void addListener(){
    	
        jButtonAdd.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				if(apic.getDatasourcesController().getCurrentDataSource()!= null){
					addRDBMSForeignKeyConstraint();
				}else{
					JOptionPane.showMessageDialog(null, "Please select a data source first.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
        	
        });
        jButtonWizard.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
			
				if(apic.getDatasourcesController().getCurrentDataSource()!= null){
					
					Thread t = new Thread(){
		    			public void run(){
		    				ProgressMonitorDialog pmg = new ProgressMonitorDialog(myself, "Mining Foreign Key Constraints...");
							pmg.show();
							AbstractAssertionMiner miner = new AbstractAssertionMiner(apic);
							try {
								List<RDBMSForeignKeyConstraint> results = miner.mineForeignkeyConstraints();
								RDBMSForeignKeyConstraintController con = (RDBMSForeignKeyConstraintController) apic.getController(RDBMSForeignKeyConstraint.class);
								Iterator<RDBMSForeignKeyConstraint> it = results.iterator();
								while(it.hasNext()){
									con.addAssertion(it.next());
								}
								pmg.stop();
							} catch (Exception e1) {
								e1.printStackTrace();
								pmg.stop();
								JOptionPane.showMessageDialog(null, "Error during mining!", "ERROR", JOptionPane.ERROR_MESSAGE);
							}
		    			}
					};
					t.start();
					
				}else{
					JOptionPane.showMessageDialog(null, "Please select a data source first.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
        	
        });
        
        jButtonDelete.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				if(apic.getDatasourcesController().getCurrentDataSource()!= null){
					TreePath[] selection =jTreeFKConstraints.getSelectionPaths();
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
				TreePath[] selection =jTreeFKConstraints.getSelectionPaths();
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
				
				TreePath[] paths = jTreeFKConstraints.getSelectionPaths();
				if(paths != null){
					
				}
			}
    		
    	});
    	menu.add(validate);
    	jTreeFKConstraints.setComponentPopupMenu(menu);
    }

    public void colorPeferenceChanged(String preference, Color col) {
		
		DefaultTreeModel model = (DefaultTreeModel)jTreeFKConstraints.getModel();
		model.reload();
	}

	public void fontFamilyPreferenceChanged(String preference, String font) {
		DefaultTreeModel model = (DefaultTreeModel)jTreeFKConstraints.getModel();
		model.reload();
		
	}

	public void fontSizePreferenceChanged(String preference, int size) {
		DefaultTreeModel model = (DefaultTreeModel)jTreeFKConstraints.getModel();
		model.reload();
		
	}

	public void isBoldPreferenceChanged(String preference, Boolean isBold) {
		DefaultTreeModel model = (DefaultTreeModel)jTreeFKConstraints.getModel();
		model.reload();
		
	}

	public void shortCutChanged(String preference, String shortcut) {
		DefaultTreeModel model = (DefaultTreeModel)jTreeFKConstraints.getModel();
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
				DefaultAssertionTreeNode<RDBMSForeignKeyConstraint> node = (DefaultAssertionTreeNode<RDBMSForeignKeyConstraint>)o;
				RDBMSForeignKeyConstraint con = (RDBMSForeignKeyConstraint) node.getUserObject();
				fkController.removeAssertion(con);
				
				
			}
		}
    }
    
    private void addRDBMSForeignKeyConstraint(){
		apic.getDatasourcesController(); 
		ForeignKeyConstraintTreeModel model =(ForeignKeyConstraintTreeModel) jTreeFKConstraints.getModel();
		DefaultAssertionTreeNode<RDBMSForeignKeyConstraint> node = new DefaultAssertionTreeNode<RDBMSForeignKeyConstraint>(null);
		MutableTreeNode root = (MutableTreeNode) model.getRoot();
		int index = ((DefaultMutableTreeNode)model.getRoot()).getChildCount();
		root.insert(node, index);
		model.nodesWereInserted(root, new int[]{index});
		model.nodeStructureChanged(root);
		jTreeFKConstraints.setVisibleRowCount(index+1);
//		model.insertNodeInto(node, (MutableTreeNode) model.getRoot(), ((DefaultMutableTreeNode)model.getRoot()).getChildCount());
		TreePath path = jTreeFKConstraints.getPathForRow(index);
		if(path == null){
			root.remove(index);
			model.nodesWereRemoved(root, new int[] {index}, new Object[]{node});
		}
		jTreeFKConstraints.setSelectionPath(path);
		jTreeFKConstraints.startEditingAtPath(path);
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
        jTreeFKConstraints = new javax.swing.JTree();
        jLabel1 = new javax.swing.JLabel();
        jButtonWizard = new javax.swing.JButton();
        jButtonAdd = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Foreign Key Constraints"));
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

        jTreeFKConstraints.setBorder(null);
        jScrollPane1.setViewportView(jTreeFKConstraints);

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

        jButtonWizard.setText(" Mine ");
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonWizard;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTreeFKConstraints;
    // End of variables declaration//GEN-END:variables

}
