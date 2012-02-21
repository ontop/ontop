package it.unibz.krdb.obda.owlrefplatform.questdb;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;

public abstract class QuestDBAbstractStore implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8088123404566560283L;

	protected Quest questInstance = null;

	protected String name;

	public QuestDBAbstractStore(String name) {
		this.name = name;
	}

	/* Serialize methods */

	public static void saveState(String storePath, QuestDBAbstractStore store) throws IOException {
		StringBuffer filename = new StringBuffer();
		filename.append(storePath);
		if (!storePath.endsWith(System.getProperty("file.separator")))
			filename.append(System.getProperty("file.separator"));
		filename.append(store.getName());
		filename.append(".qst");
		ObjectOutput out = new ObjectOutputStream(new FileOutputStream(filename.toString()));
		out.writeObject(store);
		out.close();

	}

	public static QuestDBAbstractStore restore(String storePath) throws IOException, ClassNotFoundException {
		File file = new File(storePath);
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));

		QuestDBAbstractStore store = (QuestDBAbstractStore) in.readObject();

		in.close();
		return store;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/* Move to query time ? */
	public Properties getPreferences() {
		return questInstance.getPreferences();
	}

	/* Move to query time ? */
	public boolean setProperty(String key, String value) {
		return false;
	}

	public QuestDBConnection getConnection() throws OBDAException {
		return new QuestDBConnection(questInstance.getConnection());
	}

}
