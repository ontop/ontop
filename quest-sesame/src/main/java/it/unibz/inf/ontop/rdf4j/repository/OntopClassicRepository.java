package it.unibz.inf.ontop.rdf4j.repository;

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

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestDBConnection;
import it.unibz.inf.ontop.owlrefplatform.questdb.QuestDBClassicStore;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.repository.RepositoryException;

public class OntopClassicRepository extends AbstractOntopRepository {

	private final QuestDBClassicStore classicStore;
	
	public OntopClassicRepository(String name, QuestConfiguration config) throws Exception {
		if (config.getSettings().isInVirtualMode()) {
			throw new RepositoryException("Must be in classic A-box mode!");
		}
		this.classicStore = new QuestDBClassicStore(name, config);
	}
	
	public OntopClassicRepository(String name, Dataset data, QuestConfiguration config) throws Exception {
		if (config.getSettings().isInVirtualMode()) {
			throw new RepositoryException("Must be in classic A-box mode!");
		}
		this.classicStore = new QuestDBClassicStore(name, data, config);
	}
	
	@Override
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
	
	@Override
    public  String getType() {
		return QuestConstants.CLASSIC;
	}
}
