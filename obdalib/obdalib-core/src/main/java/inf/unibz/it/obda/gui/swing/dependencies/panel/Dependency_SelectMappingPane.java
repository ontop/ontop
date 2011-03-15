/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Dependency_SelectMappingPane.java
 *
 * Created on Aug 12, 2009, 9:42:43 AM
 */
package inf.unibz.it.obda.gui.swing.dependencies.panel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.dependencies.controller.RDBMSDisjointnessDependencyController;
import inf.unibz.it.obda.dependencies.controller.RDBMSFunctionalDependencyController;
import inf.unibz.it.obda.dependencies.controller.RDBMSInclusionDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSFunctionalDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSInclusionDependency;
import inf.unibz.it.obda.dependencies.miner.IMiner;
import inf.unibz.it.obda.dependencies.miner.RDBMSDisjointnessDependencyMiner;
import inf.unibz.it.obda.dependencies.miner.RDBMSDisjointnessDependencyMiner.DisjointnessMiningResult;
import inf.unibz.it.obda.dependencies.miner.RDBMSFunctionalDependencyFromDBSchemaMiner;
import inf.unibz.it.obda.dependencies.miner.RDBMSFunctionalDependencyMiner;
import inf.unibz.it.obda.dependencies.miner.RDBMSFunctionalDependencyMiner.FunctionalDependencyMiningResult;
import inf.unibz.it.obda.dependencies.miner.RDBMSInclusionDependencyFromDBSchemaMiner;
import inf.unibz.it.obda.dependencies.miner.RDBMSInclusionDependencyMiner;
import inf.unibz.it.obda.dependencies.miner.RDBMSInclusionDependencyMiner.InclusionMiningResult;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.datasource.DatasourceSelectorListener;
import inf.unibz.it.obda.gui.swing.dependencies.treemodel.DisjoinednessAssertionTreeModel;
import inf.unibz.it.obda.gui.swing.dependencies.treemodel.FunctionalDependenciesTreeModel;
import inf.unibz.it.obda.gui.swing.dependencies.treemodel.InclusionDependencyTreeModel;
import inf.unibz.it.obda.gui.swing.mapping.panel.MappingRenderer;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingNode;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingTreeModel;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingTreeSelectionModel;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.tree.TreePath;

import org.obda.query.domain.Term;
import org.obda.query.domain.Variable;
import org.obda.query.domain.imp.FunctionalTermImpl;
import org.obda.query.domain.imp.VariableImpl;

/**
 * The panel in the dependency manager showing all current mapping.
 *
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
 */
