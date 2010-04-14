package inf.unibz.it.obda.protege4.gui.preferences;

import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferenceChangeListener;
import inf.unibz.it.obda.gui.swing.preferences.panel.OBDAPreferencesPanel;

import java.awt.Color;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

public class OBDAPluginPreferencesPanel extends OWLPreferencesPanel implements MappingManagerPreferenceChangeListener {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = -5041996765493979793L;
	private Preferences pref = null;
	
	@Override
	public void applyChanges() {
		// TODO Auto-generated method stub
		
	}

	public void initialise() throws Exception {
		PreferencesManager man = PreferencesManager.getInstance();
		pref = man.getApplicationPreferences("OBDA Plugin");
		OBDAPreferencesPanel pane = new OBDAPreferencesPanel();
		setLayout(new java.awt.GridBagLayout());
		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(pane, gridBagConstraints);
        
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
		// TODO Auto-generated method stub
		OBDAPreferences.getOBDAPreferences().getMappingsPreference().removePreferenceChangedListener(this);
	}

	public void colorPeferenceChanged(String preference, Color col) {
	
		pref.putInt(preference, col.getRGB());
	}

	public void fontFamilyPreferenceChanged(String preference, String font) {
		
		pref.putString(preference, font);
	}

	public void fontSizePreferenceChanged(String preference, int size) {
		
		pref.putInt(preference, size);
	}

	public void isBoldPreferenceChanged(String preference, Boolean isBold) {
		
		pref.putBoolean(preference, isBold.booleanValue());
	}

	public void shortCutChanged(String preference, String shortcut) {
		
		pref.putString(preference, shortcut);
	}

	public void preferenceChanged(String preference, String value) {
		pref.putString(preference, value);
	}


}
