package it.unibz.krdb.obda.owlrefplatform.core.questdb;

import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Properties;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntologyManager;

public abstract class QuestDBAbstractStore implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8088123404566560283L;

	protected Quest questInstance = null;

	protected static transient OWLOntologyManager man = OWLManager.createOWLOntologyManager();

	protected String name;

	public QuestDBAbstractStore(String name) {
		this.name = name;
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

	public void disconnect() throws SQLException {
		questInstance.disconnect();
	}

	public void connect() throws SQLException {
		questInstance.connect();
	}

	public boolean isConnected() throws SQLException {
		return questInstance.isConnected();
	}

	public OBDAStatement getStatement() throws Exception {
		return questInstance.getStatement();
	}

	public void createDB() throws Exception {
		questInstance.createDB();
	}

	public OBDAResultSet executeQuery(String query) throws Exception {
		return questInstance.getStatement().executeQuery(query);
	}

	public String getSQL(String query) throws Exception {
		OBDAStatement st = questInstance.getStatement();
		String sql = st.getUnfolding(query);
		st.close();
		return sql;
	}

	public String getReformulation(String query) throws Exception {
		OBDAStatement st = questInstance.getStatement();
		String reformulation = st.getRewriting(query);
		st.close();
		return reformulation;
	}

}
