package it.unibz.inf.ontop.protege.gui.dialogs;

/*
 * #%L
 * ontop-protege
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

import it.unibz.inf.ontop.protege.utils.DialogUtils;

import javax.swing.*;

import java.awt.*;

public class TextMessageDialog extends javax.swing.JDialog {

	private static final long serialVersionUID = -200114540739796897L;


    public TextMessageDialog(String title, String sql, String processingTime) {

        setTitle(title);

        JLabel jLabel1 = new JLabel(processingTime);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(700, 550));
        getContentPane().setLayout(new GridBagLayout());

        JPanel jPanel1 = new JPanel(new GridBagLayout());

        JTextArea jTextArea1 = new JTextArea(sql);
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(5);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setFont(new Font("Dialog", Font.PLAIN, 12));

        jPanel1.add(new JScrollPane(jTextArea1),
                new GridBagConstraints(0, 0, 1, 1, 1, 1,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                        new Insets(5, 5, 5, 5), 0, 0));

        jPanel1.add(jLabel1,
                new GridBagConstraints(0, 1, 1, 1, 1, 0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 5));

        getContentPane().add(jPanel1,
                new GridBagConstraints(0, 0, 1, 1, 1, 1,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                        new Insets(5, 5, 5, 5), 20, 0));

        JButton jButton1 = new JButton("Close");
        jButton1.addActionListener(evt -> dispose());
        getContentPane().add(jButton1,
                new GridBagConstraints(0, 1, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));

        pack();
        DialogUtils.installEscapeCloseOperation(this);
    }
}
