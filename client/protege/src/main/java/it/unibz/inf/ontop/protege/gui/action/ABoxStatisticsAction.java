package it.unibz.inf.ontop.protege.gui.action;

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

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.panels.OBDAModelStatisticsPanel;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.JDBCConnectionManager;
import it.unibz.inf.ontop.protege.utils.OBDAProgressMonitor;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ABoxStatisticsAction extends ProtegeAction {

	private static final Logger log = LoggerFactory.getLogger(ABoxStatisticsAction.class);

	private static final long serialVersionUID = 3322509244957306932L;

	@Override
	public void actionPerformed(ActionEvent e) {
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setSize(520, 400);
		dialog.setLocationRelativeTo(getWorkspace());
		dialog.setTitle("OBDA Model Statistics");

		OBDAModelStatisticsPanel pnlStatistics = new OBDAModelStatisticsPanel();

		Thread thread = new Thread("OBDAModelStatistics Thread") {
			@Override
			public void run() {
				OBDAProgressMonitor monitor = new OBDAProgressMonitor("Create statistics...", getWorkspace());
				monitor.addProgressListener(pnlStatistics);
				monitor.start();
				OBDAModelManager obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(getEditorKit());
				SwingUtilities.invokeLater(() -> pnlStatistics.initContent(getStatistics(obdaModelManager)));
				monitor.stop();
				if(!pnlStatistics.isCancelled() && !pnlStatistics.isErrorShown()) {
					SwingUtilities.invokeLater(() -> dialog.setVisible(true));
				}
			}
		};
		thread.start();

		JPanel pnlCommandButton = new JPanel();
		pnlCommandButton.setLayout(new FlowLayout());

		JButton cmdCloseInformation = new JButton("Close");
		cmdCloseInformation.addActionListener(evt -> {
			dialog.setVisible(false);
			dialog.removeAll();
			dialog.dispose();
		});
		pnlCommandButton.add(cmdCloseInformation);

		dialog.setLayout(new BorderLayout());
		dialog.add(pnlStatistics, BorderLayout.CENTER);
		dialog.add(pnlCommandButton, BorderLayout.SOUTH);
		
		DialogUtils.installEscapeCloseOperation(dialog);
		
		dialog.pack();
	}

	private static ImmutableMap<String, Integer> getStatistics(OBDAModelManager obdaModelManager) {
		JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();
		OBDADataSource source = obdaModelManager.getDatasource();
		try (Connection c = man.getConnection(source.getURL(), source.getUsername(), source.getPassword());
			 Statement st = c.createStatement()) {
			ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
			for (SQLPPTriplesMap mapping : obdaModelManager.getActiveOBDAModel().getMapping()) {
				SQLPPSourceQuery sourceQuery = mapping.getSourceQuery();
				try {
					String sql = String.format("select COUNT(*) %s", getSelectionString(sourceQuery.getSQL()));
					try (ResultSet rs = st.executeQuery(sql)) {
						int count = 0;
						while (rs.next()) {
							count = rs.getInt(1);
						}
						int atoms = mapping.getTargetAtoms().size();
						builder.put(mapping.getId(), count * atoms);
					}
				}
				catch (Exception e) {
					builder.put(mapping.getId(), -1); // fails to count
					log.error("Exception while computing mapping statistics", e);
				}
			}
			return builder.build();
		}
		catch (SQLException e) {
			return ImmutableMap.of();
		}
	}


	private static String getSelectionString(String originalSql) throws Exception {
		String sql = originalSql.toLowerCase(); // make it lower case to help identify a string.
		Matcher m = Pattern.compile("[\\n\\s\\t]*from[\\n\\s\\t]*").matcher(sql);
		if (m.find()) {
			int start = m.start();
			int end = sql.length();
			return originalSql.substring(start, end);
		}
		throw new Exception("Could not find the \"from\" keyword in the source query\n" + originalSql);
	}

	@Override
	public void initialise()  { /* NO-OP */}

	@Override
	public void dispose() { /* NO-OP */ }

}
