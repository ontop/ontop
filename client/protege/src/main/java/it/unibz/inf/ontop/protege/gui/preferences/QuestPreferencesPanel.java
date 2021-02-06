package it.unibz.inf.ontop.protege.gui.preferences;

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


import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.OntopOBDASettings;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.protege.core.DisposableProperties;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

import javax.swing.*;
import java.awt.*;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.HTML_TAB;

public class QuestPreferencesPanel extends OWLPreferencesPanel {

	private static final long serialVersionUID = 2017399622537704497L;

	@Override
	public void initialise() {
		DisposableProperties preferences = OBDAEditorKitSynchronizerPlugin.getProperties(getEditorKit());

		JPanel pnlReformulationMethods = new JPanel(new GridLayout(0, 1));
		pnlReformulationMethods.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), "First Order reformulation"));
		pnlReformulationMethods.setMinimumSize(new Dimension(620, 120));
		pnlReformulationMethods.setPreferredSize(new Dimension(620, 120));

		JCheckBox chkRewrite = new JCheckBox("Enable reasoning over anonymous individuals (tree-witness rewriting)",
				preferences.getBoolean(OntopReformulationSettings.EXISTENTIAL_REASONING));
		chkRewrite.setToolTipText("Enable only if your application requires reasoning w.r.t. to existential constants in the queries");
		chkRewrite.addActionListener(evt ->
				preferences.put(OntopReformulationSettings.EXISTENTIAL_REASONING, String.valueOf(chkRewrite.isSelected())));
		pnlReformulationMethods.add(chkRewrite);

		JCheckBox chkAnnotations = new JCheckBox("Enable querying annotations in the ontology",
				preferences.getBoolean(OntopMappingSettings.QUERY_ONTOLOGY_ANNOTATIONS));
		chkAnnotations.setToolTipText("Enable only if your application requires querying annotation properties defined in the ontology.");
		chkAnnotations.addActionListener(evt ->
				preferences.put(OntopMappingSettings.QUERY_ONTOLOGY_ANNOTATIONS, String.valueOf(chkAnnotations.isSelected())));
		pnlReformulationMethods.add(chkAnnotations);

		JCheckBox chkSameAs = new JCheckBox("Enable reasoning with owl:sameAs from mappings",
				preferences.getBoolean(OntopOBDASettings.SAME_AS));
		chkSameAs.setToolTipText("Enable only if your application requires reasoning with owl:sameAs from mappings");
		chkSameAs.addActionListener(evt ->
				preferences.put(OntopOBDASettings.SAME_AS, String.valueOf(chkSameAs.isSelected())));
		pnlReformulationMethods.add(chkSameAs);

		JPanel configPanel = new JPanel(new GridBagLayout());

		JLabel labelNote = new JLabel("<html><b>Note:</b> You will need to restart Ontop Reasoner for any changes to take effect.<p/>" +
				HTML_TAB + HTML_TAB + "(i.e., select \"Reasoner-> None\" and then \"Reasoner -> Ontop\" in Protege's menu)</html>");
		configPanel.add(labelNote,
				new GridBagConstraints(0, 0, 1, 1, 0, 0,
						GridBagConstraints.PAGE_START, GridBagConstraints.HORIZONTAL,
						new Insets(10, 10, 10, 10), 0, 0));

		configPanel.add(pnlReformulationMethods,
				new GridBagConstraints(0, 1, 1, 1, 0, 0,
						GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL,
						new Insets(10, 10, 10, 10), 10, 10));

		configPanel.add(new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(32767, 32767)),
				new GridBagConstraints(0, 2, 1, 1, 1, 1,
						GridBagConstraints.LAST_LINE_START, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));

		setLayout(new BorderLayout());
		add(configPanel, BorderLayout.CENTER);

		setMinimumSize(new Dimension(620, 300));
		setPreferredSize(new Dimension(620, 300));
	}

	@Override
	public void applyChanges() { /* NO-OP */ }

	@Override
	public void dispose() { /* NO-OP */ }
}
