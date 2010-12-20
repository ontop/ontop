package inf.unibz.it.obda.gui.swing.constraints.treemodel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.constraints.AbstractConstraintAssertion;
import inf.unibz.it.obda.constraints.controller.RDBMSCheckConstraintController;
import inf.unibz.it.obda.constraints.controller.RDBMSForeignKeyConstraintController;
import inf.unibz.it.obda.constraints.controller.RDBMSPrimaryKeyConstraintController;
import inf.unibz.it.obda.constraints.controller.RDBMSUniquenessConstraintController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSCheckConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSForeignKeyConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSPrimaryKeyConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSUniquenessConstraint;
import inf.unibz.it.obda.constraints.parser.ConstraintsRenderer;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNode;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.EventObject;
import java.util.Vector;

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

public class ConstraintsTreeCellEditor implements TreeCellEditor {

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
	private String constraintAssertion = null;
	/**
	 * The API controller
	 */
	private APIController apic = null;
	/**
	 * the panel containing the icon and the text field
	 */
	private JPanel panel = null;
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
	private AbstractConstraintAssertion output = null;
	
	
	/**
	 * Creates a new instance of the ConstraintsTreeCellEditor
	 * 
	 * @param apic the api controller
	 * @param constraint the name of the edited contraint
	 */
	public ConstraintsTreeCellEditor (APIController apic ,String constraint){
		listener = new Vector<CellEditorListener>();
		constraintAssertion = constraint;
		this.apic = apic;
	}
	
	
	@Override
	public Component getTreeCellEditorComponent(JTree arg0, Object arg1,
			boolean arg2, boolean arg3, boolean arg4, int arg5) {
	
		
		Border  aBorder = UIManager.getBorder("Tree.editorBorder");
		editor = new JTextPane();
		editor.setBorder(aBorder);
		editor.setMinimumSize(new Dimension(arg0.getWidth(),15));
		this.tree = arg0;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) arg1;
		editedNode = node;
		if(node.getUserObject() == null){
			newNode = true;
			output = null;
		}else{
			newNode = false;
		}
		editor.setText(arg1.toString());
		JLabel label = new JLabel();
		label.setIcon(((JLabel) tree.getCellRenderer()).getIcon());
		
		
		
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

	@Override
	public void addCellEditorListener(CellEditorListener arg0) {
		
		listener.add(arg0);
	}

	@Override
	public void cancelCellEditing() {
		if(newNode){
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			MutableTreeNode root =(MutableTreeNode)model.getRoot();
			int i = indexOf(model);
			((MutableTreeNode) root).remove(i);
			model.nodesWereRemoved(root, new int []{i}, new Object[]{editedNode});
		}
		
	}

	@Override
	public Object getCellEditorValue() {
		
		if(constraintAssertion.equals(RDBMSCheckConstraint.RDBMSCHECKSONSTRAINT)){
			if(newNode){
				return (RDBMSCheckConstraint) output;
			}else{
				return (RDBMSCheckConstraint) parseInput();
			}
		}else if(constraintAssertion.equals(RDBMSForeignKeyConstraint.RDBMSFOREIGNKEYCONSTRAINT)){
			if(newNode){
				return (RDBMSForeignKeyConstraint) output;
			}else{
				return (RDBMSForeignKeyConstraint) parseInput();
			}
		}else if(constraintAssertion.equals(RDBMSPrimaryKeyConstraint.RDBMSPRIMARYKEYCONSTRAINT)){
			if(newNode){
				return (RDBMSPrimaryKeyConstraint) output;
			}else{
				return (RDBMSPrimaryKeyConstraint) parseInput();
			}
		}else if(constraintAssertion.equals(RDBMSUniquenessConstraint.RDBMSUNIQUENESSCONSTRAINT)){
			if(newNode){
				return (RDBMSUniquenessConstraint) output;
			}else{
				return (RDBMSUniquenessConstraint) parseInput();
			}
		}else{
			return "ERROR";
		}	
	}

