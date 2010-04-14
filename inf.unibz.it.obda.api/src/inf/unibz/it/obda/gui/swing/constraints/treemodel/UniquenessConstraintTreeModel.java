package inf.unibz.it.obda.gui.swing.constraints.treemodel;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.constraints.controller.RDBMSUniquenessConstraintController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSUniquenessConstraint;
import inf.unibz.it.obda.dependencies.controller.RDBMSDisjointnessDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionControllerTreeModel;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNode;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNodeFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.tree.MutableTreeNode;

public class UniquenessConstraintTreeModel extends
AssertionControllerTreeModel<RDBMSUniquenessConstraint> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1667243960222120670L;

	public UniquenessConstraintTreeModel(MutableTreeNode root,
			AssertionController<RDBMSUniquenessConstraint> conroller,
			AssertionTreeNodeFactory<RDBMSUniquenessConstraint> factory) {
		super(root, conroller, factory);
		// TODO Auto-generated constructor stub
	}
	
	public void synchronize() {
		Collection<RDBMSUniquenessConstraint> assertions = ((RDBMSUniquenessConstraintController)controller).getDependenciesForCurrentDataSource();
		for (RDBMSUniquenessConstraint assertion : assertions) {
			AssertionTreeNode<RDBMSUniquenessConstraint> node = renderer.render(assertion);
			insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
		}
		nodeStructureChanged(root);
	}
	
	public void addAssertions(HashSet<RDBMSUniquenessConstraint> assertinos){
		Iterator<RDBMSUniquenessConstraint> it = assertinos.iterator();
		while(it.hasNext()){
			AssertionTreeNode<RDBMSUniquenessConstraint> node = renderer.render(it.next());
			insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
		}
		nodeStructureChanged(root);
	}

}
