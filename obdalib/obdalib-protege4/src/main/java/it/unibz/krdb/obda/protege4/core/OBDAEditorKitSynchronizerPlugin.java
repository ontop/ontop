package it.unibz.krdb.obda.protege4.core;

import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.protege4.gui.preferences.ProtegeOBDAPerferencesPersistanceManager;
import it.unibz.krdb.obda.utils.OBDAPreferences;

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

	OBDAModelManager instance = null;
	OWLEditorKit kit = null;
//	OWLModelManager mmgr = null;
	
	public void initialise() throws Exception {

		/***
		 * Each editor kit has its own instance of the ProtegePluginController.
		 * Note, the OBDA model is inside this object (do
		 * .getOBDAModelManager())
		 */
		instance = new OBDAModelManager(this.getEditorKit());
		getEditorKit().put(OBDAEditorKitSynchronizerPlugin.class.getName(), this);
		kit = (OWLEditorKit)getEditorKit();
//		mmgr = (OWLModelManager)kit.getModelManager();
//		mmgr.addListener(instance.getModelManagerListener());
		getEditorKit().put(OBDAModelImpl.class.getName(), instance);

		// getEditorKit().getModelManager().put(APIController.class.getName(),
		// instance);

		ProtegeOBDAPerferencesPersistanceManager man = new ProtegeOBDAPerferencesPersistanceManager();
		
		/***
		 * Preferences for the OBDA plugin (gui, etc)
		 */
		getEditorKit().put(OBDAPreferences.class.getName(), new ProtegeOBDAPreferences(man));

		/***
		 * Preferences for Quest
		 */
		getEditorKit().put(ReformulationPlatformPreferences.class.getName(), new ProtegeReformulationPlatformPreferences(man));

	}

	@Override
	public void dispose() throws Exception {
//		mmgr.removeListener(instance.getModelManagerListener());
	}
}
