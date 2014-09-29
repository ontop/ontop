package org.semanticweb.ontop.protege4.panels;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.io.TargetQueryVocabularyValidator;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.protege4.dialogs.MappingValidationDialog;
import org.semanticweb.ontop.protege4.gui.IconLoader;
import org.semanticweb.ontop.protege4.gui.treemodels.FilteredModel;
import org.semanticweb.ontop.protege4.gui.treemodels.SynchronizedMappingListModel;
import org.semanticweb.ontop.protege4.gui.treemodels.TreeModelFilter;
import org.semanticweb.ontop.protege4.utils.DatasourceSelectorListener;
import org.semanticweb.ontop.protege4.utils.DialogUtils;
import org.semanticweb.ontop.protege4.utils.MappingFilterLexer;
import org.semanticweb.ontop.protege4.utils.MappingFilterParser;
import org.semanticweb.ontop.protege4.utils.OBDAMappingListRenderer;
import org.semanticweb.ontop.utils.IDGenerator;
import org.semanticweb.ontop.mapping.sql.SQLSourceQueryValidator;
import org.slf4j.LoggerFactory;

public class MappingManagerPanel extends JPanel implements DatasourceSelectorListener {

	private static final long serialVersionUID = -486013653814714526L;

	private Thread validatorThread;

	private SQLSourceQueryValidator validator;

	private TargetQueryVocabularyValidator validatortrg;

	private OBDAModel mapc;

	private OBDAModel apic;

	private OBDADataSource selectedSource;

	private boolean canceled;
	
	private JTree mappingsTree;

	private JMenuItem menuValidateBody;

	private JMenuItem menuExecuteBody;

