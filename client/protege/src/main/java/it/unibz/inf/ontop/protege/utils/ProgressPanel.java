package it.unibz.inf.ontop.protege.utils;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class ProgressPanel extends JPanel {

	private static final long serialVersionUID = 8447122293962076783L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ProgressPanel.class);

    public ProgressPanel(OBDAProgressMonitor monitor, String msg) {
        setLayout(new GridBagLayout());

        add(new JLabel(msg),
                new GridBagConstraints(0, 0, 1, 1, 0, 0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                        new Insets(10, 10, 10, 10), 0, 0));

        JProgressBar barProgressActivity = new JProgressBar();
        barProgressActivity.setIndeterminate(true);
        barProgressActivity.setMinimumSize(new Dimension(200, 15));
        barProgressActivity.setPreferredSize(new Dimension(300, 18));
        add(barProgressActivity,
                new GridBagConstraints(0, 1, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 10, 0, 10), 0, 0));

        JButton cmdCancelOperation = new JButton("Cancel Operation");
        cmdCancelOperation.addActionListener(evt -> {
            try {
                monitor.cancel();
            }
            catch (Exception e) {
                DialogUtils.showSeeLogErrorDialog(this, "Error canceling action", LOGGER, e);
            }
        });
        add(cmdCancelOperation,
                new GridBagConstraints(0, 2, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(10, 0, 10, 0), 0, 0));
    }
}
