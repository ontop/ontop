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
import inf.unibz.it.obda.dependencies.controller.RDBMSInclusionDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSInclusionDependency;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.datasource.DatasourceSelectorListener;
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

import org.obda.query.domain.Variable;

/**
 * The tree pane showing all inclusion dependencies associated the selected 
 * data source
 *
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
 */
public class InclusionDependencyTreePane extends JPanel implements 
    MappingManagerPreferenceChangeListener, DatasourceSelectorListener {

  private APIController apic;

  private RDBMSInclusionDependencyController incController;

  private boolean canceled;

  private SQLQueryValidator validator;

  private	Thread	validatorThread;

  private MappingManagerPreferences pref;

  private DataSource selectedSource;
  
  private static InclusionDependencyTreePane instance;
  
  /**
   * Creates a new panel.
   * 
   * @param apic
   *          The API controller object.
   * @param preference
   *          The preference object.
   */
  public InclusionDependencyTreePane(APIController apic, 
      OBDAPreferences preference) {
    
    instance = this;
    this.apic = apic;
    incController = (RDBMSInclusionDependencyController) 
        apic.getController(RDBMSInclusionDependency.class);
    pref = preference.getMappingsPreference();
    
    initComponents();
    addListener();
    addMenu();
    
    DefaultMutableTreeNode root = 
        new DefaultMutableTreeNode("Inclusion Dependencies");
    DefaultAssertionTreeNodeRenderer renderer = 
        new DefaultAssertionTreeNodeRenderer();
    InclusionDependencyTreeModel model = 
        new InclusionDependencyTreeModel(root, incController, renderer);
    DependencyAssertionTreeCellRenderer tcr = 
        new DependencyAssertionTreeCellRenderer(apic, preference);
    
    treInclusionDependency.setCellRenderer(tcr);
    treInclusionDependency.setModel(model);
    treInclusionDependency.setEditable(true);
    treInclusionDependency.setCellEditor(new DependencyTreeCellEditor(apic, 
        RDBMSInclusionDependency.INCLUSIONDEPENDENCY));
    treInclusionDependency.setInvokesStopCellEditing(true);
    treInclusionDependency.setRootVisible(false);
    treInclusionDependency.setRowHeight(0);
    pref.registerPreferenceChangedListener(this);
  }
  
  private void addListener() {
    
    cmdDelete.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] selection = treInclusionDependency.getSelectionPaths();
        if (selection != null) {
          delete(selection);
        }
      }
    });

    cmdAdd.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        addRDBMSInclusionDependency();
      }
    });

    cmdWizard.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] paths = 
            Dependency_SelectMappingPane.gestInstance().getSelection();
        Dependency_SelectMappingPane.gestInstance().createDialog(
            "Create Inclusion Dependency", paths, 
            RDBMSInclusionDependency.INCLUSIONDEPENDENCY);
      }
    });

    cmdMine.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Dependency_SelectMappingPane.gestInstance().showInclusionMiningDialog(
            treInclusionDependency);
      }
    });
  }

  private void addRDBMSInclusionDependency() {

    if (selectedSource == null) {
      JOptionPane.showMessageDialog(null, 
          "Please select a data source.", "ERROR", 
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    InclusionDependencyTreeModel model =
        (InclusionDependencyTreeModel) treInclusionDependency.getModel();
    DefaultAssertionTreeNode<RDBMSInclusionDependency> node = 
        new DefaultAssertionTreeNode<RDBMSInclusionDependency>(null);
    MutableTreeNode root = (MutableTreeNode) model.getRoot();
    int index = ((DefaultMutableTreeNode)model.getRoot()).getChildCount();
    root.insert(node, index);
    
    model.nodesWereInserted(root, new int[]{index});
    model.nodeStructureChanged(root);
    treInclusionDependency.setVisibleRowCount(index+1);
    TreePath path = treInclusionDependency.getPathForRow(index);
    if (path == null) {
      root.remove(index);
      model.nodesWereRemoved(root, new int[] {index}, new Object[]{node});
    }
    treInclusionDependency.setSelectionPath(path);
    treInclusionDependency.startEditingAtPath(path);
  }

  public static InclusionDependencyTreePane getInstance() {
    
    return instance;
  }

  public JTree getInclusionDependencyTree() {
    
    return treInclusionDependency;
  }
  /**
   * adds a popup menu to the tree
   */
  private void addMenu() {
    
    JPopupMenu menu = new JPopupMenu();
    JMenuItem del = new JMenuItem();
    del.setText("Delete");
    del.setToolTipText("Deletes all selected assertions");
    del.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] selection = treInclusionDependency.getSelectionPaths();
        if (selection != null) {
          delete(selection);
        }
      }
    });
    menu.add(del);
    menu.addSeparator();

    JMenuItem validate = new JMenuItem();
    validate.setText("Validate Dependency");
    validate.setToolTipText("Check whether the produced SQL query is valid.");
    validate.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] paths = treInclusionDependency.getSelectionPaths();
        if (paths != null) {
          validateRDBMSInclusionDependencies(paths);
        }
      }
    });
    menu.add(validate);
    treInclusionDependency.setComponentPopupMenu(menu);
  }

  /**
   * Validates whether the data source fulfills the selected assertions.
   * 
   * @param paths 
   *          Selected assertions.
   */
  private void validateRDBMSInclusionDependencies(TreePath[] paths) {

    final ValidateDepenencyDialog dialog = 
        new ValidateDepenencyDialog(treInclusionDependency);
    
    Runnable action = new Runnable() {
      @Override
      public void run() {
        canceled = false;
        final TreePath path[] = treInclusionDependency.getSelectionPaths();
        dialog.setVisible(true);
        if (path == null) {
          return;
        }
        dialog.addText("Validating " + path.length + 
            " inclusion dependencies.\n", dialog.NORMAL);
        for (int i = 0; i < path.length; i++) {
          boolean error = false;
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path[i].getLastPathComponent();
          RDBMSInclusionDependency inc = (RDBMSInclusionDependency)node.getUserObject();
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
                output = "INVALID - Reason: Datasource violates assertion\n";
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

  private void delete(TreePath[] selection) {
    
    for(int i = 0; i < selection.length; i++) {
      TreePath path = selection[i];
      Object o = path.getLastPathComponent();
      if (o instanceof DefaultAssertionTreeNode) {
        DefaultAssertionTreeNode<RDBMSInclusionDependency> node = 
            (DefaultAssertionTreeNode<RDBMSInclusionDependency>) o;
        RDBMSInclusionDependency dep = node.getUserObject();
        incController.removeAssertion(dep);
      }
    }
    treInclusionDependency.setSelectionPath(null);
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

  @Override
  public void colorPeferenceChanged(String preference, Color col) {
    
    DefaultTreeModel model = (DefaultTreeModel)treInclusionDependency.getModel();
    model.reload();
  }

  @Override
  public void fontFamilyPreferenceChanged(String preference, String font) {
    
    DefaultTreeModel model = (DefaultTreeModel)treInclusionDependency.getModel();
    model.reload();
  }

  @Override
  public void fontSizePreferenceChanged(String preference, int size) {
    
    DefaultTreeModel model = (DefaultTreeModel)treInclusionDependency.getModel();
    model.reload();
  }

  @Override
  public void isBoldPreferenceChanged(String preference, Boolean isBold) {
    
    DefaultTreeModel model = (DefaultTreeModel)treInclusionDependency.getModel();
    model.reload();
  }

  @Override
  public void shortCutChanged(String preference, String shortcut) {
    
    DefaultTreeModel model = (DefaultTreeModel)treInclusionDependency.getModel();
    model.reload();
  }

  @Override
  public void datasourceChanged(DataSource oldSource, DataSource newSource) {
    
    this.selectedSource = newSource;
    
    incController.changeDatasource(oldSource, newSource);
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
    cmdValidate = new javax.swing.JButton();
    scrInclusionDependency = new javax.swing.JScrollPane();
    treInclusionDependency = new javax.swing.JTree();

    setBorder(javax.swing.BorderFactory.createTitledBorder("Inclusion Dependency"));
    setMinimumSize(new java.awt.Dimension(70, 70));
    setLayout(new java.awt.BorderLayout(0, 5));

    pnlButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    cmdAdd.setText("Add");
    cmdAdd.setMaximumSize(null);
    cmdAdd.setMinimumSize(new java.awt.Dimension(95, 23));
    cmdAdd.setPreferredSize(new java.awt.Dimension(95, 23));
    pnlButtons.add(cmdAdd);

    cmdDelete.setText("Delete");
    cmdDelete.setMaximumSize(null);
    cmdDelete.setMinimumSize(new java.awt.Dimension(95, 23));
    cmdDelete.setPreferredSize(new java.awt.Dimension(95, 23));
    pnlButtons.add(cmdDelete);

    cmdMine.setText("Mine");
    cmdMine.setMaximumSize(new java.awt.Dimension(95, 23));
    cmdMine.setMinimumSize(new java.awt.Dimension(95, 23));
    cmdMine.setPreferredSize(new java.awt.Dimension(95, 23));
    pnlButtons.add(cmdMine);

    cmdWizard.setText("Wizard...");
    cmdWizard.setMinimumSize(new java.awt.Dimension(95, 23));
    cmdWizard.setPreferredSize(new java.awt.Dimension(95, 23));
    cmdWizard.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cmdWizardActionPerformed(evt);
      }
    });
    pnlButtons.add(cmdWizard);

    cmdValidate.setText("Validate");
    cmdValidate.setToolTipText("validates wether the selected dependencies are satisfied by the database");
    cmdValidate.setMaximumSize(new java.awt.Dimension(95, 23));
    cmdValidate.setMinimumSize(new java.awt.Dimension(95, 23));
    cmdValidate.setPreferredSize(new java.awt.Dimension(95, 23));
    cmdValidate.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cmdValidateActionPerformed(evt);
      }
    });
    pnlButtons.add(cmdValidate);

    add(pnlButtons, java.awt.BorderLayout.NORTH);

    scrInclusionDependency.setMinimumSize(new java.awt.Dimension(700, 600));
    scrInclusionDependency.setPreferredSize(new java.awt.Dimension(700, 600));
    scrInclusionDependency.setViewportView(treInclusionDependency);

    add(scrInclusionDependency, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void cmdWizardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdWizardActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_cmdWizardActionPerformed

  private void cmdValidateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdValidateActionPerformed
    TreePath[] paths = treInclusionDependency.getSelectionPaths();
    if (paths != null){
      validateRDBMSInclusionDependencies(paths);
    }
  }//GEN-LAST:event_cmdValidateActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton cmdAdd;
  private javax.swing.JButton cmdDelete;
  private javax.swing.JButton cmdMine;
  private javax.swing.JButton cmdValidate;
  private javax.swing.JButton cmdWizard;
  private javax.swing.JPanel pnlButtons;
  private javax.swing.JScrollPane scrInclusionDependency;
  private javax.swing.JTree treInclusionDependency;
  // End of variables declaration//GEN-END:variables
}