	/**
	 * Creates a new panel.
	 * 
	 * @param apic
	 *            The API controller object.
	 * @param preference
	 *            The preference object.
	 */
	public MappingManagerPanel(OBDAModel apic, TargetQueryVocabularyValidator validator) {

		validatortrg = validator;
		
		mappingsTree = new JTree();

		initComponents();
		addMenu();

		// Setting up the mappings tree
		mappingList.setCellRenderer(new OBDAMappingListRenderer(apic, validator));
		mappingList.setModel(new SynchronizedMappingListModel(apic));
		mappingList.setFixedCellWidth(-1);
		mappingList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		mappingList.addMouseListener(new PopupListener());
		
		mappingList.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// Do nothing
			}
			@Override
			public void keyReleased(KeyEvent e) {
				// Do nothing
			}
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					removeMapping();
				} else if (e.getKeyCode() == KeyEvent.VK_INSERT) {
					addMapping();
				} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					editMapping();
				}
			}
		});

		mappingList.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// do nothing
			}
			@Override
			public void mousePressed(MouseEvent e) {
				// do nothing
			}
			@Override
			public void mouseExited(MouseEvent e) {
				// do nothing
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				// do nothing
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				int count = e.getClickCount();
				if (count == 2) {
					editMapping();
				}
			}
		});

		cmdAddMapping.setToolTipText("Create a new mapping");
		cmdRemoveMapping.setToolTipText("Remove selected mappings");
		cmdDuplicateMapping.setToolTipText("Copy selected mappings");

		setOBDAModel(apic); // TODO Bad code! Change this later!
	}

	/**
	 * A listener to trigger the context meny of the mapping list.
	 */
	class PopupListener extends MouseAdapter {
		
		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				menuMappings.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public void setOBDAModel(OBDAModel omodel) {
		
		this.apic = omodel;
		this.mapc = apic;
		ListModel model = new SynchronizedMappingListModel(omodel);
		
		model.addListDataListener(new ListDataListener() {
			@Override
			public void intervalRemoved(ListDataEvent e) {
				fieldMappings.setText(String.valueOf(mappingList.getModel().getSize()));
			}
			@Override
			public void intervalAdded(ListDataEvent e) {
				fieldMappings.setText(String.valueOf(mappingList.getModel().getSize()));
			}
			@Override
			public void contentsChanged(ListDataEvent e) {
				fieldMappings.setText(String.valueOf(mappingList.getModel().getSize()));
			}
		});
		mappingList.setModel(model);
	}

	public void setTargetQueryValidator(TargetQueryVocabularyValidator validator) {
		this.validatortrg = validator;
	}

	private void addMenu() {
		JMenuItem add = new JMenuItem();
		add.setText("Create mapping...");
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addMapping();
			}
		});
		menuMappings.add(add);

		JMenuItem delete = new JMenuItem();
		delete.setText("Remove mapping(s)...");
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeMapping();
			}
		});
		menuMappings.add(delete);

		JMenuItem editMapping = new JMenuItem();
		editMapping.setText("Edit mapping...");
		editMapping.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editMapping();

			}
		});
		menuMappings.add(editMapping);

		menuMappings.addSeparator();

		menuValidateBody = new JMenuItem();
		menuValidateBody.setText("Validate SQL");
		menuValidateBody.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuValidateBodyActionPerformed(evt);
			}
		});
		menuMappings.add(menuValidateBody);

		menuExecuteBody = new JMenuItem();
		menuExecuteBody.setText("Execute SQL");
		menuExecuteBody.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuExecuteBodyActionPerformed(evt);
			}
		});
		menuMappings.add(menuExecuteBody);
	}

	public void editMapping() {
		OBDAMappingAxiom mapping = (OBDAMappingAxiom) mappingList.getSelectedValue();
		if (mapping == null) {
			return;
		}
		JDialog dialog = new JDialog();

		dialog.setTitle("Edit Mapping");
		dialog.setModal(true);

		NewMappingDialogPanel panel = new NewMappingDialogPanel(apic, dialog, selectedSource, validatortrg);
		panel.setMapping(mapping);
		dialog.setContentPane(panel);
		dialog.setSize(600, 500);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        menuMappings = new javax.swing.JPopupMenu();
        pnlMappingManager = new javax.swing.JPanel();
        pnlMappingButtons = new javax.swing.JPanel();
        cmdAddMapping = new javax.swing.JButton();
        cmdRemoveMapping = new javax.swing.JButton();
        cmdDuplicateMapping = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        cmdSelectAll = new javax.swing.JButton();
        cmdDeselectAll = new javax.swing.JButton();
        pnlExtraButtons = new javax.swing.JPanel();
        labelMappings = new javax.swing.JLabel();
        fieldMappings = new javax.swing.JTextField();
        lblInsertFilter = new javax.swing.JLabel();
        txtFilter = new javax.swing.JTextField();
        chkFilter = new javax.swing.JCheckBox();
        mappingScrollPane = new javax.swing.JScrollPane();
        mappingList = new javax.swing.JList();

        setLayout(new java.awt.GridBagLayout());

        pnlMappingManager.setAutoscrolls(true);
        pnlMappingManager.setPreferredSize(new java.awt.Dimension(400, 200));
        pnlMappingManager.setLayout(new java.awt.BorderLayout());

        pnlMappingButtons.setEnabled(false);
        pnlMappingButtons.setLayout(new java.awt.GridBagLayout());

        cmdAddMapping.setIcon(IconLoader.getImageIcon("images/plus.png"));
        cmdAddMapping.setText("Create");
        cmdAddMapping.setToolTipText("Create a new mapping");
        cmdAddMapping.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cmdAddMapping.setContentAreaFilled(false);
        cmdAddMapping.setIconTextGap(5);
        cmdAddMapping.setMaximumSize(new java.awt.Dimension(75, 25));
        cmdAddMapping.setMinimumSize(new java.awt.Dimension(75, 25));
        cmdAddMapping.setPreferredSize(new java.awt.Dimension(75, 25));
        cmdAddMapping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdAddMappingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pnlMappingButtons.add(cmdAddMapping, gridBagConstraints);

        cmdRemoveMapping.setIcon(IconLoader.getImageIcon("images/minus.png"));
        cmdRemoveMapping.setText("Remove");
        cmdRemoveMapping.setToolTipText("Remove the selected mapping");
        cmdRemoveMapping.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cmdRemoveMapping.setContentAreaFilled(false);
        cmdRemoveMapping.setIconTextGap(5);
        cmdRemoveMapping.setMaximumSize(new java.awt.Dimension(75, 25));
        cmdRemoveMapping.setMinimumSize(new java.awt.Dimension(75, 25));
        cmdRemoveMapping.setPreferredSize(new java.awt.Dimension(75, 25));
        cmdRemoveMapping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRemoveMappingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pnlMappingButtons.add(cmdRemoveMapping, gridBagConstraints);

        cmdDuplicateMapping.setIcon(IconLoader.getImageIcon("images/copy.png"));
        cmdDuplicateMapping.setText("Copy");
        cmdDuplicateMapping.setToolTipText("Make a duplicate of the selected mapping");
        cmdDuplicateMapping.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cmdDuplicateMapping.setContentAreaFilled(false);
        cmdDuplicateMapping.setIconTextGap(5);
        cmdDuplicateMapping.setMaximumSize(new java.awt.Dimension(70, 25));
        cmdDuplicateMapping.setMinimumSize(new java.awt.Dimension(70, 25));
        cmdDuplicateMapping.setPreferredSize(new java.awt.Dimension(70, 25));
        cmdDuplicateMapping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdDuplicateMappingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pnlMappingButtons.add(cmdDuplicateMapping, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        pnlMappingButtons.add(jPanel1, gridBagConstraints);

        cmdSelectAll.setIcon(IconLoader.getImageIcon("images/select-all.png"));
        cmdSelectAll.setText("Select all");
        cmdSelectAll.setToolTipText("Select all");
        cmdSelectAll.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cmdSelectAll.setContentAreaFilled(false);
        cmdSelectAll.setIconTextGap(5);
        cmdSelectAll.setMaximumSize(new java.awt.Dimension(83, 25));
        cmdSelectAll.setMinimumSize(new java.awt.Dimension(83, 25));
        cmdSelectAll.setPreferredSize(new java.awt.Dimension(83, 25));
        cmdSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSelectAllActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pnlMappingButtons.add(cmdSelectAll, gridBagConstraints);

        cmdDeselectAll.setIcon(IconLoader.getImageIcon("images/select-none.png"));
        cmdDeselectAll.setText("Select none");
        cmdDeselectAll.setToolTipText("Select none");
        cmdDeselectAll.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cmdDeselectAll.setContentAreaFilled(false);
        cmdDeselectAll.setIconTextGap(5);
        cmdDeselectAll.setMaximumSize(new java.awt.Dimension(92, 25));
        cmdDeselectAll.setMinimumSize(new java.awt.Dimension(92, 25));
        cmdDeselectAll.setPreferredSize(new java.awt.Dimension(92, 25));
        cmdDeselectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdDeselectAllActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pnlMappingButtons.add(cmdDeselectAll, gridBagConstraints);

        pnlMappingManager.add(pnlMappingButtons, java.awt.BorderLayout.NORTH);

        pnlExtraButtons.setMinimumSize(new java.awt.Dimension(532, 25));
        pnlExtraButtons.setPreferredSize(new java.awt.Dimension(532, 25));
        pnlExtraButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));

        labelMappings.setText("Mapping count:");
        pnlExtraButtons.add(labelMappings);

        fieldMappings.setEditable(false);
        fieldMappings.setText("0");
        fieldMappings.setPreferredSize(new java.awt.Dimension(50, 28));
        pnlExtraButtons.add(fieldMappings);

        lblInsertFilter.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        lblInsertFilter.setForeground(new java.awt.Color(53, 113, 163));
        lblInsertFilter.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblInsertFilter.setText("Search:");
        lblInsertFilter.setMinimumSize(new java.awt.Dimension(120, 20));
        lblInsertFilter.setPreferredSize(new java.awt.Dimension(75, 20));
        pnlExtraButtons.add(lblInsertFilter);

        txtFilter.setPreferredSize(new java.awt.Dimension(250, 20));
        txtFilter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sendFilters(evt);
            }
        });
        pnlExtraButtons.add(txtFilter);

        chkFilter.setText("Enable filter");
        chkFilter.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkFilterItemStateChanged(evt);
            }
        });
        pnlExtraButtons.add(chkFilter);

        pnlMappingManager.add(pnlExtraButtons, java.awt.BorderLayout.SOUTH);

        mappingList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        mappingScrollPane.setViewportView(mappingList);

        pnlMappingManager.add(mappingScrollPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pnlMappingManager, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

	private void cmdSelectAllActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdSelectAllActionPerformed
		mappingList.setSelectionInterval(0, mappingList.getModel().getSize());
	}// GEN-LAST:event_cmdSelectAllActionPerformed

	private void cmdDeselectAllActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdDeselectAllActionPerformed
		mappingList.clearSelection();
	}// GEN-LAST:event_cmdDeselectAllActionPerformed

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
			if (txtFilter.getText().isEmpty()) {
				chkFilter.setSelected(false);
				applyFilters(new ArrayList<TreeModelFilter<OBDAMappingAxiom>>());
				return;
			}
			try {
				List<TreeModelFilter<OBDAMappingAxiom>> filters = parseSearchString(txtFilter.getText());
				if (filters == null) {
					throw new Exception("Impossible to parse search string");
				}
				applyFilters(filters);
			} catch (Exception e) {
				LoggerFactory.getLogger(this.getClass()).debug(e.getMessage(), e);
				JOptionPane.showMessageDialog(this, e.getMessage());
			}
		}
	}

	/***
	 * Action for the filter check-box
	 */
	private void chkFilterItemStateChanged(java.awt.event.ItemEvent evt) {// GEN-FIRST:event_jCheckBox1ItemStateChanged
		processFilterAction();

	}// GEN-LAST:event_jCheckBox1ItemStateChanged

	/***
	 * Action for key's entered in the search text box.
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

	private void menuValidateBodyActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_menuValidateBodyActionPerformed
		final MappingValidationDialog outputField = new MappingValidationDialog(mappingsTree);

		outputField.setLocationRelativeTo(this);
		Runnable action = new Runnable() {
			@Override
			public void run() {
				canceled = false;
				final Object[] path = mappingList.getSelectedValues();
				if (path == null) {
					JOptionPane.showMessageDialog(MappingManagerPanel.this, "Select at least one mapping");
					return;
				}
				outputField.addText("Validating " + path.length + " SQL queries.\n", outputField.NORMAL);
				for (int i = 0; i < path.length; i++) {
					final int index = i;
					OBDAMappingAxiom o = (OBDAMappingAxiom) path[index];
					String id = o.getId();
					outputField.addText("  id: '" + id + "'... ", outputField.NORMAL);
					validator = new SQLSourceQueryValidator(selectedSource, o.getSourceQuery());
					long timestart = System.nanoTime();

					if (canceled) {
						return;
					}
					if (validator.validate()) {
						long timestop = System.nanoTime();
						String output = " valid  \n";
						outputField.addText("Time to query: " + ((timestop - timestart) / 1000) + " ns. Result: ", outputField.NORMAL);
						outputField.addText(output, outputField.VALID);
					} else {
						long timestop = System.nanoTime();
						String output = " invalid Reason: " + validator.getReason().getMessage() + " \n";
						outputField.addText("Time to query: " + ((timestop - timestart) / 1000) + " ns. Result: ", outputField.NORMAL);
						outputField.addText(output, outputField.CRITICAL_ERROR);
					}

					if (canceled) {
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
		OBDAMappingAxiom mapping = (OBDAMappingAxiom) mappingList.getSelectedValue();
		if (mapping == null) {
			return;
		}
		final String sqlQuery = mapping.getSourceQuery().toString();

		SQLQueryPanel pnlQueryResult = new SQLQueryPanel(selectedSource, sqlQuery);

		JDialog dlgQueryResult = new JDialog();
		DialogUtils.installEscapeCloseOperation(dlgQueryResult);
		dlgQueryResult.setContentPane(pnlQueryResult);
		dlgQueryResult.pack();
		dlgQueryResult.setLocationRelativeTo(this);
		dlgQueryResult.setVisible(true);
		dlgQueryResult.setTitle("SQL Query Result");
	}// GEN-LAST:event_menuExecuteBodyActionPerformed

	private void cmdDuplicateMappingActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_duplicateMappingButtonActionPerformed
		Object[] currentSelection = mappingList.getSelectedValues();
		if (currentSelection == null) {
			JOptionPane.showMessageDialog(this, "No mappings have been selected", "ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int confirm = JOptionPane.showConfirmDialog(
				this,
				"This will create copies of the selected mappings. \nNumber of mappings selected = " 
				+ currentSelection.length + "\nContinue? ", 
				"Copy confirmation", 
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		if (confirm == JOptionPane.NO_OPTION || confirm == JOptionPane.CANCEL_OPTION || confirm == JOptionPane.CLOSED_OPTION) {
			return;
		}
		OBDAModel controller = mapc;
		URI current_srcuri = selectedSource.getSourceID();

		for (int i = 0; i < currentSelection.length; i++) {
			OBDAMappingAxiom mapping = (OBDAMappingAxiom) currentSelection[i];

			String id = mapping.getId();

			// Computing the next available ID
			int new_index = -1;
			for (int index = 0; index < 999999999; index++) {
				if (controller.indexOf(current_srcuri, id + "(" + index + ")") == -1) {
					new_index = index;
					break;
				}
			}
			String new_id = id + "(" + new_index + ")";

			// inserting the new mapping
			try {

				OBDAMappingAxiom oldmapping = controller.getMapping(current_srcuri, id);
				OBDAMappingAxiom newmapping = null;
				newmapping = (OBDAMappingAxiom) oldmapping.clone();
				newmapping.setId(new_id);
				controller.addMapping(current_srcuri, newmapping);

			} catch (DuplicateMappingException e) {
				JOptionPane.showMessageDialog(this, "Duplicate Mapping: " + new_id);
				return;
			}
		}

	}// GEN-LAST:event_duplicateMappingButtonActionPerformed

	private void cmdRemoveMappingActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_removeMappingButtonActionPerformed
		removeMapping();
	}// GEN-LAST:event_removeMappingButtonActionPerformed

	private void removeMapping() {
		int[] indexes = mappingList.getSelectedIndices();
		if (indexes == null) {
			return;
		}
		int confirm = JOptionPane.showConfirmDialog(
				this, 
				"Proceed deleting " + indexes.length + " mappings?", "Conform",
				JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.CANCEL_OPTION || confirm == JOptionPane.CLOSED_OPTION) {
			return;
		}
		
		// The manager panel can handle multiple deletions.
		Object[] values = mappingList.getSelectedValues();

		OBDAModel controller = mapc;
		URI srcuri = selectedSource.getSourceID();

		for (int i = 0; i < values.length; i++) {
			OBDAMappingAxiom mapping = (OBDAMappingAxiom) values[i];
			if (mapping!=null)
			controller.removeMapping(srcuri, mapping.getId());
		}
		mappingList.clearSelection();
	}

	private void cmdAddMappingActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_addMappingButtonActionPerformed
		if (selectedSource != null) {
			addMapping();
		} else {
			JOptionPane.showMessageDialog(this, "Select a data source to proceed", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
	}// GEN-LAST:event_addMappingButtonActionPerformed

	private void addMapping() {
		String id = IDGenerator.getNextUniqueID("MAPID-");

		JDialog dialog = new JDialog();
		dialog.setTitle("New Mapping");
		dialog.setModal(true);

		NewMappingDialogPanel panel = new NewMappingDialogPanel(apic, dialog, selectedSource, validatortrg);
		panel.setID(id);
		dialog.setContentPane(panel);
		dialog.setSize(600, 500);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}
	
	public void setFilter(String filter) {
		txtFilter.setText(filter);
		processFilterAction();
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkFilter;
    private javax.swing.JButton cmdAddMapping;
    private javax.swing.JButton cmdDeselectAll;
    private javax.swing.JButton cmdDuplicateMapping;
    private javax.swing.JButton cmdRemoveMapping;
    private javax.swing.JButton cmdSelectAll;
    private javax.swing.JTextField fieldMappings;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel labelMappings;
    private javax.swing.JLabel lblInsertFilter;
    private javax.swing.JList mappingList;
    private javax.swing.JScrollPane mappingScrollPane;
    private javax.swing.JPopupMenu menuMappings;
    private javax.swing.JPanel pnlExtraButtons;
    private javax.swing.JPanel pnlMappingButtons;
    private javax.swing.JPanel pnlMappingManager;
    private javax.swing.JTextField txtFilter;
    // End of variables declaration//GEN-END:variables

	/**
	 * Parses the string in the search field.
	 * 
	 * @param textToParse
	 *            The target text
	 * @return A list of filter objects or null if the string was empty or
	 *         erroneous
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
				throw new Exception("Syntax Error: The filter string invalid");
			}
		}
		return listOfFilters;
	}

	/**
	 * This function add the list of current filters to the model and then the
	 * Tree is refreshed shows the mappings after the filters have been applied.
	 */
	private void applyFilters(List<TreeModelFilter<OBDAMappingAxiom>> filters) {
		FilteredModel model = (FilteredModel) mappingList.getModel();
		model.removeAllFilters();
		model.addFilters(filters);
	}

	@Override
	public void datasourceChanged(OBDADataSource oldSource, OBDADataSource newSource) {
		
		if (newSource == null) {
			return;
		}		
		this.selectedSource = newSource;

		// Update the mapping tree.
		SynchronizedMappingListModel model = (SynchronizedMappingListModel) mappingList.getModel();
		model.setFocusedSource(newSource.getSourceID());
		
		mappingList.revalidate();
	}
}
