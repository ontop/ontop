package inf.unibz.it.obda.gui.swing.dependencies.treemodel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.dependencies.AbstractDependencyAssertion;
import inf.unibz.it.obda.dependencies.controller.RDBMSDisjointnessDependencyController;
import inf.unibz.it.obda.dependencies.controller.RDBMSFunctionalDependencyController;
import inf.unibz.it.obda.dependencies.controller.RDBMSInclusionDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSFunctionalDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSInclusionDependency;
import inf.unibz.it.obda.dependencies.parser.DependencyAssertionRenderer;
import inf.unibz.it.obda.gui.IconLoader;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNode;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellEditor;

/**
 * A modified tree cell editor, which uses customized icons and 
 * components   
 * 
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy 
 *
 */

public class DependencyTreeCellEditor implements TreeCellEditor {

	/**
	 * The text pane
	 */
	private JTextPane editor = null;
	/**
	 * a list of cell editor listeners
	 */
	private Vector<CellEditorListener> listener = null;
	/**
	 * name of the edited dependency
	 */
	private String dependencyAssertion = null;
	/**
	 * The API controller
	 */
	private APIController apic = null;
	/**
	 * the panel containing the icon and the text field
	 */
	private JPanel panel = null;
	/**
	 * The icon for functional dependencies
	 */
	private Icon								functionalDependency					= null;
	/**
	 * The icon for inclusion dependencies
	 */
	private Icon								inclusionDependency				= null;
	/**
	 * The icon for disjointness dependencies
	 */
	private Icon								disjoinetnessAssertion					= null;
	
	/**
	 * the number of clicks needed for starting editing (default is 2)
	 */
	private int nrOfClicksToStartEditing = 2;
	/**
	 * boolean field indicating whether a new node is edited or an
	 * already existing
	 */
	private boolean newNode = false;
	/**
	 * the tree, which is edited
	 */
	private JTree tree = null;
	/**
	 * the node which is edited
	 */
	private DefaultMutableTreeNode editedNode = null;
	/**
	 * the created dependency assertion after the editing is finished 
	 */
	private AbstractDependencyAssertion output = null;
	 
	/**
	 * Creates a new instance of the DependencyTreeCellEditor
	 * 
	 * @param apic the api controller
	 * @param dependency the name of the edited dependency
	 */
	public DependencyTreeCellEditor (APIController apic ,String dependency){
		listener = new Vector<CellEditorListener>();
		dependencyAssertion = dependency;
		inclusionDependency = IconLoader.getImageIcon("images/inclusion_16.png");
		functionalDependency = IconLoader.getImageIcon("images/functional_16.png");
		disjoinetnessAssertion = IconLoader.getImageIcon("images/disjoint_16.png");
		this.apic = apic;
	}
	
	public Component getTreeCellEditorComponent(JTree tree, Object value,
			boolean isSelected, boolean expanded, boolean leaf, int row) {
	
		Border  aBorder = UIManager.getBorder("Tree.editorBorder");
		editor = new JTextPane();
		editor.setBorder(aBorder);
		editor.setMinimumSize(new Dimension(tree.getWidth(),15));
		this.tree = tree;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		editedNode = node;
		if(node.getUserObject() == null){
			newNode = true;
			output = null;
		}else{
			newNode = false;
		}
		editor.setText(value.toString());
		JLabel label = new JLabel();
		if(dependencyAssertion.equals(RDBMSInclusionDependency.INCLUSIONDEPENDENCY)){
			label.setIcon(inclusionDependency);
		}else if(dependencyAssertion.equals(RDBMSFunctionalDependency.FUNCTIONALDEPENDENCY)){
			label.setIcon(functionalDependency);
		}else if(dependencyAssertion.equals(RDBMSDisjointnessDependency.DISJOINEDNESSASSERTION)){
			label.setIcon(disjoinetnessAssertion);
		}else {
			label.setIcon(((JLabel) tree.getCellRenderer()).getIcon());
		}
		
		
		java.awt.GridBagConstraints grid;
		GridBagLayout l = new GridBagLayout();
		panel = new JPanel();
		panel.setMinimumSize(new Dimension(tree.getWidth(),15));
		panel.setLayout(l);
		
		grid = new java.awt.GridBagConstraints();
		grid.gridx = 1;
		grid.gridy = 0;
		grid.gridwidth = 1;
		grid.weightx = 0;
		grid.weighty = 0;
		grid.fill = java.awt.GridBagConstraints.VERTICAL;
		panel.add(label, grid, 0);
		

		grid = new java.awt.GridBagConstraints();
		grid.gridx = 2;
		grid.gridy = 0;
		grid.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		grid.fill = java.awt.GridBagConstraints.BOTH;
		grid.weightx = 1.0;
		grid.weighty = 1.0;
		panel.add(editor, grid, 1);
		
		panel.setMinimumSize(new Dimension(tree.getWidth(),50));
		panel.setPreferredSize(new Dimension(tree.getWidth(),50));
		
		return panel;
	}

	public void addCellEditorListener(CellEditorListener l) {
		
		listener.add(l);
	}

	public void cancelCellEditing() {
		if(newNode){
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			MutableTreeNode root =(MutableTreeNode)model.getRoot();
			int i = indexOf(model);
			((MutableTreeNode) root).remove(i);
			model.nodesWereRemoved(root, new int []{i}, new Object[]{editedNode});
		}
	}

