/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.protege4.gui.preferences;

import it.unibz.krdb.obda.protege4.panels.OBDAPreferencesPanel;
import it.unibz.krdb.obda.utils.OBDAPreferences;

import java.awt.GridBagConstraints;

import javax.swing.JLabel;

import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

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
	private OBDAPreferences obdaPreference = null;

	@Override
	public void applyChanges() {
		// TODO Auto-generated method stub
	}

	@Override
	public void initialise() throws Exception {
		// Global preference settings using the Protege framework.
		PreferencesManager man = PreferencesManager.getInstance();

		// Preference settings using the OBDA API framework
		obdaPreference = (OBDAPreferences) getEditorKit().get(OBDAPreferences.class.getName());
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
