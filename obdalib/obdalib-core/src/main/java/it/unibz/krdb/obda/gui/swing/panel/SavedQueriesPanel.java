/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro. All rights reserved.
 * 
 * The OBDA-API is licensed under the terms of the Lesser General Public License
 * v.3 (see OBDAAPI_LICENSE.txt for details). The components of this work
 * include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, b)
 * third-party components licensed under terms that may be different from those
 * of the LGPL. Information about such licenses can be found in the file named
 * OBDAAPI_3DPARTY-LICENSES.txt.
 */
package it.unibz.krdb.obda.gui.swing.panel;

import it.unibz.krdb.obda.gui.swing.IconLoader;
import it.unibz.krdb.obda.gui.swing.treemodel.QueryControllerTreeModel;
import it.unibz.krdb.obda.gui.swing.treemodel.QueryGroupTreeElement;
import it.unibz.krdb.obda.gui.swing.treemodel.QueryTreeElement;
import it.unibz.krdb.obda.gui.swing.treemodel.TreeElement;
import it.unibz.krdb.obda.io.QueryStorageManager;
import it.unibz.krdb.obda.querymanager.QueryController;
import it.unibz.krdb.obda.querymanager.QueryControllerEntity;
import it.unibz.krdb.obda.querymanager.QueryControllerGroup;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * GUI for Managing queries.
 * 
 * @author mariano
 */
public class SavedQueriesPanel extends javax.swing.JPanel {

	private static final long					serialVersionUID	= 6920100822784727963L;
	public Vector<SavedQueriesPanelListener>	listeners;
	private QueryController						queryController		= null;
	private QueryControllerTreeModel			queryControllerModel;
	private String								currentQuery		= null;
	private QueryTreeElement					currentId			= null;
	private QueryTreeElement					previousId			= null;

	/** Creates new form SavedQueriesPanel */

	public SavedQueriesPanel(QueryController queryController) {

		initComponents();
		addListenerToButtons();

		// TreeDragSource ds = new TreeDragSource(treeSavedQueries,
		// DnDConstants.ACTION_COPY_OR_MOVE);
		// TreeDropTarget dt = new TreeDropTarget(treeSavedQueries);
		listeners = new Vector<SavedQueriesPanelListener>();

		queryControllerModel = new QueryControllerTreeModel();

		treSavedQuery.setModel(queryControllerModel);
		treSavedQuery.setCellRenderer(new SavedQueriesTreeCellRenderer());
		treSavedQuery.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		setQueryController(queryController);
	}

	public void setQueryController(QueryController qmodel) {
		if (queryController != null) {
			queryController.removeListener(queryControllerModel);
		}
		this.queryController = qmodel;
		queryController.addListener(queryControllerModel);
		refreshQueryControllerTreeM();
	}

