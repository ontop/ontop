package it.unibz.krdb.obda.protege4.core;

import it.unibz.krdb.obda.utils.OBDAPreferencePersistanceManager;
import it.unibz.krdb.obda.utils.OBDAPreferences;

import org.protege.editor.core.Disposable;

public class ProtegeOBDAPreferences extends OBDAPreferences 
		implements Disposable {
	
	public ProtegeOBDAPreferences(OBDAPreferencePersistanceManager pm) {
		super(pm);
		// TODO Auto-generated constructor stub
	}

	public void dispose() {
		// Do nothing.
	}
}
