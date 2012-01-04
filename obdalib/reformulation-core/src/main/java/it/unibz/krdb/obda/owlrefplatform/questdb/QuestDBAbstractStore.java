package it.unibz.krdb.obda.owlrefplatform.questdb;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntologyManager;

public abstract class QuestDBAbstractStore implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8088123404566560283L;

	protected Quest questInstance = null;

	protected QuestConnection conn = null;

	protected static transient OWLOntologyManager man = OWLManager.createOWLOntologyManager();

	protected String name;

	public QuestDBAbstractStore(String name) {
		this.name = name;
	}

	/***
	 * Throws an exception if the connection is null or is inactive.
	 * 
	 * @throws OBDAException
	 */
	protected void checkConnection() throws OBDAException {
		if (conn == null && conn.isClosed())
			throw new OBDAException("An active connection must exists before calling this method");
	}

	/* Serialize methods */

	public static void saveState(String storePath, QuestDBAbstractStore store) throws IOException {
		ObjectOutput out = new ObjectOutputStream(new FileOutputStream(storePath));
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

	public abstract void drop() throws Exception;

	/* Move to query time ? */
	public Properties getPreferences() {
		return questInstance.getPreferences();
	}

	/* Move to query time ? */
	public boolean setProperty(String key, String value) {
		return false;
	}

	public void disconnect() throws OBDAException {
		conn.close();
	}

	public void connect() throws OBDAException {
		if (conn != null && !conn.isClosed())
			throw new OBDAException("Cannot connect when an active connection already exists.");
		conn = questInstance.getConnection();
	}

	public boolean isConnected() throws OBDAException {
		return conn != null & !conn.isClosed();
	}

	public QuestConnection getConnection() {
		return conn;
	}

	// public OBDAResultSet executeQuery(String query) throws Exception {
	// return questInstance.getStatement().executeQuery(query);
	// }
	//
	// public String getSQL(String query) throws Exception {
	// OBDAStatement st = questInstance.getStatement();
	// String sql = st.getUnfolding(query);
	// st.close();
	// return sql;
	// }
	//
	// public String getReformulation(String query) throws Exception {
	// OBDAStatement st = questInstance.getStatement();
	// String reformulation = st.getRewriting(query);
	// st.close();
	// return reformulation;
	// }

}
