/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DisjoinednessAssertionTreePane.java
 *
 * Created on Aug 12, 2009, 10:14:26 AM
 */
package inf.unibz.it.obda.gui.swing.dependencies.panel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.dependencies.controller.RDBMSDisjointnessDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.datasource.DatasourceSelectorListener;
import inf.unibz.it.obda.gui.swing.datasource.panels.ResultSetTableModel;
import inf.unibz.it.obda.gui.swing.dependencies.treemodel.DependencyAssertionTreeCellRenderer;
import inf.unibz.it.obda.gui.swing.dependencies.treemodel.DependencyTreeCellEditor;
import inf.unibz.it.obda.gui.swing.dependencies.treemodel.DisjoinednessAssertionTreeModel;
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
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.obda.query.domain.Variable;

/**
 * The Tree Pane showing the disjointness dependency assertion of the
 * selected data source
 *
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
 */
public class DisjoinednessAssertionTreePane extends JPanel implements 
    MappingManagerPreferenceChangeListener, DatasourceSelectorListener {

  private APIController apic;

  private RDBMSDisjointnessDependencyController disController;

  private boolean canceled;

  private SQLQueryValidator validator;

  private	Thread validatorThread;

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
  public DisjoinednessAssertionTreePane(APIController apic, 
      OBDAPreferences preference) {
    
    this.apic = apic;    
    pref = preference.getMappingsPreference();
    disController =(RDBMSDisjointnessDependencyController) 
        apic.getController(RDBMSDisjointnessDependency.class);
    
    initComponents();
    addListener();
    addMenu();
    
    DefaultMutableTreeNode root = 
        new DefaultMutableTreeNode("Disjointness Constraints");
    DefaultAssertionTreeNodeRenderer renderer = 
        new DefaultAssertionTreeNodeRenderer();
    DisjoinednessAssertionTreeModel model = 
        new DisjoinednessAssertionTreeModel(root, disController, renderer);
    DependencyAssertionTreeCellRenderer tcr = 
        new DependencyAssertionTreeCellRenderer(apic, preference);
    
    treDisjointnessDependency.setCellRenderer(tcr);
    treDisjointnessDependency.setModel(model);
    treDisjointnessDependency.setCellEditor(new DependencyTreeCellEditor(apic, 
        RDBMSDisjointnessDependency.DISJOINEDNESSASSERTION));
    treDisjointnessDependency.setEditable(true);
    treDisjointnessDependency.setInvokesStopCellEditing(true);
    treDisjointnessDependency.setRootVisible(false);
    treDisjointnessDependency.setRowHeight(0);
    pref.registerPreferenceChangedListener(this);
  }
  
  private void addListener() {  
    
    cmdDelete.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] selection =treDisjointnessDependency.getSelectionPaths();
        if(selection != null){
          delete(selection);
        }
      }
    });

    cmdAdd.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        addRDBMSDisjointnessDependency();
      }
    });

    cmdWizard.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] paths = 
            Dependency_SelectMappingPane.gestInstance().getSelection();
        Dependency_SelectMappingPane.gestInstance().createDialog2(
            "Create Disjointness Dependency", paths, 
            RDBMSDisjointnessDependency.DISJOINEDNESSASSERTION);
      }
    });
    
    cmdValidate.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] paths = treDisjointnessDependency.getSelectionPaths();
        if (paths != null) {
          validateRDBMSDisjointnessDependencies(paths);
        }
      }
    });
  }

  private void addRDBMSDisjointnessDependency() {
    
    DisjoinednessAssertionTreeModel model =
        (DisjoinednessAssertionTreeModel) treDisjointnessDependency.getModel();
    
    DefaultAssertionTreeNode<RDBMSDisjointnessDependency> node = 
        new DefaultAssertionTreeNode<RDBMSDisjointnessDependency>(null);
    MutableTreeNode root = (MutableTreeNode) model.getRoot();
    int index = ((DefaultMutableTreeNode)model.getRoot()).getChildCount();
    root.insert(node, index);
    
    model.nodesWereInserted(root, new int[]{index});
    model.nodeStructureChanged(root);
    treDisjointnessDependency.setVisibleRowCount(index+1);
    TreePath path = treDisjointnessDependency.getPathForRow(index);
    if (path == null) {
      root.remove(index);
      model.nodesWereRemoved(root, new int[] {index}, new Object[]{node});
    }
    treDisjointnessDependency.setSelectionPath(path);
    treDisjointnessDependency.startEditingAtPath(path);
  }
  
  /**
   * adds popup menu to the tree pane
   */
  private void addMenu() {
    
    JPopupMenu menu = new JPopupMenu();
    JMenuItem del = new JMenuItem();
    del.setText("Delete");
    del.setToolTipText("Deletes all selected assertions");
    del.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] selection = treDisjointnessDependency.getSelectionPaths();
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
        TreePath[] paths = treDisjointnessDependency.getSelectionPaths();
        if (paths != null) {
          validateRDBMSDisjointnessDependencies(paths);
        }
      }
    });
    menu.add(validate);
    treDisjointnessDependency.setComponentPopupMenu(menu);
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
        DefaultAssertionTreeNode<RDBMSDisjointnessDependency> node = 
            (DefaultAssertionTreeNode<RDBMSDisjointnessDependency>) o;
        RDBMSDisjointnessDependency dep = node.getUserObject();
        disController.removeAssertion(dep);
      }
    }
  }
  
  private boolean validateAssertion(SQLQueryValidator v) {

    ResultSetTableModel model = (ResultSetTableModel) v.execute();
    if (model == null) {
      v.getReason().printStackTrace();
      return false;
    }
    else {
      if (model.getRowCount() == 0) {
        return true;
      }
      else {
        return false;
      }
    }
  }

  /**
   * validates whether the data source fulfills the selected assertions
   * @param paths selected assertions
   */
  private void validateRDBMSDisjointnessDependencies(TreePath[] paths) {

    final ValidateDepenencyDialog dialog = 
        new ValidateDepenencyDialog(treDisjointnessDependency);
    Runnable action = new Runnable() {
      @Override
      public void run() {
        dialog.setVisible(true);
        
        canceled = false;
        final TreePath path[] = treDisjointnessDependency.getSelectionPaths();
        if (path == null)
          return;

        dialog.addText("Validating " + path.length + 
            " disjointness dependencies.\n", dialog.NORMAL);
        
        for (int i = 0; i < path.length; i++) {
          boolean error = false;
          DefaultMutableTreeNode node = 
              (DefaultMutableTreeNode) path[i].getLastPathComponent();
          RDBMSDisjointnessDependency inc = 
              (RDBMSDisjointnessDependency)node.getUserObject();
          RDBMSSQLQuery query1 = (RDBMSSQLQuery) inc.getSourceQueryOne();
          RDBMSSQLQuery query2 = (RDBMSSQLQuery) inc.getSourceQueryTwo();
          List<Variable> vars1 = inc.getVariablesOfQueryOne();
          List<Variable> vars2 = inc.getVariablesOfQueryTwo();
          dialog.addText(inc.toString() +"... ", dialog.NORMAL);
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
            aux2= aux2 + "table2." + it2.next().getName();
          }
          String query = String.format("SELECT %s FROM (%s) table1 WHERE " +
              "ROW(%s) NOT IN (SELECT %s FROM (%s) table2)",
              aux1, query1.toString(), aux1, aux2, query2.toString());

          validator = 
              new SQLQueryValidator(selectedSource, new RDBMSSQLQuery(query));
          
          if (canceled)
            return;
          
          if (!error) {
            if (validateAssertion(validator)) {
              String output = "VALID \n";
              dialog.addText(output, dialog.VALID);
            }
            else {
              Exception e = validator.getReason();
              String output = "";
              if (e == null) {
                output = "INVALID - Reason: Datasource violates assertion\n";;
              }
              else {
                output = "INVALID - Reason: " + e.getMessage() + "\n";
              }
              dialog.addText(output, dialog.ERROR);
            }
            validator.dispose();
          }
        }
      }
    };
    validatorThread = new Thread(action);
    validatorThread.start();

    Thread cancelThread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (!dialog.closed) {
          try {
            Thread.currentThread();
            Thread.sleep(100);
          } 
          catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        if (validatorThread.isAlive()) {
          try {
            Thread.currentThread();
            Thread.sleep(250);
          } 
          catch (InterruptedException e) {
            e.printStackTrace();
          }
          try {
            canceled = true;
            validator.cancelValidation();
          } 
          catch (SQLException e) {
            e.printStackTrace();
          }
        }
      }
    });
    cancelThread.start();
  }


  @Override
  public void colorPeferenceChanged(String preference, Color col) {
    
    DefaultTreeModel model = (DefaultTreeModel)treDisjointnessDependency.getModel();
    model.reload();
  }
  
  @Override
  public void fontFamilyPreferenceChanged(String preference, String font) {
    
    DefaultTreeModel model = (DefaultTreeModel)treDisjointnessDependency.getModel();
    model.reload();

  }
  
  @Override
  public void fontSizePreferenceChanged(String preference, int size) {
    
    DefaultTreeModel model = (DefaultTreeModel)treDisjointnessDependency.getModel();
    model.reload();
  }
  
  @Override
  public void isBoldPreferenceChanged(String preference, Boolean isBold) {
    
    DefaultTreeModel model = (DefaultTreeModel)treDisjointnessDependency.getModel();
    model.reload();
  }
  
  @Override
  public void shortCutChanged(String preference, String shortcut) {
    
    DefaultTreeModel model = (DefaultTreeModel)treDisjointnessDependency.getModel();
    model.reload();
  }

  @Override
  public void datasourceChanged(DataSource oldSource, DataSource newSource) {
    
    this.selectedSource = newSource;
    
    disController.changeDatasource(oldSource, newSource);
  }
  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    scrDisjointnessDependency = new javax.swing.JScrollPane();
    treDisjointnessDependency = new javax.swing.JTree();
    pnlButtons = new javax.swing.JPanel();
    cmdAdd = new javax.swing.JButton();
    cmdDelete = new javax.swing.JButton();
    cmdMine = new javax.swing.JButton();
    cmdWizard = new javax.swing.JButton();
    cmdValidate = new javax.swing.JButton();

    setBorder(javax.swing.BorderFactory.createTitledBorder("Disjointness Dependency"));
    setMinimumSize(new java.awt.Dimension(70, 70));
    setPreferredSize(new java.awt.Dimension(700, 700));
    setLayout(new java.awt.BorderLayout(0, 5));

    scrDisjointnessDependency.setMinimumSize(new java.awt.Dimension(700, 600));
    scrDisjointnessDependency.setPreferredSize(new java.awt.Dimension(700, 600));
    scrDisjointnessDependency.setViewportView(treDisjointnessDependency);

    add(scrDisjointnessDependency, java.awt.BorderLayout.CENTER);

    pnlButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    cmdAdd.setText("Add\n");
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
    cmdMine.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cmdMineActionPerformed(evt);
      }
    });
    pnlButtons.add(cmdMine);

    cmdWizard.setText("Wizard... ");
    cmdWizard.setMaximumSize(new java.awt.Dimension(95, 23));
    cmdWizard.setMinimumSize(new java.awt.Dimension(95, 23));
    cmdWizard.setPreferredSize(new java.awt.Dimension(95, 23));
    pnlButtons.add(cmdWizard);

    cmdValidate.setText("Validate");
    cmdValidate.setMaximumSize(new java.awt.Dimension(95, 23));
    cmdValidate.setMinimumSize(new java.awt.Dimension(95, 23));
    cmdValidate.setPreferredSize(new java.awt.Dimension(95, 23));
    pnlButtons.add(cmdValidate);

    add(pnlButtons, java.awt.BorderLayout.NORTH);
  }// </editor-fold>//GEN-END:initComponents

  private void cmdMineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMineActionPerformed
    Dependency_SelectMappingPane.gestInstance().startDisjointnessDependencyMining(treDisjointnessDependency);
  }//GEN-LAST:event_cmdMineActionPerformed
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton cmdAdd;
  private javax.swing.JButton cmdDelete;
  private javax.swing.JButton cmdMine;
  private javax.swing.JButton cmdValidate;
  private javax.swing.JButton cmdWizard;
  private javax.swing.JPanel pnlButtons;
  private javax.swing.JScrollPane scrDisjointnessDependency;
  private javax.swing.JTree treDisjointnessDependency;
  // End of variables declaration//GEN-END:variables
}
