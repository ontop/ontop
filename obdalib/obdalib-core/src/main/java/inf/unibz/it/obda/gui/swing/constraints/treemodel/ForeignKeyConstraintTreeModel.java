package inf.unibz.it.obda.gui.swing.constraints.treemodel;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.constraints.controller.RDBMSForeignKeyConstraintController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSForeignKeyConstraint;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionControllerTreeModel;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNode;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNodeFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.tree.MutableTreeNode;

public class ForeignKeyConstraintTreeModel extends
AssertionControllerTreeModel<RDBMSForeignKeyConstraint>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7519417686346306519L;

	public ForeignKeyConstraintTreeModel(MutableTreeNode root,
			AssertionController<RDBMSForeignKeyConstraint> conroller,
			AssertionTreeNodeFactory<RDBMSForeignKeyConstraint> factory) {
		super(root, conroller, factory);
		// TODO Auto-generated constructor stub
	}

	public void synchronize() {
		Collection<RDBMSForeignKeyConstraint> assertions = ((RDBMSForeignKeyConstraintController)controller).getDependenciesForCurrentDataSource();
		for (RDBMSForeignKeyConstraint assertion : assertions) {
			AssertionTreeNode<RDBMSForeignKeyConstraint> node = renderer.render(assertion);
			insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
		}
		nodeStructureChanged(root);
	}
	
	public void addAssertions(HashSet<RDBMSForeignKeyConstraint> assertinos){
		Iterator<RDBMSForeignKeyConstraint> it = assertinos.iterator();
		while(it.hasNext()){
			AssertionTreeNode<RDBMSForeignKeyConstraint> node = renderer.render(it.next());
			insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
		}
		nodeStructureChanged(root);
	}
}
