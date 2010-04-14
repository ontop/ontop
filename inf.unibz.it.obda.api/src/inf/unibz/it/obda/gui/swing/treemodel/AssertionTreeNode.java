package inf.unibz.it.obda.gui.swing.treemodel;

import inf.unibz.it.dl.assertion.Assertion;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public abstract class AssertionTreeNode<AssertionClass extends Assertion> extends DefaultMutableTreeNode {

	public AssertionTreeNode(AssertionClass assertion) {
		setUserObject(assertion);
	}

	/***************************************************************************
	 * The setUserObject method of an AssertionTreeNode should configure the
	 * node to reflect the tree structure that is desired for a given assertion.
	 * It should set the current user object as well as the required children
	 * depending on the content of the assertion
	 * 
	 * @param assertion
	 */
	public abstract void setUserObject(AssertionClass assertion);

	public void setUserObject(Object object) {
		if(object instanceof Assertion){
			this.userObject = (Assertion)object;
		}else{
			throw new RuntimeException("Assertion tree node only accepts assertions as user objects");
		}
	}

	/***************************************************************************
	 * This method sould compare two nodes and return true if the assertions
	 * this nodes contain are equal
	 * 
	 * @param node
	 * @return
	 */
	public abstract boolean equals(AssertionTreeNode<AssertionClass> node);

	public boolean equals(Object object) {
		return false;
	}

	@Override
	public AssertionClass getUserObject() {
		return (AssertionClass) userObject;
	}

	@Override
	public int getIndex(TreeNode aChild) {
		if (aChild == null) {
			throw new IllegalArgumentException("argument is null");
		}

		if (!isNodeChild(aChild)) {
			return -1;
		}
		return children.indexOf(aChild); // linear search
	}

}
