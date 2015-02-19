package org.semanticweb.ontop.sesame;

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

import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.questdb.QuestDB;
import org.semanticweb.ontop.owlrefplatform.questdb.QuestDBClassicStore;

import java.util.Properties;

public class SesameClassicJDBCRepo extends SesameClassicRepo {

    private static QuestPreferences preferences = new QuestPreferences();
    private String restorePath = "src/test/resources/";
    private String storePath = "src/test/resources/";


    public SesameClassicJDBCRepo(String name, String tboxFile) throws Exception {
        super();

        if (classicStore == null) {
            Properties p = new Properties();
            p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
            p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
            p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
            p.setProperty(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
            p.setProperty(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
            p.setProperty(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
            p.setProperty(QuestPreferences.STORAGE_LOCATION, QuestConstants.JDBC);
            p.setProperty(OBDAProperties.JDBC_DRIVER, "org.h2.Driver");
            p.setProperty(OBDAProperties.JDBC_URL, "jdbc:h2:mem:stockclient1");
            p.setProperty(OBDAProperties.DB_USER, "sa");
            p.setProperty(OBDAProperties.DB_PASSWORD, "");
            preferences = new QuestPreferences(p);

            createStore(name, tboxFile, preferences);
        }

//		classicStore.saveState(storePath);
    }

}
