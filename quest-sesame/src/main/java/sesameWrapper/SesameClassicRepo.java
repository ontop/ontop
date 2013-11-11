/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package sesameWrapper;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.questdb.QuestDBClassicStore;

import org.openrdf.query.Dataset;
import org.openrdf.repository.RepositoryException;

public abstract class SesameClassicRepo extends SesameAbstractRepo {

	protected QuestDBClassicStore classicStore;

	public SesameClassicRepo() {
		super();
	}
	
	protected void createStore(String name, String tboxFile, QuestPreferences config) throws Exception {
		if (!config.getProperty(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			throw new RepositoryException("Must be in classic mode!");
		}
		this.classicStore = new QuestDBClassicStore(name, tboxFile, config);
	}
	
	protected void createStore(String name, Dataset data, QuestPreferences config) throws Exception {
		if (!config.getProperty(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			throw new RepositoryException("Must be in classic mode!");
		}
		this.classicStore = new QuestDBClassicStore(name, data, config);
	}
	
	public void initialize() throws RepositoryException {
		super.initialize();
		try {
			classicStore.getConnection();
		} catch (OBDAException e) {
			e.printStackTrace();
			throw new RepositoryException(e.getMessage());
		}
	}
	
	@Override
	public QuestDBConnection getQuestConnection() throws OBDAException {
		return classicStore.getConnection();
	}
	
	@Override
	public boolean isWritable() throws RepositoryException {
		return true;
	}
	
	public  String getType() {
		return QuestConstants.CLASSIC;
	}
}
