package org.obda.reformulation.protege4.configpanel;

import java.awt.Panel;

import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

public class OBDAOWLReformulationPlatformConfigPanel extends OWLPreferencesPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1564382319783207265L;
	
	
	private ConfigPanel configPanel = null;
	
	@Override
	public void applyChanges() {
		
	}

	@Override
	public void initialise() throws Exception {
		
		configPanel = new ConfigPanel();
		this.add(configPanel);
	}

	@Override
	public void dispose() throws Exception {
		
	}

}