	@Override
	public boolean isCellEditable(EventObject arg0) {
		
		if(arg0 == null){
			return true;
		}
		 if (arg0 instanceof MouseEvent) { 
			return ((MouseEvent)arg0).getClickCount() >= nrOfClicksToStartEditing;
		 }
		 return false;
	}

	@Override
	public void removeCellEditorListener(CellEditorListener arg0) {
		
		listener.remove(arg0);
	}

	@Override
	public boolean shouldSelectCell(EventObject arg0) {
		
		return true;
	}

	@Override
	public boolean stopCellEditing() {
		
		if(newNode){
			String text = editor.getText();
			boolean valid = ConstraintsRenderer.getInstance().isValid(text, constraintAssertion);
			boolean add = addAssertion();
			if( valid &&  add){
				return true;
			}else{
				return false;
			}
		}else{
			return ConstraintsRenderer.getInstance().isValid(editor.getText(), constraintAssertion);
			}
	}
	
	/**
	 * adds an assertion to its controller
	 */
	private boolean addAssertion(){
		if(constraintAssertion.equals(RDBMSCheckConstraint.RDBMSCHECKSONSTRAINT)){
			RDBMSCheckConstraintController con = (RDBMSCheckConstraintController) apic.getController(RDBMSCheckConstraint.class);
			RDBMSCheckConstraint inc = (RDBMSCheckConstraint)parseInput();
			output = inc;
			return con.insertAssertion(inc);
		}else if(constraintAssertion.equals(RDBMSForeignKeyConstraint.RDBMSFOREIGNKEYCONSTRAINT)){
			RDBMSForeignKeyConstraintController con = (RDBMSForeignKeyConstraintController) apic.getController(RDBMSForeignKeyConstraint.class);
			RDBMSForeignKeyConstraint func = (RDBMSForeignKeyConstraint)parseInput();
			output = func;
			return con.insertAssertion(func);
		}else if(constraintAssertion.equals(RDBMSPrimaryKeyConstraint.RDBMSPRIMARYKEYCONSTRAINT)){
			RDBMSPrimaryKeyConstraintController con =(RDBMSPrimaryKeyConstraintController) apic.getController(RDBMSPrimaryKeyConstraint.class);
			RDBMSPrimaryKeyConstraint dis = (RDBMSPrimaryKeyConstraint)parseInput();
			output = dis;
			return con.insertAssertion(dis);
		}else if(constraintAssertion.equals(RDBMSUniquenessConstraint.RDBMSUNIQUENESSCONSTRAINT)){
			RDBMSUniquenessConstraintController con =(RDBMSUniquenessConstraintController) apic.getController(RDBMSUniquenessConstraint.class);
			RDBMSUniquenessConstraint dis = (RDBMSUniquenessConstraint)parseInput();
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
	private AbstractConstraintAssertion parseInput(){
	
		String text =editor.getText();
		URI uri = apic.getDatasourcesController().getCurrentDataSource().getSourceID();
		if(constraintAssertion.equals(RDBMSCheckConstraint.RDBMSCHECKSONSTRAINT)){
			try {
				return ConstraintsRenderer.getInstance().renderSingleCeckConstraint(text, uri);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}else if(constraintAssertion.equals(RDBMSForeignKeyConstraint.RDBMSFOREIGNKEYCONSTRAINT)){
			try {
				return ConstraintsRenderer.getInstance().renderSingleRDBMSForeignKeyConstraint(text, uri);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}else if(constraintAssertion.equals(RDBMSPrimaryKeyConstraint.RDBMSPRIMARYKEYCONSTRAINT)){
			try {
				return ConstraintsRenderer.getInstance().renderSingleRDBMSPrimaryKeyConstraint(text, uri);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}else if(constraintAssertion.equals(RDBMSUniquenessConstraint.RDBMSUNIQUENESSCONSTRAINT)){
			try {
				return ConstraintsRenderer.getInstance().renderSingleRDBMSUniquenessConstraint(text, uri);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}else{
			return null;
		}
	}

}
