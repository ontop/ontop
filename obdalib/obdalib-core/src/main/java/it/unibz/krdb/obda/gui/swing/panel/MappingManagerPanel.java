/***
 * 
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

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.gui.swing.dialog.MappingValidationDialog;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingBodyNode;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingHeadNode;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingNode;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingTreeModel;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingTreeNodeCellEditor;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingTreeSelectionModel;
import it.unibz.krdb.obda.gui.swing.treemodel.TargetQueryVocabularyValidator;
import it.unibz.krdb.obda.gui.swing.treemodel.TreeModelFilter;
import it.unibz.krdb.obda.gui.swing.utils.DatasourceSelectorListener;
import it.unibz.krdb.obda.gui.swing.utils.MappingFilterLexer;
import it.unibz.krdb.obda.gui.swing.utils.MappingFilterParser;
import it.unibz.krdb.obda.gui.swing.utils.MappingTreeCellRenderer2;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.parser.DatalogProgramParser;
import it.unibz.krdb.obda.parser.DatalogQueryHelper;
import it.unibz.krdb.obda.utils.OBDAPreferenceChangeListener;
import it.unibz.krdb.obda.utils.OBDAPreferences;
import it.unibz.krdb.obda.utils.RDBMSMappingValidator;
import it.unibz.krdb.obda.utils.SourceQueryValidator;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingManagerPanel extends JPanel implements OBDAPreferenceChangeListener, DatasourceSelectorListener {

	private static final long serialVersionUID = -486013653814714526L;

	private DefaultMutableTreeNode editedNode;
	private OBDAPreferences preference;
	private KeyStroke addMapping;
	private KeyStroke editBody;
	private KeyStroke editHead;
	private KeyStroke editID;

	private Thread validatorThread;

	private SourceQueryValidator validator;

	private TargetQueryVocabularyValidator validatortrg;

	private OBDAModel mapc;

	protected OBDAModel apic;

	// private OWLOntology ontology;

	private DatalogProgramParser datalogParser;

	private OBDADataSource selectedSource;

	private boolean canceled;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	/**
	 * Creates a new panel.
	 * 
	 * @param apic
	 *            The API controller object.
	 * @param preference
	 *            The preference object.
	 */
	public MappingManagerPanel(OBDAModel apic, OBDAPreferences preference, TargetQueryVocabularyValidator validator) {

		this.preference = preference;
		datalogParser = new DatalogProgramParser();
		this.validatortrg = validator;

		initComponents();
		registerAction();
		addMenu();

		/***********************************************************************
		 * Setting up the mappings tree
		 */
//		mappingsTree.setRootVisible(false);

		// MappingTreeCellRenderer map_renderer = new
		// MappingTreeCellRenderer(apic, preference);
		
		MappingTreeCellRenderer2 map_renderer = new MappingTreeCellRenderer2(preference);
//		scrMappingsTree.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		mappingsTree.setCellRenderer(map_renderer);
		mappingsTree.setEditable(true);
		mappingsTree.setCellEditor(new MappingTreeNodeCellEditor(apic, validatortrg, preference));
		mappingsTree.setSelectionModel(new MappingTreeSelectionModel());
		mappingsTree.setRowHeight(0);
//		mappingsTree.setMaximumSize(new Dimension(scrMappingsTree.getWidth() - 50, 65000));
		mappingsTree.setToggleClickCount(1);
		mappingsTree.setRootVisible(true);
//		mappingsTree.setInvokesStopCellEditing(true);
		cmdAddMapping.setToolTipText("Add a new mapping");
		cmdRemoveMapping.setToolTipText("Remove selected mappings");
		cmdDuplicateMapping.setToolTipText("Duplicate selected mappings");
		preference.registerPreferenceChangedListener(this);

		setOBDAModel(apic); // TODO Bad code! Change this later!
	}

	public void setOBDAModel(OBDAModel omodel) {
		this.apic = omodel;
		this.mapc = apic;
		MappingTreeModel maptreemodel = new MappingTreeModel(apic);
		mapc.addMappingsListener(maptreemodel);
		mappingsTree.setModel(maptreemodel);
	}

	public void setTargetQueryValidator(TargetQueryVocabularyValidator validator) {
		this.validatortrg = validator;
	}

	//
	// public OWLOntology getOntology() {
	// return ontology;
	// }

	private void registerAction() {

		InputMap inputmap = mappingsTree.getInputMap();
		ActionMap actionmap = mappingsTree.getActionMap();

		lblInsertFilter.setBackground(new java.awt.Color(153, 153, 153));
		lblInsertFilter.setFont(new java.awt.Font("Arial", 1, 11));
		lblInsertFilter.setForeground(new java.awt.Color(153, 153, 153));
		lblInsertFilter.setPreferredSize(new Dimension(75, 14));
		lblInsertFilter.setText("Insert Filter: ");

		cmdAddMapping.setText("Insert");
		cmdAddMapping.setMnemonic('i');
		cmdAddMapping.setIcon(null);
		cmdAddMapping.setPreferredSize(new Dimension(40, 21));
		cmdAddMapping.setMinimumSize(new Dimension(40, 21));

		cmdRemoveMapping.setText("Remove");
		cmdRemoveMapping.setMnemonic('r');
		cmdRemoveMapping.setIcon(null);
		cmdRemoveMapping.setPreferredSize(new Dimension(50, 21));
		cmdRemoveMapping.setMinimumSize(new Dimension(50, 21));

		cmdDuplicateMapping.setText("Duplicate");
		cmdDuplicateMapping.setMnemonic('d');
		cmdDuplicateMapping.setIcon(null);
		cmdDuplicateMapping.setPreferredSize(new Dimension(60, 21));
		cmdDuplicateMapping.setMinimumSize(new Dimension(60, 21));

		String add = preference.get(OBDAPreferences.ADD_MAPPING).toString();
		addMapping = KeyStroke.getKeyStroke(add);
		String body = preference.get(OBDAPreferences.EDIT_BODY).toString();
		editBody = KeyStroke.getKeyStroke(body);
		String head = preference.get(OBDAPreferences.EDIT_HEAD).toString();
		editHead = KeyStroke.getKeyStroke(head);
		String id = preference.get(OBDAPreferences.EDIT_ID).toString();
		editID = KeyStroke.getKeyStroke(id);

		AbstractAction addAction = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6534327279595000401L;

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				addMapping();
			}
		};
		inputmap.put(addMapping, OBDAPreferences.ADD_MAPPING);
		actionmap.put(OBDAPreferences.ADD_MAPPING, addAction);

		AbstractAction editBodyAction = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5829816762114042796L;

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				TreePath path = mappingsTree.getSelectionPath();
				if (path == null) {
					return;
				}
				startEditBodyOfMapping(path);
			}
		};
		inputmap.put(editBody, OBDAPreferences.EDIT_BODY);
		actionmap.put(OBDAPreferences.EDIT_BODY, editBodyAction);

		AbstractAction editHeadAction = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -2418619999144506977L;

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				TreePath path = mappingsTree.getSelectionPath();
				if (path == null) {
					return;
				}
				startEditHeadOfMapping(path);
			}
		};
		inputmap.put(editHead, OBDAPreferences.EDIT_HEAD);
		actionmap.put(OBDAPreferences.EDIT_HEAD, editHeadAction);

		AbstractAction editIDAction = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1643902301281442494L;

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				TreePath path = mappingsTree.getSelectionPath();
				if (path == null) {
					return;
				}
				mappingsTree.setEditable(true);
				editedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
				mappingsTree.startEditingAtPath(path);
			}
		};
		inputmap.put(editID, OBDAPreferences.EDIT_ID);
		actionmap.put(OBDAPreferences.EDIT_ID, editIDAction);
	}

	private void addMenu() {
		JMenuItem add = new JMenuItem();
		add.setText("Add Mapping");
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addMapping();
			}
		});
		add.setMnemonic(addMapping.getKeyCode());
		add.setAccelerator(addMapping);
		menuMappings.add(add);

		JMenuItem delete = new JMenuItem();
		delete.setText("Remove Mapping");
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeMapping();
			}
		});
		menuMappings.add(delete);
		menuMappings.addSeparator();

		JMenuItem editID = new JMenuItem();
		editID.setText("Edit Mapping ID");
		editID.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath path = mappingsTree.getSelectionPath();
				if (path == null) {
					return;
				}
				startEditIdOfMapping(path);
			}
		});
		editID.setMnemonic(this.editID.getKeyCode());
		editID.setAccelerator(this.editID);
		menuMappings.add(editID);

		JMenuItem editHead = new JMenuItem();
		editHead.setText("Edit Mapping Target Query");
		editHead.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath path = mappingsTree.getSelectionPath();
				if (path == null) {
					return;
				}
				startEditHeadOfMapping(path);
			}
		});
		editHead.setMnemonic(this.editHead.getKeyCode());
		editHead.setAccelerator(this.editHead);
		menuMappings.add(editHead);

		JMenuItem editBody = new JMenuItem();
		editBody.setText("Edit Mapping Source Query");
		editBody.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath path = mappingsTree.getSelectionPath();
				if (path == null) {
					return;
				}
				startEditBodyOfMapping(path);
			}
		});
		editBody.setMnemonic(this.editBody.getKeyCode());
		editBody.setAccelerator(this.editBody);
		menuMappings.add(editBody);
		menuMappings.addSeparator();

		menuValidateAll = new JMenuItem();
		menuValidateAll.setText("Validate All");
		menuValidateAll.setEnabled(false);
		menuValidateAll.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuValidateAllActionPerformed(evt);
			}
		});
		menuMappings.add(menuValidateAll);

		menuValidateHead = new JMenuItem();
		menuValidateHead.setText("Validate Target Query");
		menuValidateHead.setEnabled(false);
		menuValidateHead.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuValidateHeadActionPerformed(evt);
			}
		});
		menuMappings.add(menuValidateHead);

		menuValidateBody = new JMenuItem();
		menuValidateBody.setText("Validate Source Query");
		menuValidateBody.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuValidateBodyActionPerformed(evt);
			}
		});
		menuMappings.add(menuValidateBody);

		menuExecuteBody = new JMenuItem();
		menuExecuteBody.setText("Execute Source Query");
		menuExecuteBody.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuExecuteBodyActionPerformed(evt);
			}
		});
		menuMappings.add(menuExecuteBody);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		menuMappings = new javax.swing.JPopupMenu();
		pnlMappingManager = new javax.swing.JPanel();
		pnlMappingButtons = new javax.swing.JPanel();
		lblInsertFilter = new javax.swing.JLabel();
		txtFilter = new javax.swing.JTextField();
		chkFilter = new javax.swing.JCheckBox();
		cmdAddMapping = new javax.swing.JButton();
		cmdRemoveMapping = new javax.swing.JButton();
		cmdDuplicateMapping = new javax.swing.JButton();
		scrMappingsTree = new javax.swing.JScrollPane();
		mappingsTree = new javax.swing.JTree();
		pnlExtraButtons = new javax.swing.JPanel();
		cmdSelectAll = new javax.swing.JButton();
		cmdDeselectAll = new javax.swing.JButton();
		cmdExpandAll = new javax.swing.JButton();
		cmdCollapseAll = new javax.swing.JButton();

		setLayout(new java.awt.GridBagLayout());

		pnlMappingManager.setAutoscrolls(true);
		pnlMappingManager.setPreferredSize(new java.awt.Dimension(400, 200));
		pnlMappingManager.setLayout(new java.awt.BorderLayout());

		pnlMappingButtons.setEnabled(false);
		pnlMappingButtons.setLayout(new java.awt.GridBagLayout());

		lblInsertFilter.setText("Insert Filter");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
		pnlMappingButtons.add(lblInsertFilter, gridBagConstraints);

		txtFilter.setPreferredSize(new java.awt.Dimension(250, 20));
		txtFilter.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent evt) {
				sendFilters(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 0.9;
		gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
		pnlMappingButtons.add(txtFilter, gridBagConstraints);

		chkFilter.setText("Apply Filters");
		chkFilter.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				chkFilterItemStateChanged(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
		pnlMappingButtons.add(chkFilter, gridBagConstraints);

		cmdAddMapping.setToolTipText("Add new mapping");
		cmdAddMapping.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cmdAddMapping.setContentAreaFilled(false);
		cmdAddMapping.setIconTextGap(0);
		cmdAddMapping.setMaximumSize(new java.awt.Dimension(25, 25));
		cmdAddMapping.setMinimumSize(new java.awt.Dimension(25, 25));
		cmdAddMapping.setPreferredSize(new java.awt.Dimension(25, 25));
		cmdAddMapping.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cmdAddMappingActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
		pnlMappingButtons.add(cmdAddMapping, gridBagConstraints);

		cmdRemoveMapping.setToolTipText("Remove mappings");
		cmdRemoveMapping.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cmdRemoveMapping.setContentAreaFilled(false);
		cmdRemoveMapping.setIconTextGap(0);
		cmdRemoveMapping.setMaximumSize(new java.awt.Dimension(25, 25));
		cmdRemoveMapping.setMinimumSize(new java.awt.Dimension(25, 25));
		cmdRemoveMapping.setPreferredSize(new java.awt.Dimension(25, 25));
		cmdRemoveMapping.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cmdRemoveMappingActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
		pnlMappingButtons.add(cmdRemoveMapping, gridBagConstraints);

		cmdDuplicateMapping.setToolTipText("Duplicate mappings");
		cmdDuplicateMapping.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cmdDuplicateMapping.setContentAreaFilled(false);
		cmdDuplicateMapping.setIconTextGap(0);
		cmdDuplicateMapping.setMaximumSize(new java.awt.Dimension(25, 25));
		cmdDuplicateMapping.setMinimumSize(new java.awt.Dimension(25, 25));
		cmdDuplicateMapping.setPreferredSize(new java.awt.Dimension(25, 25));
		cmdDuplicateMapping.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cmdDuplicateMappingActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
		pnlMappingButtons.add(cmdDuplicateMapping, gridBagConstraints);

		pnlMappingManager.add(pnlMappingButtons, java.awt.BorderLayout.NORTH);

		mappingsTree.setComponentPopupMenu(menuMappings);
		mappingsTree.setEditable(true);
		scrMappingsTree.setViewportView(mappingsTree);

		pnlMappingManager.add(scrMappingsTree, java.awt.BorderLayout.CENTER);

		pnlExtraButtons.setMinimumSize(new java.awt.Dimension(532, 25));
		pnlExtraButtons.setPreferredSize(new java.awt.Dimension(532, 25));
		pnlExtraButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));

		cmdSelectAll.setText("Select All");
		cmdSelectAll.setToolTipText("Select all");
		cmdSelectAll.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cmdSelectAll.setMaximumSize(new java.awt.Dimension(70, 21));
		cmdSelectAll.setMinimumSize(new java.awt.Dimension(70, 21));
		cmdSelectAll.setPreferredSize(new java.awt.Dimension(70, 21));
		cmdSelectAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cmdSelectAllActionPerformed(evt);
			}
		});
		pnlExtraButtons.add(cmdSelectAll);

		cmdDeselectAll.setText("Deselect All");
		cmdDeselectAll.setToolTipText("Deselect all");
		cmdDeselectAll.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cmdDeselectAll.setMaximumSize(new java.awt.Dimension(70, 21));
		cmdDeselectAll.setMinimumSize(new java.awt.Dimension(70, 21));
		cmdDeselectAll.setPreferredSize(new java.awt.Dimension(70, 21));
		cmdDeselectAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cmdDeselectAllActionPerformed(evt);
			}
		});
		pnlExtraButtons.add(cmdDeselectAll);

		cmdExpandAll.setText("Expand All");
		cmdExpandAll.setToolTipText("Expand all the mapping nodes or the selected ones");
		cmdExpandAll.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cmdExpandAll.setMaximumSize(new java.awt.Dimension(70, 21));
		cmdExpandAll.setMinimumSize(new java.awt.Dimension(70, 21));
		cmdExpandAll.setPreferredSize(new java.awt.Dimension(70, 21));
		cmdExpandAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cmdExpandAllActionPerformed(evt);
			}
		});
		pnlExtraButtons.add(cmdExpandAll);

		cmdCollapseAll.setText("Collapse All");
		cmdCollapseAll.setToolTipText("Collapse all the mapping nodes or the selected ones");
		cmdCollapseAll.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cmdCollapseAll.setMaximumSize(new java.awt.Dimension(70, 21));
		cmdCollapseAll.setMinimumSize(new java.awt.Dimension(70, 21));
		cmdCollapseAll.setPreferredSize(new java.awt.Dimension(70, 21));
		cmdCollapseAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cmdCollapseAllActionPerformed(evt);
			}
		});
		pnlExtraButtons.add(cmdCollapseAll);

		pnlMappingManager.add(pnlExtraButtons, java.awt.BorderLayout.SOUTH);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(pnlMappingManager, gridBagConstraints);
	}// </editor-fold>//GEN-END:initComponents

	private void cmdSelectAllActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdSelectAllActionPerformed
		TreePath[] paths = getAllMappingNodePaths();
		mappingsTree.addSelectionPaths(paths);
	}// GEN-LAST:event_cmdSelectAllActionPerformed

	private void cmdDeselectAllActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdDeselectAllActionPerformed
		TreePath[] paths = getAllMappingNodePaths();
		mappingsTree.removeSelectionPaths(paths);
	}// GEN-LAST:event_cmdDeselectAllActionPerformed

	private void cmdExpandAllActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdExpandAllActionPerformed
		final int nSelectedNode = mappingsTree.getSelectionCount();

		TreePath[] paths = getAllMappingNodePaths();
		if (nSelectedNode > 1) {
			paths = mappingsTree.getSelectionPaths();
		}

		for (int i = 0; i < paths.length; i++) {
			mappingsTree.expandPath(paths[i]);
		}
	}// GEN-LAST:event_cmdExpandAllActionPerformed

	private void cmdCollapseAllActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdCollapseAllActionPerformed
		final int nSelectedNode = mappingsTree.getSelectionCount();

		TreePath[] paths = getAllMappingNodePaths();
		if (nSelectedNode > 1) {
			paths = mappingsTree.getSelectionPaths();
		}

		for (int i = 0; i < paths.length; i++) {
			mappingsTree.collapsePath(paths[i]);
		}
	}// GEN-LAST:event_cmdCollapseAllActionPerformed

	private TreePath[] getAllMappingNodePaths() {
		final MappingTreeModel mapModel = (MappingTreeModel) mappingsTree.getModel();
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) mapModel.getRoot();
		final int nChild = root.getChildCount();

		TreePath[] paths = new TreePath[nChild]; // initialize

		if (nChild > 0) {
			for (int i = 0; i < nChild; i++) {
				MappingNode child = (MappingNode) root.getChildAt(i);
				paths[i] = new TreePath(child.getPath());
			}
		}
		return paths;
	}

	/***
	 * The action for the search field and the search checkbox. If the checkbox
	 * is not selected it cleans the filters. If it is selected it updates to
	 * the current search string.
	 */
	private void processFilterAction() {
		if (!(chkFilter.isSelected())) {
			applyFilters(new ArrayList<TreeModelFilter<OBDAMappingAxiom>>());
		}
		if (chkFilter.isSelected()) {
			try {
				List<TreeModelFilter<OBDAMappingAxiom>> filters = parseSearchString(txtFilter.getText());
				if (filters == null) {
					throw new Exception("Impossible to parse search string.");
				}
				applyFilters(filters);
			} catch (Exception e) {
				LoggerFactory.getLogger(this.getClass()).debug(e.getMessage(), e);
				JOptionPane.showMessageDialog(this, e.getMessage());
			}
		}
	}

	/***
	 * Action for the filter checkbox
	 * 
	 * @param evt
	 * @throws Exception
	 */
	private void chkFilterItemStateChanged(java.awt.event.ItemEvent evt) {// GEN-FIRST:event_jCheckBox1ItemStateChanged
		processFilterAction();

	}// GEN-LAST:event_jCheckBox1ItemStateChanged

	/***
	 * Action for key's entered in the search textbox
	 * 
	 * @param evt
	 * @throws Exception
	 */
	private void sendFilters(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_sendFilters
		int key = evt.getKeyCode();
		if (key == java.awt.event.KeyEvent.VK_ENTER) {
			if (!chkFilter.isSelected()) {
				chkFilter.setSelected(true);
			} else {
				processFilterAction();
			}
		}

	}// GEN-LAST:event_sendFilters

	private void menuValidateAllActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuValidateAllActionPerformed
		MappingValidationDialog outputField = new MappingValidationDialog(mappingsTree);
		TreePath path[] = mappingsTree.getSelectionPaths();
		if (path == null) {
			return;
		}

		for (int i = 0; i < path.length; i++) {
			Object o = path[i].getLastPathComponent();
			if (o instanceof MappingNode) {
				MappingNode node = (MappingNode) o;
				String id = node.getMappingID();
				MappingBodyNode body = node.getBodyNode();
				MappingHeadNode head = node.getHeadNode();
				RDBMSMappingValidator v;

				OBDASQLQuery rdbmssqlQuery = fac.getSQLQuery(body.getQuery());
				CQIE conjunctiveQuery = parse(head.getQuery());
				v = new RDBMSMappingValidator(apic, selectedSource, rdbmssqlQuery, conjunctiveQuery);
				Enumeration<String> errors = v.validate();
				if (!errors.hasMoreElements()) {
					String output = id + ": " + "valid  \n";
					outputField.addText(output, outputField.VALID);
				} else {
					while (errors.hasMoreElements()) {
						String ele = errors.nextElement();
						String output = id + ": " + ele + "  \n";
						if (ele.startsWith("N")) {
							outputField.addText(output, outputField.NONCRITICAL_ERROR);
						} else if (ele.startsWith("C")) {
							outputField.addText(output, outputField.CRITICAL_ERROR);
						} else {
							outputField.addText(output, outputField.NORMAL);
						}
					}
				}
			}
		}
	}// GEN-LAST:event_menuValidateAllActionPerformed

	private void menuValidateBodyActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuValidateBodyActionPerformed
		final MappingValidationDialog outputField = new MappingValidationDialog(mappingsTree);
		Runnable action = new Runnable() {
			@Override
			public void run() {
				canceled = false;
				final TreePath path[] = mappingsTree.getSelectionPaths();
				if (path == null) {
					return;
				}
				outputField.addText("Validating " + path.length + " SQL queries.\n", outputField.NORMAL);
				for (int i = 0; i < path.length; i++) {
					final int index = i;
					Object o = path[index].getLastPathComponent();
					if (o instanceof MappingNode) {
						MappingNode node = (MappingNode) o;
						String id = node.getMappingID();
						MappingBodyNode body = node.getBodyNode();
						outputField.addText("  id: '" + id + "'... ", outputField.NORMAL);
						validator = new SourceQueryValidator(selectedSource, fac.getSQLQuery(body.getQuery()));
						long timestart = System.currentTimeMillis();

						if (canceled)
							return;

						if (validator.validate()) {
							long timestop = System.currentTimeMillis();
							String output = " valid  \n";
							outputField.addText("Time to query: " + ((timestop - timestart) / 1000) + " ms. Result: ", outputField.NORMAL);
							outputField.addText(output, outputField.VALID);
						} else {
							long timestop = System.currentTimeMillis();
							String output = " invalid Reason: " + validator.getReason().getMessage() + " \n";
							outputField.addText("Time to query: " + ((timestop - timestart) / 1000) + " ms. Result: ", outputField.NORMAL);
							outputField.addText(output, outputField.CRITICAL_ERROR);
						}
						validator.dispose();

						if (canceled)
							return;
					}
				}
				outputField.setVisible(true);
			}
		};
		validatorThread = new Thread(action);
		validatorThread.start();

		Thread cancelThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!outputField.closed) {
					try {
						Thread.currentThread();
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (validatorThread.isAlive()) {
					try {
						Thread.currentThread();
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					try {
						canceled = true;
						validator.cancelValidation();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		});
		cancelThread.start();

	}// GEN-LAST:event_menuValidateBodyActionPerformed

	private void menuExecuteBodyActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuExecuteBodyActionPerformed
		final TreePath path = mappingsTree.getSelectionPath();
		final MappingNode mapping = (MappingNode) path.getLastPathComponent();
		final String sqlQuery = mapping.getBodyNode().getQuery();

		SQLQueryPanel pnlQueryResult = new SQLQueryPanel(selectedSource, sqlQuery);

		JDialog dlgQueryResult = new JDialog();
		dlgQueryResult.setContentPane(pnlQueryResult);
		dlgQueryResult.pack();
		dlgQueryResult.setLocationRelativeTo(this);
		dlgQueryResult.setVisible(true);
		dlgQueryResult.setTitle("SQL Query Result");
	}// GEN-LAST:event_menuExecuteBodyActionPerformed

	private void menuValidateHeadActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuValidateHeadActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_menuValidateHeadActionPerformed

	private void cmdDuplicateMappingActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_duplicateMappingButtonActionPerformed
		TreePath[] currentSelection = mappingsTree.getSelectionPaths();
		if (currentSelection == null) {
			JOptionPane.showMessageDialog(this, "Please Select a Mapping first", "ERROR", JOptionPane.ERROR_MESSAGE);
		} else {
			if (JOptionPane.showConfirmDialog(this, "This will create copies of the selected mappings. \nNumber of mappings selected = "
					+ mappingsTree.getSelectionPaths().length + "\n Continue? ", "Copy confirmation", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
				return;
			}
			OBDAModel controller = mapc;
			URI current_srcuri = selectedSource.getSourceID();

			if (currentSelection != null) {
				for (int i = 0; i < currentSelection.length; i++) {
					TreePath current_path = currentSelection[i];
					MappingNode mapping = (MappingNode) current_path.getLastPathComponent();
					String id = (String) mapping.getUserObject();

					/* Computing the next available ID */

					int new_index = -1;
					for (int index = 0; index < 999999999; index++) {
						if (controller.indexOf(current_srcuri, id + "(" + index + ")") == -1) {
							new_index = index;
							break;
						}
					}
					String new_id = id + "(" + new_index + ")";

					/* inserting the new mapping */
					try {

						OBDAMappingAxiom oldmapping = controller.getMapping(current_srcuri, id);
						OBDAMappingAxiom newmapping = null;
						newmapping = (OBDAMappingAxiom) oldmapping.clone();
						newmapping.setId(new_id);
						controller.addMapping(current_srcuri, newmapping);

					} catch (DuplicateMappingException e) {
						JOptionPane.showMessageDialog(this, "Duplicate Mapping: " + new_id);
					}
				}
			}
		}
	}// GEN-LAST:event_duplicateMappingButtonActionPerformed

	private void cmdRemoveMappingActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_removeMappingButtonActionPerformed
		removeMapping();
	}// GEN-LAST:event_removeMappingButtonActionPerformed

	private void removeMapping() {
		if (JOptionPane.showConfirmDialog(this, "This will delete ALL the selected mappings. \nNumber of mappings selected = "
				+ mappingsTree.getSelectionPaths().length + "\n Continue? ", "Delete confirmation", JOptionPane.WARNING_MESSAGE,
				JOptionPane.YES_NO_OPTION) == JOptionPane.CANCEL_OPTION) {
			return;
		}
		// The manager panel can handle multiple deletions.
		TreePath[] currentSelection = mappingsTree.getSelectionPaths();
		OBDAModel controller = mapc;
		URI srcuri = selectedSource.getSourceID();

		if (currentSelection != null) {
			for (int i = 0; i < currentSelection.length; i++) {
				TreePath current_path = currentSelection[i];
				MappingNode mappingnode = (MappingNode) current_path.getLastPathComponent();
				controller.removeMapping(srcuri, mappingnode.getMappingID());
			}
		}
	}

	private void cmdAddMappingActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_addMappingButtonActionPerformed
		if (selectedSource != null) {
			addMapping();

			// Make sure the user can see the new node.
			MappingTreeModel model = (MappingTreeModel) mappingsTree.getModel();
			MappingNode newNode = model.getLastMappingNode();
			mappingsTree.scrollPathToVisible(new TreePath(newNode.getPath()));
		} else {
			JOptionPane.showMessageDialog(this, "Select the data source first!", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
	}// GEN-LAST:event_addMappingButtonActionPerformed

	private void addMapping() {

		JDialog dialog = new JDialog();
		dialog.setTitle("Insert New Mapping");
		dialog.setModal(true);
		dialog.setContentPane(new NewMappingDialogPanel(apic, preference, dialog, selectedSource, validatortrg));
		dialog.setSize(500, 300);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	private void startEditIdOfMapping(TreePath path) {
		mappingsTree.setEditable(true);
		editedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		mappingsTree.startEditingAtPath(path);
	}

	private void startEditHeadOfMapping(TreePath path) {
		mappingsTree.setEditable(true);
		MappingNode mapping = (MappingNode) path.getLastPathComponent();
		MappingHeadNode head = mapping.getHeadNode();
		editedNode = head;
		mappingsTree.startEditingAtPath(new TreePath(head.getPath()));
	}

	private void startEditBodyOfMapping(TreePath path) {
		mappingsTree.setEditable(true);
		MappingNode mapping = (MappingNode) path.getLastPathComponent();
		MappingBodyNode body = mapping.getBodyNode();
		editedNode = body;
		mappingsTree.startEditingAtPath(new TreePath(body.getPath()));
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JCheckBox chkFilter;
	private javax.swing.JButton cmdAddMapping;
	private javax.swing.JButton cmdCollapseAll;
	private javax.swing.JButton cmdDeselectAll;
	private javax.swing.JButton cmdDuplicateMapping;
	private javax.swing.JButton cmdExpandAll;
	private javax.swing.JButton cmdRemoveMapping;
	private javax.swing.JButton cmdSelectAll;
	private javax.swing.JLabel lblInsertFilter;
	private javax.swing.JTree mappingsTree;
	private javax.swing.JPopupMenu menuMappings;
	private javax.swing.JMenuItem menuValidateAll;
	private javax.swing.JMenuItem menuValidateBody;
	private javax.swing.JMenuItem menuValidateHead;
	private javax.swing.JMenuItem menuExecuteBody;
	private javax.swing.JPanel pnlExtraButtons;
	private javax.swing.JPanel pnlMappingButtons;
	private javax.swing.JPanel pnlMappingManager;
	private javax.swing.JScrollPane scrMappingsTree;
	private javax.swing.JTextField txtFilter;

	// End of variables declaration//GEN-END:variables

	// TODO NOTE: This method duplicates the valueForPathChanged(..) method in
	// the MappingTreeModel class.
	private void updateNode(String str) {
		OBDAModel con = mapc;
		URI sourceName = selectedSource.getSourceID();
		String nodeContent = (String) editedNode.getUserObject();
		if (editedNode instanceof MappingNode) {
			// con.updateMapping(sourceName, nodeContent, str);
		} else if (editedNode instanceof MappingBodyNode) {
			MappingBodyNode node = (MappingBodyNode) editedNode;
			MappingNode parent = (MappingNode) node.getParent();
			OBDAQuery b = fac.getSQLQuery(str);
			con.updateMappingsSourceQuery(sourceName, parent.getMappingID(), b);
		} else if (editedNode instanceof MappingHeadNode) {
			MappingHeadNode node = (MappingHeadNode) editedNode;
			MappingNode parent = (MappingNode) node.getParent();
			OBDAQuery h = parse(str);
			con.updateTargetQueryMapping(sourceName, parent.getMappingID(), h);
		}
	}

	public void shortCutChanged(String preference, String shortcut) {
		registerAction();
	}

	public void stopTreeEditing() {
		TreeCellEditor editor = mappingsTree.getCellEditor();
		if (editor.stopCellEditing()) {
			String txt = editor.getCellEditorValue().toString();
			updateNode(txt);

		}

	}

	// public void applyChangedToNode(String txt) {
	// updateNode(txt);
	// }

	/***
	 * Parses the string in the search field.
	 * 
	 * @param textToParse
	 * @return A list of filter objects or null if the string was empty or
	 *         erroneous
	 * @throws Exception
	 */
	private List<TreeModelFilter<OBDAMappingAxiom>> parseSearchString(String textToParse) throws Exception {

		List<TreeModelFilter<OBDAMappingAxiom>> listOfFilters = null;

		if (textToParse != null) {
			ANTLRStringStream inputStream = new ANTLRStringStream(textToParse);
			MappingFilterLexer lexer = new MappingFilterLexer(inputStream);
			CommonTokenStream tokenStream = new CommonTokenStream(lexer);
			MappingFilterParser parser = new MappingFilterParser(tokenStream);

			listOfFilters = parser.parse();

			if (parser.getNumberOfSyntaxErrors() != 0) {
				throw new Exception("Syntax Error: The filter string is unrecognized!");
			}
		}
		return listOfFilters;
	}

	/***
	 * This function add the list of current filters to the model and then the
	 * Tree is refreshed shows the mappings after the filters have been applied
	 * 
	 * 
	 * @param ListOfMappings
	 */
	private void applyFilters(List<TreeModelFilter<OBDAMappingAxiom>> filters) {
		MappingTreeModel model = (MappingTreeModel) mappingsTree.getModel();
		model.removeAllFilters();
		model.addFilters(filters);
		model.currentSourceChanged(selectedSource.getSourceID(), selectedSource.getSourceID());
	}

	private CQIE parse(String query) {
		CQIE cq = null;
		query = prepareQuery(query);
		try {
			datalogParser.parse(query);
			cq = datalogParser.getRule(0);
		} catch (RecognitionException e) {
			log.warn(e.getMessage());
		}
		return cq;
	}

	private String prepareQuery(String input) {
		String query = "";
		DatalogQueryHelper queryHelper = new DatalogQueryHelper(apic.getPrefixManager());

		String[] atoms = input.split(OBDALibConstants.DATALOG_IMPLY_SYMBOL, 2);
		if (atoms.length == 1) // if no head
			query = queryHelper.getDefaultHead() + " " + OBDALibConstants.DATALOG_IMPLY_SYMBOL + " " + input;

		// Append the prefixes
		query = queryHelper.getPrefixes() + query;

		return query;
	}

	@Override
	public void datasourceChanged(OBDADataSource oldSource, OBDADataSource newSource) {
		this.selectedSource = newSource;

		// Update the mapping tree.
		MappingTreeModel model = (MappingTreeModel) mappingsTree.getModel();
		URI oldSourceUri = null;
		if (oldSource != null) {
			oldSourceUri = oldSource.getSourceID();
		}
		URI newSourceUri = null;
		if (newSource != null) {
			newSourceUri = newSource.getSourceID();
		}
		model.currentSourceChanged(oldSourceUri, newSourceUri);
	}

	@Override
	public void preferenceChanged() {
		DefaultTreeModel model = (DefaultTreeModel) mappingsTree.getModel();
		model.reload();
	}
}
