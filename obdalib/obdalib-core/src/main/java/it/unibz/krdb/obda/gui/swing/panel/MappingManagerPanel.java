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
import it.unibz.krdb.obda.gui.swing.treemodel.FilteredModel;
import it.unibz.krdb.obda.gui.swing.treemodel.SynchronizedMappingListModel;
import it.unibz.krdb.obda.gui.swing.treemodel.TargetQueryVocabularyValidator;
import it.unibz.krdb.obda.gui.swing.treemodel.TreeModelFilter;
import it.unibz.krdb.obda.gui.swing.utils.DatasourceSelectorListener;
import it.unibz.krdb.obda.gui.swing.utils.DialogUtils;
import it.unibz.krdb.obda.gui.swing.utils.MappingFilterLexer;
import it.unibz.krdb.obda.gui.swing.utils.MappingFilterParser;
import it.unibz.krdb.obda.gui.swing.utils.OBDAMappingListRenderer;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.parser.DatalogProgramParser;
import it.unibz.krdb.obda.utils.OBDAPreferences;
import it.unibz.krdb.obda.utils.SourceQueryValidator;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingManagerPanel extends JPanel implements DatasourceSelectorListener {

	private static final long serialVersionUID = -486013653814714526L;

	private OBDAPreferences preference;

	private Thread validatorThread;

	private SourceQueryValidator validator;

	private TargetQueryVocabularyValidator validatortrg;

	private OBDAModel mapc;

	protected OBDAModel apic;

	private OBDADataSource selectedSource;

	private boolean canceled;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

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
	public MappingManagerPanel(OBDAModel apic, OBDAPreferences preference, TargetQueryVocabularyValidator validator) {

		this.preference = preference;
//		datalogParser = new DatalogProgramParser();
		this.validatortrg = validator;

		mappingsTree = new JTree();

		initComponents();
		registerAction();
		addMenu();

		/***********************************************************************
		 * Setting up the mappings tree
		 */
		// mappingsTree.setRootVisible(false);

		// MappingTreeCellRenderer map_renderer = new
		// MappingTreeCellRenderer(apic, preference);

		// MappingTreeCellRenderer2 map_renderer = new
		// MappingTreeCellRenderer2(preference);
		// scrMappingsTree.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// mappingsTree = new JTree();
		// mappingsTree.setCellRenderer(map_renderer);
		// mappingsTree.setEditable(true);
		// mappingsTree.setCellEditor(new MappingTreeNodeCellEditor(apic,
		// validatortrg, preference));
		// mappingsTree.setSelectionModel(new MappingTreeSelectionModel());
		// mappingsTree.setRowHeight(0);
		// mappingsTree.setMaximumSize(new Dimension(scrMappingsTree.getWidth()
		// - 50, 65000));
		// mappingsTree.setToggleClickCount(1);
		// mappingsTree.setRootVisible(true);
		// mappingsTree.setInvokesStopCellEditing(true);

		mappingList.setCellRenderer(new OBDAMappingListRenderer(preference, apic, validator));
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
		// preference.registerPreferenceChangedListener(this);

		setOBDAModel(apic); // TODO Bad code! Change this later!
	}

	/***
	 * A listener to trigger the context meny of the mapping list.
	 * 
	 * @author mariano
	 * 
	 */
	class PopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

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

	private void registerAction() {

		// lblInsertFilter.setBackground(new java.awt.Color(153, 153, 153));
		// lblInsertFilter.setFont(new java.awt.Font("Arial", 1, 11));
		// lblInsertFilter.setForeground(new java.awt.Color(153, 153, 153));
		// lblInsertFilter.setPreferredSize(new Dimension(75, 14));
		// lblInsertFilter.setText("Insert Filter: ");

		// cmdAddMapping.setText("Insert");
		// cmdAddMapping.setMnemonic('i');
		// cmdAddMapping.setIcon(null);
		// cmdAddMapping.setPreferredSize(new Dimension(40, 21));
		// cmdAddMapping.setMinimumSize(new Dimension(40, 21));
		//
		// cmdRemoveMapping.setText("Remove");
		// cmdRemoveMapping.setMnemonic('r');
		// cmdRemoveMapping.setIcon(null);
		// cmdRemoveMapping.setPreferredSize(new Dimension(50, 21));
		// cmdRemoveMapping.setMinimumSize(new Dimension(50, 21));
		//
		// cmdDuplicateMapping.setText("Duplicate");
		// cmdDuplicateMapping.setMnemonic('d');
		// cmdDuplicateMapping.setIcon(null);
		// cmdDuplicateMapping.setPreferredSize(new Dimension(60, 21));
		// cmdDuplicateMapping.setMinimumSize(new Dimension(60, 21));

	}

	private void addMenu() {
		JMenuItem add = new JMenuItem();
		add.setText("Add mapping...");
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addMapping();
			}
		});
		// add.setMnemonic(addMapping.getKeyCode());
		// add.setAccelerator(addMapping);
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

	protected void editMapping() {

		OBDAMappingAxiom mapping = (OBDAMappingAxiom) mappingList.getSelectedValue();
		if (mapping == null)
			return;

		JDialog dialog = new JDialog();

		dialog.setTitle("Edit mapping");
		dialog.setModal(true);

		NewMappingDialogPanel panel = new NewMappingDialogPanel(apic, preference, dialog, selectedSource, validatortrg);
		panel.setMapping(mapping);
		dialog.setContentPane(panel);
		dialog.setSize(600, 400);
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
	// desc=" Generated Code ">//GEN-BEGIN:initComponents
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

		menuMappings.setLayout(null);

		setLayout(new java.awt.GridBagLayout());

		pnlMappingManager.setLayout(new java.awt.BorderLayout());

		pnlMappingManager.setAutoscrolls(true);
		pnlMappingManager.setPreferredSize(new java.awt.Dimension(400, 200));
		pnlMappingButtons.setLayout(new java.awt.GridBagLayout());

		pnlMappingButtons.setEnabled(false);
		cmdAddMapping.setText("Create...");
		cmdAddMapping.setToolTipText("Create...");
		cmdAddMapping.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cmdAddMapping.setContentAreaFilled(false);
		cmdAddMapping.setIconTextGap(0);
		cmdAddMapping.setMaximumSize(new java.awt.Dimension(25, 25));
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

		cmdRemoveMapping.setText("Remove...");
		cmdRemoveMapping.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cmdRemoveMapping.setContentAreaFilled(false);
		cmdRemoveMapping.setIconTextGap(0);
		cmdRemoveMapping.setMaximumSize(new java.awt.Dimension(25, 25));
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

		cmdDuplicateMapping.setText("Copy...");
		cmdDuplicateMapping.setToolTipText("Duplicate mappings");
		cmdDuplicateMapping.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cmdDuplicateMapping.setContentAreaFilled(false);
		cmdDuplicateMapping.setIconTextGap(0);
		cmdDuplicateMapping.setMaximumSize(new java.awt.Dimension(25, 25));
		cmdDuplicateMapping.setMinimumSize(new java.awt.Dimension(75, 25));
		cmdDuplicateMapping.setPreferredSize(new java.awt.Dimension(75, 25));
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

		cmdSelectAll.setText("Select all");
		cmdSelectAll.setToolTipText("Select all");
		cmdSelectAll.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cmdSelectAll.setContentAreaFilled(false);
		cmdSelectAll.setIconTextGap(0);
		cmdSelectAll.setMaximumSize(new java.awt.Dimension(25, 25));
		cmdSelectAll.setMinimumSize(new java.awt.Dimension(75, 25));
		cmdSelectAll.setPreferredSize(new java.awt.Dimension(75, 25));
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

		cmdDeselectAll.setText("Select none");
		cmdDeselectAll.setToolTipText("Select none");
		cmdDeselectAll.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cmdDeselectAll.setContentAreaFilled(false);
		cmdDeselectAll.setIconTextGap(0);
		cmdDeselectAll.setMaximumSize(new java.awt.Dimension(25, 25));
		cmdDeselectAll.setMinimumSize(new java.awt.Dimension(75, 25));
		cmdDeselectAll.setPreferredSize(new java.awt.Dimension(75, 25));
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

		pnlExtraButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));

		pnlExtraButtons.setMinimumSize(new java.awt.Dimension(532, 25));
		pnlExtraButtons.setPreferredSize(new java.awt.Dimension(532, 25));
		labelMappings.setText("Mapping count:");
		pnlExtraButtons.add(labelMappings);

		fieldMappings.setEditable(false);
		fieldMappings.setText("0");
		fieldMappings.setPreferredSize(new java.awt.Dimension(50, 28));
		pnlExtraButtons.add(fieldMappings);

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

		chkFilter.setText("Apply Filters");
		chkFilter.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				chkFilterItemStateChanged(evt);
			}
		});

		pnlExtraButtons.add(chkFilter);

		pnlMappingManager.add(pnlExtraButtons, java.awt.BorderLayout.SOUTH);

		mappingList.setModel(new javax.swing.AbstractListModel() {
			String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };

			public int getSize() {
				return strings.length;
			}

			public Object getElementAt(int i) {
				return strings[i];
			}
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
					validator = new SourceQueryValidator(selectedSource, o.getSourceQuery());
					long timestart = System.nanoTime();

					if (canceled)
						return;

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

					if (canceled)
						return;

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
		if (mapping == null)
			return;
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
		int confirm = JOptionPane.showConfirmDialog(this,
				"This will create copies of the selected mappings. \nNumber of mappings selected = " + currentSelection.length
						+ "\nContinue? ", "Copy confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (confirm == JOptionPane.NO_OPTION || confirm == JOptionPane.CANCEL_OPTION || confirm == JOptionPane.CLOSED_OPTION) {
			return;
		}
		OBDAModel controller = mapc;
		URI current_srcuri = selectedSource.getSourceID();

		for (int i = 0; i < currentSelection.length; i++) {
			OBDAMappingAxiom mapping = (OBDAMappingAxiom) currentSelection[i];

			String id = mapping.getId();

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
				return;
			}
		}

	}// GEN-LAST:event_duplicateMappingButtonActionPerformed

	private void cmdRemoveMappingActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_removeMappingButtonActionPerformed
		removeMapping();
	}// GEN-LAST:event_removeMappingButtonActionPerformed

	private void removeMapping() {
		int[] indexes = mappingList.getSelectedIndices();
		if (indexes == null)
			return;
		int confirm = JOptionPane.showConfirmDialog(this, "Proceed deleting " + indexes.length + " mappings?", "Conform",
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

		URI sourceID = this.selectedSource.getSourceID();

		/* Computing an ID for the new mapping */

		int index = 0;
		for (int i = 0; i < 99999999; i++) {
			index = this.mapc.indexOf(sourceID, "M:" + Integer.toHexString(i));
			if (index == -1) {
				index = i;
				break;
			}
		}
		String id = "M:" + Integer.toHexString(index);

		JDialog dialog = new JDialog();

		dialog.setTitle("Create mapping");
		dialog.setModal(true);

		NewMappingDialogPanel panel = new NewMappingDialogPanel(apic, preference, dialog, selectedSource, validatortrg);
		panel.setID(id);
		dialog.setContentPane(panel);
		dialog.setSize(600, 400);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}
	
	public void setFilter(String filter) {
		
		txtFilter.setText(filter);
		chkFilter.setSelected(true);
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
				throw new Exception("Syntax Error: The filter string invalid");
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
		FilteredModel model = (FilteredModel) mappingList.getModel();
		model.removeAllFilters();
		model.addFilters(filters);
		// model.currentSourceChanged(selectedSource.getSourceID(),
		// selectedSource.getSourceID());
	}

	// private CQIE parse(String query) {
	// CQIE cq = null;
	// query = prepareQuery(query);
	// try {
	// datalogParser.parse(query);
	// cq = datalogParser.getRule(0);
	// } catch (RecognitionException e) {
	// log.warn(e.getMessage());
	// }
	// return cq;
	// }
	//
	// private String prepareQuery(String input) {
	// String query = "";
	// DatalogQueryHelper queryHelper = new
	// DatalogQueryHelper(apic.getPrefixManager());
	//
	// String[] atoms = input.split(OBDALibConstants.DATALOG_IMPLY_SYMBOL, 2);
	// if (atoms.length == 1) // if no head
	// query = queryHelper.getDefaultHead() + " " +
	// OBDALibConstants.DATALOG_IMPLY_SYMBOL + " " + input;
	//
	// // Append the prefixes
	// query = queryHelper.getPrefixes() + query;
	//
	// return query;
	// }

	@Override
	public void datasourceChanged(OBDADataSource oldSource, OBDADataSource newSource) {
		this.selectedSource = newSource;

		// Update the mapping tree.
		SynchronizedMappingListModel model = (SynchronizedMappingListModel) mappingList.getModel();
		URI oldSourceUri = null;
		if (oldSource != null) {
			oldSourceUri = oldSource.getSourceID();
		}
		URI newSourceUri = null;
		if (newSource != null) {
			newSourceUri = newSource.getSourceID();
		}

		model.setFocusedSource(newSourceUri);
		mappingList.revalidate();
		// repaint();
	}

	// @Override
	// public void preferenceChanged() {
	// DefaultTreeModel model = (DefaultTreeModel) mappingsTree.getModel();
	// model.reload();
	// }
}
