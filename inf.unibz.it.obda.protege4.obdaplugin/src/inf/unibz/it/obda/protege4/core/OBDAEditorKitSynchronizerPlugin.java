package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.obda.api.controller.APIController;

import org.protege.editor.core.editorkit.plugin.EditorKitHook;

public class OBDAEditorKitSynchronizerPlugin extends EditorKitHook {

	private OBDAPluginController	instance;

	public void initialise() throws Exception {
		instance = new OBDAPluginController(this.getEditorKit());
		getEditorKit().put(APIController.class.getName(), instance);
		getEditorKit().getModelManager().put(APIController.class.getName(), instance);
		instance.setupModelManagerListener();
	}

	public void dispose() throws Exception {
		
//		instance.removeModelManagerListener();
		
	}

}
