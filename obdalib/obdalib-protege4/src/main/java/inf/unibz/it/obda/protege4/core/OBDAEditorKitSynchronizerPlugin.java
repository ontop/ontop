package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.owlapi.ReformulationPlatformPreferences;

import org.obda.reformulation.protege4.ProtegeReformulationPlatformPreferences;
import org.protege.editor.core.editorkit.plugin.EditorKitHook;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;

/***
 * This class is responsible for initializing all base classes for the OBDA
 * plugin. In particular this class will register an instance of
 * OBDAPluginController and server preference holder objects into the current
 * EditorKit. These instances can be retrieved by other components (Tabs, Views,
 * Actions, etc) by doing EditorKit.get(key).
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class OBDAEditorKitSynchronizerPlugin extends EditorKitHook {

	OBDAPluginController instance = null;
	OWLEditorKit kit = null;
	OWLModelManager mmgr = null;
	
	public void initialise() throws Exception {

		/***
		 * Each editor kit has its own instance of the ProtegePluginController.
		 * Note, the OBDA model is inside this object (do
		 * .getOBDAModelManager())
		 */
		instance = new OBDAPluginController(this.getEditorKit());
		getEditorKit().put(OBDAEditorKitSynchronizerPlugin.class.getName(), this);
		kit = (OWLEditorKit)getEditorKit();
		mmgr = (OWLModelManager)kit.getModelManager();
		mmgr.addListener(instance.getModelManagerListener());
		getEditorKit().put(APIController.class.getName(), instance);

		// getEditorKit().getModelManager().put(APIController.class.getName(),
		// instance);

		/***
		 * Preferences for the OBDA plugin (gui, etc)
		 */
		getEditorKit().put(OBDAPreferences.class.getName(), new ProtegeOBDAPreferences());

		/***
		 * Preferences for Quest
		 */
		getEditorKit().put(ReformulationPlatformPreferences.class.getName(), new ProtegeReformulationPlatformPreferences());

	}

	@Override
	public void dispose() throws Exception {
		mmgr.removeListener(instance.getModelManagerListener());
	}
}
