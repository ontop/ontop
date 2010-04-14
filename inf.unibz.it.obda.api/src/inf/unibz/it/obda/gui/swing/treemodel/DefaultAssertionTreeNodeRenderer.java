package inf.unibz.it.obda.gui.swing.treemodel;

import inf.unibz.it.dl.assertion.Assertion;

public class DefaultAssertionTreeNodeRenderer extends AssertionTreeNodeFactory{

	@Override
	public AssertionTreeNode render(Assertion assertion) {
		return new DefaultAssertionTreeNode(assertion);
	}

}
