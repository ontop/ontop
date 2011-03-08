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
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.constraints.treemodel.ConstraintsTreeCellEditor;
import inf.unibz.it.obda.gui.swing.constraints.treemodel.ConstraintsTreeCellRenderer;
import inf.unibz.it.obda.gui.swing.constraints.treemodel.ForeignKeyConstraintTreeModel;
import inf.unibz.it.obda.gui.swing.datasource.DatasourceSelectorListener;
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
public class ForeignKeyConstraintsPanel extends javax.swing.JPanel implements
    MappingManagerPreferenceChangeListener, DatasourceSelectorListener {

  private APIController apic;

  private MappingManagerPreferences pref;

  private RDBMSForeignKeyConstraintController fkController;

  private JPanel myself;

  private DataSource selectedSource;

  /**
   * Creates a new panel.
   * 
   * @param apic
   *          The API controller object.
   * @param preference
   *          The preference object.
   */
  public ForeignKeyConstraintsPanel(APIController apic, 
      OBDAPreferences preference) {
    
    this.apic = apic;
    myself = this;
    initComponents();
    addListener();
    addMenu();
    
    pref = preference.getMappingsPreference();
    fkController = (RDBMSForeignKeyConstraintController) 
        apic.getController(RDBMSForeignKeyConstraint.class);
    
    DefaultMutableTreeNode root = 
        new DefaultMutableTreeNode("Disjoinedness Constraints");
    DefaultAssertionTreeNodeRenderer renderer = 
        new DefaultAssertionTreeNodeRenderer();
    ForeignKeyConstraintTreeModel model = 
        new ForeignKeyConstraintTreeModel(root, fkController, renderer);
    ConstraintsTreeCellRenderer tcr = 
        new ConstraintsTreeCellRenderer(apic, preference);
    
    treFKConstraint.setCellRenderer(tcr);
    treFKConstraint.setModel(model);
    treFKConstraint.setCellEditor(new ConstraintsTreeCellEditor(apic, 
        RDBMSForeignKeyConstraint.RDBMSFOREIGNKEYCONSTRAINT));
    treFKConstraint.setEditable(true);
    treFKConstraint.setInvokesStopCellEditing(true);
    treFKConstraint.setRootVisible(false);
    treFKConstraint.setRowHeight(0);
    pref.registerPreferenceChangedListener(this);
  }

  private void addListener() {

    cmdAdd.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {        
        if (selectedSource != null) {
          addRDBMSForeignKeyConstraint();
        }
        else {
          JOptionPane.showMessageDialog(null, 
              "Please select a data source first.", "ERROR", 
              JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    
    cmdMine.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (selectedSource != null) {
          Thread t = new Thread() {
            @Override
            public void run() {
              ProgressMonitorDialog pmg = new ProgressMonitorDialog(myself, 
                  "Mining Foreign Key Constraints...");
              pmg.show();
              AbstractAssertionMiner miner = 
                  new AbstractAssertionMiner(apic, selectedSource);
              try {
                List<RDBMSForeignKeyConstraint> results = 
                    miner.mineForeignkeyConstraints();
                Iterator<RDBMSForeignKeyConstraint> it = results.iterator();
                while(it.hasNext()){
                  fkController.addAssertion(it.next());
                }
                pmg.stop();
              } 
              catch (Exception e1) {
                e1.printStackTrace();
                pmg.stop();
                JOptionPane.showMessageDialog(null, 
                    "Error during mining!", "ERROR", 
                    JOptionPane.ERROR_MESSAGE);
              }
            }
          };
          t.start();
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
          TreePath[] selection =treFKConstraint.getSelectionPaths();
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
        TreePath[] selection =treFKConstraint.getSelectionPaths();
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
        TreePath[] paths = treFKConstraint.getSelectionPaths();
        if(paths != null) {
          // Do nothing.
        }
      }
    });
    menu.add(validate);
    treFKConstraint.setComponentPopupMenu(menu);
  }

  @Override
  public void colorPeferenceChanged(String preference, Color col) {
    
    DefaultTreeModel model = (DefaultTreeModel) treFKConstraint.getModel();
    model.reload();
  }

  @Override
  public void fontFamilyPreferenceChanged(String preference, String font) {
    
    DefaultTreeModel model = (DefaultTreeModel) treFKConstraint.getModel();
    model.reload();
  }

  @Override
  public void fontSizePreferenceChanged(String preference, int size) {
    
    DefaultTreeModel model = (DefaultTreeModel) treFKConstraint.getModel();
    model.reload();
  }

  @Override
  public void isBoldPreferenceChanged(String preference, Boolean isBold) {
    
    DefaultTreeModel model = (DefaultTreeModel) treFKConstraint.getModel();
    model.reload();
  }

  @Override
  public void shortCutChanged(String preference, String shortcut) {
    
    DefaultTreeModel model = (DefaultTreeModel) treFKConstraint.getModel();
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
      if (o instanceof DefaultAssertionTreeNode) {
        DefaultAssertionTreeNode<RDBMSForeignKeyConstraint> node = 
            (DefaultAssertionTreeNode<RDBMSForeignKeyConstraint>) o;
        RDBMSForeignKeyConstraint con = node.getUserObject();
        fkController.removeAssertion(con);
      }
    }
  }

  private void addRDBMSForeignKeyConstraint() {
    
    ForeignKeyConstraintTreeModel model = 
        (ForeignKeyConstraintTreeModel) treFKConstraint.getModel();
    
    DefaultAssertionTreeNode<RDBMSForeignKeyConstraint> node = 
        new DefaultAssertionTreeNode<RDBMSForeignKeyConstraint>(null);
    MutableTreeNode root = (MutableTreeNode) model.getRoot();
    int index = ((DefaultMutableTreeNode)model.getRoot()).getChildCount();
    root.insert(node, index);
    
    model.nodesWereInserted(root, new int[]{index});
    model.nodeStructureChanged(root);
    treFKConstraint.setVisibleRowCount(index+1);
    TreePath path = treFKConstraint.getPathForRow(index);
    if (path == null) {
      root.remove(index);
      model.nodesWereRemoved(root, new int[] {index}, new Object[]{node});
    }
    treFKConstraint.setSelectionPath(path);
    treFKConstraint.startEditingAtPath(path);
  }

  @Override
  public void datasourceChanged(DataSource oldSource, DataSource newSource) {
    
    this.selectedSource = newSource;
    
    fkController.changeDatasource(oldSource, newSource);
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
    cmdMine = new javax.swing.JButton();
    scrFKConstraint = new javax.swing.JScrollPane();
    treFKConstraint = new javax.swing.JTree();

    setBorder(javax.swing.BorderFactory.createTitledBorder("Foreign Key Constraint"));
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

    cmdMine.setText("Mine");
    cmdMine.setMaximumSize(new java.awt.Dimension(95, 23));
    cmdMine.setMinimumSize(new java.awt.Dimension(95, 23));
    cmdMine.setPreferredSize(new java.awt.Dimension(95, 23));
    pnlButtons.add(cmdMine);

    add(pnlButtons, java.awt.BorderLayout.NORTH);

    scrFKConstraint.setMinimumSize(new java.awt.Dimension(700, 600));
    scrFKConstraint.setPreferredSize(new java.awt.Dimension(700, 600));
    scrFKConstraint.setViewportView(treFKConstraint);

    add(scrFKConstraint, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton cmdAdd;
  private javax.swing.JButton cmdDelete;
  private javax.swing.JButton cmdMine;
  private javax.swing.JPanel pnlButtons;
  private javax.swing.JScrollPane scrFKConstraint;
  private javax.swing.JTree treFKConstraint;
  // End of variables declaration//GEN-END:variables
}
