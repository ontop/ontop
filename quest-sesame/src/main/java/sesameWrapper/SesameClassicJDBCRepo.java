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

import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.questdb.QuestDB;
import it.unibz.krdb.obda.owlrefplatform.questdb.QuestDBClassicStore;

public class SesameClassicJDBCRepo extends SesameClassicRepo {

	private static QuestPreferences p = new QuestPreferences();
	private String restorePath="src/test/resources/";
	private String storePath="src/test/resources/";

	
	public SesameClassicJDBCRepo(String name, String tboxFile) throws Exception {

		super();
		
		//try to restore
		try{
			
		
		}
		catch(Exception e)
		{
			classicStore = null;
		}
		
		//if restore unsuccessful
		if (classicStore == null)
		{
		
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX); 
		p.setCurrentValueOf(QuestPreferences.STORAGE_LOCATION, QuestConstants.JDBC);
		 p.setCurrentValueOf(QuestPreferences.JDBC_DRIVER, "org.h2.Driver");
		 p.setCurrentValueOf(QuestPreferences.JDBC_URL, "jdbc:h2:mem:stockclient1");
		// p.setCurrentValueOf(QuestPreferences.DBTYPE, );
		 p.setCurrentValueOf(QuestPreferences.DBUSER, "sa");
		 p.setCurrentValueOf(QuestPreferences.DBPASSWORD, "");
		
		 
		 
		createStore(name, tboxFile, p); 
		}
		
//		classicStore.saveState(storePath);
	}

}
