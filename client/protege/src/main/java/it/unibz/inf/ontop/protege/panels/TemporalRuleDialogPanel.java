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

import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.core.TemporalOBDAModel;
import it.unibz.inf.ontop.protege.gui.IconLoader;
import it.unibz.inf.ontop.protege.utils.DatasourceSelectorListener;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.QueryPainter;
import it.unibz.inf.ontop.protege.utils.TabKeyListener;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLSyntaxParser;
import it.unibz.inf.ontop.spec.datalogmtl.parser.impl.DatalogMTLSyntaxParserImpl;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class TemporalRuleDialogPanel extends JPanel implements DatasourceSelectorListener {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final TemporalOBDAModel obdaModel;
    private final JDialog parent;
    private OBDADataSource dataSource;
    private JButton btnInsertRule;
    private JCheckBox chkStatic;
    private JTextPane txtRule;
    private DatalogMTLRule datalogMTLRule;

    TemporalRuleDialogPanel(TemporalOBDAModel obdaModel, JDialog parent, OBDADataSource dataSource) {
        this(obdaModel, parent, dataSource, null);
    }

    TemporalRuleDialogPanel(TemporalOBDAModel obdaModel, JDialog parent, OBDADataSource dataSource, DatalogMTLRule datalogMTLRule) {

        DialogUtils.installEscapeCloseOperation(parent);
        this.obdaModel = obdaModel;
        this.parent = parent;
        this.dataSource = dataSource;
        this.datalogMTLRule = datalogMTLRule;

        PrefixManager prefixManager = obdaModel.getMutablePrefixManager();

        initComponents();

        btnInsertRule.setEnabled(false);
        // TODO painter for rules
        //QueryPainter painter = new QueryPainter(obdaModel, txtRule);
        //painter.addValidatorListener(result -> btnInsertRule.setEnabled(result));

        btnInsertRule.addActionListener(e -> cmdInsertMappingActionPerformed());
        txtRule.addKeyListener(new TabKeyListener());
        txtRule.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // NO-OP
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (btnInsertRule.isEnabled() && (e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyCode() == KeyEvent.VK_ENTER)) {
                    cmdInsertMappingActionPerformed();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        if (this.datalogMTLRule != null) {
            btnInsertRule.setText("Update");
            txtRule.setText(this.datalogMTLRule.render());
        }
    }

    private void insertRule(String rule) {
        DatalogMTLRule parsedRule = parse(rule);
        if (rule != null) {
            //TODO do we need to validate rule?
            //List<String> invalidPredicates = TargetQueryValidator.validate(targetRule, obdaModel.getCurrentVocabulary());
            //if (invalidPredicates.isEmpty()) {
            try {
                TemporalOBDAModel mapcon = obdaModel;
                LOGGER.info("Insert rule: \n" + rule);
                if (datalogMTLRule == null) {
                    mapcon.addRule(parsedRule);
                } else {
                    mapcon.updateRule(datalogMTLRule, parsedRule);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error while inserting rule: " + e.getMessage() + " is already taken");
                return;
            }
            parent.setVisible(false);
            parent.dispose();
            /*} else {
                StringBuilder invalidList = new StringBuilder();
                for (String predicate : invalidPredicates) {
                    invalidList.append("- ").append(predicate).append("\n");
                }
                JOptionPane.showMessageDialog(this,
                        "This list of predicates is unknown by the ontology: \n" + invalidList,
                        "New Rule", JOptionPane.WARNING_MESSAGE);
            }*/
        }
    }

    private void initComponents() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();


        setFocusable(false);
        setMinimumSize(new Dimension(600, 500));
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
            parent.setVisible(false);
            parent.dispose();
        });
        pnlCommandButton.add(cmdCancel);

        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(pnlCommandButton, gridBagConstraints);

        JScrollPane scrTargetQuery = new JScrollPane();
        scrTargetQuery.setFocusable(false);
        scrTargetQuery.setMinimumSize(new Dimension(600, 170));
        scrTargetQuery.setPreferredSize(new Dimension(600, 170));

        txtRule = new JTextPane();
        txtRule.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 13)); // NOI18N
        txtRule.setFocusCycleRoot(false);
        txtRule.setMinimumSize(new Dimension(600, 170));
        txtRule.setPreferredSize(new Dimension(600, 170));
        scrTargetQuery.setViewportView(txtRule);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(scrTargetQuery, gridBagConstraints);

        getAccessibleContext().setAccessibleName("Rule editor");
    }

    private void cmdInsertMappingActionPerformed() {
        final String ruleString = txtRule.getText();
        if (ruleString.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "ERROR: The rule cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (chkStatic.isSelected()) {
            insertRule("[static]\n" + ruleString);
        } else {
            insertRule(ruleString);
        }
    }

    private DatalogMTLRule parse(String query) {
        DatalogMTLSyntaxParser datalogMTLSyntaxParser = new DatalogMTLSyntaxParserImpl(obdaModel.getAtomFactory(), obdaModel.getTermFactory());
        return datalogMTLSyntaxParser.parse(query).getRules().get(0);
    }

    @Override
    public void datasourceChanged(OBDADataSource oldSource, OBDADataSource newSource) {
        dataSource = newSource;
    }
}
