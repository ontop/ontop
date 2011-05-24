package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.owlapi.ReformulationPlatformPreferences;

import org.obda.reformulation.protege4.ProtegeReformulationPlatformPreferences;
import org.protege.editor.core.editorkit.plugin.EditorKitHook;

public class OBDAEditorKitSynchronizerPlugin extends EditorKitHook {

	

	public void initialise() throws Exception {

		/***
		 * Each editor kit has its own instance of the ProtegePluginController. Note,
		 * the OBDA model is inside this object (do .getOBDAModelManager())
		 */
		OBDAPluginController instance = new OBDAPluginController(this.getEditorKit());
		getEditorKit().put(APIController.class.getName(), instance);
		
//		getEditorKit().getModelManager().put(APIController.class.getName(), instance);
		
		/***
		 * Preferences for the OBDA plugin (gui, etc)
		 */
		getEditorKit().put(OBDAPreferences.class.getName(), new ProtegeOBDAPreferences());
		
		/***
		 * Preferences for Quest
		 */
		getEditorKit().put(ReformulationPlatformPreferences.class.getName(), new ProtegeReformulationPlatformPreferences());
		
		
	}

	public void dispose() throws Exception {
//		instance.removeModelManagerListener();
	}
}
