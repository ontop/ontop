/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * UniquenessConstraintPanel.java
 *
 * Created on Nov 6, 2009, 10:59:44 AM
 */

package inf.unibz.it.obda.gui.swing.constraints.panel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.constraints.controller.RDBMSUniquenessConstraintController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSUniquenessConstraint;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.constraints.treemodel.ConstraintsTreeCellEditor;
import inf.unibz.it.obda.gui.swing.constraints.treemodel.ConstraintsTreeCellRenderer;
import inf.unibz.it.obda.gui.swing.constraints.treemodel.UniquenessConstraintTreeModel;
import inf.unibz.it.obda.gui.swing.datasource.DatasourceSelectorListener;
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
public class UniquenessConstraintPanel extends javax.swing.JPanel implements 
    MappingManagerPreferenceChangeListener, DatasourceSelectorListener {

  private APIController apic;

  private MappingManagerPreferences pref;

  private RDBMSUniquenessConstraintController uqController;
  
  private DataSource selectedSource;

  /**
   * Creates a new panel.
   * 
   * @param apic
   *          The API controller object.
   * @param preference
   *          The preference object.
   */
  public UniquenessConstraintPanel(APIController apic, 
      OBDAPreferences preference) {
    
    this.apic = apic;  
    pref = preference.getMappingsPreference();
    uqController = (RDBMSUniquenessConstraintController) apic.getController(
        RDBMSUniquenessConstraint.class);
    
    initComponents();
    addListener();
    addMenu();
    
    DefaultMutableTreeNode root = 
        new DefaultMutableTreeNode("Disjoinedness Constraints");
    DefaultAssertionTreeNodeRenderer renderer = 
        new DefaultAssertionTreeNodeRenderer();
    UniquenessConstraintTreeModel model = 
        new UniquenessConstraintTreeModel(root, uqController, renderer);
    ConstraintsTreeCellRenderer tcr = 
        new ConstraintsTreeCellRenderer(apic, preference);
    
    TreUniquenessConstraint.setCellRenderer(tcr);
    TreUniquenessConstraint.setModel(model);
    TreUniquenessConstraint.setCellEditor(new ConstraintsTreeCellEditor(apic, 
        RDBMSUniquenessConstraint.RDBMSUNIQUENESSCONSTRAINT));
    TreUniquenessConstraint.setEditable(true);
    TreUniquenessConstraint.setInvokesStopCellEditing(true);
    TreUniquenessConstraint.setRootVisible(false);
    TreUniquenessConstraint.setRowHeight(0);
    pref.registerPreferenceChangedListener(this);
  }

  private void addListener() {

    cmdAdd.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (selectedSource != null) {
          addRDBMSUniquenessConstraint();
        }
        else {
          JOptionPane.showMessageDialog(null, 
              "Please select a data source first.", "ERROR", 
              JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    
    cmdWizard.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (selectedSource != null) {
          // TODO Implement the wizard.
        }
        else {
          JOptionPane.showMessageDialog(null, 
              "Please select a data source first.", "ERROR", 
              JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    cmdDelete.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (selectedSource != null) {
          TreePath[] selection =TreUniquenessConstraint.getSelectionPaths();
          if (selection != null) {
            delete(selection);
          }
        }
        else {
          JOptionPane.showMessageDialog(null, 
              "Please select a data source first.", "ERROR", 
              JOptionPane.ERROR_MESSAGE);
        }
      } 
    });
  }

  private void addMenu() {
    
    JPopupMenu menu = new JPopupMenu();
    JMenuItem del = new JMenuItem();
    del.setText("delete");
    del.setToolTipText("deletes all selected Assertions");
    del.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] selection =TreUniquenessConstraint.getSelectionPaths();
        if (selection != null) {
          // Do nothing.
        }
      }
    });
    menu.add(del);
    menu.addSeparator();

    JMenuItem validate = new JMenuItem();
    validate.setEnabled(false);
    validate.setText("Validate Dependency");
    validate.setToolTipText("Check wehter the produced SQL query is valid.");
    validate.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] paths = TreUniquenessConstraint.getSelectionPaths();
        if(paths != null){
          // Do nothing.
        }
      }
    });
    menu.add(validate);
    TreUniquenessConstraint.setComponentPopupMenu(menu);
  }

  @Override
  public void colorPeferenceChanged(String preference, Color col) {

    DefaultTreeModel model = (DefaultTreeModel)TreUniquenessConstraint.getModel();
    model.reload();
  }

  @Override
  public void fontFamilyPreferenceChanged(String preference, String font) {
    
    DefaultTreeModel model = (DefaultTreeModel)TreUniquenessConstraint.getModel();
    model.reload();
  }

  @Override
  public void fontSizePreferenceChanged(String preference, int size) {
    
    DefaultTreeModel model = (DefaultTreeModel)TreUniquenessConstraint.getModel();
    model.reload();
  }

  @Override
  public void isBoldPreferenceChanged(String preference, Boolean isBold) {
    
    DefaultTreeModel model = (DefaultTreeModel)TreUniquenessConstraint.getModel();
    model.reload();
  }

  @Override
  public void shortCutChanged(String preference, String shortcut) {
    
    DefaultTreeModel model = (DefaultTreeModel)TreUniquenessConstraint.getModel();
    model.reload();
  }

  /**
   * Removes the selected assertions
   * @param selection
   */
  private void delete(TreePath[] selection) {
    
    for (int i = 0; i < selection.length; i++) {
      TreePath path = selection[i];
      Object o = path.getLastPathComponent();
      if(o instanceof DefaultAssertionTreeNode){
        DefaultAssertionTreeNode<RDBMSUniquenessConstraint> node = 
            (DefaultAssertionTreeNode<RDBMSUniquenessConstraint>) o;
        RDBMSUniquenessConstraint con = node.getUserObject();
        uqController.removeAssertion(con);
      }
    }
  }

  private void addRDBMSUniquenessConstraint() {
    
    UniquenessConstraintTreeModel model = 
        (UniquenessConstraintTreeModel) TreUniquenessConstraint.getModel();
    
    DefaultAssertionTreeNode<RDBMSUniquenessConstraint> node = 
        new DefaultAssertionTreeNode<RDBMSUniquenessConstraint>(null);
    MutableTreeNode root = (MutableTreeNode) model.getRoot();
    int index = ((DefaultMutableTreeNode)model.getRoot()).getChildCount();
    root.insert(node, index);
    
    model.nodesWereInserted(root, new int[]{index});
    model.nodeStructureChanged(root);
    TreUniquenessConstraint.setVisibleRowCount(index+1);
    TreePath path = TreUniquenessConstraint.getPathForRow(index);
    if (path == null) {
      root.remove(index);
      model.nodesWereRemoved(root, new int[] {index}, new Object[]{node});
    }
    TreUniquenessConstraint.setSelectionPath(path);
    TreUniquenessConstraint.startEditingAtPath(path);
  }

  @Override
  public void datasourceChanged(DataSource oldSource, DataSource newSource) {
    
    this.selectedSource = newSource;
    
    uqController.changeDatasource(oldSource, newSource);
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
    scrUniquenessConstraint = new javax.swing.JScrollPane();
    TreUniquenessConstraint = new javax.swing.JTree();

    setBorder(javax.swing.BorderFactory.createTitledBorder("Uniqueness Constraint"));
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

    cmdWizard.setText("Wizard...");
    cmdWizard.setMaximumSize(new java.awt.Dimension(95, 23));
    cmdWizard.setMinimumSize(new java.awt.Dimension(95, 23));
    cmdWizard.setPreferredSize(new java.awt.Dimension(95, 23));
    pnlButtons.add(cmdWizard);

    add(pnlButtons, java.awt.BorderLayout.NORTH);

    scrUniquenessConstraint.setMinimumSize(new java.awt.Dimension(700, 600));
    scrUniquenessConstraint.setPreferredSize(new java.awt.Dimension(700, 600));
    scrUniquenessConstraint.setViewportView(TreUniquenessConstraint);

    add(scrUniquenessConstraint, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTree TreUniquenessConstraint;
  private javax.swing.JButton cmdAdd;
  private javax.swing.JButton cmdDelete;
  private javax.swing.JButton cmdWizard;
  private javax.swing.JPanel pnlButtons;
  private javax.swing.JScrollPane scrUniquenessConstraint;
  // End of variables declaration//GEN-END:variables
}
