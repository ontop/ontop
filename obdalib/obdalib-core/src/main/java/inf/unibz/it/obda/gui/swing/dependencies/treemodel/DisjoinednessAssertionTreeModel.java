package inf.unibz.it.obda.gui.swing.dependencies.treemodel;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.dependencies.controller.RDBMSDisjointnessDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionControllerTreeModel;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNode;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNodeFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.tree.MutableTreeNode;

/**
 * A modified tree model, adapted to the needs for handling disjointness
 * dependencies
 * 
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy 
 *
 */

public class DisjoinednessAssertionTreeModel extends
	AssertionControllerTreeModel<RDBMSDisjointnessDependency> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9124488563466495350L;

	/**
	 * the RDBMS disjointness dependency controller
	 */
	
	public DisjoinednessAssertionTreeModel(MutableTreeNode root,
			AssertionController<RDBMSDisjointnessDependency> conroller,
			AssertionTreeNodeFactory<RDBMSDisjointnessDependency> factory) {
		super(root, conroller, factory);

//		List<RDBMSDisjoinednessAssertion> list = disController.getDependenciesForCurrentDataSource();
//		if(list != null){
//			Iterator<RDBMSDisjoinednessAssertion> it = list.iterator();
//			while(it.hasNext()){
//				AssertionTreeNode<RDBMSDisjoinednessAssertion> node = factory.render(it.next());
//				insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
//				nodeStructureChanged(root);
//			}
//		}
	}
	
	public void synchronize() {
		Collection<RDBMSDisjointnessDependency> assertions = ((RDBMSDisjointnessDependencyController)controller).getDependenciesForCurrentDataSource();
		for (RDBMSDisjointnessDependency assertion : assertions) {
			AssertionTreeNode<RDBMSDisjointnessDependency> node = renderer.render(assertion);
			insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
		}
		nodeStructureChanged(root);
	}
	
	public void addAssertions(HashSet<RDBMSDisjointnessDependency> assertinos){
		Iterator<RDBMSDisjointnessDependency> it = assertinos.iterator();
		while(it.hasNext()){
			AssertionTreeNode<RDBMSDisjointnessDependency> node = renderer.render(it.next());
			insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
		}
		nodeStructureChanged(root);
	}
}
