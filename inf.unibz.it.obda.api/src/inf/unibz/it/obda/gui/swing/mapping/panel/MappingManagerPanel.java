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

package inf.unibz.it.obda.gui.swing.mapping.panel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.api.controller.exception.DuplicateMappingException;
import inf.unibz.it.obda.domain.SourceQuery;
import inf.unibz.it.obda.domain.TargetQuery;
import inf.unibz.it.obda.gui.IconLoader;
import inf.unibz.it.obda.gui.swing.MappingValidationDialog;
import inf.unibz.it.obda.gui.swing.datasource.panels.SQLQueryPanel;
import inf.unibz.it.obda.gui.swing.exception.NoDatasourceSelectedException;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingBodyNode;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingHeadNode;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingNode;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingTreeModel;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingTreeSelectionModel;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferenceChangeListener;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferences;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.obda.rdbmsgav.validator.RDBMSMappingValidator;
import inf.unibz.it.obda.rdbmsgav.validator.SQLQueryValidator;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.parser.exception.QueryParseException;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

// import edu.stanford.smi.protege.resource.Icons;

/**
 * 
 * @author mariano
 */
public class MappingManagerPanel extends JPanel implements MappingManagerPreferenceChangeListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9105604240311706162L;

	Thread					validatorThread	= null;

	SQLQueryValidator		v				= null;

	boolean					canceled		= false;

	MappingController		mapc			= null;

	DatasourcesController	dsc				= null;

	protected APIController	apic 			= null;
	
	DefaultMutableTreeNode editedNode 		=null;
	
	MappingManagerPreferences pref = null;
	KeyStroke addMapping = null;
	KeyStroke editBody = null;
	KeyStroke editHead = null;
	KeyStroke editID = null;
	
	
	/** Creates new form MappingManagerPanel */
	public MappingManagerPanel(APIController apic, MappingController mapc, DatasourcesController dsc) {
		this.apic = apic;
		this.mapc = mapc;
		this.dsc = dsc;
		pref =  OBDAPreferences.getOBDAPreferences().getMappingsPreference();
		initComponents();
		registerAction();
		addMenu();

		/***********************************************************************
		 * Setting up the mappings tree
		 */
		MappingTreeModel maptreemodel = mapc.getTreeModel();

		treeMappingsTree.setRootVisible(false);
		treeMappingsTree.setModel(maptreemodel);
		MappingRenderer map_renderer = new MappingRenderer(apic);
		treeMappingsTree.setCellRenderer(map_renderer);
		treeMappingsTree.setEditable(true);
		treeMappingsTree.setCellEditor(new MappingTreeNodeCellEditor(treeMappingsTree, this,apic));
		treeMappingsTree.setSelectionModel(new MappingTreeSelectionModel());
		treeMappingsTree.setRowHeight(0);
		treeMappingsTree.setMaximumSize(new Dimension(scrollMappingsTree.getWidth() - 50, 65000));
		treeMappingsTree.setToggleClickCount(1);
		treeMappingsTree.setInvokesStopCellEditing(true);
//		MouseListener[] ls = treeMappingsTree.getMouseListeners();
//		for(int i=0; i<ls.length;i++){
//			treeMappingsTree.removeMouseListener(ls[i]);
//		}
//		treeMappingsTree.addMouseListener(new MouseListener(){
//
//			public void mouseClicked(MouseEvent e) {
//				
//					treeMappingsTree.requestFocus();
//					e.consume();
//					if(e.getButton() == MouseEvent.BUTTON1){	
//						
//						if(treeMappingsTree.isEditing()){
//							MappingTreeNodeCellEditor editor = (MappingTreeNodeCellEditor) treeMappingsTree.getCellEditor();
//							if(editor.isInputValid()){
//								if(treeMappingsTree.stopEditing()){
//									String txt = editor.getCellEditorValue().toString();
//									updateNode(txt);
//								}
//							}
//						}else{
//							if(e.getModifiers()==18){
//								TreePath[] currentSelection = treeMappingsTree.getSelectionPaths();
//								TreePath path = treeMappingsTree.getPathForLocation(e.getX(), e.getY());
//								if(path !=null && !treeMappingsTree.isPathSelected(path)){
//									treeMappingsTree.setSelectionPaths(selectPath(currentSelection, path));
//								}else{
//								
//									String name = System.getProperty("os.name");
//									if((path == null || treeMappingsTree.isPathSelected(path)) && name.equals("Darwin")){
//										menuMappings.show(treeMappingsTree, e.getX(), e.getY());
//									}
//								}
//							}else{
//								TreePath path =treeMappingsTree.getPathForLocation(e.getX(), e.getY());
//								if(path != null){
//									treeMappingsTree.setSelectionPath(path);
//									if(treeMappingsTree.isExpanded(path)){
//										treeMappingsTree.collapsePath(path);
//									}else{
//										treeMappingsTree.expandPath(path);
//									}
//								}
//							}
//						}
//					}else if(e.getButton() == MouseEvent.BUTTON3){
//						TreePath path =treeMappingsTree.getPathForLocation(e.getX(), e.getY());
//						treeMappingsTree.setSelectionPath(path);
//						menuMappings.show(treeMappingsTree, e.getX(), e.getY());
//					}
//
//			}
//			public void mouseEntered(MouseEvent e) {}
//			public void mouseExited(MouseEvent e) {}
//			public void mousePressed(MouseEvent e) {}
//			public void mouseReleased(MouseEvent e) {}
//			
//		});
//		KeyListener[] kl = treeMappingsTree.getKeyListeners();
//		for(int j=0; j<kl.length;j++){
//			treeMappingsTree.removeKeyListener(kl[j]);
//		}
//		treeMappingsTree.addKeyListener(new KeyListener(){
//
//			public void keyPressed(KeyEvent e) {
//				
//					int mod = e.getModifiers();
//					int key = e.getKeyCode();
//					KeyStroke stroke = KeyStroke.getKeyStroke(key, mod);
//					if(stroke.equals(addMapping)){
//						addMapping();
//					}else if(stroke.equals(deleteMapping)){
//						try {
//							e.consume();
//							removeMapping();
//						} catch (Exception e1) {
//							System.out.println("---------------------------------ERROR-----------------------------------------------------");
//							e1.printStackTrace();
//							return;
//						}
//					}else if(stroke.equals(editBody)){
//						e.consume();
//						TreePath path =treeMappingsTree.getSelectionPath();
//						if(path == null){
//							return;
//						}
//						startEditBodyOfMapping(path);
//					}else if(stroke.equals(editHead)){
//						e.consume();
//						TreePath path =treeMappingsTree.getSelectionPath();
//						if(path == null){
//							return;
//						}
//						startEditHeadOfMapping(path);
//					}else if(stroke.equals(editID)){
//						e.consume();
//						TreePath path =treeMappingsTree.getSelectionPath();
//						if(path == null){
//							return;
//						}
//						treeMappingsTree.setEditable(true);
//						editedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
//						treeMappingsTree.startEditingAtPath(path);
//					}else{
//						return;
//					}			
//			}
//			public void keyReleased(KeyEvent e) {}
//			public void keyTyped(KeyEvent e) {}
//			
//		});
		addMappingButton.setIcon(IconLoader.getImageIcon("images/plus.png"));
		addMappingButton.setToolTipText("Add a new mapping");
		removeMappingButton.setIcon(IconLoader.getImageIcon("images/minus.png"));
		removeMappingButton.setToolTipText("Remove selected mappings");
		duplicateMappingButton.setIcon(IconLoader.getImageIcon("images/duplicate.png"));
		duplicateMappingButton.setToolTipText("Duplicate selected mappings");
		pref.registerPreferenceChangedListener(this);
	}

	private void registerAction(){
		
		InputMap inputmap = treeMappingsTree.getInputMap();
		ActionMap actionmap = treeMappingsTree.getActionMap();
		
		String add = pref.getShortCut(MappingManagerPreferences.ADD_MAPPING);
		addMapping = KeyStroke.getKeyStroke(add);
//		String delete = pref.getShortCut(MappingManagerPreferences.DELETE_MAPPING);
//		KeyStroke deleteMapping = KeyStroke.getKeyStroke(delete);
		String body = pref.getShortCut(MappingManagerPreferences.EDIT_BODY);
		editBody = KeyStroke.getKeyStroke(body);
		String head = pref.getShortCut(MappingManagerPreferences.EDIT_HEAD);
		editHead = KeyStroke.getKeyStroke(head);
		String id = pref.getShortCut(MappingManagerPreferences.EDIT_ID);
		editID = KeyStroke.getKeyStroke(id);

		
		AbstractAction addAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  addMapping();
		      }
		};
		inputmap.put(addMapping, MappingManagerPreferences.ADD_MAPPING);
		actionmap.put(MappingManagerPreferences.ADD_MAPPING, addAction);
		
