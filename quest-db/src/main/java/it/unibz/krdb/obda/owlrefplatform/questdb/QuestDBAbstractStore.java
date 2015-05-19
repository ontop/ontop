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
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBConnection;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSSIRepositoryManager;

import java.io.Serializable;
import java.util.Properties;

public abstract class QuestDBAbstractStore implements Serializable {

	private static final long serialVersionUID = -8088123404566560283L;


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
	public abstract Properties getPreferences(); 

	/* Move to query time ? */
	public boolean setProperty(String key, String value) {
		return false;
	}

	public QuestDBConnection getConnection() throws OBDAException {
	//	System.out.println("getquestdbconn..");
		return new QuestDBConnection(getQuestConnection());
	}
	
	public abstract QuestConnection getQuestConnection();

	public abstract RDBMSSIRepositoryManager getSemanticIndexRepository();
}
