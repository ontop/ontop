package it.unibz.krdb.obda.owlrefplatform.questdb;

/*
 * #%L
 * ontop-quest-db
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBStatement;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSSIRepositoryManager;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestDB {

	private static final Logger log = LoggerFactory.getLogger(QuestDB.class);

	private Map<String, QuestDBAbstractStore> stores = new HashMap<>();
	private Map<String, QuestDBConnection> connections = new HashMap<>();

	private final String QUESTDB_HOME;
	private final String STORES_HOME;
	private final String STORE_PATH;

	// private final String CONFIG_HOME;

	public QuestDB() {
		String value = System.getenv("QUESTDB_HOME");
		String fileSeparator = System.getProperty("file.separator");

		if (value == null || value.trim().equals("")) {
			QUESTDB_HOME = System.getProperty("user.dir") + fileSeparator;
		} 
		else {
			if (value.charAt(value.length() - 1) != fileSeparator.charAt(0)) {
				value = value + fileSeparator;
			}
			QUESTDB_HOME = value;
		}

		STORES_HOME = QUESTDB_HOME + "stores/";
		System.out.println(STORES_HOME);

		// CONFIG_HOME = QUESTDB_HOME + "config/";

		STORE_PATH = STORES_HOME + "%s.sto";

		restoreStores();

		// start all stores
		for (String storename : stores.keySet()) {
			try {
				startStore(storename);
			} 
			catch (Exception e) {
				log.error(e.getMessage());
			}
		}


		/*
		 * Called when System.exit() is called or Control+C happens.
		 */
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				log.info("Shutting down.");

				/*
				 * This cleans all resources and saves the current state of each
				 * store
				 */
				shutdown();
			}
		});
	}

	public static void main(String argsp[]) {
		QuestDB db = new QuestDB();
	}

	private void restoreStores() {
		File storesFolder = new File(STORES_HOME);

		/*
		 * Checking if it exist, otherwise we need to create it
		 */
		if (!storesFolder.exists()) {
			storesFolder.mkdir();
		}

		/*
		 * Checking for all the files in the STORES_HOME folder, if they are
		 * stores we try to de-serialize them.
		 */
		String[] storeFiles = storesFolder.list();
		if (storeFiles != null) {
			for (int i = 0; i < storeFiles.length; i++) {
				int dotindex = storeFiles[i].lastIndexOf('.');
				if (dotindex == -1)
					continue;
				String extension = storeFiles[i].substring(dotindex, storeFiles[i].length());
				if (!extension.equals(".sto"))
					continue;

				QuestDBAbstractStore dbstore;
				try {
					//dbstore = QuestDBAbstractStore.restore(STORES_HOME + storeFiles[i]);

					//stores.put(dbstore.getName(), dbstore);
				} catch (Exception e) {
					log.error("Couldn't restore \"" + storeFiles[i] + "\". Corrupted file?");
					log.error(e.getMessage());
				}

			}
		}
	}

	public void createClassicStore(String name, URI tboxUri, Properties params) throws Exception {

		if (stores.containsKey(name))
			throw new Exception("A store already exists with the name" + name);

		QuestPreferences config = new QuestPreferences();
		config.putAll(params);

		QuestDBClassicStore store = new QuestDBClassicStore(name, tboxUri, config);

		stores.put(name, store);

		saveStore(name);
	}

	public void createVirtualStore(String name, URI tboxUri, URI obdaUri) throws Exception {

		if (stores.containsKey(name))
			throw new Exception("A store already exists with the name" + name);

		QuestDBVirtualStore store = new QuestDBVirtualStore(name, tboxUri, obdaUri);

		stores.put(name, store);

		saveStore(name);
	}

	private void saveStore(String storename) throws Exception {

		if (!stores.containsKey(storename))
			throw new Exception(String.format("The store \"%s\" does not exists.", storename));

		QuestDBAbstractStore dbstore = stores.get(storename);
		try {
		//	QuestDBAbstractStore.saveState(String.format(STORE_PATH, storename), dbstore);
		} 
		catch (Exception e) {
			throw new Exception("Impossible to serialize to the store. ", e);
		}

	}


	public void dropStore(String storename) throws Exception {

		if (!stores.containsKey(storename))
			throw new Exception(String.format("The store \"%s\" does not exists.", storename));

		QuestDBAbstractStore dbstore = stores.get(storename);
		try {
			QuestDBConnection conn = connections.get(storename);
			RDBMSSIRepositoryManager si = dbstore.getSemanticIndexRepository();
			if (si != null)
				si.dropDBSchema(conn.getConnection());
			conn.commit();
			conn.close();
		} 
		catch (Exception e) {
			throw new Exception("Impossible to drop the store. ", e);
		}
		stores.remove(storename);
		connections.remove(storename);

		/* Deleting the file */

		File storefile = new File(String.format(STORE_PATH), storename);
		storefile.delete();

	}

	public void startStore(String storename) throws Exception {

		if (!stores.containsKey(storename))
			throw new Exception(String.format("The store \"%s\" does not exists.", storename));

		QuestDBAbstractStore dbstore = stores.get(storename);
		try {
			QuestDBConnection conn = dbstore.getConnection();
			boolean classic = dbstore.getPreferences().get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC);
			boolean inmemory = dbstore.getPreferences().get(QuestPreferences.STORAGE_LOCATION).equals(QuestConstants.INMEMORY);
			if (classic && inmemory) {
				dbstore.getSemanticIndexRepository().createDBSchemaAndInsertMetadata(conn.getConnection());
				conn.commit();
			}
			connections.put(storename, conn);
		} 
		catch (Exception e) {
			throw new Exception("Impossible to connect to the store. ", e);
		}
	}

	public void stopStore(String storename) throws Exception {

		if (!stores.containsKey(storename))
			throw new Exception(String.format("The store \"%s\" does not exists.", storename));

		QuestDBAbstractStore dbstore = stores.get(storename);
		try {
			QuestDBConnection conn = connections.get(storename);
			conn.close();
		} 
		catch (Exception e) {
			throw new Exception("Impossible to disconnect to the store. ", e);
		}
	}


	public class StoreStatus {
		public String name = "";
		public boolean isOnline = false;
		public String type = "";
	}
		
	public List<StoreStatus> listStores() {
		List<StoreStatus> statuses = new LinkedList<QuestDB.StoreStatus>();

		Set<String> keys = stores.keySet();
		for (String storename : keys) {
			StoreStatus status = new StoreStatus();
			status.name = storename;

			QuestDBAbstractStore store = stores.get(storename);
			try {
				QuestDBConnection conn = connections.get(storename);
				status.isOnline = !conn.isClosed();
			} 
			catch (OBDAException e) {
				log.error(e.getMessage());
			}

			if (store instanceof QuestDBClassicStore)
				status.type = "classic";
			else if (store instanceof QuestDBVirtualStore)
				status.type = "virtual";

			statuses.add(status);
		}

		return statuses;
	}

	public void shutdown() {
		// stop all stores
		for (String storename : stores.keySet()) {
			try {
				stopStore(storename);
			} 
			catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		
		// save all stores 
		for (String storename : stores.keySet()) {
			try {
				saveStore(storename);
			} 
			catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	/* Queries and requests */

	public void createIndexes(String storename) throws Exception {
		if (!stores.containsKey(storename))
			throw new Exception(String.format("The store \"%s\" does not exists.", storename));
		QuestDBAbstractStore dbstore = stores.get(storename);
		if (!(dbstore instanceof QuestDBClassicStore))
			throw new Exception("Unsupported request");
		//QuestDBClassicStore cstore = (QuestDBClassicStore) dbstore;
		QuestDBConnection conn = connections.get(storename);
		RDBMSSIRepositoryManager si = dbstore.getSemanticIndexRepository();
		si.createIndexes(conn.getConnection());
		conn.commit();
		//QuestDBStatement st = conn.createStatement();
		//st.getSIRepository().createIndexes();
		//st.close();
	}

	public void dropIndexes(String storename) throws Exception {
		if (!stores.containsKey(storename))
			throw new Exception(String.format("The store \"%s\" does not exists.", storename));
		QuestDBAbstractStore dbstore = stores.get(storename);
		if (!(dbstore instanceof QuestDBClassicStore))
			throw new Exception("Unsupported request");
		//QuestDBClassicStore cstore = (QuestDBClassicStore) dbstore;
		QuestDBConnection conn = connections.get(storename);
		//QuestDBStatement st = conn.createStatement();
		//st.getSIRepository().dropIndexes();
		RDBMSSIRepositoryManager si = dbstore.getSemanticIndexRepository();
		si.dropIndexes(conn.getConnection());
		//st.close();
		conn.commit();
	}

	public boolean isIndexed(String storename) throws Exception {
		if (!stores.containsKey(storename))
			throw new Exception(String.format("The store \"%s\" does not exists.", storename));
		QuestDBAbstractStore dbstore = stores.get(storename);
		if (!(dbstore instanceof QuestDBClassicStore))
			throw new Exception("Unsupported request");
		
		QuestDBConnection conn = connections.get(storename);
		//QuestDBStatement st = conn.createStatement();
		
		RDBMSSIRepositoryManager si = dbstore.getSemanticIndexRepository();
		boolean response = false;
		if (si != null)
			response = si.isIndexed(conn.getConnection());
		
		//st.close();
		return response;
	}

	public int loadOBDAModel(String storename, URI obdamodelURI) throws Exception {
		if (!stores.containsKey(storename))
			throw new Exception(String.format("The store \"%s\" does not exists.", storename));
		QuestDBAbstractStore dbstore = stores.get(storename);
		if (!(dbstore instanceof QuestDBClassicStore))
			throw new Exception("Unsupported request");
		QuestDBConnection conn = connections.get(storename);
		QuestDBStatement st = conn.createStatement();
		int result = st.addFromOBDA(obdamodelURI);
		st.close();
		return result;
	}

	public int load(String storename, URI dataURI, boolean useFile) throws Exception {
		if (!stores.containsKey(storename))
			throw new Exception(String.format("The store \"%s\" does not exists.", storename));
		QuestDBAbstractStore dbstore = stores.get(storename);
		if (!(dbstore instanceof QuestDBClassicStore))
			throw new Exception("Unsupported request");
		QuestDBConnection conn = connections.get(storename);
		QuestDBStatement st = conn.createStatement();
		if (useFile) {
			int result = st.addWithTempFile(dataURI);
			st.close();
			return result;
		} else {
			int result = st.add(dataURI);
			st.close();
			return result;
		}
	}

	/***
	 * Indicates if a store by the given name has been previously created.
	 * 
	 * @param storename
	 * @return
	 */
	public boolean exists(String storename) {
		return stores.containsKey(storename);
	}

	public QuestDBStatement getStatement(String storename) throws Exception {
		if (!stores.containsKey(storename))
			throw new Exception(String.format("The store \"%s\" does not exists.", storename));
		QuestDBConnection conn = connections.get(storename);
		return conn.createStatement();
	}

}
