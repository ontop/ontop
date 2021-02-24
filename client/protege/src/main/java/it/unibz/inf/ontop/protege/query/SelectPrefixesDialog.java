package it.unibz.inf.ontop.protege.query;

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
import it.unibz.inf.ontop.protege.core.OntologyPrefixManager;
import it.unibz.inf.ontop.protege.utils.OntopAbstractAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.*;
import static java.awt.event.KeyEvent.*;

public class SelectPrefixesDialog extends JDialog {

	private static final long serialVersionUID = -8277829841902027620L;

	private final Map<String, String> prefixMap;
	private final ArrayList<JCheckBox> checkboxes = new ArrayList<>();

	private String directives;

	/**
	 * Extract existing prefixes (using regex) from queryString
	 * and generates additional selected prefixes if OK is chosen
	 *
	 * @param prefixManager
	 * @param queryString query
	 */

	public SelectPrefixesDialog(OntologyPrefixManager prefixManager, String queryString) {
		prefixMap = prefixManager.getPrefixMap();

		setTitle("Select Prefixes for the Query");
		setModal(true);

		setLayout(new GridBagLayout());

		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		controlPanel.add(getButton(selectAllAction));
		controlPanel.add(getButton(selectNoneAction));
		JButton acceptButton = getButton(acceptAction);
		controlPanel.add(acceptButton);
		add(controlPanel,
				new GridBagConstraints(0, 2, 1, 1, 0, 0,
						GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		JPanel prefixPanel = new JPanel(new GridBagLayout());

		ImmutableSet<String> presentPrefixes = getPresentPrefixes(queryString);

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

		prefixPanel.add(new JPanel(), // to gobble up the vertical space
				new GridBagConstraints(1, gridYIndex, 1, 1, 1, 1,
						GridBagConstraints.WEST, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));

		add(new JScrollPane(prefixPanel),
				new GridBagConstraints(0, 1, 1, 1, 1, 1,
						GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
						new Insets(5, 10, 5, 10), 0, 0));

		setUpAccelerator(getRootPane(), selectAllAction);
		setUpAccelerator(getRootPane(), selectNoneAction);
		setUpAccelerator(getRootPane(), cancelAction);
		getRootPane().setDefaultButton(acceptButton);
	}

	private final OntopAbstractAction acceptAction = new OntopAbstractAction(OK_BUTTON_TEXT,
			null,
			"Add selected prefixes to the query",
			null) {
		@Override
		public void actionPerformed(ActionEvent e) {
			directives = getDirectives();
			dispatchEvent(new WindowEvent(SelectPrefixesDialog.this, WindowEvent.WINDOW_CLOSING));
		}
	};

	private final OntopAbstractAction cancelAction = getStandardCloseWindowAction(CANCEL_BUTTON_TEXT, this);

	private final OntopAbstractAction selectAllAction = new OntopAbstractAction(
			"Select All",
			null,
			"Select all shown prefixes",
			getKeyStrokeWithCtrlMask(VK_A)) {
		@Override
		public void actionPerformed(ActionEvent e) {
			checkboxes.forEach(c -> c.setSelected(true));
		}
	};

	private final OntopAbstractAction selectNoneAction = new OntopAbstractAction(
			"Select None",
			null,
			"Deselect all shown prefixes",
			getKeyStrokeWithCtrlMask(VK_N)) {
		@Override
		public void actionPerformed(ActionEvent e) {
			checkboxes.stream()
					.filter(Component::isEnabled)
					.forEach(c -> c.setSelected(false));
		}
	};



	public Optional<String> getPrefixDirectives() {
		return directives == null || directives.isEmpty() ? Optional.empty() : Optional.of(directives);
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
