package it.unibz.inf.ontop.protege.core;

/*
 * #%L
 * ontop-protege
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

import com.google.common.collect.ImmutableList;
import org.protege.editor.core.Disposable;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.editorkit.plugin.EditorKitHook;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.owl.OWLEditorKit;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/***
 * This class is responsible for initializing all base classes for the OBDA
 * plugin. In particular this class will register an obdaModelManager of
 * OBDAPluginController and server preference holder objects into the current
 * EditorKit. These instances can be retrieved by other components (Tabs, Views,
 * Actions, etc) by doing EditorKit.get(key).
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class OBDAEditorKitSynchronizerPlugin extends EditorKitHook {

	private OBDAModelManager obdaModelManager;

	private final Map<String, Color> colorProperties = new HashMap<>();

	@Override
	public void initialise() {
		EditorKit editorKit = getEditorKit();
		if (!(editorKit instanceof OWLEditorKit))
			throw new IllegalArgumentException("The OBDA Plugin only works with OWLEditorKit instances.");

		editorKit.put(OBDAEditorKitSynchronizerPlugin.class.getName(), this);

		obdaModelManager = new OBDAModelManager((OWLEditorKit) editorKit);
		PreferencesManager man = PreferencesManager.getInstance();
		Preferences pref = man.getApplicationPreferences("OBDA Plugin");
		List<String> keys = pref.getStringList("Colors", new ArrayList<>());
		for (String k : keys)
			colorProperties.put(k, new Color(pref.getInt(k, 0)));
	}

	@Override
	public void dispose()  {
		PreferencesManager man = PreferencesManager.getInstance();
		Preferences pref = man.getApplicationPreferences("OBDA Plugin");
		pref.putStringList("Colors", ImmutableList.copyOf(colorProperties.keySet()));
		for (Map.Entry<String, Color> e : colorProperties.entrySet())
			pref.putInt(e.getKey(), e.getValue().getRGB());

		obdaModelManager.dispose();
	}

	public static Optional<Color> getColor(EditorKit editorKit, String property) {
		return Optional.ofNullable(get(editorKit).colorProperties.get(property));
	}

	public static void setColor(EditorKit editorKit, String property, Color color) {
		get(editorKit).colorProperties.put(property, color);
	}

	public static OBDAModelManager getOBDAModelManager(EditorKit editorKit) {
		return get(editorKit).obdaModelManager;
	}

	public static OBDAModel getCurrentOBDAModel(EditorKit editorKit) {
		return get(editorKit).obdaModelManager.getCurrentOBDAModel();
	}

	private static @Nonnull OBDAEditorKitSynchronizerPlugin get(EditorKit editorKit) {
		Disposable object = editorKit.get(OBDAEditorKitSynchronizerPlugin.class.getName());
		if (!(object instanceof OBDAEditorKitSynchronizerPlugin))
			throw new RuntimeException("Cannot find OBDAEditorKitSynchronizerPlugin");

		return (OBDAEditorKitSynchronizerPlugin)object;
	}
}
