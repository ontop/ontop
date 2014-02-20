package sesameWrapper;

/*
 * #%L
 * ontop-quest-sesame
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
