package org.semanticweb.ontop.protege4.gui.preferences;

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

import java.awt.GridBagConstraints;

import javax.swing.JLabel;

import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;
import org.semanticweb.ontop.protege4.panels.OBDAPreferencesPanel;
import org.semanticweb.ontop.protege4.core.ProtegeOBDAPreferences;

/***
 * This class is deprecated
 * 
 * @author mariano
 * 
 */
public class OBDAPluginPreferencesPanel extends OWLPreferencesPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -238263730527609043L;
	private ProtegeOBDAPreferences obdaPreference = null;

	@Override
	public void applyChanges() {
		// TODO Auto-generated method stub
	}

	@Override
	public void initialise() throws Exception {
		// Global preference settings using the Protege framework.
		PreferencesManager man = PreferencesManager.getInstance();

		// Preference settings using the OBDA API framework
		obdaPreference = (ProtegeOBDAPreferences) getEditorKit().get(ProtegeOBDAPreferences.class.getName());
		OBDAPreferencesPanel panel = new OBDAPreferencesPanel(obdaPreference);

		setLayout(new java.awt.GridBagLayout());
		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
		add(panel, gridBagConstraints);

		JLabel placeholder = new JLabel();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(placeholder, gridBagConstraints);
	}

	public void dispose() throws Exception {
	}

}
