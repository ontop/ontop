package inf.unibz.it.obda.protege4.gui.preferences;

import inf.unibz.it.obda.gui.swing.panel.OBDAPreferencesPanel;
import inf.unibz.it.obda.utils.OBDAPreferences;
import inf.unibz.it.obda.utils.OBDAPreferences.MappingManagerPreferenceChangeListener;

import java.awt.Color;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

public class OBDAPluginPreferencesPanel extends OWLPreferencesPanel 
		implements MappingManagerPreferenceChangeListener {
		
	private Preferences preference = null;
	private OBDAPreferences obdaPreference = null;
	
	@Override
	public void applyChanges() {
		// TODO Auto-generated method stub
	}

	@Override
	public void initialise() throws Exception {		
		// Global preference settings using the Protege framework.
		PreferencesManager man = PreferencesManager.getInstance();
		preference = man.getApplicationPreferences("OBDA Plugin");
		
		// Preference settings using the OBDA API framework
		obdaPreference = (OBDAPreferences)
				getEditorKit().get(OBDAPreferences.class.getName());
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
		obdaPreference.getMappingsPreference().removePreferenceChangedListener(this);
	}

	public void colorPeferenceChanged(String key, Color col) {
		preference.putInt(key, col.getRGB());
	}

	public void fontFamilyPreferenceChanged(String key, String font) {
		preference.putString(key, font);
	}

	public void fontSizePreferenceChanged(String key, int size) {
		preference.putInt(key, size);
	}

	public void isBoldPreferenceChanged(String key, Boolean isBold) {
		preference.putBoolean(key, isBold.booleanValue());
	}

	public void shortCutChanged(String key, String shortcut) {
		preference.putString(key, shortcut);
	}

	public void preferenceChanged(String key, String value) {
		preference.putString(key, value);
	}

	@Override
	public void useDefaultPreferencesChanged(String key, String value) {
		preference.putString(key, value);
	}
}