	private void addListenerToButtons() {
		cmdExport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setMultiSelectionEnabled(false);
				fc.setSelectedFile(new File("OBDAqueries.obda"));
				fc.addChoosableFileFilter(new FileFilter() {

					@Override
					public String getDescription() {
						return "obda files";
					}

					@Override
					public boolean accept(File f) {
						return f.isDirectory() || f.getName().toLowerCase().endsWith(".obda");
					}
				});
				int returnVal = fc.showSaveDialog(SavedQueriesPanel.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					QueryStorageManager man = new QueryStorageManager(queryController);
					man.saveQueries(file.toURI());
				}

			}
		});

		cmdImport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setMultiSelectionEnabled(false);
				fc.setSelectedFile(new File("OBDAqueries.obda"));
				fc.addChoosableFileFilter(new FileFilter() {

					@Override
					public String getDescription() {
						return "obda files";
					}

					@Override
					public boolean accept(File f) {
						return f.isDirectory() || f.getName().toLowerCase().endsWith(".obda");
					}
				});
				int returnVal = fc.showOpenDialog(SavedQueriesPanel.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					QueryStorageManager man = new QueryStorageManager(queryController);
					man.loadQueries(file.toURI());
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
	// <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pnlSavedQuery = new javax.swing.JPanel();
        scrSavedQuery = new javax.swing.JScrollPane();
        treSavedQuery = new javax.swing.JTree();
        pnlCommandPanel = new javax.swing.JPanel();
        lblSavedQuery = new javax.swing.JLabel();
        cmdRemove = new javax.swing.JButton();
        cmdImport = new javax.swing.JButton();
        cmdExport = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        pnlSavedQuery.setMinimumSize(new java.awt.Dimension(200, 50));
        pnlSavedQuery.setLayout(new java.awt.BorderLayout());

        scrSavedQuery.setMinimumSize(new java.awt.Dimension(400, 200));
        scrSavedQuery.setOpaque(false);
        scrSavedQuery.setPreferredSize(new java.awt.Dimension(300, 200));

        treSavedQuery.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        treSavedQuery.setForeground(new java.awt.Color(51, 51, 51));
        treSavedQuery.setRootVisible(false);
        treSavedQuery.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                reselectQueryNode(evt);
            }
        });
        treSavedQuery.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                selectQueryNode(evt);
            }
        });
        scrSavedQuery.setViewportView(treSavedQuery);

        pnlSavedQuery.add(scrSavedQuery, java.awt.BorderLayout.CENTER);

        pnlCommandPanel.setLayout(new java.awt.GridBagLayout());

        lblSavedQuery.setFont(new java.awt.Font("Arial", 1, 11));
        lblSavedQuery.setForeground(new java.awt.Color(153, 153, 153));
        lblSavedQuery.setText("Stored Query:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.5;
        pnlCommandPanel.add(lblSavedQuery, gridBagConstraints);

        cmdRemove.setIcon(IconLoader.getImageIcon("images/minus.png"));
        cmdRemove.setToolTipText("Remove the selected datasource");
        cmdRemove.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cmdRemove.setContentAreaFilled(false);
        cmdRemove.setIconTextGap(0);
        cmdRemove.setMaximumSize(new java.awt.Dimension(25, 25));
        cmdRemove.setMinimumSize(new java.awt.Dimension(25, 25));
        cmdRemove.setPreferredSize(new java.awt.Dimension(25, 25));
        cmdRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRemoveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCommandPanel.add(cmdRemove, gridBagConstraints);

        cmdImport.setText("Import");
        cmdImport.setToolTipText("Import queries from an obda file");
        cmdImport.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cmdImport.setContentAreaFilled(false);
        cmdImport.setEnabled(false);
        cmdImport.setMaximumSize(new java.awt.Dimension(100, 25));
        cmdImport.setMinimumSize(new java.awt.Dimension(25, 25));
        cmdImport.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCommandPanel.add(cmdImport, gridBagConstraints);

        cmdExport.setText("Export");
        cmdExport.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cmdExport.setContentAreaFilled(false);
        cmdExport.setEnabled(false);
        cmdExport.setMaximumSize(new java.awt.Dimension(100, 25));
        cmdExport.setMinimumSize(new java.awt.Dimension(50, 25));
        cmdExport.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pnlCommandPanel.add(cmdExport, gridBagConstraints);

        pnlSavedQuery.add(pnlCommandPanel, java.awt.BorderLayout.NORTH);

        add(pnlSavedQuery, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    private void selectQueryNode(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_selectQueryNode
        TreePath path = evt.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();

        if (node instanceof QueryTreeElement) {
            QueryTreeElement queryElement = (QueryTreeElement)node;
            currentQuery = queryElement.getQuery();
            currentId = queryElement;
            
            TreeNode parent = queryElement.getParent();
            if (parent == null || parent.toString().equals("")) {
                fireQueryChanged(null, currentQuery, currentId.getID());
            }
            else {
                fireQueryChanged(parent.toString(), currentQuery, currentId.getID());
            }
        }
        else if (node instanceof QueryGroupTreeElement) {
            QueryGroupTreeElement groupElement = (QueryGroupTreeElement)node;
            currentId = null;
            currentQuery = null;
            fireQueryChanged(groupElement.toString(), null, null);
        }
        else if (node == null) {
            currentId = null;
            currentQuery = null;
        }
    }//GEN-LAST:event_selectQueryNode

	private void reselectQueryNode(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reselectQueryNode
		if (currentId == null) {
			return;
		}
		
		if (previousId == currentId) {
			fireQueryChanged(currentId.getParent().toString(), currentId.getQuery(), currentId.getID());
		}
		else { // register the selected node
			previousId = currentId;
		}
	}//GEN-LAST:event_reselectQueryNode

	private void cmdRemoveActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_removeQueryButtonActionPerformed
		TreePath selected_path = treSavedQuery.getSelectionPath();
		if (selected_path == null)
			return;

		if (JOptionPane.showConfirmDialog(this, "This will delete the selected query. \n Continue? ", "Delete confirmation",
				JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION) == JOptionPane.CANCEL_OPTION) {
			return;
		}

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) selected_path.getLastPathComponent();
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
    private javax.swing.JButton cmdExport;
    private javax.swing.JButton cmdImport;
    private javax.swing.JButton cmdRemove;
    private javax.swing.JLabel lblSavedQuery;
    private javax.swing.JPanel pnlCommandPanel;
    private javax.swing.JPanel pnlSavedQuery;
    private javax.swing.JScrollPane scrSavedQuery;
    private javax.swing.JTree treSavedQuery;
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
			String nodeId = elementGroup.getID();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) queryControllerModel.getNode(nodeId);

			treSavedQuery.requestFocus();
			treSavedQuery.expandPath(new TreePath(node.getPath()));
			treSavedQuery.setSelectionPath(new TreePath(((DefaultMutableTreeNode) node).getPath()));
			treSavedQuery.scrollPathToVisible(new TreePath(((DefaultMutableTreeNode) node).getPath()));
		}
		if (element instanceof QueryControllerQuery) {
			QueryControllerQuery elementQuery = (QueryControllerQuery) element;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) queryControllerModel.getNode(elementQuery.getID());

			treSavedQuery.requestFocus();
			treSavedQuery.expandPath(new TreePath(node.getPath()));
			treSavedQuery.setSelectionPath(new TreePath(((DefaultMutableTreeNode) node).getPath()));
			treSavedQuery.scrollPathToVisible(new TreePath(((DefaultMutableTreeNode) node).getPath()));
		}
	}

	/**
	 * Selects the new query added into a group
	 */
	public void elementAdded(QueryControllerQuery query, QueryControllerGroup group) {
		QueryControllerQuery elementTreeQuery = (QueryControllerQuery) query;
		QueryControllerGroup elementTreeGroup = (QueryControllerGroup) group;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) queryControllerModel.getElementQuery(elementTreeQuery.getID(),
				elementTreeGroup.getID());

		treSavedQuery.requestFocus();
		treSavedQuery.setSelectionPath(new TreePath(node.getPath()));
		treSavedQuery.expandPath(new TreePath(node.getPath()));
		treSavedQuery.scrollPathToVisible(new TreePath(node.getPath()));
	}

	public void elementRemoved(QueryControllerEntity element) {
	}

	public void elementRemoved(QueryControllerQuery query, QueryControllerGroup group) {
	}

	/**
	 * Selects the query that was moved using Drag&Drop into a group
	 */
	public void elementChanged(QueryControllerQuery query, QueryControllerGroup group) {
		QueryControllerQuery elementTreeQuery = (QueryControllerQuery) query;
		QueryControllerGroup elementTreeGroup = (QueryControllerGroup) group;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) queryControllerModel.getElementQuery(elementTreeQuery.getID(),
				elementTreeGroup.getID());

		treSavedQuery.requestFocus();
		treSavedQuery.setSelectionPath(new TreePath(node.getPath()));
		treSavedQuery.expandPath(new TreePath(node.getPath()));
		treSavedQuery.scrollPathToVisible(new TreePath(node.getPath()));
	}

	/**
	 * Selects the query moved using Drag&Drop
	 */
	public void elementChanged(QueryControllerQuery query) {
		QueryControllerQuery elementQuery = (QueryControllerQuery) query;
		String nodeId = elementQuery.getID();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) queryControllerModel.getNode(nodeId);

		treSavedQuery.requestFocus();
		treSavedQuery.expandPath(new TreePath(node.getPath()));
		treSavedQuery.setSelectionPath(new TreePath(((DefaultMutableTreeNode) node).getPath()));
		treSavedQuery.scrollPathToVisible(new TreePath(((DefaultMutableTreeNode) node).getPath()));
	}

	/**
	 * This class can be used to make a rearrangeable DnD tree with the
	 * TransferableTreeNode class as the transfer data type.
	 */
	class TreeDragSource implements DragSourceListener, DragGestureListener {
		DragSource				source;
		DragGestureRecognizer	recognizer;
		TransferableTreeNode	transferable;
		DefaultMutableTreeNode	oldNode;
		JTree					sourceTree;

		public TreeDragSource(JTree tree, int actions) {
			sourceTree = tree;
			source = new DragSource();
			recognizer = source.createDefaultDragGestureRecognizer(sourceTree, actions, this);
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
			source.startDrag(dge, DragSource.DefaultCopyDrop, transferable, this);
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
		DropTarget	target;
		JTree		targetTree;

		public TreeDropTarget(JTree tree) {
			targetTree = tree;
			target = new DropTarget(targetTree, this);
		}

		/**
		 * Drop Event Handlers
		 */
		// private TreeNode getNodeForEvent(DropTargetDragEvent dtde) {
		// Point p = dtde.getLocation();
		// DropTargetContext dtc = dtde.getDropTargetContext();
		// JTree tree = (JTree) dtc.getComponent();
		// TreePath path = tree.getClosestPathForLocation(p.x, p.y);
		// return (TreeNode) path.getLastPathComponent();
		// }

		public void dragEnter(DropTargetDragEvent dtde) {
			// TreeNode node = getNodeForEvent(dtde);
			dtde.acceptDrag(dtde.getDropAction());
		}

		public void dragOver(DropTargetDragEvent dtde) {
			// TreeNode node = getNodeForEvent(dtde);
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
					parent = (DefaultMutableTreeNode) parentpath.getLastPathComponent();
				}
				Transferable tr = dtde.getTransferable();
				DataFlavor[] flavors = tr.getTransferDataFlavors();

				for (int i = 0; i < flavors.length; i++) {
					if (tr.isDataFlavorSupported(flavors[i])) {
						dtde.acceptDrop(dtde.getDropAction());
						TreePath p = (TreePath) tr.getTransferData(flavors[i]);
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) p.getLastPathComponent();
						QueryTreeElement queryNode = (QueryTreeElement) node;

						if ((parent instanceof QueryTreeElement) && (((DefaultMutableTreeNode) parent.getParent()).getLevel() == 1)) {
							queryController.setEventsDisabled(true);
							queryController.removeQuery(currentId.getID());
							queryChanged = (queryController
									.addQuery(queryNode.getQuery(), currentId.getID(), parent.getParent().toString()));
							QueryControllerGroup group = queryController.getGroup(parent.getParent().toString());
							queryController.setEventsDisabled(false);
							dtde.dropComplete(true);
							refreshQueryControllerTreeM();
							queryController.fireElementChanged(queryChanged, group);

							return;
						}
						if (parent instanceof QueryGroupTreeElement && parent.getLevel() == 1) {
							if (!(currentId instanceof QueryTreeElement))
								return;

							queryController.setEventsDisabled(true);
							queryController.removeQuery(currentId.getID());
							queryChanged = queryController.addQuery(queryNode.getQuery(), currentId.getID(), parent.toString());
							QueryControllerGroup group = queryController.getGroup(parent.toString());
							queryController.setEventsDisabled(false);

							dtde.dropComplete(true);

							refreshQueryControllerTreeM();
							queryController.fireElementChanged(queryChanged, group);

							return;
						}

						if (parent == null) {
							if (!(currentId instanceof QueryTreeElement))
								return;
							queryController.setEventsDisabled(true);
							queryController.removeQuery(currentId.getID());
							queryChanged = (queryController.addQuery(queryNode.getQuery(), currentId.getID()));
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
		public DataFlavor	TREE_PATH_FLAVOR	= new DataFlavor(TreePath.class, "Tree Path");
		DataFlavor			flavors[]			= { TREE_PATH_FLAVOR };
		TreePath			path;

		public TransferableTreeNode(TreePath tp) {
			path = tp;
		}

		public synchronized DataFlavor[] getTransferDataFlavors() {
			return flavors;
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return (flavor.getRepresentationClass() == TreePath.class);
		}

		public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
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
		queryControllerModel.reset();
		queryControllerModel.synchronize(queryController.getElements());
		queryControllerModel.reload();
		treSavedQuery.setModel(queryControllerModel);
	}
}