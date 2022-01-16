package it.unibz.inf.ontop.protege.action;

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

import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.mapping.TriplesMap;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OntopAbstractAction;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.*;

public class MappingStatisticsAction extends ProtegeAction {

	private static final long serialVersionUID = 3322509244957306932L;

	private static final Logger LOGGER = LoggerFactory.getLogger(MappingStatisticsAction.class);

	private static final String DIALOG_TITLE = "Mapping Statistics";

	private static final int ERROR_ENTRY = -1;

	private static final class TriplesMapInfo {
		private final String id;
		private final int count;

		TriplesMapInfo(String id, int count) {
			this.id = id;
			this.count = count;
		}

		boolean isValid() { return count != ERROR_ENTRY; }

		Object[] asRow() {
			return new Object[] { id, isValid() ? count : "<html><i>error</i></html>" };
		}
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		JDialog dialog = new JDialog((Frame)null, DIALOG_TITLE, true);

		JPanel statisticsPanel = new JPanel(new BorderLayout());
		JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		summaryPanel.add(new JLabel("<html><b>Total number of triples:</b></html>"));
		JLabel summaryLabel = new JLabel("<html><i>retrieving...</i></html>");
		summaryPanel.add(summaryLabel);
		statisticsPanel.add(summaryPanel, BorderLayout.NORTH);

		String[] columnNames = {"Mapping ID", "Triples"};
		DefaultTableModel tableModel = DialogUtils.createNonEditableTableModel(columnNames);

		JTable triplesCountTable = new JTable(tableModel);
		triplesCountTable.getColumnModel().getColumn(0).setPreferredWidth(400);
		triplesCountTable.getColumnModel().getColumn(1).setMaxWidth(100);
		statisticsPanel.add(new JScrollPane(triplesCountTable), BorderLayout.CENTER);

		OBDAModel obdaModel = OBDAEditorKitSynchronizerPlugin.getCurrentOBDAModel(getEditorKit());
		SwingWorker<Integer, TriplesMapInfo> worker = new SwingWorker<Integer, TriplesMapInfo>() {

			@Override
			protected Integer doInBackground() throws Exception {
				try (Connection c = obdaModel.getDataSource().getConnection();
					 Statement st = c.createStatement()) {
					int total = 0;
					for (TriplesMap map : obdaModel.getTriplesMapManager()) {
						if (isCancelled())
							break;

						TriplesMapInfo info = retrieveInfo(map, st);
						if (info.isValid() && total != ERROR_ENTRY)
							total += info.count;
						else
							total = ERROR_ENTRY;

						publish(info);
					}
					return total;
				}
			}

			@Override
			protected void process(java.util.List<TriplesMapInfo> list) {
				for (TriplesMapInfo info : list)
					tableModel.addRow(info.asRow());
			}

			@Override
			protected void done() {
				try {
					int count = get();
					summaryLabel.setText(count != ERROR_ENTRY
						? "<html><b>" + count + "</b></html>"
						: "An error occurred in the counting process.");
				}
				catch (CancellationException e) {
					/* NO-OP */
				}
				catch (InterruptedException e) {
					summaryLabel.setText("An error occurred: " + e.getMessage());
				}
				catch (ExecutionException e) {
					dialog.dispose();
					DialogUtils.showErrorDialog(getWorkspace(), DIALOG_TITLE, DIALOG_TITLE + "error.", LOGGER, e, obdaModel.getDataSource());
				}
			}
		};
		worker.execute();

		JPanel commandPanel = new JPanel(new FlowLayout());

		OntopAbstractAction closeAction = getStandardCloseWindowAction("Close", dialog);
		JButton closeButton = getButton(closeAction);
		commandPanel.add(closeButton);

		setUpAccelerator(dialog.getRootPane(), closeAction);
		dialog.getRootPane().setDefaultButton(closeButton);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				worker.cancel(false);
			}
		});

		dialog.setLayout(new BorderLayout());
		dialog.add(statisticsPanel, BorderLayout.CENTER);
		dialog.add(commandPanel, BorderLayout.SOUTH);

		dialog.setPreferredSize(new Dimension(520, 400));
		DialogUtils.setLocationRelativeToProtegeAndOpen(getEditorKit(), dialog);
	}

	private static TriplesMapInfo retrieveInfo(TriplesMap triplesMap, Statement st) {
		try {
			String sql = getSelectionString(triplesMap.getSqlQuery());
			try (ResultSet rs = st.executeQuery(sql)) {
				int count = 0;
				while (rs.next()) {
					count = rs.getInt(1);
				}
				return new TriplesMapInfo(triplesMap.getId(), count * triplesMap.getTargetAtoms().size());
			}
		}
		catch (Exception e) {
			LOGGER.error("Exception while computing mapping statistics", e);
			return new TriplesMapInfo(triplesMap.getId(), ERROR_ENTRY);
		}
	}

	private static String getSelectionString(String originalSql) throws Exception {
		String sql = originalSql.toLowerCase(); // make it lower case to help identify a string.
		Matcher m = Pattern.compile("[\\n\\s\\t]*from[\\n\\s\\t]*").matcher(sql);
		if (m.find()) {
			int start = m.start();
			return "select COUNT(*) " + originalSql.substring(start);
		}
		throw new Exception("Could not find the \"FROM\" keyword in the source query\n" + originalSql);
	}

	@Override
	public void initialise()  { /* NO-OP */}

	@Override
	public void dispose() { /* NO-OP */ }

}