public class Dependency_SelectMappingPane extends javax.swing.JPanel implements
  DatasourceSelectorListener {

  /**
   * the current of it self
   */
  private static Dependency_SelectMappingPane instance;
  /**
   * the API controller
   */
  private APIController apic;
  /**
   * the mapping controller
   */
  private MappingController  mapc;
  /**
   * the data source controller
   */
  private DatasourcesController dsc;
  /**
   * the current inclusion dependency miner
   */
  private IMiner miner;
  /**
   * the current progress monitor showing the progress during the inclusion dependency mining
   */
  private ProgressMonitorDialog dialog;
  /**
   * A list of assertion added during the mining. Only used to
   * remove those assertion in case the mining is canceled.
   */
  private HashSet<RDBMSInclusionDependency> addedInclusionDependencies;
  /**
   * boolean field indicating whether the last mining was canceld
   */
  private boolean miningCanceled;

  private OBDAPreferences preference;
  
  private DataSource selectedSource;

  /**
   * A list of assertion added during the mining. Only used to
   * remove those assertion in case the mining is canceled.
   */
  private HashSet<RDBMSDisjointnessDependency> addedDisjointnessDependencies;

  private HashSet<RDBMSFunctionalDependency> addedFunctionalDependencies;

  private JTree assertionTree;

  /**
   * Creates a new panel.
   * 
   * @param apic
   *          The API controller object.
   * @param preference
   *          The preference object.
   */
  public Dependency_SelectMappingPane(APIController apic, MappingController mapc,
      DatasourcesController dsc, OBDAPreferences preference) {
    
    this.apic = apic;
    this.mapc = mapc;
    this.dsc = dsc;
    this.preference = preference;
    instance = this;
    initComponents();
    adjustTree();
  }

  /**
   * returns the current instance of the Pane
   * @return the current instance
   */
  public static Dependency_SelectMappingPane gestInstance() {
    
    return instance;
  }

  /**
   * Create the dialog for the Add assertion wizard
   *
   * @param title title of the dialog
   * @param mappings the two selected mappings
   * @param assertion the name of the assertion to create
   */
  public void createDialog(String title, TreePath[] mappings, String assertion) {

    if (mappings == null || mappings.length != 2) {
      JOptionPane.showMessageDialog(null, 
          "Please select two Mappings.", "ERROR",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    MappingNode node1 = (MappingNode) mappings[0].getLastPathComponent();
    MappingNode node2 = (MappingNode) mappings[1].getLastPathComponent();
    String nodeString1 = node1.getUserObject().toString();
    String nodeString2 = node2.getUserObject().toString();
    CreateDependencyDialog dialog = new CreateDependencyDialog(
        new JFrame(), true, apic, nodeString1, nodeString2, assertion);
    dialog.setTitle(title);
    dialog.setLocation(250, 400);
    dialog.setVisible(true);
  }

  public void createDialog2(String title, TreePath[] mappings, String assertion) {

    if (mappings == null || mappings.length < 2) {
      JOptionPane.showMessageDialog(null, 
          "Please select at least two Mappings.", "ERROR",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    
    Vector<MappingNode> aux = new Vector<MappingNode>();
    for (int i = 0; i < mappings.length; i++) {
      MappingNode node = (MappingNode) mappings[i].getLastPathComponent();
      aux.add(node);
    }
    CreateDependencyDialog2 dialog = new CreateDependencyDialog2(new JFrame(), true, apic, aux, assertion);
    dialog.setTitle(title);
    dialog.setLocation(250, 400);
    dialog.setVisible(true);
  }

  /**
   * Returns the current selection in tree pane
   * @return current selection
   */
  public TreePath[] getSelection() {
    
    return treMapping.getSelectionPaths();
  }

  /**
   * Interrupts all mining threads and undoes all changes
   */
  public void cancelMining() {
    miningCanceled = true;
    dialog.stop();
    miner.cancelMining();
    //    	undoChanges();
  }

  /**
   * Undo all changes done by a cancel mining session
   */
  private void undoChanges() {
    
    if (addedInclusionDependencies != null) {
      RDBMSInclusionDependencyController incCon = 
          (RDBMSInclusionDependencyController) 
          apic.getController(RDBMSInclusionDependency.class);
      Iterator<RDBMSInclusionDependency> it = addedInclusionDependencies.iterator();
      while (it.hasNext()) {
        incCon.removeAssertion(it.next());
      }
      addedInclusionDependencies = null;
    }

    if (addedDisjointnessDependencies != null) {
      RDBMSDisjointnessDependencyController disCon = 
          (RDBMSDisjointnessDependencyController) 
          apic.getController(RDBMSDisjointnessDependency.class);
      Iterator<RDBMSDisjointnessDependency> it = addedDisjointnessDependencies.iterator();
      while (it.hasNext()) {
        disCon.removeAssertion(it.next());
      }
      addedDisjointnessDependencies = null;
    }
    
    if (addedFunctionalDependencies != null) {
      RDBMSFunctionalDependencyController funCon = 
          (RDBMSFunctionalDependencyController) 
          apic.getController(RDBMSFunctionalDependency.class);
      Iterator<RDBMSFunctionalDependency> it = addedFunctionalDependencies.iterator();
      while (it.hasNext()) {
        funCon.removeAssertion(it.next());
      }
      addedFunctionalDependencies = null;
    }
  }

  public void showInclusionMiningDialog(JTree tree) {
    
    assertionTree = tree;
    addedInclusionDependencies = new HashSet<RDBMSInclusionDependency>();
    
    if (selectedSource == null) {
      JOptionPane.showMessageDialog(null, 
          "Please select a data source.", "ERROR", 
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    MiningDialog dialog = new MiningDialog(RDBMSInclusionDependency.INCLUSIONDEPENDENCY);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setLocation(250, 400);
    dialog.setVisible(true);
  }

  public void showFunctionalDependencyMiningDialog(JTree tree) {
    
    assertionTree = tree;
    addedFunctionalDependencies = new HashSet<RDBMSFunctionalDependency>();

    if (selectedSource == null) {
      JOptionPane.showMessageDialog(null, 
          "Please select a data source.", "ERROR", 
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    MiningDialog dialog = new MiningDialog(RDBMSFunctionalDependency.FUNCTIONALDEPENDENCY);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setLocation(250, 400);
    dialog.setVisible(true);
  }

  public void startDisjointnessDependencyMining(JTree tree) {
    
    assertionTree = tree;
    miningCanceled = false;
    addedDisjointnessDependencies = new HashSet<RDBMSDisjointnessDependency>();
    Thread t = new Thread() {
      @Override
      public void run() {
        mineDisjointnessDependencies();
      }
    };
    t.start();
  }

  private void startInclusionMining(boolean useData, boolean useDBSchema) {
    
    miningCanceled = false;
    if (useData) {
      Thread t = new Thread(){
        @Override
        public void run(){
          mineInclusionDependencies();
        }
      };
      t.start();
    }

    if (useDBSchema) {
      Thread t = new Thread() {
        @Override
        public void run() {
          mineInclusionDependencyFromDBSchema();
        }
      };
      t.start();
    }
  }

  private void startfunctionalDependencyMining(boolean useData, 
      boolean useDBSchema) {
    
    miningCanceled = false;
    if (useData) {
      Thread t = new Thread() {
        @Override
        public void run() {
          mineFunctionalDependencies();
        }
      };
      t.start();
    }

    if (useDBSchema) {
      Thread t = new Thread() {
        @Override
        public void run() {
          mineFunctionalDependenciesFromDBSchema();
        }
      };
      t.start();
    }
  }

  /**
   * starts a new mining session
   */
  private void mineDisjointnessDependencies() {

    dialog = new ProgressMonitorDialog(instance);
    dialog.show();
    CountDownLatch lat = new CountDownLatch(1);
    miner = new RDBMSDisjointnessDependencyMiner(apic, lat);
    try {
      miner.startMining();
      lat.await();
    } catch (InterruptedException e1) {
      throw new RuntimeException(e1);
    }
    if(miner.hasErrorOccurred()){
      throw new RuntimeException(miner.getException());
    }
    HashSet<DisjointnessMiningResult> results = ((RDBMSDisjointnessDependencyMiner)miner).getFoundInclusionDependencies();
    URI dsUri = selectedSource.getSourceID();
    RDBMSDisjointnessDependencyController disCon = (RDBMSDisjointnessDependencyController) apic.getController(RDBMSDisjointnessDependency.class);
    Iterator<DisjointnessMiningResult> it = results.iterator();
    addedInclusionDependencies = new HashSet<RDBMSInclusionDependency>();
    while(it.hasNext() /*&& !miningCanceled*/){
      DisjointnessMiningResult r = it.next();
      Term t1 = r.getFirstElement();
      Term t2 = r.getSecondElement();
      List<Variable> termsOfT1 = null;
      List<Variable> termsOfT2 = null;
      if(t1 instanceof VariableImpl && t2 instanceof VariableImpl){
        termsOfT1 = new Vector<Variable>();
        termsOfT2 = new Vector<Variable>();
        termsOfT1.add((VariableImpl)t1);
        termsOfT2.add((VariableImpl)t2);
      }else{
        try {
          throw new Exception("Incompatible QueryTerms!");
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      }
      RDBMSDisjointnessDependency dis = new RDBMSDisjointnessDependency(dsUri, r.getMappingIdOfFirstMapping(), r.getMappingIdOfSecondMapping(),
          r.getFirstMappingElement(), r.geSeecondMappingElement(), termsOfT1, termsOfT2);

      if(disCon.insertAssertion(dis)){
        addedDisjointnessDependencies.add(dis);
      }
    }
    //		if(!miningCanceled){
    addRDBMSDisjointnessDependenciesToTree(addedDisjointnessDependencies);
    //		}
    dialog.stop();
  }

  /**
   * starts a new mining session
   */
  private void mineInclusionDependencies(){

    dialog = new ProgressMonitorDialog(instance);
    dialog.show();
    CountDownLatch lat = new CountDownLatch(2);
    miner = new RDBMSInclusionDependencyMiner(apic, lat);
    try {
      miner.startMining();
      lat.await();
    } catch (InterruptedException e1) {
      throw new RuntimeException(e1);
    }
    if(miner.hasErrorOccurred()){
      throw new RuntimeException(miner.getException());
    }
    HashSet<InclusionMiningResult> results = ((RDBMSInclusionDependencyMiner)miner).getFoundInclusionDependencies();
    URI dsUri = selectedSource.getSourceID();
    RDBMSInclusionDependencyController incCon = (RDBMSInclusionDependencyController) apic.getController(RDBMSInclusionDependency.class);
    Iterator<InclusionMiningResult> it = results.iterator();
    addedInclusionDependencies = new HashSet<RDBMSInclusionDependency>();
    while(it.hasNext() && !miningCanceled){
      InclusionMiningResult r = it.next();
      Term t1 = r.getFirstElement();
      Term t2 = r.getSecondElement();
      List<Variable> termsOfT1 = null;
      List<Variable> termsOfT2 = null;
      if(t1 instanceof VariableImpl && t2 instanceof VariableImpl){
        termsOfT1 = new Vector<Variable>();
        termsOfT2 = new Vector<Variable>();
        termsOfT1.add((VariableImpl)t1);
        termsOfT2.add((VariableImpl)t2);
      }else{
        try {
          throw new Exception("Incompatible QueryTerms!");
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      }
      RDBMSInclusionDependency inc = new RDBMSInclusionDependency(dsUri, r.getMappingIdOfFirstMapping(), r.getMappingIdOfSecondMapping(),
          r.getFirstMappingElement(), r.geSeecondMappingElement(), termsOfT1, termsOfT2);

      if(incCon.insertAssertion(inc)){
        addedInclusionDependencies.add(inc);
      }
    }
    if(!miningCanceled){
      addRDBMSInclusionDependenciesToTree(addedInclusionDependencies);
    }
    dialog.stop();
  }

  private void mineFunctionalDependencies(){

    dialog = new ProgressMonitorDialog(instance);
    dialog.show();
    CountDownLatch lat = new CountDownLatch(1);
    miner = new RDBMSFunctionalDependencyMiner(apic, lat);
    try {
      miner.startMining();
      lat.await();
    } catch (InterruptedException e1) {
      throw new RuntimeException(e1);
    }
    if(miner.hasErrorOccurred()){
      throw new RuntimeException(miner.getException());
    }
    HashSet<FunctionalDependencyMiningResult> results = ((RDBMSFunctionalDependencyMiner)miner).getFoundInclusionDependencies();
    URI dsUri = selectedSource.getSourceID();
    RDBMSFunctionalDependencyController incCon = (RDBMSFunctionalDependencyController) apic.getController(RDBMSFunctionalDependency.class);
    Iterator<FunctionalDependencyMiningResult> it = results.iterator();
    addedInclusionDependencies = new HashSet<RDBMSInclusionDependency>();
    while(it.hasNext() && !miningCanceled){
      FunctionalDependencyMiningResult r = it.next();
      Set<Term> dependees = r.getDependee();
      List<Variable> candidate = new ArrayList<Variable>();
      FunctionalTermImpl ft = r.getDependent();
      List<Term> list = ft.getTerms();
      Iterator<Term> l_it = list.iterator();
      HashSet<String> candidateName = new HashSet<String>();
      while(l_it.hasNext()){
        Term t = l_it.next();
        if (t instanceof VariableImpl) {
          candidate.add((Variable)t);
          candidateName.add(t.getName());
        }
      }
      Vector<Variable> aux = new Vector<Variable>();
      Iterator<Term> s_it = dependees.iterator();
      HashSet<String> depNames = new HashSet<String>();
      while(s_it.hasNext()){
        Term term = s_it.next();
        if(term instanceof FunctionalTermImpl){
          FunctionalTermImpl f = (FunctionalTermImpl) term;
          Iterator<Term> f_it = f.getTerms().iterator();
          while(f_it.hasNext()){
            Term t = f_it.next();
            if (t instanceof VariableImpl) {
              if(!candidateName.contains(t.getName()) &&depNames.add(t.getName())){
                aux.add((Variable)t);
              }
            }
          }
        }else if (term instanceof VariableImpl) {
          if(!candidateName.contains(term.getName()) && depNames.add(term.getName())){
            aux.add((Variable)term);
          }

        }
      }
      String id = r.getMappingId();
      RDBMSSQLQuery query = r.getSourceQuery();
      if(!aux.isEmpty() && !candidate.isEmpty()){
        RDBMSFunctionalDependency inc = new RDBMSFunctionalDependency(dsUri, id,id,
            query, query, aux, candidate);

        if(incCon.insertAssertion(inc)){
          addedFunctionalDependencies.add(inc);
        }
      }
    }
    if(!miningCanceled){
      addRDBMSFunctionalDependenciesToTree(addedFunctionalDependencies);
    }
    dialog.stop();
  }

  private List<Term> convertSet(Set<Term> set){
    Iterator<Term> it = set.iterator();
    Vector<Term> aux = new Vector<Term>();
    while(it.hasNext()){
      aux.add(it.next());
    }
    return aux;
  }

  private void mineInclusionDependencyFromDBSchema(){
    dialog = new ProgressMonitorDialog(instance);
    dialog.show();
    CountDownLatch lat = new CountDownLatch(1);
    miner = new RDBMSInclusionDependencyFromDBSchemaMiner(apic, selectedSource, lat);
    miner.startMining();

    try {
      lat.await();
    } catch (InterruptedException e1) {
      throw new RuntimeException(e1);
    }
    if(miner.hasErrorOccurred()){
      throw new RuntimeException(miner.getException());
    }
    HashSet<RDBMSInclusionDependency> results =((RDBMSInclusionDependencyFromDBSchemaMiner)miner).getMiningResults();
    if(miningCanceled){
      Iterator<RDBMSInclusionDependency> it = results.iterator();
      RDBMSInclusionDependencyController incCon = (RDBMSInclusionDependencyController)apic.getController(RDBMSInclusionDependency.class);
      while(it.hasNext()){
        incCon.removeAssertion(it.next());
      }
    }else{
      addRDBMSInclusionDependenciesToTree(results);
    }
    dialog.stop();
  }

  private void mineFunctionalDependenciesFromDBSchema(){
    dialog = new ProgressMonitorDialog(instance);
    dialog.show();
    CountDownLatch lat = new CountDownLatch(1);
    miner = new RDBMSFunctionalDependencyFromDBSchemaMiner(apic, selectedSource, lat);
    try {
      miner.startMining();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    if(miner.hasErrorOccurred()){
      throw new RuntimeException(miner.getException());
    }
    try {
      lat.await();
    } catch (InterruptedException e1) {
      JOptionPane.showMessageDialog(null, "Mining interrupted. Please try again.", "ERROR", JOptionPane.ERROR_MESSAGE);
      e1.printStackTrace();
    }
    HashSet<RDBMSFunctionalDependency> results =((RDBMSFunctionalDependencyFromDBSchemaMiner)miner).getMiningResults();
    if(miningCanceled){
      Iterator<RDBMSFunctionalDependency> it = results.iterator();
      RDBMSFunctionalDependencyController funcCon = (RDBMSFunctionalDependencyController)apic.getController(RDBMSFunctionalDependency.class);
      while(it.hasNext()){
        funcCon.removeAssertion(it.next());
      }
    }else{
      addRDBMSFunctionalDependenciesToTree(results);
    }
    dialog.stop();
  }

  /**
   * Adjusts some configuration of the tree and adds listener and menus
   *  to it.
   */
  private void adjustTree() {
    
    MappingTreeModel maptreemodel = new MappingTreeModel(apic, mapc);
    MappingRenderer map_renderer = new MappingRenderer(apic, preference);
    treMapping.setModel(maptreemodel);
    treMapping.setSelectionModel(new MappingTreeSelectionModel());
    treMapping.setCellRenderer(map_renderer);
    treMapping.setEditable(false);
    treMapping.setRowHeight(0);
    treMapping.setRootVisible(false);

    JPopupMenu menu = new JPopupMenu();
    JMenuItem createIncDep = new JMenuItem();
    createIncDep.setText("Create Inclusion Dependency");
    createIncDep.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] paths = treMapping.getSelectionPaths();
        createDialog("Create Inclusion Dependency", paths, RDBMSInclusionDependency.INCLUSIONDEPENDENCY);
      }
    });
    menu.add(createIncDep);

    JMenuItem createFuncDep = new JMenuItem();
    createFuncDep.setText("Create Functional Dependency");
    createFuncDep.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] paths = treMapping.getSelectionPaths();
        createDialog("Create Functional Dependency", paths, RDBMSFunctionalDependency.FUNCTIONALDEPENDENCY);
      }
    });
    menu.add(createFuncDep);

    JMenuItem createDisAssertion = new JMenuItem();
    createDisAssertion.setText("Create Disjointness Dependency");
    createDisAssertion.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TreePath[] paths = treMapping.getSelectionPaths();
        createDialog("Create Disjointness Assertion", paths, RDBMSDisjointnessDependency.DISJOINEDNESSASSERTION);
      }
    });
    menu.add(createDisAssertion);

    JMenuItem mine = new JMenuItem();
    mine.setText("Mine Inclusion Dependencies");
    mine.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mineInclusionDependencyFromDBSchema();
      }
    });
    menu.addSeparator();
    menu.add(mine);

    treMapping.setComponentPopupMenu(menu);
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    scrMapping = new javax.swing.JScrollPane();
    treMapping = new javax.swing.JTree();

    setMinimumSize(new java.awt.Dimension(600, 300));
    setLayout(new java.awt.BorderLayout());

    scrMapping.setViewportView(treMapping);

    add(scrMapping, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void addRDBMSInclusionDependenciesToTree(
      HashSet<RDBMSInclusionDependency> vecInclusionDependency) {
    
    if (assertionTree.getModel() instanceof InclusionDependencyTreeModel && 
        !vecInclusionDependency.isEmpty()) {
      InclusionDependencyTreeModel model = 
        (InclusionDependencyTreeModel) assertionTree.getModel();
      model.addAssertions(vecInclusionDependency);
    }
  }

  private void addRDBMSDisjointnessDependenciesToTree(
      HashSet<RDBMSDisjointnessDependency> vecDisjointnessDependency) {
    
    if (assertionTree.getModel() instanceof DisjoinednessAssertionTreeModel && 
        !vecDisjointnessDependency.isEmpty()) {
      DisjoinednessAssertionTreeModel model = 
        (DisjoinednessAssertionTreeModel) assertionTree.getModel();
      model.addAssertions(vecDisjointnessDependency);
    }
  }

  private void addRDBMSFunctionalDependenciesToTree(
      HashSet<RDBMSFunctionalDependency> vecFunctionalDependency) {
    
    if (assertionTree.getModel() instanceof FunctionalDependenciesTreeModel && 
        !vecFunctionalDependency.isEmpty()) {
      FunctionalDependenciesTreeModel model = 
          (FunctionalDependenciesTreeModel) assertionTree.getModel();
      model.addAssertions(vecFunctionalDependency);
    }
  }

  @Override
  public void datasourceChanged(DataSource oldSource, DataSource newSource)
  {
    this.selectedSource = newSource;    
    
    // Update the data source tree.
    MappingTreeModel model = (MappingTreeModel) treMapping.getModel();
    URI oldSourceUri = null;
    if (oldSource != null) {
      oldSourceUri = oldSource.getSourceID();
    }
    URI newSourceUri = null;
    if (newSource != null) {
      newSourceUri = newSource.getSourceID();
    }
    model.currentSourceChanged(oldSourceUri, newSourceUri);
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JScrollPane scrMapping;
  private javax.swing.JTree treMapping;
  // End of variables declaration//GEN-END:variables


  /**
   *
   * @author obda
   */
  private class MiningDialog extends javax.swing.JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 8531081198402826251L;
    /** Creates new form MiningDialog */

    String dependency = null;

    public MiningDialog(String dep){
      dependency = dep;
      initComponents();
    }

    private void initComponents() {
      java.awt.GridBagConstraints gridBagConstraints;

      jLabelTitle = new javax.swing.JLabel();
      jCheckBoxData = new javax.swing.JCheckBox();
      jCheckBoxSchema = new javax.swing.JCheckBox();
      jButtonOK = new javax.swing.JButton();
      jButtonCancel = new javax.swing.JButton();
      jLabel2 = new javax.swing.JLabel();

      setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
      setResizable(false);
      getContentPane().setLayout(new java.awt.GridBagLayout());

      jLabelTitle.setText("Select the base data on which the mining should be done:");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
      getContentPane().add(jLabelTitle, gridBagConstraints);

      jCheckBoxData.setText("The available data");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 5);
      getContentPane().add(jCheckBoxData, gridBagConstraints);

      jCheckBoxSchema.setText("The database schema");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 5);
      getContentPane().add(jCheckBoxSchema, gridBagConstraints);

      jButtonOK.setText("OK");
      jButtonOK.setMaximumSize(new java.awt.Dimension(60, 22));
      jButtonOK.setMinimumSize(new java.awt.Dimension(60, 22));
      jButtonOK.setPreferredSize(new java.awt.Dimension(60, 22));
      jButtonOK.addActionListener(new java.awt.event.ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          jButtonOKActionPerformed(evt);
        }
      });
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 3;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
      getContentPane().add(jButtonOK, gridBagConstraints);

      jButtonCancel.setText("Cancel");
      jButtonCancel.setMaximumSize(new java.awt.Dimension(60, 22));
      jButtonCancel.setMinimumSize(new java.awt.Dimension(60, 22));
      jButtonCancel.setPreferredSize(new java.awt.Dimension(60, 22));
      jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          jButtonCancelActionPerformed(evt);
        }
      });
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 3;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
      getContentPane().add(jButtonCancel, gridBagConstraints);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 4;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      getContentPane().add(jLabel2, gridBagConstraints);

      pack();
    }

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {
      this.setVisible(false);
      if(dependency.equals(RDBMSInclusionDependency.INCLUSIONDEPENDENCY)){
        startInclusionMining(jCheckBoxData.isSelected(), jCheckBoxSchema.isSelected());
      }else if(dependency.equals(RDBMSFunctionalDependency.FUNCTIONALDEPENDENCY)){
        startfunctionalDependencyMining(jCheckBoxData.isSelected(), jCheckBoxSchema.isSelected());
      }
    }

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
      this.dispose();
    }

    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JCheckBox jCheckBoxData;
    private javax.swing.JCheckBox jCheckBoxSchema;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelTitle;
  }
}
