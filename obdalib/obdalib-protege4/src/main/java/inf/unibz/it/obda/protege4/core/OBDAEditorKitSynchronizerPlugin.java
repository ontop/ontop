package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;

import org.obda.owlrefplatform.core.ReformulationPlatformPreferences;
import org.obda.reformulation.protege4.ProtegeReformulationPlatformPreferences;
import org.protege.editor.core.editorkit.plugin.EditorKitHook;

public class OBDAEditorKitSynchronizerPlugin extends EditorKitHook {

	private OBDAPluginController	instance;

	public void initialise() throws Exception {
		instance = new OBDAPluginController(this.getEditorKit());
		getEditorKit().put(APIController.class.getName(), instance);
		getEditorKit().getModelManager().put(APIController.class.getName(), instance);
		instance.setupModelManagerListener();
		
		getEditorKit().put(OBDAPreferences.class.getName(), new ProtegeOBDAPreferences());
	}

	public void dispose() throws Exception {
//		instance.removeModelManagerListener();
	}
}
