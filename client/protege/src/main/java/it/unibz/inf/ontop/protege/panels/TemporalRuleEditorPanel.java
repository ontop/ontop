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

import it.unibz.inf.ontop.protege.core.TemporalOBDAModel;
import it.unibz.inf.ontop.protege.gui.IconLoader;
import it.unibz.inf.ontop.protege.utils.EditorKeyListener;
import it.unibz.inf.ontop.protege.utils.EditorPanel;
import it.unibz.inf.ontop.protege.utils.PopupListener;
import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class TemporalRuleEditorPanel extends JPanel implements EditorPanel {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private TemporalOBDAModel tobdaModel;
    private JCheckBox chkFilter;
    private JTextField txtRules;
    private JList<DatalogMTLRule> lstRules;
    private JTextField txtFilter;

    public TemporalRuleEditorPanel(TemporalOBDAModel tobdaModel) {
        this.tobdaModel = tobdaModel;
        initComponents();
        lstRules.setFixedCellWidth(-1);
        lstRules.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstRules.addMouseListener(new PopupListener(createPopupMenu()));
        lstRules.addKeyListener(new EditorKeyListener(this));
        lstRules.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    edit();
                }
            }
        });

        lstRules.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> new TemporalRuleTextPane(tobdaModel.dmtlRuleToString(value), isSelected));

        DefaultListModel<DatalogMTLRule> model = new DefaultListModel<>();

        if (!tobdaModel.getRules().isEmpty()) {
            for (DatalogMTLRule datalogMTLRule : tobdaModel.getRules()) {
                model.addElement(datalogMTLRule);
            }
        }

        model.addListDataListener(new ListDataListener() {
            @Override
            public void intervalRemoved(ListDataEvent e) {
                txtRules.setText(String.valueOf(lstRules.getModel().getSize()));
            }

            @Override
            public void intervalAdded(ListDataEvent e) {
                txtRules.setText(String.valueOf(lstRules.getModel().getSize()));
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                txtRules.setText(String.valueOf(lstRules.getModel().getSize()));
            }
        });
        lstRules.setModel(model);
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu menuMappings = new JPopupMenu();
        JMenuItem add = new JMenuItem();
        add.setText("Create rule...");
        add.addActionListener(e -> add());
        menuMappings.add(add);

        JMenuItem delete = new JMenuItem();
        delete.setText("Remove rule(s)...");
        delete.addActionListener(e -> remove());
        menuMappings.add(delete);

        JMenuItem editMapping = new JMenuItem();
        editMapping.setText("Edit rule...");
        editMapping.addActionListener(e -> edit());
        menuMappings.add(editMapping);

        return menuMappings;
    }

    @Override
    public void edit() {
        if(!lstRules.isSelectionEmpty()) {
            new TemporalRuleDialogPanel(this,
                    lstRules.getSelectedIndex(),
                    tobdaModel.dmtlRuleToString(lstRules.getSelectedValue()),
                    lstRules.getSelectedValue().isStatic());
        }
    }

    private void initComponents() {
        JPanel pnlExtraButtons = new JPanel();
        JLabel lblRules = new JLabel("Rule count:");
        txtRules = new JTextField();
        JLabel lblInsertFilter = new JLabel();
        txtFilter = new JTextField();
        chkFilter = new JCheckBox();
        JScrollPane mappingScrollPane = new JScrollPane();
        lstRules = new JList<>();

        setLayout(new GridBagLayout());

        JPanel pnlMappingManager = new JPanel();
        pnlMappingManager.setAutoscrolls(true);
        pnlMappingManager.setPreferredSize(new Dimension(400, 300));
        pnlMappingManager.setLayout(new BorderLayout());

        JPanel pnlMappingButtons = new JPanel();
        pnlMappingButtons.setEnabled(false);
        pnlMappingButtons.setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        JButton btnAdd = new JButton("Create");
        btnAdd.setIcon(IconLoader.getImageIcon("images/plus.png"));
        btnAdd.setToolTipText("Create a new Rule");
        btnAdd.setBorder(BorderFactory.createEtchedBorder());
        btnAdd.setContentAreaFilled(false);
        btnAdd.setIconTextGap(5);
        btnAdd.setPreferredSize(new Dimension(75, 25));
        btnAdd.addActionListener(e -> add());
        pnlMappingButtons.add(btnAdd, gridBagConstraints);

        JButton btnRemove = new JButton("Remove");
        btnRemove.setToolTipText("Remove selected rules");
        btnRemove.setIcon(IconLoader.getImageIcon("images/minus.png"));
        btnRemove.setBorder(BorderFactory.createEtchedBorder());
        btnRemove.setContentAreaFilled(false);
        btnRemove.setIconTextGap(5);
        btnRemove.setPreferredSize(new Dimension(75, 25));
        btnRemove.addActionListener(e -> remove());
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        pnlMappingButtons.add(btnRemove, gridBagConstraints);

        /*JButton btnDuplicate = new JButton("Copy");
        btnDuplicate.setToolTipText("Copy selected rules");
        btnDuplicate.setIcon(IconLoader.getImageIcon("images/copy.png"));
        btnDuplicate.setToolTipText("Make a duplicate of the selected rule");
        btnDuplicate.setBorder(BorderFactory.createEtchedBorder());
        btnDuplicate.setContentAreaFilled(false);
        btnDuplicate.setIconTextGap(5);
        btnDuplicate.setPreferredSize(new Dimension(70, 25));
        btnDuplicate.addActionListener(e -> cmdDuplicateMappingActionPerformed());
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        pnlMappingButtons.add(btnDuplicate, gridBagConstraints);*/

        JButton btnSelectAll = new JButton("Select all");
        btnSelectAll.setIcon(IconLoader.getImageIcon("images/select-all.png"));
        btnSelectAll.setToolTipText("Select all");
        btnSelectAll.setBorder(BorderFactory.createEtchedBorder());
        btnSelectAll.setContentAreaFilled(false);
        btnSelectAll.setIconTextGap(5);
        btnSelectAll.setPreferredSize(new Dimension(83, 25));
        btnSelectAll.addActionListener(e -> lstRules.setSelectionInterval(0, lstRules.getModel().getSize()));
        gridBagConstraints.gridx = 7;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        pnlMappingButtons.add(btnSelectAll, gridBagConstraints);

        JButton btnDeselectAll = new JButton("Select none");
        btnDeselectAll.setIcon(IconLoader.getImageIcon("images/select-none.png"));
        btnDeselectAll.setToolTipText("Select none");
        btnDeselectAll.setBorder(BorderFactory.createEtchedBorder());
        btnDeselectAll.setContentAreaFilled(false);
        btnDeselectAll.setIconTextGap(5);
        btnDeselectAll.setPreferredSize(new Dimension(92, 25));
        btnDeselectAll.addActionListener(e -> lstRules.clearSelection());
        gridBagConstraints.gridx = 8;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        pnlMappingButtons.add(btnDeselectAll, gridBagConstraints);

        JLabel lblNamespace = new JLabel("Namespace:");
        gridBagConstraints.gridx = 3;
        pnlMappingButtons.add(lblNamespace, gridBagConstraints);

        JTextField txtNamespace = new JTextField(tobdaModel.getNamespace());

        JButton btnNamespace = new JButton("Save Namespace");
        gridBagConstraints.gridx = 5;
        btnNamespace.setPreferredSize(new Dimension(80, 25));
        btnNamespace.addActionListener(e -> tobdaModel.updateNamespace(txtNamespace.getText()));
        pnlMappingButtons.add(btnNamespace, gridBagConstraints);

        gridBagConstraints.gridx = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        txtNamespace.setPreferredSize(new Dimension(250, 25));
        pnlMappingButtons.add(txtNamespace, gridBagConstraints);

        pnlMappingManager.add(pnlMappingButtons, BorderLayout.NORTH);

        pnlExtraButtons.setPreferredSize(new Dimension(532, 25));
        pnlExtraButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));

        pnlExtraButtons.add(lblRules);

        txtRules.setEditable(false);
        txtRules.setText("0");
        txtRules.setPreferredSize(new Dimension(50, 28));
        pnlExtraButtons.add(txtRules);

        lblInsertFilter.setFont(new Font("Dialog", Font.BOLD, 12)); // NOI18N
        lblInsertFilter.setForeground(new Color(53, 113, 163));
        lblInsertFilter.setHorizontalAlignment(SwingConstants.RIGHT);
        lblInsertFilter.setText("Search:");
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
        chkFilter.addItemListener(evt -> processFilterAction());
        pnlExtraButtons.add(chkFilter);

        pnlMappingManager.add(pnlExtraButtons, BorderLayout.SOUTH);

        mappingScrollPane.setViewportView(lstRules);

        pnlMappingManager.add(mappingScrollPane, BorderLayout.CENTER);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pnlMappingManager, gridBagConstraints);
    }

    private void processFilterAction() {
        //TODO filter the rules
    }

    /***
     * Action for key's entered in the search text box.
     */
    private void sendFilters(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            if (!chkFilter.isSelected()) {
                chkFilter.setSelected(true);
            } else {
                processFilterAction();
            }
        }

    }

    private void cmdDuplicateMappingActionPerformed() {
        List<DatalogMTLRule> currentSelection = lstRules.getSelectedValuesList();
        if (currentSelection == null) {
            JOptionPane.showMessageDialog(this, "No rules have been selected", "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "This will create copies of the selected rules. \nNumber of rules selected = "
                        + currentSelection.size() + "\nContinue? ",
                "Copy confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.NO_OPTION || confirm == JOptionPane.CANCEL_OPTION || confirm == JOptionPane.CLOSED_OPTION) {
            return;
        }

        for (DatalogMTLRule datalogMTLRule : currentSelection) {

                //TODO create a duplication of rules
                // OR remove copy button
        }

    }

    @Override
    public void remove() {
        if (lstRules.isSelectionEmpty()) {
            return;
        }
        List<DatalogMTLRule> values = lstRules.getSelectedValuesList();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Proceed deleting " + values.size() + " rules?", "Delete Confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.CANCEL_OPTION || confirm == JOptionPane.CLOSED_OPTION) {
            return;
        }
        for (DatalogMTLRule rule : values) {
            if (rule != null) {
                tobdaModel.removeRule(rule);
                ((DefaultListModel<DatalogMTLRule>) lstRules.getModel()).removeElement(rule);
            }
        }
        lstRules.clearSelection();
    }

    @Override
    public void add() {
        new TemporalRuleDialogPanel(this);
    }

    public void setFilter(String filter) {
        txtFilter.setText(filter);
        processFilterAction();
    }

    boolean addRule(int ruleIndex, String rule) {
        try {
            DatalogMTLRule parsedRule = tobdaModel.parseRule(rule);
            if (parsedRule != null) {
                DefaultListModel<DatalogMTLRule> ruleListModel = (DefaultListModel<DatalogMTLRule>) lstRules.getModel();
                if (ruleIndex < 0) {
                    tobdaModel.addRule(parsedRule);
                    ruleListModel.addElement(parsedRule);
                } else {
                    tobdaModel.updateRule(ruleListModel.getElementAt(ruleIndex), parsedRule);
                    ruleListModel.setElementAt(parsedRule, ruleIndex);
                }
                return true;
        }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error while inserting rule: " + e.getMessage() + " is already taken");
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    public void ontologyChanged(TemporalOBDAModel tobdaModel) {
        this.tobdaModel = tobdaModel;
        DefaultListModel<DatalogMTLRule> model = (DefaultListModel) lstRules.getModel();
        model.removeAllElements();
        if (!tobdaModel.getRules().isEmpty()) {
            for (DatalogMTLRule datalogMTLRule : tobdaModel.getRules()) {
                model.addElement(datalogMTLRule);
            }
        }
    }
}
