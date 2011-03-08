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
import inf.unibz.it.obda.dependencies.controller.RDBMSFunctionalDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSFunctionalDependency;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.datasource.DatasourceSelectorListener;
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

import org.obda.query.domain.Variable;

/**
 * The tree pane showing all functional dependencies associated the selected
 * data source
 *
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
 */
public class FunctionalDepTreePane extends JPanel implements
    MappingManagerPreferenceChangeListener, DatasourceSelectorListener {

  private APIController apic;

  private RDBMSFunctionalDependencyController fdController;
  
  private SQLQueryValidator validator;

  private MappingManagerPreferences pref;
  
  private DataSource selectedSource;

  /**
   * Creates a new panel.
   * 
   * @param apic
   *          The API controller object.
   * @param preference
   *          The preference object.
   */
  public FunctionalDepTreePane(APIController apic, OBDAPreferences preference) {

    this.apic = apic;  
    pref = preference.getMappingsPreference();
    fdController = (RDBMSFunctionalDependencyController) 
        apic.getController(RDBMSFunctionalDependency.class);
    
    initComponents();
    addListener();
    addMenu();
    
    DefaultMutableTreeNode root = 
        new DefaultMutableTreeNode("Functional Dependencies");
    DefaultAssertionTreeNodeRenderer renderer = 
        new DefaultAssertionTreeNodeRenderer();
    FunctionalDependenciesTreeModel model = 
        new FunctionalDependenciesTreeModel(root, fdController, renderer);
    DependencyAssertionTreeCellRenderer tcr = 
        new DependencyAssertionTreeCellRenderer(apic, preference);
    
    treFunctionalDependency.setCellRenderer(tcr);
    treFunctionalDependency.setModel(model);
    treFunctionalDependency.setCellEditor(new DependencyTreeCellEditor(apic, 
        RDBMSFunctionalDependency.FUNCTIONALDEPENDENCY));
    treFunctionalDependency.setEditable(true);
    treFunctionalDependency.setInvokesStopCellEditing(true);
    treFunctionalDependency.setRootVisible(false);
    treFunctionalDependency.setRowHeight(0);
    pref.registerPreferenceChangedListener(this);
  }
  
  private void addListener() {
    
    cmdDelete.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] selection =treFunctionalDependency.getSelectionPaths();
        if(selection != null){
          delete(selection);
        }
      }
    });

    cmdAdd.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        addRDBMSFunctionalDependency();
      }
    });
    
    cmdWizard.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] paths = 
            Dependency_SelectMappingPane.gestInstance().getSelection();
        Dependency_SelectMappingPane.gestInstance().createDialog(
            "Create Functional Dependency", paths, 
            RDBMSFunctionalDependency.FUNCTIONALDEPENDENCY);
      }
    });
    
    cmdMine.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Dependency_SelectMappingPane.gestInstance()
          .showFunctionalDependencyMiningDialog(treFunctionalDependency);
      }
    });
  }

  private void addRDBMSFunctionalDependency() {

    FunctionalDependenciesTreeModel model =
        (FunctionalDependenciesTreeModel) treFunctionalDependency.getModel();
    
    DefaultAssertionTreeNode<RDBMSFunctionalDependency> node = 
        new DefaultAssertionTreeNode<RDBMSFunctionalDependency>(null);
    MutableTreeNode root = (MutableTreeNode) model.getRoot();
    int index = ((DefaultMutableTreeNode)model.getRoot()).getChildCount();
    root.insert(node, index);
    
    model.nodesWereInserted(root, new int[]{index});
    model.nodeStructureChanged(root);
    treFunctionalDependency.setVisibleRowCount(index+1);
    TreePath path = treFunctionalDependency.getPathForRow(index);
    if (path == null) {
      root.remove(index);
      model.nodesWereRemoved(root, new int[] {index}, new Object[]{node});
    }
    treFunctionalDependency.setSelectionPath(path);
    treFunctionalDependency.startEditingAtPath(path);
  }

  /**
   * adds a popup menu to the tree
   */
  private void addMenu() {
    
    JPopupMenu menu = new JPopupMenu();
    JMenuItem del = new JMenuItem();
    del.setText("Delete");
    del.setToolTipText("Deletes all selected Assertions");
    del.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] selection =treFunctionalDependency.getSelectionPaths();
        if (selection != null) {
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
    validate.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] paths = treFunctionalDependency.getSelectionPaths();
        if (paths != null) {
          validateRDBMSFunctionalDependencies(paths);
        }
      }
    });
    menu.add(validate);
    treFunctionalDependency.setComponentPopupMenu(menu);
  }

  /**
   * deletes the selected assertions
   * @param selection current selection in the tree
   */
  private void delete(TreePath[] selection) {
    
    for (int i = 0; i < selection.length; i++) {
      TreePath path = selection[i];
      Object o = path.getLastPathComponent();
      if (o instanceof DefaultAssertionTreeNode) {
        DefaultAssertionTreeNode<RDBMSFunctionalDependency> node = 
            (DefaultAssertionTreeNode<RDBMSFunctionalDependency>) o;
        RDBMSFunctionalDependency dep = node.getUserObject();
        fdController.removeAssertion(dep);
      }
    }
  }

  /**
   * validates whether the data source fulfills the assertions
   * @param paths assertions to validate
   */
  private void validateRDBMSFunctionalDependencies(TreePath[] paths) {

    /*
     * Sample of an sql query produced by this method:
		 * SELECT a1.nid, a1.value FROM (
     *   SELECT id as nid, 2 as value, name from name) as a1
		 *   WHERE ROW(a1.nid, a1.value) NOT IN (
		 *      SELECT nid, value FROM (
		 *         SELECT id as nid, 2 as value, name from name) a2)
     */
    for (int i = 0; i < paths.length; i++) {
      Object o =  paths[i].getLastPathComponent();
      if (!o.equals(treFunctionalDependency.getModel().getRoot())) {
        DefaultAssertionTreeNode<RDBMSFunctionalDependency> node = 
            (DefaultAssertionTreeNode<RDBMSFunctionalDependency>) o;
        RDBMSFunctionalDependency dep = node.getUserObject();
        RDBMSSQLQuery query1 = (RDBMSSQLQuery) dep.getSourceQueryOne();
        RDBMSSQLQuery query2 = (RDBMSSQLQuery) dep.getSourceQueryOne();
        List<Variable> vars1 = dep.getVariablesOfQueryOne();
        List<Variable> vars2 = dep.getVariablesOfQueryTwo();

        String aux1 = "";
        Iterator<Variable> it1 = vars1.iterator();
        while (it1.hasNext()) {
          if (aux1.length() > 0) {
            aux1 = aux1 + ",";
          }
          aux1= aux1 + "table1." + it1.next().getName();
        }
        String aux2 = "";
        Iterator<Variable> it2 = vars2.iterator();
        while (it2.hasNext()) {
          if (aux2.length() > 0) {
            aux2 = aux2 + ",";
          }
          aux2= aux2 + "table1." + it2.next().getName();
        }
        String query = String.format("SELECT %s FROM (%s) table1 WHERE " +
            "ROW(%s) NOT IN (SELECT %s FROM (%s) table2)",
            aux2, query2.toString(), aux2, aux1, query1.toString());

        validator = 
            new SQLQueryValidator(selectedSource, new RDBMSSQLQuery(query));
        if (validator.validate()) {
          JOptionPane.showMessageDialog(this, 
              "The Assertion produces a valid SQL query.", 
              "Validation successful", 
              JOptionPane.INFORMATION_MESSAGE);
        }
        else {
          JOptionPane.showMessageDialog(this, 
              "The Assertion produces a non valid SQL query. " +
              "Please check it again.", "ERROR", 
              JOptionPane.ERROR_MESSAGE);
          validator.getReason().printStackTrace();
        }
      }
    }
  }

  @Override
  public void colorPeferenceChanged(String preference, Color col) {

    DefaultTreeModel model = (DefaultTreeModel)treFunctionalDependency.getModel();
    model.reload();
  }

  @Override
  public void fontFamilyPreferenceChanged(String preference, String font) {
    
    DefaultTreeModel model = (DefaultTreeModel)treFunctionalDependency.getModel();
    model.reload();
  }

  @Override
  public void fontSizePreferenceChanged(String preference, int size) {
    
    DefaultTreeModel model = (DefaultTreeModel)treFunctionalDependency.getModel();
    model.reload();
  }

  @Override
  public void isBoldPreferenceChanged(String preference, Boolean isBold) {
    
    DefaultTreeModel model = (DefaultTreeModel)treFunctionalDependency.getModel();
    model.reload();
  }

  @Override
  public void shortCutChanged(String preference, String shortcut) {
    
    DefaultTreeModel model = (DefaultTreeModel)treFunctionalDependency.getModel();
    model.reload();
  }

  @Override
  public void datasourceChanged(DataSource oldSource, DataSource newSource)
  {
    this.selectedSource = newSource;
    
    fdController.changeDatasource(oldSource, newSource);
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
    cmdWizard = new javax.swing.JButton();
    scrFunctionalDependency = new javax.swing.JScrollPane();
    treFunctionalDependency = new javax.swing.JTree();

    setBorder(javax.swing.BorderFactory.createTitledBorder("Functional Dependency"));
    setMinimumSize(new java.awt.Dimension(70, 70));
    setPreferredSize(new java.awt.Dimension(700, 700));
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

    cmdWizard.setText("Wizard...");
    cmdWizard.setMaximumSize(new java.awt.Dimension(95, 23));
    cmdWizard.setMinimumSize(new java.awt.Dimension(95, 23));
    cmdWizard.setPreferredSize(new java.awt.Dimension(95, 23));
    pnlButtons.add(cmdWizard);

    add(pnlButtons, java.awt.BorderLayout.NORTH);

    scrFunctionalDependency.setMinimumSize(new java.awt.Dimension(700, 600));
    scrFunctionalDependency.setPreferredSize(new java.awt.Dimension(700, 600));
    scrFunctionalDependency.setViewportView(treFunctionalDependency);

    add(scrFunctionalDependency, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton cmdAdd;
  private javax.swing.JButton cmdDelete;
  private javax.swing.JButton cmdMine;
  private javax.swing.JButton cmdWizard;
  private javax.swing.JPanel pnlButtons;
  private javax.swing.JScrollPane scrFunctionalDependency;
  private javax.swing.JTree treFunctionalDependency;
  // End of variables declaration//GEN-END:variables
}