	public Object getCellEditorValue() {
		
		if(dependencyAssertion.equals(RDBMSInclusionDependency.INCLUSIONDEPENDENCY)){
			if(newNode){
				return (RDBMSInclusionDependency) output;
			}else{
				return (RDBMSInclusionDependency) parseInput();
			}
		}else if(dependencyAssertion.equals(RDBMSFunctionalDependency.FUNCTIONALDEPENDENCY)){
			if(newNode){
				return (RDBMSFunctionalDependency) output;
			}else{
				return (RDBMSFunctionalDependency) parseInput();
			}
		}else if(dependencyAssertion.equals(RDBMSDisjointnessDependency.DISJOINEDNESSASSERTION)){
			if(newNode){
				return (RDBMSDisjointnessDependency) output;
			}else{
				return (RDBMSDisjointnessDependency) parseInput();
			}
		}else{
			return "ERROR";
		}	
	}

	public boolean isCellEditable(EventObject anEvent) {
		if(anEvent == null){
			return true;
		}
		 if (anEvent instanceof MouseEvent) { 
			return ((MouseEvent)anEvent).getClickCount() >= nrOfClicksToStartEditing;
		 }
		 return false;
	}

	public void removeCellEditorListener(CellEditorListener l) {
		
		listener.remove(l);
	}

	public boolean shouldSelectCell(EventObject anEvent) {
		return true;
	}

	public boolean stopCellEditing() {
		if(newNode){
			if(DependencyAssertionRenderer.getInstance().isValid(editor.getText(), dependencyAssertion) && addAssertion()){
				return true;
			}else{
				return false;
			}
		}else{
			return DependencyAssertionRenderer.getInstance().isValid(editor.getText(), dependencyAssertion);
			}
	}

	/**
	 * Returns the number of click needed for starting editing
	 * @return
	 */
	public int getNrOfClicksToStartEditing() {
		return nrOfClicksToStartEditing;
	}

	/**
	 * Updates the number of clicks needed to start editing to the given value
	 * @param nrOfClicksToStartEditing
	 */
	public void setNrOfClicksToStartEditing(int nrOfClicksToStartEditing) {
		this.nrOfClicksToStartEditing = nrOfClicksToStartEditing;
	}

	/**
	 * adds an assertion to its controller
	 */
	private boolean addAssertion(){
		if(dependencyAssertion.equals(RDBMSInclusionDependency.INCLUSIONDEPENDENCY)){
			RDBMSInclusionDependencyController con = (RDBMSInclusionDependencyController) apic.getController(RDBMSInclusionDependency.class);
			RDBMSInclusionDependency inc = (RDBMSInclusionDependency)parseInput();
			output = inc;
			return con.insertAssertion(inc);
		}else if(dependencyAssertion.equals(RDBMSFunctionalDependency.FUNCTIONALDEPENDENCY)){
			RDBMSFunctionalDependencyController con = (RDBMSFunctionalDependencyController) apic.getController(RDBMSFunctionalDependency.class);
			RDBMSFunctionalDependency func = (RDBMSFunctionalDependency)parseInput();
			output = func;
			return con.insertAssertion(func);
		}else if(dependencyAssertion.equals(RDBMSDisjointnessDependency.DISJOINEDNESSASSERTION)){
			RDBMSDisjointnessDependencyController con =(RDBMSDisjointnessDependencyController) apic.getController(RDBMSDisjointnessDependency.class);
			RDBMSDisjointnessDependency dis = (RDBMSDisjointnessDependency)parseInput();
			output = dis;
			return con.insertAssertion(dis);
		}else{
			return false;
		}
	}
	
	/**
	 * Returns the index of the new added node in the given model
	 * @param model the tree model
	 * @return the index of the new node
	 */
	private int indexOf(DefaultTreeModel model) {
		MutableTreeNode root = (MutableTreeNode) model.getRoot();
		int count = root.getChildCount();
		for (int i = 0; i < count; i++) {
			AssertionTreeNode node = (AssertionTreeNode) root.getChildAt(i);
			if (node.getUserObject() == null) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * method to parse the input of the edited node
	 * 
	 * @return an abstract dependency assertion if the input is correct, null otherwise
	 */
	private AbstractDependencyAssertion parseInput(){
	
		String text =editor.getText();
		String uri = apic.getDatasourcesController().getCurrentDataSource().getName();
		if(dependencyAssertion.equals(RDBMSInclusionDependency.INCLUSIONDEPENDENCY)){
			try {
				return DependencyAssertionRenderer.getInstance().renderSingleRBMSInclusionDependency(text, uri);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}else if(dependencyAssertion.equals(RDBMSFunctionalDependency.FUNCTIONALDEPENDENCY)){
			try {
				return DependencyAssertionRenderer.getInstance().renderSingleRDBMSFunctionalDependency(text, uri);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}else if(dependencyAssertion.equals(RDBMSDisjointnessDependency.DISJOINEDNESSASSERTION)){
			try {
				return DependencyAssertionRenderer.getInstance().renderSingleRDBMSDisjoinednessAssertion(text, uri);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}else{
			return null;
		}
	}
}