//		AbstractAction deleteAction = new AbstractAction() {
//		      public void actionPerformed(ActionEvent actionEvent) {
//		    	  removeMapping();
//		      }
//		};
//		inputmap.put(deleteMapping, MappingManagerPreferences.DELETE_MAPPING);
//		actionmap.put(MappingManagerPreferences.DELETE_MAPPING, deleteAction);
		
		AbstractAction editBodyAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
					TreePath path =treeMappingsTree.getSelectionPath();
					if(path == null){
						return;
					}
					startEditBodyOfMapping(path);
					
		      }
		};
		inputmap.put(editBody, MappingManagerPreferences.EDIT_BODY);
		actionmap.put(MappingManagerPreferences.EDIT_BODY, editBodyAction);
		
		AbstractAction editHeadAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
					TreePath path =treeMappingsTree.getSelectionPath();
					if(path == null){
						return;
					}
					startEditHeadOfMapping(path);
		      }
		};
		inputmap.put(editHead, MappingManagerPreferences.EDIT_HEAD);
		actionmap.put(MappingManagerPreferences.EDIT_HEAD, editHeadAction);
		
		AbstractAction editIDAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
					TreePath path =treeMappingsTree.getSelectionPath();
					if(path == null){
						return;
					}
					treeMappingsTree.setEditable(true);
					editedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
					treeMappingsTree.startEditingAtPath(path);
		      }
		};
		inputmap.put(editID, MappingManagerPreferences.EDIT_ID);
		actionmap.put(MappingManagerPreferences.EDIT_ID, editIDAction);
	}
	
	private void addMenu(){
		
		JMenuItem add = new JMenuItem();
		add.setText("Add Mapping");
		add.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				addMapping();
			}
			
		});
		add.setMnemonic(addMapping.getKeyCode());
		add.setAccelerator(addMapping); 
		menuMappings.add(add);
		
		JMenuItem delete = new JMenuItem();
		delete.setText("Remove Mapping");
		delete.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				removeMapping();
			}
			
		});
		menuMappings.add(delete);
