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
package inf.unibz.it.obda.gui.swing.dataquery.panel;

import inf.unibz.it.obda.api.controller.QueryController;
import inf.unibz.it.obda.api.controller.QueryControllerEntity;
import inf.unibz.it.obda.api.controller.QueryControllerListener;
import inf.unibz.it.obda.gui.IconLoader;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerGroup;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerQuery;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerTreeModel;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryGroupTreeElement;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryTreeElement;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.TreeElement;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * GUI for Managing queries.
 * 
 * @author mariano
 */
public class SavedQueriesPanel extends javax.swing.JPanel implements
		QueryControllerListener {

	// private static final long serialVersionUID = 6920100822784727963L;
	public Vector<SavedQueriesPanelListener> listeners;
	private QueryController queryController = null;
	private String currentQuery = null;
	private QueryTreeElement currentId = null;

	/** Creates new form SavedQueriesPanel */

	public SavedQueriesPanel(QueryController queryController) {
		this.queryController = queryController;
		initComponents();

		TreeDragSource ds = new TreeDragSource(treeSavedQueries,
				DnDConstants.ACTION_COPY_OR_MOVE);
		TreeDropTarget dt = new TreeDropTarget(treeSavedQueries);
		listeners = new Vector<SavedQueriesPanelListener>();
		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.queryController
				.getTreeModel().getRoot();
		DefaultTreeModel obj = new DefaultTreeModel(root);

		QueryControllerTreeModel queryControllerModel = this.queryController
				.getTreeModel();

		treeSavedQueries.setModel(queryControllerModel);
		treeSavedQueries.setCellRenderer(new SavedQueriesTreeCellRenderer());
		treeSavedQueries.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		treeSavedQueries.expandPath(new TreePath(root.getPath()));

		/**
		 * Keep this in this position to guarantee the proper order for calling
		 * of the listeners
		 */
		this.queryController.addListener(this);

		treeSavedQueries.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				TreePath currentSelection = e.getPath();
				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection
						.getLastPathComponent());

				if (currentNode instanceof QueryTreeElement) {
					QueryTreeElement current_query = (QueryTreeElement) currentNode;
					currentQuery = current_query.getQuery();
					currentId = current_query;
					if ((current_query.getParent() == null) || (current_query.getParent().toString()).equals(""))
						fireQueryChanged(null, currentQuery, currentId.getID());
					else
						fireQueryChanged(current_query.getParent().toString(),
								currentQuery, currentId.getID());
				} else if (currentNode instanceof QueryGroupTreeElement) {
					QueryGroupTreeElement current_group = (QueryGroupTreeElement) currentNode;
					currentId = null;
					currentQuery = null;
					fireQueryChanged(current_group.toString(), null, null);
				} else if (currentNode == null) {
					currentId = null;
					currentQuery = null;
				}
			}
		});
	}

	public void addQueryManagerListener(SavedQueriesPanelListener listener) {
		if (listener == null)
			return;
		if (listeners.contains(listener))
			return;
		listeners.add(listener);
	}

	public void removeQueryManagerListener(SavedQueriesPanelListener listener) {
		if (listener == null)
			return;
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc=" Generated Code
	// ">//GEN-BEGIN:initComponents
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		panel_saved_queries = new javax.swing.JPanel();
		scroll_saved_queries = new javax.swing.JScrollPane();
		treeSavedQueries = new javax.swing.JTree();
		panel_saved_queries_header = new javax.swing.JPanel();
		lavel_saved_queries = new javax.swing.JLabel();
		removeQueryButton = new javax.swing.JButton();

		setLayout(new java.awt.BorderLayout());

		panel_saved_queries.setLayout(new java.awt.BorderLayout());

		panel_saved_queries.setMinimumSize(new java.awt.Dimension(200, 50));
		scroll_saved_queries.setMinimumSize(new java.awt.Dimension(400, 200));
		scroll_saved_queries.setOpaque(false);
		scroll_saved_queries.setPreferredSize(new java.awt.Dimension(300, 200));
		treeSavedQueries.setBorder(javax.swing.BorderFactory
				.createEtchedBorder());
		treeSavedQueries.setForeground(new java.awt.Color(51, 51, 51));
		treeSavedQueries.setMaximumSize(new java.awt.Dimension(5000, 5000));
		treeSavedQueries.setRootVisible(false);
		scroll_saved_queries.setViewportView(treeSavedQueries);

		panel_saved_queries.add(scroll_saved_queries,
				java.awt.BorderLayout.CENTER);

		panel_saved_queries_header.setLayout(new java.awt.GridBagLayout());

		lavel_saved_queries.setFont(new java.awt.Font("Arial", 1, 11));
		lavel_saved_queries.setForeground(new java.awt.Color(153, 153, 153));
		lavel_saved_queries.setText("  Saved queries:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.5;
		panel_saved_queries_header.add(lavel_saved_queries, gridBagConstraints);

		removeQueryButton.setIcon(IconLoader.getImageIcon("images/minus.png"));
		removeQueryButton.setToolTipText("Remove the selected datasource");
		removeQueryButton.setBorder(javax.swing.BorderFactory
				.createEtchedBorder());
		removeQueryButton.setContentAreaFilled(false);
		removeQueryButton.setIconTextGap(0);
		removeQueryButton.setMaximumSize(new java.awt.Dimension(25, 25));
		removeQueryButton.setMinimumSize(new java.awt.Dimension(25, 25));
		removeQueryButton.setPreferredSize(new java.awt.Dimension(25, 25));
		removeQueryButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						removeQueryButtonActionPerformed(evt);
					}
				});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		panel_saved_queries_header.add(removeQueryButton, gridBagConstraints);

		panel_saved_queries.add(panel_saved_queries_header,
				java.awt.BorderLayout.NORTH);

		add(panel_saved_queries, java.awt.BorderLayout.CENTER);

	}// </editor-fold>//GEN-END:initComponents

	private void removeQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_removeQueryButtonActionPerformed
		TreePath selected_path = treeSavedQueries.getSelectionPath();
		if (selected_path == null)
			return;

		if (JOptionPane.showConfirmDialog(this,
				"This will delete the selected query. \n Continue? ",
				"Delete confirmation", JOptionPane.WARNING_MESSAGE,
				JOptionPane.YES_NO_OPTION) == JOptionPane.CANCEL_OPTION) {
			return;
		}

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) selected_path
				.getLastPathComponent();
		if (node instanceof TreeElement) {
			TreeElement element = (TreeElement) node;
			QueryController qc = this.queryController;
			if (node instanceof QueryTreeElement) {
				qc.removeQuery(element.getID());
			} else if (node instanceof QueryGroupTreeElement) {
				qc.removeGroup(element.getID());
			}
		}
	}// GEN-LAST:event_removeQueryButtonActionPerformed

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel lavel_saved_queries;
	private javax.swing.JPanel panel_saved_queries;
	private javax.swing.JPanel panel_saved_queries_header;
	private javax.swing.JButton removeQueryButton;
	private javax.swing.JScrollPane scroll_saved_queries;
	private javax.swing.JTree treeSavedQueries;

	// End of variables declaration//GEN-END:variables

	public void fireQueryChanged(String newgroup, String newquery, String newid) {
		for (SavedQueriesPanelListener listener : listeners) {
			listener.selectedQuerychanged(newgroup, newquery, newid);
		}
	}

	/**
	 * Selects the query or group added into the tree
	 */
	public void elementAdded(QueryControllerEntity element) {
		if (element instanceof QueryControllerGroup) {
			QueryControllerGroup elementGroup = (QueryControllerGroup) element;
			QueryControllerTreeModel queryControllerT = this.queryController
					.getTreeModel();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) queryControllerT
					.getNode(elementGroup.getID());

			treeSavedQueries.requestFocus();
			treeSavedQueries.expandPath(new TreePath(node.getPath()));
			treeSavedQueries.setSelectionPath(new TreePath(
					((DefaultMutableTreeNode) node).getPath()));
			treeSavedQueries.scrollPathToVisible(new TreePath(
					((DefaultMutableTreeNode) node).getPath()));
		}
		if (element instanceof QueryControllerQuery) {
			QueryControllerQuery elementQuery = (QueryControllerQuery) element;
			QueryControllerTreeModel queryControllerT = this.queryController
					.getTreeModel();

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) queryControllerT
					.getNode(elementQuery.getID());

			treeSavedQueries.requestFocus();
			treeSavedQueries.expandPath(new TreePath(node.getPath()));
			treeSavedQueries.setSelectionPath(new TreePath(
					((DefaultMutableTreeNode) node).getPath()));
			treeSavedQueries.scrollPathToVisible(new TreePath(
					((DefaultMutableTreeNode) node).getPath()));
		}
	}

	/**
	 * Selects the new query added into a group
	 */
	public void elementAdded(QueryControllerQuery query,
			QueryControllerGroup group) {
		QueryControllerQuery elementTreeQuery = (QueryControllerQuery) query;
		QueryControllerGroup elementTreeGroup = (QueryControllerGroup) group;
		QueryControllerTreeModel queryControllerT = this.queryController
				.getTreeModel();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) queryControllerT
				.getElementQuery(elementTreeQuery.getID(), elementTreeGroup
						.getID());

		treeSavedQueries.requestFocus();
		treeSavedQueries.setSelectionPath(new TreePath(node.getPath()));
		treeSavedQueries.expandPath(new TreePath(node.getPath()));
		treeSavedQueries.scrollPathToVisible(new TreePath(node.getPath()));
	}

	public void elementRemoved(QueryControllerEntity element) {
	}

	public void elementRemoved(QueryControllerQuery query,
			QueryControllerGroup group) {
	}

	/**
	 * Selects the query that was moved using Drag&Drop into a group
	 */
	public void elementChanged(QueryControllerQuery query,
			QueryControllerGroup group) {
		QueryControllerTreeModel queryControllerT = this.queryController
				.getTreeModel();
		QueryControllerQuery elementTreeQuery = (QueryControllerQuery) query;
		QueryControllerGroup elementTreeGroup = (QueryControllerGroup) group;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) queryControllerT
				.getElementQuery(elementTreeQuery.getID(), elementTreeGroup
						.getID());

		treeSavedQueries.requestFocus();
		treeSavedQueries.setSelectionPath(new TreePath(node.getPath()));
		treeSavedQueries.expandPath(new TreePath(node.getPath()));
		treeSavedQueries.scrollPathToVisible(new TreePath(node.getPath()));
	}

	/**
	 * Selects the query moved using Drag&Drop
	 */
	public void elementChanged(QueryControllerQuery query) {
		QueryControllerQuery elementQuery = (QueryControllerQuery) query;
		QueryControllerTreeModel queryControllerT = this.queryController
				.getTreeModel();

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) queryControllerT
				.getNode(elementQuery.getID());

		treeSavedQueries.requestFocus();
		treeSavedQueries.expandPath(new TreePath(node.getPath()));
		treeSavedQueries.setSelectionPath(new TreePath(
				((DefaultMutableTreeNode) node).getPath()));
		treeSavedQueries.scrollPathToVisible(new TreePath(
				((DefaultMutableTreeNode) node).getPath()));
	}

	/**
	 * This class can be used to make a rearrangeable DnD tree with the
	 * TransferableTreeNode class as the transfer data type.
	 */
	class TreeDragSource implements DragSourceListener, DragGestureListener {
		DragSource source;
		DragGestureRecognizer recognizer;
		TransferableTreeNode transferable;
		DefaultMutableTreeNode oldNode;
		JTree sourceTree;

		public TreeDragSource(JTree tree, int actions) {
			sourceTree = tree;
			source = new DragSource();
			recognizer = source.createDefaultDragGestureRecognizer(sourceTree,
					actions, this);
		}

		/**
		 * Drag Gesture Handler
		 */
		public void dragGestureRecognized(DragGestureEvent dge) {
			TreePath path = sourceTree.getSelectionPath();
			if ((path == null) || (path.getPathCount() <= 1)) {
				return;
			}
			oldNode = (DefaultMutableTreeNode) path.getLastPathComponent();
			transferable = new TransferableTreeNode(path);
			source.startDrag(dge, DragSource.DefaultCopyDrop, transferable,
					this);
		}

		/**
		 * Drag Event Handlers
		 */
		public void dragEnter(DragSourceDragEvent dsde) {
		}

		public void dragExit(DragSourceEvent dse) {
		}

		public void dragOver(DragSourceDragEvent dsde) {
		}

		public void dropActionChanged(DragSourceDragEvent dsde) {
		}

		public void dragDropEnd(DragSourceDropEvent dsde) {
		}
	}

	/**
	 * Class TreeDropTarget
	 */
	class TreeDropTarget implements DropTargetListener {
		DropTarget target;
		JTree targetTree;

		public TreeDropTarget(JTree tree) {
			targetTree = tree;
			target = new DropTarget(targetTree, this);
		}

		/**
		 * Drop Event Handlers
		 */
		private TreeNode getNodeForEvent(DropTargetDragEvent dtde) {
			Point p = dtde.getLocation();
			DropTargetContext dtc = dtde.getDropTargetContext();
			JTree tree = (JTree) dtc.getComponent();
			TreePath path = tree.getClosestPathForLocation(p.x, p.y);
			return (TreeNode) path.getLastPathComponent();
		}

		public void dragEnter(DropTargetDragEvent dtde) {
			TreeNode node = getNodeForEvent(dtde);
			dtde.acceptDrag(dtde.getDropAction());
		}

		public void dragOver(DropTargetDragEvent dtde) {
			TreeNode node = getNodeForEvent(dtde);
			dtde.acceptDrag(dtde.getDropAction());
		}

		public void dragExit(DropTargetEvent dte) {
		}

		public void dropActionChanged(DropTargetDragEvent dtde) {
		}

		public void drop(DropTargetDropEvent dtde) {
			Point pt = dtde.getLocation();
			QueryControllerQuery queryChanged = null;
			DropTargetContext dtc = dtde.getDropTargetContext();
			JTree tree = (JTree) dtc.getComponent();

			try {
				DefaultMutableTreeNode parent;
				TreePath parentpath = tree.getPathForLocation(pt.x, pt.y);
				if (parentpath == (null)) {
					parent = null;
				} else {
					parent = (DefaultMutableTreeNode) parentpath
							.getLastPathComponent();
				}
				Transferable tr = dtde.getTransferable();
				DataFlavor[] flavors = tr.getTransferDataFlavors();

				for (int i = 0; i < flavors.length; i++) {
					if (tr.isDataFlavorSupported(flavors[i])) {
						dtde.acceptDrop(dtde.getDropAction());
						TreePath p = (TreePath) tr.getTransferData(flavors[i]);
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) p
								.getLastPathComponent();
						QueryTreeElement queryNode = (QueryTreeElement) node;

						if ((parent instanceof QueryTreeElement)
								&& (((DefaultMutableTreeNode) parent
										.getParent()).getLevel() == 1)) {
							queryController.setEventsDisabled(true);
							queryController.removeQuery(currentId.getID());
							queryChanged = (queryController.addQuery(queryNode
									.getQuery(), currentId.getID(), parent
									.getParent().toString()));
							QueryControllerGroup group = queryController
									.getGroup(parent.getParent().toString());
							queryController.setEventsDisabled(false);
							dtde.dropComplete(true);
							refreshQueryControllerTreeM();
							queryController.fireElementChanged(queryChanged,
									group);

							return;
						}
						if (parent instanceof QueryGroupTreeElement
								&& parent.getLevel() == 1) {
							if (!(currentId instanceof QueryTreeElement))
								return;

							queryController.setEventsDisabled(true);
							queryController.removeQuery(currentId.getID());
							queryChanged = queryController.addQuery(queryNode
									.getQuery(), currentId.getID(), parent
									.toString());
							QueryControllerGroup group = queryController
									.getGroup(parent.toString());
							queryController.setEventsDisabled(false);

							dtde.dropComplete(true);

							refreshQueryControllerTreeM();
							queryController.fireElementChanged(queryChanged,
									group);

							return;
						}

						if (parent == null) {
							if (!(currentId instanceof QueryTreeElement))
								return;
							queryController.setEventsDisabled(true);
							queryController.removeQuery(currentId.getID());
							queryChanged = (queryController.addQuery(queryNode
									.getQuery(), currentId.getID()));
							queryController.setEventsDisabled(false);

							dtde.dropComplete(true);

							refreshQueryControllerTreeM();
							queryController.fireElementChanged(queryChanged);

							return;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				// Add a lock to register any exception
				dtde.rejectDrop();
			}
		}
	}

	/**
	 * Class TransferableTreeNode A Transferable TreePath to be used with Drag &
	 * Drop applications.
	 */
	class TransferableTreeNode implements Transferable {
		public DataFlavor TREE_PATH_FLAVOR = new DataFlavor(TreePath.class,
				"Tree Path");
		DataFlavor flavors[] = { TREE_PATH_FLAVOR };
		TreePath path;

		public TransferableTreeNode(TreePath tp) {
			path = tp;
		}

		public synchronized DataFlavor[] getTransferDataFlavors() {
			return flavors;
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return (flavor.getRepresentationClass() == TreePath.class);
		}

		public synchronized Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			if (isDataFlavorSupported(flavor)) {
				return (Object) path;
			} else {
				throw new UnsupportedFlavorException(flavor);
			}
		}
	}

	/**
	 * Reset and reload the content of the tree
	 */
	public void refreshQueryControllerTreeM() {
		QueryControllerTreeModel queryCTreeM = (QueryControllerTreeModel) queryController
				.getTreeModel();
		queryCTreeM.reset();
		queryCTreeM.init();
		queryCTreeM.reload();
		treeSavedQueries.setModel(queryCTreeM);
	}
}