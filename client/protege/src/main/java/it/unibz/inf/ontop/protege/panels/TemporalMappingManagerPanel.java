package it.unibz.inf.ontop.protege.panels;

/*
 * #%L
 * ontop-protege
 * %%
 * Copyright (C) 2009 - 2018 KRDB Research Centre. Free University of Bozen Bolzano.
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

import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.OntopSQLCredentialConfiguration;
import it.unibz.inf.ontop.protege.core.TemporalOBDAModel;
import it.unibz.inf.ontop.protege.dialogs.MappingValidationDialog;
import it.unibz.inf.ontop.protege.gui.IconLoader;
import it.unibz.inf.ontop.protege.gui.treemodels.FilteredModel;
import it.unibz.inf.ontop.protege.gui.treemodels.SynchronizedMappingListModel;
import it.unibz.inf.ontop.protege.gui.treemodels.TreeModelFilter;
import it.unibz.inf.ontop.protege.utils.*;
import it.unibz.inf.ontop.spec.mapping.parser.DataSource2PropertiesConvertor;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.validation.SQLSourceQueryValidator;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalTriplesMap;
import it.unibz.inf.ontop.temporal.mapping.impl.SQLPPTemporalTriplesMapImpl;
import it.unibz.inf.ontop.utils.IDGenerator;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TemporalMappingManagerPanel extends JPanel implements EditorPanel {

    private static final long serialVersionUID = -486013653814714526L;

    private Thread validatorThread;

    private SQLSourceQueryValidator validator;

    private TemporalOBDAModel temporalOBDAModel;

    private boolean canceled;

    private JCheckBox chkFilter;
    private JTextField fieldMappings;
    private JList<SQLPPTemporalTriplesMap> mappingList;
    private JTextField txtFilter;

    public TemporalMappingManagerPanel(TemporalOBDAModel _temporalOBDAModel) {
        temporalOBDAModel = _temporalOBDAModel;

        initComponents();

        mappingList.setCellRenderer(new OBDAMappingListRenderer(temporalOBDAModel));
        mappingList.setFixedCellWidth(-1);
        mappingList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        mappingList.addMouseListener(new PopupListener(createPopupMenu()));
        mappingList.addKeyListener(new EditorKeyListener(this));
        mappingList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int count = e.getClickCount();
                if (count == 2) {
                    edit();
                }
            }
        });

        ListModel model = new SynchronizedMappingListModel(temporalOBDAModel);

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

    private JPopupMenu createPopupMenu() {
        JPopupMenu menuMappings = new JPopupMenu();
        JMenuItem add = new JMenuItem();
        add.setText("Create mapping...");
        add.addActionListener((ActionEvent e) -> add());
        menuMappings.add(add);

        JMenuItem delete = new JMenuItem();
        delete.setText("Remove mapping(s)...");
        delete.addActionListener((ActionEvent e) -> remove());
        menuMappings.add(delete);

        JMenuItem editMapping = new JMenuItem();
        editMapping.setText("Edit mapping...");
        editMapping.addActionListener((ActionEvent e) -> edit());
        menuMappings.add(editMapping);

        menuMappings.addSeparator();

        JMenuItem menuValidateBody = new JMenuItem();
        menuValidateBody.setText("Validate SQL");
        menuValidateBody.addActionListener(e -> menuValidateBodyActionPerformed());
        menuMappings.add(menuValidateBody);

        JMenuItem menuExecuteBody = new JMenuItem();
        menuExecuteBody.setText("Execute SQL");
        menuExecuteBody.addActionListener(e -> menuExecuteBodyActionPerformed());
        menuMappings.add(menuExecuteBody);
        return menuMappings;
    }

    @Override
    public void edit() {
        SQLPPTemporalTriplesMap mapping = mappingList.getSelectedValue();
        if (mapping == null) {
            return;
        }
        JDialog dialog = new JDialog();

        dialog.setTitle("Edit Mapping");
        dialog.setModal(true);

        TemporalMappingDialogPanel panel = new TemporalMappingDialogPanel(temporalOBDAModel, dialog);
        panel.setMapping(mapping);
        dialog.setContentPane(panel);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void initComponents() {
        JPanel pnlMappingManager = new JPanel();
        JPanel pnlMappingButtons = new JPanel();
        JButton cmdAddMapping = new JButton();
        JButton cmdRemoveMapping = new JButton();
        JButton cmdDuplicateMapping = new JButton();
        JButton cmdSelectAll = new JButton();
        JButton cmdDeselectAll = new JButton();
        JPanel pnlExtraButtons = new JPanel();
        JLabel labelMappings = new JLabel();
        fieldMappings = new JTextField();
        JLabel lblInsertFilter = new JLabel();
        txtFilter = new JTextField();
        chkFilter = new JCheckBox();
        JScrollPane mappingScrollPane = new JScrollPane();
        mappingList = new JList<>();

        setLayout(new GridBagLayout());

        pnlMappingManager.setAutoscrolls(true);
        pnlMappingManager.setPreferredSize(new Dimension(400, 300));
        pnlMappingManager.setLayout(new BorderLayout());

        pnlMappingButtons.setEnabled(false);
        pnlMappingButtons.setLayout(new GridBagLayout());

        cmdAddMapping.setIcon(IconLoader.getImageIcon("images/plus.png"));
        cmdAddMapping.setText("Create");
        cmdAddMapping.setToolTipText("Create a new mapping");
        cmdAddMapping.setBorder(BorderFactory.createEtchedBorder());
        cmdAddMapping.setContentAreaFilled(false);
        cmdAddMapping.setIconTextGap(5);
        cmdAddMapping.setPreferredSize(new Dimension(75, 25));
        cmdAddMapping.addActionListener(e -> cmdAddMappingActionPerformed());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        pnlMappingButtons.add(cmdAddMapping, gridBagConstraints);

        cmdRemoveMapping.setIcon(IconLoader.getImageIcon("images/minus.png"));
        cmdRemoveMapping.setText("Remove");
        cmdRemoveMapping.setToolTipText("Remove the selected mapping");
        cmdRemoveMapping.setBorder(BorderFactory.createEtchedBorder());
        cmdRemoveMapping.setContentAreaFilled(false);
        cmdRemoveMapping.setIconTextGap(5);
        cmdRemoveMapping.setMaximumSize(new Dimension(75, 25));
        cmdRemoveMapping.setMinimumSize(new Dimension(75, 25));
        cmdRemoveMapping.setPreferredSize(new Dimension(75, 25));
        cmdRemoveMapping.addActionListener(e -> remove());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        pnlMappingButtons.add(cmdRemoveMapping, gridBagConstraints);

        cmdDuplicateMapping.setIcon(IconLoader.getImageIcon("images/copy.png"));
        cmdDuplicateMapping.setText("Copy");
        cmdDuplicateMapping.setToolTipText("Make a duplicate of the selected mapping");
        cmdDuplicateMapping.setBorder(BorderFactory.createEtchedBorder());
        cmdDuplicateMapping.setContentAreaFilled(false);
        cmdDuplicateMapping.setIconTextGap(5);
        cmdDuplicateMapping.setMaximumSize(new Dimension(70, 25));
        cmdDuplicateMapping.setMinimumSize(new Dimension(70, 25));
        cmdDuplicateMapping.setPreferredSize(new Dimension(70, 25));
        cmdDuplicateMapping.addActionListener(e -> cmdDuplicateMappingActionPerformed());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        pnlMappingButtons.add(cmdDuplicateMapping, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        pnlMappingButtons.add(new JPanel(), gridBagConstraints);

        cmdSelectAll.setIcon(IconLoader.getImageIcon("images/select-all.png"));
        cmdSelectAll.setText("Select all");
        cmdSelectAll.setToolTipText("Select all");
        cmdSelectAll.setBorder(BorderFactory.createEtchedBorder());
        cmdSelectAll.setContentAreaFilled(false);
        cmdSelectAll.setIconTextGap(5);
        cmdSelectAll.setPreferredSize(new Dimension(83, 25));
        cmdSelectAll.addActionListener(e -> mappingList.setSelectionInterval(0, mappingList.getModel().getSize()));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        pnlMappingButtons.add(cmdSelectAll, gridBagConstraints);

        cmdDeselectAll.setIcon(IconLoader.getImageIcon("images/select-none.png"));
        cmdDeselectAll.setText("Select none");
        cmdDeselectAll.setToolTipText("Select none");
        cmdDeselectAll.setBorder(BorderFactory.createEtchedBorder());
        cmdDeselectAll.setContentAreaFilled(false);
        cmdDeselectAll.setIconTextGap(5);
        cmdDeselectAll.setMaximumSize(new Dimension(92, 25));
        cmdDeselectAll.setMinimumSize(new Dimension(92, 25));
        cmdDeselectAll.setPreferredSize(new Dimension(92, 25));
        cmdDeselectAll.addActionListener(e -> mappingList.clearSelection());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        pnlMappingButtons.add(cmdDeselectAll, gridBagConstraints);

        pnlMappingManager.add(pnlMappingButtons, BorderLayout.NORTH);

        pnlExtraButtons.setMinimumSize(new Dimension(532, 25));
        pnlExtraButtons.setPreferredSize(new Dimension(532, 25));
        pnlExtraButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));

        labelMappings.setText("Mapping count:");
        pnlExtraButtons.add(labelMappings);

        fieldMappings.setEditable(false);
        fieldMappings.setText("0");
        fieldMappings.setPreferredSize(new Dimension(50, 28));
        pnlExtraButtons.add(fieldMappings);

        lblInsertFilter.setFont(new Font("Dialog", Font.BOLD, 12)); // NOI18N
        lblInsertFilter.setForeground(new Color(53, 113, 163));
        lblInsertFilter.setHorizontalAlignment(SwingConstants.RIGHT);
        lblInsertFilter.setText("Search:");
        lblInsertFilter.setMinimumSize(new Dimension(120, 20));
        lblInsertFilter.setPreferredSize(new Dimension(75, 20));
        pnlExtraButtons.add(lblInsertFilter);

        txtFilter.setPreferredSize(new Dimension(250, 20));
        txtFilter.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                sendFilters(evt);
            }
        });
        pnlExtraButtons.add(txtFilter);

        chkFilter.setText("Enable filter");
        chkFilter.addItemListener(e -> processFilterAction());
        pnlExtraButtons.add(chkFilter);

        pnlMappingManager.add(pnlExtraButtons, BorderLayout.SOUTH);

        mappingScrollPane.setViewportView(mappingList);

        pnlMappingManager.add(mappingScrollPane, BorderLayout.CENTER);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pnlMappingManager, gridBagConstraints);
    }

    /***
     * The action for the search field and the search checkbox. If the checkbox
     * is not selected it cleans the filters. If it is selected it updates to
     * the current search string.
     */
    private void processFilterAction() {
        if (!(chkFilter.isSelected())) {
            applyFilters(new ArrayList<>());
        }
        if (chkFilter.isSelected()) {
            if (txtFilter.getText().isEmpty()) {
                chkFilter.setSelected(false);
                applyFilters(new ArrayList<>());
                return;
            }
            try {
                List<TreeModelFilter<SQLPPTriplesMap>> filters = parseSearchString(txtFilter.getText());
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

    private void sendFilters(KeyEvent evt) {
        int key = evt.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            if (!chkFilter.isSelected()) {
                chkFilter.setSelected(true);
            } else {
                processFilterAction();
            }
        }

    }

    private void menuValidateBodyActionPerformed() {
        final MappingValidationDialog outputField = new MappingValidationDialog(null);

        outputField.setLocationRelativeTo(getParent());
        Runnable action = () -> {
            canceled = false;
            final List path = mappingList.getSelectedValuesList();
            if (path == null) {
                JOptionPane.showMessageDialog(TemporalMappingManagerPanel.this, "Select at least one mapping");
                return;
            }
            outputField.addText("Validating " + path.size() + " SQL queries.\n", outputField.NORMAL);
            for (Object aPath : path) {
                SQLPPTemporalTriplesMap o = (SQLPPTemporalTriplesMap) aPath;
                String id = o.getId();
                outputField.addText("  id: '" + id + "'... ", outputField.NORMAL);
                OntopSQLCredentialConfiguration config = OntopSQLCredentialConfiguration.defaultBuilder()
                        .properties(DataSource2PropertiesConvertor.convert(temporalOBDAModel.getDatasource()))
                        .build();
                validator = new SQLSourceQueryValidator(config.getSettings(), o.getSourceQuery());
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
        };
        validatorThread = new Thread(action);
        validatorThread.start();

        Thread cancelThread = new Thread(() -> {
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
                    if (validator != null) {
                        validator.cancelValidation();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        cancelThread.start();

    }

    private void menuExecuteBodyActionPerformed() {
        SQLPPTemporalTriplesMap mapping = mappingList.getSelectedValue();
        if (mapping == null) {
            return;
        }
        final String sqlQuery = mapping.getSourceQuery().toString();

        SQLQueryPanel pnlQueryResult = new SQLQueryPanel(temporalOBDAModel.getDatasource(), sqlQuery);

        JDialog dlgQueryResult = new JDialog();
        DialogUtils.installEscapeCloseOperation(dlgQueryResult);
        dlgQueryResult.setContentPane(pnlQueryResult);
        dlgQueryResult.pack();
        dlgQueryResult.setLocationRelativeTo(this);
        dlgQueryResult.setVisible(true);
        dlgQueryResult.setTitle("SQL Query Result");
    }

    private void cmdDuplicateMappingActionPerformed() {
        List<SQLPPTemporalTriplesMap> currentSelection = mappingList.getSelectedValuesList();
        if (currentSelection == null) {
            JOptionPane.showMessageDialog(this, "No mappings have been selected", "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "This will create copies of the selected mappings. \nNumber of mappings selected = "
                        + currentSelection.size() + "\nContinue? ",
                "Copy confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.NO_OPTION || confirm == JOptionPane.CANCEL_OPTION || confirm == JOptionPane.CLOSED_OPTION) {
            return;
        }
        URI current_srcuri = temporalOBDAModel.getDatasource().getSourceID();

        for (SQLPPTemporalTriplesMap mapping : currentSelection) {
            String id = mapping.getId();

            // Computing the next available ID
            int new_index = -1;
            for (int index = 0; index < 999999999; index++) {
                if (temporalOBDAModel.indexOf(id + "(" + index + ")") == -1) {
                    new_index = index;
                    break;
                }
            }
            String newId = id + "(" + new_index + ")";

            // inserting the new mapping
            try {

                SQLPPTemporalTriplesMap oldmapping = (SQLPPTemporalTriplesMap) temporalOBDAModel.getTriplesMap(id);
                SQLPPTemporalTriplesMap newmapping = new SQLPPTemporalTriplesMapImpl(newId, oldmapping.getSourceQuery(),
                        oldmapping.getTargetAtoms(), oldmapping.getTemporalMappingInterval());
                temporalOBDAModel.addTriplesMap(current_srcuri, newmapping, false);

            } catch (DuplicateMappingException e) {
                JOptionPane.showMessageDialog(this, "Duplicate Mapping: " + newId);
                return;
            }
        }

    }

    @Override
    public void remove() {
        if (mappingList.isSelectionEmpty()) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Proceed deleting " + mappingList.getSelectedIndices().length + " mappings?", "Conform",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.CANCEL_OPTION || confirm == JOptionPane.CLOSED_OPTION) {
            return;
        }

        List<SQLPPTemporalTriplesMap> values = mappingList.getSelectedValuesList();

        URI srcuri = temporalOBDAModel.getDatasource().getSourceID();

        for (SQLPPTemporalTriplesMap mapping : values) {
            if (mapping != null) {
                temporalOBDAModel.removeTriplesMap(srcuri, mapping.getId());
            }
        }
        mappingList.clearSelection();
    }

    private void cmdAddMappingActionPerformed() {
        if (temporalOBDAModel.getDatasource() != null) {
            add();
        } else {
            JOptionPane.showMessageDialog(this, "Select a data source to proceed", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void add() {
        String id = IDGenerator.getNextUniqueID("MAPID-");

        JDialog dialog = new JDialog();
        dialog.setTitle("New Mapping");
        dialog.setModal(true);

        TemporalMappingDialogPanel panel = new TemporalMappingDialogPanel(temporalOBDAModel, dialog);
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
    // End of variables declaration//GEN-END:variables

    /**
     * Parses the string in the search field.
     *
     * @param textToParse The target text
     * @return A list of filter objects or null if the string was empty or
     * erroneous
     */
    private List<TreeModelFilter<SQLPPTriplesMap>> parseSearchString(String textToParse) throws Exception {

        List<TreeModelFilter<SQLPPTriplesMap>> listOfFilters = null;

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
    private void applyFilters(List<TreeModelFilter<SQLPPTriplesMap>> filters) {
        FilteredModel model = (FilteredModel) mappingList.getModel();
        model.removeAllFilters();
        model.addFilters(filters);
    }

    public void updateModel(TemporalOBDAModel temporalOBDAModel) {
        this.temporalOBDAModel = temporalOBDAModel;
        SynchronizedMappingListModel model = (SynchronizedMappingListModel) mappingList.getModel();
        model.setFocusedSource(temporalOBDAModel.getSourceId());
        mappingList.revalidate();
    }


}