//		delete.setMnemonic(KeyEvent.VK_D);
//		delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,InputEvent.CTRL_DOWN_MASK)); 
		
		menuMappings.addSeparator();
		
		JMenuItem editID = new JMenuItem();
		editID.setText("Edit Mapping ID");
		editID.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				TreePath path =treeMappingsTree.getSelectionPath();
				if(path == null){
					return;
				}
				treeMappingsTree.setEditable(true);
				editedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
				treeMappingsTree.startEditingAtPath(path);
			}
			
		});
		editID.setMnemonic(this.editID.getKeyCode());
		editID.setAccelerator(this.editID); 
		menuMappings.add(editID);
		
		JMenuItem editHead = new JMenuItem();
		editHead.setText("Edit Mapping Head");
		editHead.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				TreePath path =treeMappingsTree.getSelectionPath();
				if(path == null){
					return;
				}
				startEditHeadOfMapping(path);
			}
			
		});
		editHead.setMnemonic(this.editHead.getKeyCode());
		editHead.setAccelerator(this.editHead); 
		menuMappings.add(editHead);
		
		JMenuItem editBody = new JMenuItem();
		editBody.setText("Edit Mapping Body");
		editBody.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				TreePath path =treeMappingsTree.getSelectionPath();
				if(path == null){
					return;
				}
				startEditBodyOfMapping(path);
			}
			
		});
		editBody.setMnemonic(this.editBody.getKeyCode());
		editBody.setAccelerator(this.editBody); 
		menuMappings.add(editBody);
		
		menuMappings.addSeparator();
		
		menuValidateAll.setText("Validate");
		menuValidateAll.setEnabled(false);
		menuValidateAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuValidateAllActionPerformed(evt);
			}
		});
		menuMappings.add(menuValidateAll);

		menuValidateBody.setText("Validate body");
		menuValidateBody.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuValidateBodyActionPerformed(evt);
			}
		});
		menuMappings.add(menuValidateBody);

		menuValidateHead.setText("Validate head");
		menuValidateHead.setEnabled(false);
		menuValidateHead.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuValidateHeadActionPerformed(evt);
			}
		});
		menuMappings.add(menuValidateHead);
		
		menuMappings.addSeparator(); 
		
		menuExecuteQuery.setText("Execute Query");
		menuExecuteQuery.setEnabled(true);
		menuExecuteQuery.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuExecuteQueryActionPerformed(evt);
			}
		});
		menuMappings.add(menuExecuteQuery);
		
	}
	
	    
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		menuMappings = new javax.swing.JPopupMenu();
		menuValidateAll = new javax.swing.JMenuItem();
		menuValidateBody = new javax.swing.JMenuItem();
		menuValidateHead = new javax.swing.JMenuItem();
		menuExecuteQuery = new javax.swing.JMenuItem();
		scrollMappingsManager = new javax.swing.JScrollPane();
		panelMappingManager = new javax.swing.JPanel();
		panelMappingButtons = new javax.swing.JPanel();
		jSeparator1 = new javax.swing.JSeparator();
		addMappingButton = new javax.swing.JButton();
		removeMappingButton = new javax.swing.JButton();
		duplicateMappingButton = new javax.swing.JButton();
		scrollMappingsTree = new javax.swing.JScrollPane();
		treeMappingsTree = new javax.swing.JTree();

		setLayout(new java.awt.BorderLayout());

		scrollMappingsManager.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

		panelMappingManager.setAutoscrolls(true);
		panelMappingManager.setPreferredSize(new java.awt.Dimension(400, 200));
		panelMappingManager.setLayout(new java.awt.GridBagLayout());

		panelMappingButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

		jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
		panelMappingButtons.add(jSeparator1);

		addMappingButton.setIcon(IconLoader.getImageIcon("images/plus.png"));
		addMappingButton.setToolTipText("Add new mapping");
		addMappingButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		addMappingButton.setContentAreaFilled(false);
		addMappingButton.setIconTextGap(0);
		addMappingButton.setMaximumSize(new java.awt.Dimension(25, 25));
		addMappingButton.setMinimumSize(new java.awt.Dimension(25, 25));
		addMappingButton.setPreferredSize(new java.awt.Dimension(25, 25));
		addMappingButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addMappingButtonActionPerformed(evt);
			}
		});
		panelMappingButtons.add(addMappingButton);

		removeMappingButton.setIcon(IconLoader.getImageIcon("images/minus.png"));
		removeMappingButton.setToolTipText("Remove mappings");
		removeMappingButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		removeMappingButton.setContentAreaFilled(false);
		removeMappingButton.setIconTextGap(0);
		removeMappingButton.setMaximumSize(new java.awt.Dimension(25, 25));
		removeMappingButton.setMinimumSize(new java.awt.Dimension(25, 25));
		removeMappingButton.setPreferredSize(new java.awt.Dimension(25, 25));
		removeMappingButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				removeMappingButtonActionPerformed(evt);
			}
		});
		panelMappingButtons.add(removeMappingButton);

		duplicateMappingButton.setIcon(IconLoader.getImageIcon("images/plus.png"));
		duplicateMappingButton.setToolTipText("Duplicate mappings");
		duplicateMappingButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		duplicateMappingButton.setContentAreaFilled(false);
		duplicateMappingButton.setIconTextGap(0);
		duplicateMappingButton.setMaximumSize(new java.awt.Dimension(25, 25));
		duplicateMappingButton.setMinimumSize(new java.awt.Dimension(25, 25));
		duplicateMappingButton.setPreferredSize(new java.awt.Dimension(25, 25));
		duplicateMappingButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				duplicateMappingButtonActionPerformed(evt);
			}
		});
		panelMappingButtons.add(duplicateMappingButton);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		panelMappingManager.add(panelMappingButtons, gridBagConstraints);

		treeMappingsTree.setComponentPopupMenu(menuMappings);
		treeMappingsTree.setEditable(true);
		scrollMappingsTree.setViewportView(treeMappingsTree);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 4;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		panelMappingManager.add(scrollMappingsTree, gridBagConstraints);

		scrollMappingsManager.setViewportView(panelMappingManager);

		add(scrollMappingsManager, java.awt.BorderLayout.CENTER);
	}// </editor-fold>//GEN-END:initComponents

	private void menuValidateAllActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuValidateAllActionPerformed

		// menu.setVisible(false);
		MappingValidationDialog outputField = new MappingValidationDialog(treeMappingsTree);
		TreePath path[] = treeMappingsTree.getSelectionPaths();

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
				try {
					RDBMSSQLQuery rdbmssqlQuery = new RDBMSSQLQuery(body.getQuery(), apic);
					ConjunctiveQuery conjunctiveQuery = new ConjunctiveQuery(head
							.getQuery(), apic);
					v = new RDBMSMappingValidator(apic, dsc.getCurrentDataSource(), rdbmssqlQuery, conjunctiveQuery);
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

				} catch (QueryParseException e) {
					outputField.addText(id +": syntax error \n", outputField.CRITICAL_ERROR);
				}

			}
		}
	}// GEN-LAST:event_menuValidateAllActionPerformed

	private void menuValidateBodyActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuValidateBodyActionPerformed
		final MappingValidationDialog outputField = new MappingValidationDialog(treeMappingsTree);

		Runnable action = new Runnable() {
			public void run() {
				// menu.setVisible(false);
				canceled = false;
				final TreePath path[] = treeMappingsTree.getSelectionPaths();

				outputField.setVisible(true);

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
						try {
							v = new SQLQueryValidator(dsc.getCurrentDataSource(), new RDBMSSQLQuery(body.getQuery(), apic));
						} catch (QueryParseException e) {
							String output = " invalid Reason: " + v.getReason().getMessage() + " \n";
							outputField.addText(output, outputField.CRITICAL_ERROR);
							return;
						}
						long timestart = System.currentTimeMillis();

						if (canceled)
							return;

						if (v.validate()) {
							long timestop = System.currentTimeMillis();
							String output = " valid  \n";
							outputField.addText("Time to query: " + ((timestop - timestart) / 1000) + " ms. Result: ", outputField.NORMAL);
							outputField.addText(output, outputField.VALID);

						} else {
							long timestop = System.currentTimeMillis();
							String output = " invalid Reason: " + v.getReason().getMessage() + " \n";
							outputField.addText("Time to query: " + ((timestop - timestart) / 1000) + " ms. Result: ", outputField.NORMAL);
							outputField.addText(output, outputField.CRITICAL_ERROR);
						}
						v.dispose();

						if (canceled)
							return;

					}

				}

			}

		};
		validatorThread = new Thread(action);
		validatorThread.start();

		Thread cancelThread = new Thread(new Runnable() {
			public void run() {
				while (!outputField.closed) {
					try {
						Thread.currentThread().sleep(100);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
				}
				if (validatorThread.isAlive()) {
					try {
						Thread.currentThread().sleep(250);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
					try {
						canceled = true;
						v.cancelValidation();
					} catch (SQLException e) {
						e.printStackTrace();
					}

				}
			}
		});
		cancelThread.start();

	}// GEN-LAST:event_menuValidateBodyActionPerformed

	private void menuValidateHeadActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuValidateHeadActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_menuValidateHeadActionPerformed

	private void menuExecuteQueryActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuExecuteQueryActionPerformed
		TreePath path =treeMappingsTree.getSelectionPath();
				if(path == null){
					return;
				}
				startExecuteQueryOfMapping(path);
		
	}// GEN-LAST:event_menuExecuteQueryActionPerformed
	
	
	private void menuDeleteActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuDeleteActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_menuDeleteActionPerformed

	private void duplicateMappingButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_duplicateMappingButtonActionPerformed
		
		TreePath[] currentSelection = treeMappingsTree.getSelectionPaths();
		if(currentSelection == null){
			JOptionPane.showMessageDialog(this, "Please Select a Mapping first", "ERROR", JOptionPane.ERROR_MESSAGE); 
		}else{
			if (JOptionPane.showConfirmDialog(this, "This will create copies of the selected mappings. \nNumber of mappings selected = "
					+ treeMappingsTree.getSelectionPaths().length + "\n Continue? ", "Copy confirmation", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
				return;
			}
			MappingController controller = mapc;
			String current_srcuri = dsc.getCurrentDataSource().getName();
	
			if (currentSelection != null) {
				for (int i = 0; i < currentSelection.length; i++) {
					TreePath current_path = currentSelection[i];
					MappingNode mapping = (MappingNode) current_path.getLastPathComponent();
					String id = (String) mapping.getUserObject();
					String new_id = controller.getNextAvailableDuplicateIDforMapping(current_srcuri, id);
					try {
						controller.duplicateMapping(current_srcuri, id, new_id);
					} catch (DuplicateMappingException e) {
						JOptionPane.showMessageDialog(this, "Duplicate Mapping: " + new_id);
					}
				}
			}
		}
	}// GEN-LAST:event_duplicateMappingButtonActionPerformed

	private void removeMappingButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_removeMappingButtonActionPerformed
		removeMapping();
	}// GEN-LAST:event_removeMappingButtonActionPerformed

	private void removeMapping() {
		if (JOptionPane.showConfirmDialog(this, "This will delete ALL the selected mappings. \nNumber of mappings selected = "
				+ treeMappingsTree.getSelectionPaths().length + "\n Continue? ", "Delete confirmation", JOptionPane.WARNING_MESSAGE,
				JOptionPane.YES_NO_OPTION) == JOptionPane.CANCEL_OPTION) {
			return;
		}

		TreePath[] currentSelection = treeMappingsTree.getSelectionPaths();
		// DefaultTreeModel model = (DefaultTreeModel)
		// treeMappingsTree.getModel();
		MappingController controller = mapc;
		String srcuri = dsc.getCurrentDataSource().getName();

		if (currentSelection != null) {
			for (int i = 0; i < currentSelection.length; i++) {
				TreePath current_path = currentSelection[i];
				MappingNode mappingnode = (MappingNode) current_path.getLastPathComponent();
				controller.deleteMapping(srcuri, (String) mappingnode.getMappingID());
				// model.removeNodeFromParent(mapping);

			}
		}
		registerAction();
	}

	private void addMappingButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_addMappingButtonActionPerformed

		addMapping();
	}// GEN-LAST:event_addMappingButtonActionPerformed

	private void addMapping() {
		MappingController controller = mapc;
		MappingTreeModel model = (MappingTreeModel) treeMappingsTree.getModel();
		treeMappingsTree.requestFocus();
		try {
			String mappingid = controller.insertMapping();
			MappingNode newnode = model.getMappingNode(mappingid);
			treeMappingsTree.scrollPathToVisible(new TreePath(newnode.getBodyNode().getPath()));
			treeMappingsTree.setSelectionPath(new TreePath(newnode.getPath()));
			if (!newnode.isLeaf()) {
				treeMappingsTree.expandPath(new TreePath(newnode.getPath()));
			}

		} catch (NoDatasourceSelectedException e) {
			JOptionPane.showMessageDialog(null, "Select a data source first");
		} catch (DuplicateMappingException e) {
			e.printStackTrace(System.err);
			JOptionPane.showMessageDialog(this, "ERROR: duplicate mapping: " + e.getMessage());
			return;
		}
	}

	private void startEditHeadOfMapping(TreePath path) {
		treeMappingsTree.setEditable(true);
		MappingNode mapping = (MappingNode) path.getLastPathComponent();
		MappingHeadNode head = mapping.getHeadNode();
		editedNode = head;
		treeMappingsTree.startEditingAtPath(new TreePath(head.getPath()));
		// treeMappingsTree.setRowHeight(0);
	}

	private void startEditBodyOfMapping(TreePath path) {
		treeMappingsTree.setEditable(true);
		MappingNode mapping = (MappingNode) path.getLastPathComponent();
		MappingBodyNode body = mapping.getBodyNode();
		editedNode = body;
		treeMappingsTree.startEditingAtPath(new TreePath(body.getPath()));
	}
	
	private void startExecuteQueryOfMapping(TreePath path){
		final JDialog resultquery=new JDialog();
		MappingNode mapping  =(MappingNode)path.getLastPathComponent();
		MappingBodyNode body =mapping.getBodyNode();
		SQLQueryPanel query_panel = new SQLQueryPanel (dsc,body.toString());
  
		resultquery.setSize(panelMappingManager.getWidth(), panelMappingManager.getHeight());
        resultquery.setLocationRelativeTo(null);
        resultquery.add(query_panel);
        resultquery.setVisible(true);
        resultquery.setTitle("Query Results");
        
	}

	// private JPopupMenu menu;
	// private JDialog dialog;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton		addMappingButton;
	private javax.swing.JButton		duplicateMappingButton;
	private javax.swing.JSeparator	jSeparator1;
	private javax.swing.JPopupMenu	menuMappings;
	private javax.swing.JMenuItem	menuValidateAll;
	private javax.swing.JMenuItem	menuValidateBody;
	private javax.swing.JMenuItem	menuValidateHead;
	private javax.swing.JMenuItem	menuExecuteQuery; //Add E
	private javax.swing.JPanel		panelMappingButtons;
	private javax.swing.JPanel		panelMappingManager;
	private javax.swing.JButton		removeMappingButton;
	private javax.swing.JScrollPane	scrollMappingsManager;
	private javax.swing.JScrollPane	scrollMappingsTree;
	private javax.swing.JTree		treeMappingsTree;
	// End of variables declaration//GEN-END:variables

	public void colorPeferenceChanged(String preference, Color col) {
		
		DefaultTreeModel model = (DefaultTreeModel)treeMappingsTree.getModel();
		model.reload();

	}

	public void fontFamilyPreferenceChanged(String preference, String font) {
		
		DefaultTreeModel model = (DefaultTreeModel)treeMappingsTree.getModel();
		model.reload();
	}

	public void fontSizePreferenceChanged(String preference, int size) {
		
		DefaultTreeModel model = (DefaultTreeModel)treeMappingsTree.getModel();
		model.reload();
	}

	public void isBoldPreferenceChanged(String preference, Boolean isBold) {
		
		DefaultTreeModel model = (DefaultTreeModel)treeMappingsTree.getModel();
		model.reload();
	}
	
	private void updateNode(String str) {

		try {
			MappingController con = mapc;
			String sourceName = dsc.getCurrentDataSource().getName();
			String nodeContent = (String) editedNode.getUserObject();
			if (editedNode instanceof MappingNode) {

				con.updateMapping(sourceName, nodeContent, str);

			} else if (editedNode instanceof MappingBodyNode) {

				MappingBodyNode node = (MappingBodyNode) editedNode;
				MappingNode parent = (MappingNode) node.getParent();

				SourceQuery b = new RDBMSSQLQuery(str, apic);
				con.updateMapping(sourceName, parent.getMappingID(), b);

			} else if (editedNode instanceof MappingHeadNode) {

				MappingHeadNode node = (MappingHeadNode) editedNode;
				MappingNode parent = (MappingNode) node.getParent();

				TargetQuery h = new ConjunctiveQuery(str, apic);
				con.updateMapping(sourceName, parent.getMappingID(), h);
			}
		} catch (QueryParseException e) {
			e.printStackTrace();
		}
	}
	
	private TreePath[] selectPath(TreePath[] currentselection, TreePath newPath){
		Vector<TreePath> paths = new Vector<TreePath>();
		boolean found = false;
		for(int i=0; i<currentselection.length; i++){
			if(currentselection[i].equals(newPath)){
				found = true;
			}else{
				paths.add(currentselection[i]);
			}
		}
		if(!found){
			paths.add(newPath);
		}
		TreePath[] aux = new TreePath[paths.size()];
		return paths.toArray(aux);
	}

	public void shortCutChanged(String preference, String shortcut) {
		
//		InputMap inputmap = treeMappingsTree.getInputMap();
//		
//		if(preference.equals(MappingManagerPreferences.ADD_MAPPING)){
//			addMapping = KeyStroke.getKeyStroke(shortcut);
//			inputmap.put(addMapping, preference);
//		}else if(preference.equals(MappingManagerPreferences.DELETE_MAPPING)){
////			deleteMapping = KeyStroke.getKeyStroke(shortcut);
//		}else if(preference.equals(MappingManagerPreferences.EDIT_BODY)){
//			editBody = KeyStroke.getKeyStroke(shortcut);
//			inputmap.put(editBody, preference);
//		}else if(preference.equals(MappingManagerPreferences.EDIT_HEAD)){
//			editHead = KeyStroke.getKeyStroke(shortcut);
//			inputmap.put(editHead, preference);
//		}else if(preference.equals(MappingManagerPreferences.EDIT_ID)){
//			editID = KeyStroke.getKeyStroke(shortcut);
//			inputmap.put(editID, preference);
//		}else{
//			try {
//				throw new Exception("Unknown preference String");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		registerAction();
	}
	
	public void stopTreeEditing(){
		
		if(treeMappingsTree.isEditing()){
			MappingTreeNodeCellEditor editor = (MappingTreeNodeCellEditor) treeMappingsTree.getCellEditor();
			if(editor.isInputValid()){
				if(treeMappingsTree.stopEditing()){
					String txt = editor.getCellEditorValue().toString();
					updateNode(txt);
				}
			}
		}
	}
	
	public void applyChangedToNode(String txt){
		
		updateNode(txt);
	}
}
