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
import inf.unibz.it.obda.gui.swing.constraints.treemodel.CheckConstraintTreeModel;
import inf.unibz.it.obda.gui.swing.constraints.treemodel.ConstraintsTreeCellEditor;
import inf.unibz.it.obda.gui.swing.constraints.treemodel.ConstraintsTreeCellRenderer;
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
    public CheckConstraintsPanel(APIController apic, OBDAPreferences preference) {
    	this.apic = apic;
        initComponents();
        addListener();
        addMenu();
        pref = preference.getMappingsPreference();
        ccController =(RDBMSCheckConstraintController) apic.getController(RDBMSCheckConstraint.class);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Check Constraints");
        DefaultAssertionTreeNodeRenderer renderer = new DefaultAssertionTreeNodeRenderer();
        CheckConstraintTreeModel model = new CheckConstraintTreeModel(root, ccController, renderer);
        ConstraintsTreeCellRenderer tcr = new ConstraintsTreeCellRenderer(apic, preference);
        treCheckConstraint.setCellRenderer(tcr);
        treCheckConstraint.setModel(model);
        treCheckConstraint.setCellEditor(new ConstraintsTreeCellEditor(apic, RDBMSCheckConstraint.RDBMSCHECKSONSTRAINT));
        treCheckConstraint.setEditable(true);
        treCheckConstraint.setInvokesStopCellEditing(true);
        treCheckConstraint.setRootVisible(false);
        treCheckConstraint.setRowHeight(0);
        pref.registerPreferenceChangedListener(this);
    }
    
    private void addListener(){
    	
        cmdAdd.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(apic.getDatasourcesController().getCurrentDataSource()!= null){
					addRDBMSCheckConstraint();
				}else{
					JOptionPane.showMessageDialog(null, "Please select a data source first.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
        	
        });
        cmdWizard.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
			}
        	
        });
        
        cmdDelete.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				

				if(apic.getDatasourcesController().getCurrentDataSource()!= null){
					TreePath[] selection =treCheckConstraint.getSelectionPaths();
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
				TreePath[] selection =treCheckConstraint.getSelectionPaths();
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
				
				TreePath[] paths = treCheckConstraint.getSelectionPaths();
				if(paths != null){
					
				}
			}
    		
    	});
    	menu.add(validate);
    	treCheckConstraint.setComponentPopupMenu(menu);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlButtons = new javax.swing.JPanel();
        cmdAdd = new javax.swing.JButton();
        cmdDelete = new javax.swing.JButton();
        cmdWizard = new javax.swing.JButton();
        scrCheckConstraint = new javax.swing.JScrollPane();
        treCheckConstraint = new javax.swing.JTree();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Check Constraint"));
        setMaximumSize(new java.awt.Dimension(79, 76));
        setMinimumSize(new java.awt.Dimension(0, 0));
        setPreferredSize(new java.awt.Dimension(79, 76));
        setLayout(new java.awt.BorderLayout(0, 5));

        pnlButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        cmdAdd.setText("Add");
        cmdAdd.setMaximumSize(new java.awt.Dimension(95, 23));
        cmdAdd.setMinimumSize(new java.awt.Dimension(95, 23));
        cmdAdd.setPreferredSize(new java.awt.Dimension(95, 23));
        pnlButtons.add(cmdAdd);

        cmdDelete.setText("Delete");
        cmdDelete.setMaximumSize(new java.awt.Dimension(95, 23));
        cmdDelete.setMinimumSize(new java.awt.Dimension(95, 23));
        cmdDelete.setPreferredSize(new java.awt.Dimension(95, 23));
        pnlButtons.add(cmdDelete);

        cmdWizard.setLabel("Wizard...");
        cmdWizard.setMaximumSize(new java.awt.Dimension(95, 23));
        cmdWizard.setMinimumSize(new java.awt.Dimension(95, 23));
        cmdWizard.setPreferredSize(new java.awt.Dimension(95, 23));
        pnlButtons.add(cmdWizard);

        add(pnlButtons, java.awt.BorderLayout.NORTH);

        scrCheckConstraint.setMinimumSize(new java.awt.Dimension(700, 600));
        scrCheckConstraint.setPreferredSize(new java.awt.Dimension(700, 600));
        scrCheckConstraint.setViewportView(treCheckConstraint);

        add(scrCheckConstraint, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    
	public void colorPeferenceChanged(String preference, Color col) {
		
		DefaultTreeModel model = (DefaultTreeModel)treCheckConstraint.getModel();
		model.reload();
	}

	public void fontFamilyPreferenceChanged(String preference, String font) {
		DefaultTreeModel model = (DefaultTreeModel)treCheckConstraint.getModel();
		model.reload();
		
	}

	public void fontSizePreferenceChanged(String preference, int size) {
		DefaultTreeModel model = (DefaultTreeModel)treCheckConstraint.getModel();
		model.reload();
		
	}

	public void isBoldPreferenceChanged(String preference, Boolean isBold) {
		DefaultTreeModel model = (DefaultTreeModel)treCheckConstraint.getModel();
		model.reload();
		
	}

	public void shortCutChanged(String preference, String shortcut) {
		DefaultTreeModel model = (DefaultTreeModel)treCheckConstraint.getModel();
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
		CheckConstraintTreeModel model =(CheckConstraintTreeModel) treCheckConstraint.getModel();
		DefaultAssertionTreeNode<RDBMSCheckConstraint> node = new DefaultAssertionTreeNode<RDBMSCheckConstraint>(null);
		MutableTreeNode root = (MutableTreeNode) model.getRoot();
		int index = ((DefaultMutableTreeNode)model.getRoot()).getChildCount();
		root.insert(node, index);
		model.nodesWereInserted(root, new int[]{index});
		model.nodeStructureChanged(root);
		treCheckConstraint.setVisibleRowCount(index+1);
//		model.insertNodeInto(node, (MutableTreeNode) model.getRoot(), ((DefaultMutableTreeNode)model.getRoot()).getChildCount());
		TreePath path = treCheckConstraint.getPathForRow(index);
		if(path == null){
			root.remove(index);
			model.nodesWereRemoved(root, new int[] {index}, new Object[]{node});
		}
		treCheckConstraint.setSelectionPath(path);
		treCheckConstraint.startEditingAtPath(path);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdAdd;
    private javax.swing.JButton cmdDelete;
    private javax.swing.JButton cmdWizard;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JScrollPane scrCheckConstraint;
    private javax.swing.JTree treCheckConstraint;
    // End of variables declaration//GEN-END:variables

}
