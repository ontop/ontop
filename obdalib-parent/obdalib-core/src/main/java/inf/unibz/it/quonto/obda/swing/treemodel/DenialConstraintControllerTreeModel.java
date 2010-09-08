package inf.unibz.it.quonto.obda.swing.treemodel;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionControllerTreeModel;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNodeFactory;
import inf.unibz.it.quonto.dl.assertion.DenialConstraint;

import javax.swing.tree.MutableTreeNode;

public class DenialConstraintControllerTreeModel extends AssertionControllerTreeModel<DenialConstraint> {

	private static final long	serialVersionUID	= 7789001092385914107L;

	public DenialConstraintControllerTreeModel(MutableTreeNode root, AssertionController<DenialConstraint> conroller, AssertionTreeNodeFactory<DenialConstraint> renderer) {
		super(root, conroller, renderer);
	}

}
