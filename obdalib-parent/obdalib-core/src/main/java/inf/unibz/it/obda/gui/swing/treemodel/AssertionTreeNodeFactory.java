package inf.unibz.it.obda.gui.swing.treemodel;

import inf.unibz.it.dl.assertion.Assertion;

public abstract class AssertionTreeNodeFactory<AssertionClass extends Assertion> {
	
	public AssertionTreeNodeFactory() {
		
	}
	
	public abstract AssertionTreeNode<AssertionClass> render(AssertionClass assertion);
}
