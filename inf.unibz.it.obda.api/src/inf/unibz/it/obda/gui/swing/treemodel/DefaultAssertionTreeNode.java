package inf.unibz.it.obda.gui.swing.treemodel;

import inf.unibz.it.dl.assertion.Assertion;

public class DefaultAssertionTreeNode<AssertionClass extends Assertion> extends AssertionTreeNode<AssertionClass> {

	private static final long	serialVersionUID	= 5697817828018356761L;

	public DefaultAssertionTreeNode(AssertionClass assertion) {
		super(assertion);
	}
	
	@Override
	public boolean equals(AssertionTreeNode<AssertionClass> node) {
		return this.userObject.toString().equals(node.getUserObject().toString());
	}

	@Override
	public void setUserObject(AssertionClass assertion) {
		this.userObject = assertion;
	}

}
