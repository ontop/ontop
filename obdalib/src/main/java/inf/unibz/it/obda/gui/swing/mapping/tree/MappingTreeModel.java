/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, 
 * b) third-party components licensed under terms that may be different from 
 *   those of the LGPL.  Information about such licenses can be found in the 
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.gui.swing.mapping.tree;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.api.controller.MappingControllerListener;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.domain.SourceQuery;
import inf.unibz.it.obda.domain.TargetQuery;
import inf.unibz.it.obda.gui.swing.treemodel.filter.FilteredTreeModel;
import inf.unibz.it.obda.gui.swing.treemodel.filter.MappingFunctorTreeModelFilter;
import inf.unibz.it.obda.gui.swing.treemodel.filter.MappingPredicateTreeModelFilter;
import inf.unibz.it.obda.gui.swing.treemodel.filter.MappingSQLStringTreeModelFilter;
import inf.unibz.it.obda.gui.swing.treemodel.filter.MappingStringTreeModelFilter;
import inf.unibz.it.obda.gui.swing.treemodel.filter.TreeModelFilter;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.parser.exception.QueryParseException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

// TODO Make the three model be based on the actual mapping collection
public class MappingTreeModel extends DefaultTreeModel implements
		MappingControllerListener, FilteredTreeModel {

	private static final long serialVersionUID = -1151057670798439917L;

	private MappingController controller = null;
	private DefaultMutableTreeNode root = null;
	private DatasourcesController dsc = null;
	private List<TreeModelFilter> ListFilters = new ArrayList<TreeModelFilter>();
	protected APIController apic = null;

	public MappingTreeModel(APIController apic, DatasourcesController dsc,
			MappingController controller) {
		super(new DefaultMutableTreeNode("Mappings"));
		this.apic = apic;
		this.dsc = dsc;
		root = (DefaultMutableTreeNode) getRoot();
		this.controller = controller;
		// Just for testing the filters in MappingTreemodel
		// MappingStringTreeModelFilter filter = new
		// MappingStringTreeModelFilter("Company");
		// this.addFilter(filter);
		// Finish test

	}

	/***************************************************************************
	 * Invoked when the model changes. It should be invoked only when it is
	 * changed trough a process not related to the mappings or data sources,
	 * like a node editor in the GUI. It calls the update methods of the mapping
	 * controller.
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		DefaultMutableTreeNode oldmappingnode = (DefaultMutableTreeNode) path
				.getLastPathComponent();
		DefaultMutableTreeNode parentnode = (DefaultMutableTreeNode) oldmappingnode
				.getParent();
		// System.out.println(oldmappingnode.getClass());
		Object oldmappingvalue = oldmappingnode.getUserObject();
		super.valueForPathChanged(path, newValue);

		String sourceName = dsc.getCurrentDataSource().getName();

		if (oldmappingnode instanceof MappingNode) {
			// DataSourceMapping mapping = controller.getMapping(source_uri,
			// (String) oldmappingid);
			// mapping.setId((String) newValue);
			// controller.fireMappigUpdated(source_uri, mapping.getId(),
			// mapping);
			// // controller.treeStructureChanged(e);
			controller.updateMapping(sourceName, (String) oldmappingvalue,
					(String) newValue);
		} else if (oldmappingnode instanceof MappingHeadNode) {
			// DefaultMutableTreeNode parent = (DefaultMutableTreeNode)
			// oldmappingnode.getParent();
			// String mappingId = (String) parent.getUserObject();
			// DataSourceMapping mapping = controller.getMapping(source_uri,
			// mappingId);
			// mapping.setTargetQuery(OntologyQuery.getFromString((String)
			// newValue));
			ConjunctiveQuery newquery;
			try {
				newquery = new ConjunctiveQuery((String) newValue, apic);
				controller.updateMapping(sourceName, (String) parentnode
						.getUserObject(), newquery);
			} catch (QueryParseException e) {
				// If there was an error creating the new query, revert the
				// changes

			}

			// controller.fireMappigUpdated(source_uri, mapping.getId(),
			// mapping);
		} else if (oldmappingnode instanceof MappingBodyNode) {
			// DefaultMutableTreeNode parent = (DefaultMutableTreeNode)
			// oldmappingnode.getParent();
			// String mappingId = (String) parent.getUserObject();
			// DataSourceMapping mapping = controller.getMapping(source_uri,
			// mappingId);
			// mapping.setSourceQuery(new RDBMSSQLQuery((String) newValue));
			// controller.fireMappigUpdated(source_uri, mapping.getId(),
			// mapping);

			try {
				RDBMSSQLQuery query = new RDBMSSQLQuery((String) newValue, apic);
				controller.updateMapping(sourceName, (String) parentnode
						.getUserObject(), query);
			} catch (QueryParseException e) {
				MappingBodyNode node = (MappingBodyNode) path
						.getLastPathComponent();
				OBDAMappingAxiom mapping = controller.getMapping(sourceName,
						(String) parentnode.getUserObject());
				node.setQuery(mapping.getSourceQuery().getInputQuString());
			}
		}
	}

	// public static CopyOfMappingTreeModel
	// getMappingTreeModelFromSourceMappings(String datasource_uri,
	// MappingController controller) {
	// DefaultMutableTreeNode mappings_root = new
	// DefaultMutableTreeNode(datasource_uri);
	// MappingController manager = MappingController.getInstance();
	// ArrayList<DataSourceMapping> mappings =
	// manager.getMappings(datasource_uri);
	// for (int i = 0; i < mappings.size(); i++) {
	// RDBMSDataSourceMapping current_mapping = (RDBMSDataSourceMapping)
	// mappings.get(i);
	// MappingNode mappingNode =
	// MappingNode.getMappingNodeFromMapping(current_mapping);
	// mappings_root.add(mappingNode);
	// }
	// CopyOfMappingTreeModel mappingTreeModel = new
	// CopyOfMappingTreeModel(controller);
	// return mappingTreeModel;
	// }

	/***************************************************************************
	 * Called from the mapping controller when anew mapping is added. Updates
	 * the model to include the corresponding node. Only if the current source
	 * is srcuri.
	 */
	public void mappingInserted(String srcuri, String mapping_id) {

		try {

			DataSource currentsource = dsc.getCurrentDataSource();
			if ((currentsource == null)
					|| !srcuri.equals(currentsource.getName())) {
				return;
			}

			String src_uri = dsc.getCurrentDataSource().getName();
			RDBMSOBDAMappingAxiom mapping = (RDBMSOBDAMappingAxiom) controller
					.getMapping(src_uri, mapping_id);

			MappingNode mappingNode = MappingNode
					.getMappingNodeFromMapping(mapping);

			// SYNCWITH EVERYBODY EXCEPT WITH THE CONTROLLER SINCE IT WAS THE
			// SOURCE
			// OF THIS EVENT

			removeTreeModelListener(controller);
			insertNodeInto(mappingNode, (DefaultMutableTreeNode) root, root
					.getChildCount());

			// int newchildindex = root.getIndex(mappingNode);
			// int si = mappingNode.getIndex(mappingNode.getBodyNode());
			// int ti = mappingNode.getIndex(mappingNode.getHeadNode());
			// int[] indexesRoot = new int[]{newchildindex};
			// int[] indexesMapping = new int[]{ti,si};
			// // indexes[0] = newchildindex;
			// // nodeStructureChanged(mappingNode.getParent());
			// nodesWereInserted(root, indexesRoot);
			// nodesWereInserted(mappingNode, indexesMapping);

		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			addTreeModelListener(controller);
		}

	}

	/***************************************************************************
	 * Called from the mapping controller when anew mapping is updated
	 * programatically. Updates the model to include the changes for
	 * corresponding node. Only if the current source is srcuri.
	 */
	public void mappingUpdated(String srcuri, String mapping_id,
			OBDAMappingAxiom mapping) {

		// SYNCWITH EVERYBODY EXCEPT WITH THE CONTROLLER SINCE IT WAS THE SOURCE
		// OF THIS EVENT
		if (!srcuri.equals(dsc.getCurrentDataSource().getName())) {
			return;
		}

		try {
			removeTreeModelListener(controller);

			MappingNode mappingnode = getMappingNode(mapping_id);
			if (mappingnode == null) {
				return;
			}
			MappingBodyNode body = mappingnode.getBodyNode();
			MappingHeadNode head = mappingnode.getHeadNode();

			SourceQuery srcq = mapping.getSourceQuery();
			TargetQuery trgq = mapping.getTargetQuery();
			String newbodyquery = srcq.getInputQuString();
			String newheadquery = trgq.getInputQuString();

			body.setQuery(newbodyquery);
			head.setQuery(newheadquery);
			mappingnode.setMappingID(mapping.getId());
			nodeStructureChanged(mappingnode);

		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			addTreeModelListener(controller);
		}

	}

	// public static CopyOfMappingTreeModel
	// getMappingTreeModelFromSourceMappings(String datasource_uri,
	// MappingController controller) {
	// DefaultMutableTreeNode mappings_root = new
	// DefaultMutableTreeNode(datasource_uri);
	// MappingController manager = MappingController.getInstance();
	// ArrayList<DataSourceMapping> mappings =
	// manager.getMappings(datasource_uri);
	// for (int i = 0; i < mappings.size(); i++) {
	// RDBMSDataSourceMapping current_mapping = (RDBMSDataSourceMapping)
	// mappings.get(i);
	// MappingNode mappingNode =
	// MappingNode.getMappingNodeFromMapping(current_mapping);
	// mappings_root.add(mappingNode);
	// }
	// CopyOfMappingTreeModel mappingTreeModel = new
	// CopyOfMappingTreeModel(controller);
	// return mappingTreeModel;
	// }

	/***************************************************************************
	 * Called from the mapping controller when anew mapping is deleted. Updates
	 * the model to delete the corresponding node. Only if the current source is
	 * srcuri.
	 */
	public void mappingDeleted(String srcuri, String mapping_id) {

		if (!srcuri.equals(dsc.getCurrentDataSource().getName())) {
			return;
		}
		try {
			removeTreeModelListener(controller);

			MappingNode mapping = getMappingNode(mapping_id);
			removeNodeFromParent(mapping);

		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			addTreeModelListener(controller);
		}

	}

	/***************************************************************************
	 * Invoked by the mapping controller to notify the change of the current
	 * Datasource. The model dumps all mapping nodes and adds the nodes for the
	 * mappings of the current source.
	 */
	public void currentSourceChanged(String oldsrcuri, String newsrcuri) {
		// SYNCWITH EVERYBODY EXCEPT WITH THE CONTROLLER SINCE IT WAS THE SOURCE
		// OF THIS EVENT
		// apic.getDatasourcesController().getCurrentDataSource().get
		try {
			removeTreeModelListener(controller);

			if (newsrcuri != null) {
				root.setUserObject("Mappings for: " + newsrcuri);
			} else {
				root.setUserObject("No src uri");
			}
			ArrayList<MappingNode> newnodes = new ArrayList<MappingNode>();
			ArrayList<OBDAMappingAxiom> newmappings = controller
					.getMappings(newsrcuri);

			if (newmappings != null) {
				for (OBDAMappingAxiom dataSourceMapping : newmappings) {
					RDBMSOBDAMappingAxiom mappingTest = (RDBMSOBDAMappingAxiom) dataSourceMapping;
					if (testFilters(mappingTest))
						newnodes
								.add(getMappingNodeFromMapping((OBDAMappingAxiom) mappingTest));
				}
			}

			/*
			 * for (OBDAMappingAxiom dataSourceMapping : newmappings) { newnodes
			 * .add(getMappingNodeFromMapping((OBDAMappingAxiom)
			 * dataSourceMapping)); }
			 */

			root.removeAllChildren();
			for (MappingNode newnode : newnodes) {
				root.insert(newnode, root.getChildCount());
				// insertNodeInto(newnode, (DefaultMutableTreeNode) root,
				// root.getChildCount());
			}

			nodeStructureChanged(root);

		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			addTreeModelListener(controller);
		}
	}

	/***************************************************************************
	 * Gets the mapping node for the mapping identified by mappingid
	 * 
	 * @param mappingid
	 * @return
	 */
	public MappingNode getMappingNode(String mappingid) {
		for (int i = 0; i < root.getChildCount(); i++) {
			MappingNode mapping = (MappingNode) root.getChildAt(i);
			if (mapping.getMappingID().equals(mappingid)) {
				return mapping;
			}
		}
		return null;
	}

	/***************************************************************************
	 * Gets the mapping node that corresponds to the mapping.
	 * 
	 * @param mapping
	 * @return
	 */
	private MappingNode getMappingNodeFromMapping(OBDAMappingAxiom mapping) {

		MappingNode mappingnode = new MappingNode(mapping.getId());
		SourceQuery srcquery = mapping.getSourceQuery();
		TargetQuery tgtquery = mapping.getTargetQuery();
		MappingBodyNode body = null;
		MappingHeadNode head = null;
		if (srcquery != null) {
			body = new MappingBodyNode(srcquery.getInputQuString());
		} else {
			body = new MappingBodyNode("");
		}
		if (tgtquery != null) {
			head = new MappingHeadNode(tgtquery.getInputQuString());
		} else {
			head = new MappingHeadNode("");
		}

		mappingnode.add(head);
		mappingnode.add(body);

		return mappingnode;
	}

	/*
	 * @see
	 * inf.unibz.it.obda.api.controller.MappingControllerListener#allMappingsRemoved
	 * ()
	 */
	public void allMappingsRemoved() {
		try {
			removeTreeModelListener(controller);
			root.removeAllChildren();
			nodeStructureChanged(root);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			addTreeModelListener(controller);
		}
	}

	/*
	 * @see
	 * inf.unibz.it.obda.gui.swing.treemodel.filter.FilteredTreeModel#addFilter
	 * (inf.unibz.it.obda.gui.swing.treemodel.filter.TreeModelFilter)
	 */
	@Override
	public void addFilter(TreeModelFilter T) {
		ListFilters.add(T);

	}

	/*
	 * @see
	 * inf.unibz.it.obda.gui.swing.treemodel.filter.FilteredTreeModel#addFilters
	 * (java.util.List)
	 */
	@Override
	public void addFilters(List<TreeModelFilter> T) {
		// TODO Auto-generated method stub
		for (int i = 0; i < T.size(); i++) {
			ListFilters.add(T.get(i));
		}

	}

	/*
	 * @seeinf.unibz.it.obda.gui.swing.treemodel.filter.FilteredTreeModel#
	 * removeAllFilters()
	 */
	@Override
	public void removeAllFilters() {
		ListFilters.clear();

	}

	/*
	 * @see
	 * inf.unibz.it.obda.gui.swing.treemodel.filter.FilteredTreeModel#removeFilter
	 * (inf.unibz.it.obda.gui.swing.treemodel.filter.TreeModelFilter)
	 */
	@Override
	public void removeFilter(TreeModelFilter T) {
		// TODO Auto-generated method stub
		Iterator<TreeModelFilter> iter = ListFilters.iterator();

		while (iter.hasNext()) {
			Object element = iter.next();
			if (element.equals(T))
				ListFilters.remove(element);
			break;

		}

	}

	/*
	 * @see
	 * inf.unibz.it.obda.gui.swing.treemodel.filter.FilteredTreeModel#removeFilter
	 * (java.util.List)
	 */
	@Override
	public void removeFilter(List<TreeModelFilter> T) {
		// TODO Auto-generated method stub
		Iterator<TreeModelFilter> iter = ListFilters.iterator();
		while (iter.hasNext()) {
			Object element = iter.next();
			ListFilters.remove(element);
		}

	}

	/******
	 * This function compares with all the elements of the list of filters
	 * "ListFilters", if any of it doesn't match then it returns false
	 * 
	 * @param mapping
	 * @return allFiltersTrue
	 * 
	 */
	private boolean testFilters(RDBMSOBDAMappingAxiom mapping) {
		boolean allFiltersTrue = true;
		for (int i = 0; i < ListFilters.size(); i++) {
			allFiltersTrue = allFiltersTrue
					&& ListFilters.get(i).match(mapping);
		}
		return allFiltersTrue;
	}

}
