package inf.unibz.it.quonto.obda.swing.treemodel;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionControllerTreeModel;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNodeFactory;
import inf.unibz.it.quonto.dl.assertion.IDConstraint;

import javax.swing.tree.MutableTreeNode;

public class IDConstraintControllerTreeModel extends AssertionControllerTreeModel<IDConstraint> {

	private static final long	serialVersionUID	= 7789001092385914107L;

	public IDConstraintControllerTreeModel(MutableTreeNode root, AssertionController<IDConstraint> conroller, AssertionTreeNodeFactory<IDConstraint> renderer) {
		super(root, conroller, renderer);
	}

}
