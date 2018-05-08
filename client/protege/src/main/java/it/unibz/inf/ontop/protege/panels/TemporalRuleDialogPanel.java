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

import it.unibz.inf.ontop.protege.gui.IconLoader;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.TabKeyListener;
import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TemporalRuleDialogPanel extends JDialog {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private JButton btnInsertRule;
    private JCheckBox chkStatic;
    private TemporalRuleTextPane txtRule;
    private TemporalRuleEditorPanel editorPanel;

    TemporalRuleDialogPanel(TemporalRuleEditorPanel editorPanel) {
        this(editorPanel, null);
    }

    TemporalRuleDialogPanel(TemporalRuleEditorPanel editorPanel, DatalogMTLRule datalogMTLRule) {

        DialogUtils.installEscapeCloseOperation(this);
        this.editorPanel = editorPanel;

        initComponents();

        btnInsertRule.addActionListener(e -> insertRule());
        txtRule.addKeyListener(new TabKeyListener());
        txtRule.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (btnInsertRule.isEnabled() && (e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyCode() == KeyEvent.VK_ENTER)) {
                    insertRule();
                }
            }
        });

        if (datalogMTLRule != null) {
            btnInsertRule.setText("Update");
            txtRule.setRule(datalogMTLRule, false);
            chkStatic.setSelected(datalogMTLRule.isStatic());
            setTitle("Edit Rule");
        }else {
            setTitle("New Rule");
        }
        setModal(true);
        setSize(600, 500);
        setLocationRelativeTo(this);
        setVisible(true);
    }

    private void insertRule() {
        final String ruleString = txtRule.getText();
        if (ruleString.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "ERROR: The rule cannot be empty", "Error",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            if (chkStatic.isSelected()) {
                insertRule("[static]\n" + ruleString);
            } else {
                insertRule(ruleString);
            }
        }
    }

    private void insertRule(String rule) {
        if (editorPanel.addRule(rule)) {
            setVisible(false);
            dispose();
        }
    }

    private void initComponents() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        setFocusable(false);
        setPreferredSize(new Dimension(600, 500));
        setLayout(new GridBagLayout());

        chkStatic = new JCheckBox("Static Rule");
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        add(chkStatic, gridBagConstraints);

        JPanel pnlCommandButton = new JPanel();
        pnlCommandButton.setFocusable(false);
        pnlCommandButton.setLayout(new FlowLayout(FlowLayout.RIGHT));

        btnInsertRule = new JButton();
        btnInsertRule.setIcon(IconLoader.getImageIcon("images/accept.png"));
        btnInsertRule.setText("Accept");
        btnInsertRule.setToolTipText("This will add/edit the current rule into the OBDA model");
        btnInsertRule.setActionCommand("OK");
        btnInsertRule.setBorder(BorderFactory.createEtchedBorder());
        btnInsertRule.setContentAreaFilled(false);
        btnInsertRule.setIconTextGap(5);
        btnInsertRule.setPreferredSize(new Dimension(90, 25));
        pnlCommandButton.add(btnInsertRule);

        JButton cmdCancel = new JButton();
        cmdCancel.setIcon(IconLoader.getImageIcon("images/cancel.png"));
        cmdCancel.setText("Cancel");
        cmdCancel.setBorder(BorderFactory.createEtchedBorder());
        cmdCancel.setContentAreaFilled(false);
        cmdCancel.setIconTextGap(5);
        cmdCancel.setPreferredSize(new Dimension(90, 25));
        cmdCancel.addActionListener(e -> {
            setVisible(false);
            dispose();
        });
        pnlCommandButton.add(cmdCancel);

        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(pnlCommandButton, gridBagConstraints);

        txtRule = new TemporalRuleTextPane();
        //txtRule.setPreferredSize(new Dimension(600, 170));
        //JScrollPane scrRule = new JScrollPane(txtRule);
        //scrRule.setPreferredSize(new Dimension(600, 170));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(new JScrollPane(txtRule), gridBagConstraints);

        getAccessibleContext().setAccessibleName("Rule editor");
    }
}
