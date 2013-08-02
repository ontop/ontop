/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package sesameWrapper;

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
