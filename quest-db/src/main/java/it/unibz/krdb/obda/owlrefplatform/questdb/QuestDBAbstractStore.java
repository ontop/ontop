package it.unibz.krdb.obda.owlrefplatform.questdb;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBConnection;

import java.io.Serializable;
import java.util.Properties;

public abstract class QuestDBAbstractStore implements Serializable {

	private static final long serialVersionUID = -8088123404566560283L;

	protected Quest questInstance = null;
	protected QuestConnection questConn = null;

	protected String name;

	public QuestDBAbstractStore(String name) {
		this.name = name;
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
	//	System.out.println("getquestdbconn..");
		return new QuestDBConnection(getQuestConnection());
	}
	
	public abstract QuestConnection getQuestConnection();

}
