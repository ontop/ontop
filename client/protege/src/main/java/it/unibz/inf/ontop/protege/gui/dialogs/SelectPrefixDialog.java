package it.unibz.inf.ontop.protege.gui.dialogs;

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

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.protege.core.MutablePrefixManager;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.KeyEvent.*;

public class SelectPrefixDialog extends JDialog {

	private static final long serialVersionUID = -8277829841902027620L;

	private final Map<String, String> prefixMap;
	private final ArrayList<JCheckBox> checkboxes = new ArrayList<>();

	/**
	 * Reads queryTextComponent to extract existing prefixes (using regex)
	 * Updates queryTextComponent when the choice of prefixes is confirmed
	 *
	 * @param prefixManager
	 * @param queryTextComponent query entry field
	 */

	public SelectPrefixDialog(MutablePrefixManager prefixManager, JTextComponent queryTextComponent) {
		prefixMap = prefixManager.getPrefixMap();

		setTitle("Select Prefixes for the Query");
		setModal(true);

		Action acceptAction = new AbstractAction("OK") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String directives = getDirectives();
				queryTextComponent.setText((directives.isEmpty() ? "" : directives + "\n") +
						queryTextComponent.getText());
				setVisible(false);
				dispose();
			}
		};

		Action selectAllAction = new AbstractAction("<html>Select <u>A</u>ll</html>") {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkboxes.forEach(c -> c.setSelected(true));
			}
		};

		Action selectNoneAction = new AbstractAction("<html>Select <u>N</u>one</html>") {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkboxes.forEach(c -> c.setSelected(false));
			}
		};

		Action cancelAction = new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		};

		JPanel mainPanel = new JPanel(new GridBagLayout());

		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));

		JButton selectAllButton = new JButton(selectAllAction);
		selectAllButton.setToolTipText("Select all shown prefixes.");
		buttonsPanel.add(selectAllButton);

		JButton selectNoneButton = new JButton(selectNoneAction);
		selectNoneButton.setToolTipText("Unselect all shown prefixes.");
		buttonsPanel.add(selectNoneButton);

		JButton acceptButton = new JButton(acceptAction);
		acceptButton.setToolTipText("Add selected prefixes to the query.");
		buttonsPanel.add(acceptButton);

		mainPanel.add(buttonsPanel,
				new GridBagConstraints(0, 2, 1, 1, 0, 0,
						GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		JPanel prefixPanel = new JPanel(new GridBagLayout());

		ImmutableSet<String> presentPrefixes = getPresentPrefixes(queryTextComponent.getText());

		int gridYIndex = 1;
		for (Map.Entry<String, String> e : prefixMap.entrySet()) {
			if (e.getKey().equals("version"))
				continue;

			boolean isDefaultPrefix = e.getKey().equals(prefixManager.DEFAULT_PREFIX);
			JCheckBox checkbox = new JCheckBox(e.getKey());
			checkbox.setFont(checkbox.getFont().deriveFont(Font.BOLD));
			if (presentPrefixes.contains(e.getKey())) {
				checkbox.setSelected(true);
				checkbox.setEnabled(false);
			}
			prefixPanel.add(checkbox,
					new GridBagConstraints(0, isDefaultPrefix ? 0 : gridYIndex, 1, 1, 0, 0,
							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
							new Insets(1, 2, 1, 2), 0, 0));

			JLabel label = new JLabel("<" + e.getValue() + ">");
			// the border is required for alignment with the checkbox
			label.setBorder(BorderFactory.createLineBorder(prefixPanel.getBackground()));
			prefixPanel.add(label,
					new GridBagConstraints(1, isDefaultPrefix ? 0 : gridYIndex, 1, 1, 1, 0,
							GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
							new Insets(1, 2, 1, 2), 0, 0));

			if (isDefaultPrefix)
				checkboxes.add(0, checkbox); // default prefix at the top
			else
				checkboxes.add(checkbox);

			gridYIndex++;
		}

		prefixPanel.add(new Panel(), // to gobble up the vertical space
				new GridBagConstraints(1, gridYIndex, 1, 1, 1, 1,
						GridBagConstraints.WEST, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));

		mainPanel.add(new JScrollPane(prefixPanel),
				new GridBagConstraints(0, 1, 1, 1, 1, 1,
						GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
						new Insets(5, 10, 5, 10), 0, 0));

		InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(KeyStroke.getKeyStroke(VK_ENTER, 0), "ok");
		inputMap.put(KeyStroke.getKeyStroke(VK_ESCAPE, 0), "cancel");
		inputMap.put(KeyStroke.getKeyStroke(VK_A, CTRL_DOWN_MASK), "all");
		inputMap.put(KeyStroke.getKeyStroke(VK_N, CTRL_DOWN_MASK), "none");
		ActionMap actionMap = mainPanel.getActionMap();
		actionMap.put("ok", acceptAction);
		actionMap.put("cancel", cancelAction);
		actionMap.put("all", selectAllAction);
		actionMap.put("none", selectNoneAction);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setContentPane(mainPanel);
		pack();
		setLocationRelativeTo(queryTextComponent);
	}

	private String getDirectives() {
		return checkboxes.stream()
				.filter(AbstractButton::isSelected)
				.filter(Component::isEnabled)
				.map(AbstractButton::getText)
				.map(p -> "PREFIX " + p + " <" + prefixMap.get(p) + ">\n")
				.collect(Collectors.joining());
	}

	private static final Pattern PREFIX_PATTERN = Pattern.compile("^\\s*PREFIX\\s+([a-zA-Z0-9-_.]*:)", Pattern.MULTILINE);

	private static ImmutableSet<String> getPresentPrefixes(String sparql) {
		Matcher matcher = PREFIX_PATTERN.matcher(sparql);
		ImmutableSet.Builder<String> builder = ImmutableSet.builder();
		while (matcher.find()) {
			String prefix = matcher.group(1);
			builder.add(prefix);
		}
		return builder.build();
	}
}
