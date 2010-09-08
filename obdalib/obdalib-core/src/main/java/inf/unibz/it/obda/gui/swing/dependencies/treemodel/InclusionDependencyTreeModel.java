package inf.unibz.it.obda.gui.swing.dependencies.treemodel;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.MutableTreeNode;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.dependencies.controller.RDBMSInclusionDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSFunctionalDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSInclusionDependency;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionControllerTreeModel;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNode;
import inf.unibz.it.obda.gui.swing.treemodel.AssertionTreeNodeFactory;
import inf.unibz.it.obda.gui.swing.treemodel.DefaultAssertionTreeNode;

/**
 * A modified tree model, adapted to the needs for handling inclusion
 * dependencies
 * 
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy 
 *
 */

public class InclusionDependencyTreeModel extends
	AssertionControllerTreeModel<RDBMSInclusionDependency> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -658387966782405527L;
	
	public InclusionDependencyTreeModel(MutableTreeNode root,
			AssertionController<RDBMSInclusionDependency> conroller,
			AssertionTreeNodeFactory<RDBMSInclusionDependency> factory) {
		super(root, conroller, factory);
		
//		HashSet<RDBMSInclusionDependency> list = incController.getDependenciesForCurrentDataSource();
//		if(list != null){
//			Iterator<RDBMSInclusionDependency> it = list.iterator();
//			while(it.hasNext()){
//				AssertionTreeNode<RDBMSInclusionDependency> node = factory.render(it.next());
//				insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
//				nodeStructureChanged(root);
//			}
//		}
	}
	
	public void addAssertions(HashSet<RDBMSInclusionDependency> assertinos){
		Iterator<RDBMSInclusionDependency> it = assertinos.iterator();
		while(it.hasNext()){
			AssertionTreeNode<RDBMSInclusionDependency> node = renderer.render(it.next());
			insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
		}
		nodeStructureChanged(root);
	}
	
	public void synchronize() {
		Collection<RDBMSInclusionDependency> assertions = ((RDBMSInclusionDependencyController)controller).getDependenciesForCurrentDataSource();
		for (RDBMSInclusionDependency assertion : assertions) {
			AssertionTreeNode<RDBMSInclusionDependency> node = renderer.render(assertion);
			insertNodeInto(node, (MutableTreeNode) root, root.getChildCount());
		}
		nodeStructureChanged(root);
	}
}
