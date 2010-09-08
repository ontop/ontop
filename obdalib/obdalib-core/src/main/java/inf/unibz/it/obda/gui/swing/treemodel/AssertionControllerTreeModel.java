package inf.unibz.it.obda.gui.swing.treemodel;

import inf.unibz.it.dl.assertion.Assertion;
import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.api.controller.AssertionControllerListener;

import java.util.Collection;
import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public abstract class AssertionControllerTreeModel<AssertionClass extends Assertion> extends DefaultTreeModel implements
		AssertionControllerListener<AssertionClass> {

	private static final long					serialVersionUID	= -5753951688449284609L;

	protected AssertionController<AssertionClass>			controller			= null;

	protected AssertionTreeNodeFactory<AssertionClass>	renderer			= null;

	public AssertionControllerTreeModel(MutableTreeNode root, AssertionController<AssertionClass> conroller,
			AssertionTreeNodeFactory<AssertionClass> factory) {
		super(root);
		this.renderer = factory;
		this.controller = conroller;
		synchronize();
		controller.addControllerListener(this);
	}

	public void synchronize() {
		Collection<AssertionClass> assertions = controller.getAssertions();
		for (AssertionClass assertion : assertions) {
			AssertionTreeNode<AssertionClass> node = renderer.render(assertion);
			insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
		}
	}

	public void assertionAdded(AssertionClass assertion) {
//		int index = indexOf(assertion);
//		if (index != -1)
//			return;
		AssertionTreeNode<AssertionClass> node = renderer.render(assertion);
		insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
		nodeStructureChanged(root);
	}

	public void assertionChanged(AssertionClass oldAssertion, AssertionClass newAssertion) {
		AssertionTreeNode<AssertionClass> oldNode = renderer.render(oldAssertion);
		AssertionTreeNode<AssertionClass> newNode = renderer.render(newAssertion);
//		int index = root.getIndex(oldNode);
		int index = indexOf(oldAssertion);
//		removeNodeFromParent(oldNode);
		assertionRemoved(oldAssertion);
		insertNodeInto(newNode, (MutableTreeNode) root, index);
	}

	public void assertionRemoved(AssertionClass assertion) {
		int index = indexOf(assertion);
		if (index != -1) {
			AssertionTreeNode<AssertionClass> node = (AssertionTreeNode<AssertionClass>) root.getChildAt(index);
			int[] childIndex = new int[1];
			Object[] removedArray = new Object[1];
			childIndex[0] = index;
			((MutableTreeNode) root).remove(childIndex[0]);
			removedArray[0] = node;
			nodesWereRemoved(root, childIndex, removedArray);
		}
	}
	
	public void assertionsCleared() {
		Enumeration<TreeNode> children = ((MutableTreeNode)root).children();
		while (children.hasMoreElements()) {
			TreeNode child = children.nextElement();
			removeNodeFromParent((MutableTreeNode)child);
		}
	}

	/***************************************************************************
	 * Returns the index of the node corresponding to assertion
	 * 
	 * @param assertion
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private int indexOf(AssertionClass assertion) {
		int count = root.getChildCount();
		for (int i = 0; i < count; i++) {
			AssertionTreeNode<AssertionClass> node = (AssertionTreeNode<AssertionClass>) root.getChildAt(i);
			AssertionClass currentAssertion = node.getUserObject();
			if (currentAssertion.equals(assertion)) {
				return i;
			}
		}
		return -1;
	}
}
