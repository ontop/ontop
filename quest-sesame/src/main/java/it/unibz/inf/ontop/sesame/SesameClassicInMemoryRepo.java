package it.unibz.inf.ontop.sesame;

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
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.injection.QuestCorePreferences;

import java.util.Properties;

import org.openrdf.query.Dataset;

public class SesameClassicInMemoryRepo extends SesameClassicRepo {

	public SesameClassicInMemoryRepo(String name, String tboxFile, boolean existential, String rewriting) throws Exception {
		super(name, buildConfiguration(tboxFile, existential, rewriting));
	}

	private static QuestConfiguration buildConfiguration(String tboxFile, boolean existential, String rewriting) {
		Properties props = new Properties();
		props.setProperty(QuestCorePreferences.ABOX_MODE, QuestConstants.CLASSIC);
		props.setProperty(QuestCorePreferences.OPTIMIZE_EQUIVALENCES, "true");
		props.setProperty(QuestCorePreferences.OBTAIN_FROM_MAPPINGS, "false");
		props.setProperty(QuestCorePreferences.OBTAIN_FROM_ONTOLOGY, "false");
		props.setProperty(QuestCorePreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
		props.setProperty(QuestCorePreferences.STORAGE_LOCATION, QuestConstants.INMEMORY);
		if (existential) {
			props.setProperty(QuestCorePreferences.REWRITE, "true");
		} else {
			props.setProperty(QuestCorePreferences.REWRITE, "false");
		}
		if (rewriting.equals("TreeWitness")) {
			props.setProperty(QuestCorePreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
		} else if (rewriting.equals("Default")) {
			props.setProperty(QuestCorePreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
		}

		return QuestConfiguration.defaultBuilder()
				.ontologyFile(tboxFile)
				.properties(props)
				.build();
	}

	public SesameClassicInMemoryRepo(String name, Dataset data) throws Exception {
		super(name, data, buildConfiguration());
	}

	private static QuestConfiguration buildConfiguration() {
		Properties props = new Properties();
		props.setProperty(QuestCorePreferences.ABOX_MODE, QuestConstants.CLASSIC);
		props.setProperty(QuestCorePreferences.OPTIMIZE_EQUIVALENCES, "true");
		props.setProperty(QuestCorePreferences.OBTAIN_FROM_MAPPINGS, "false");
		props.setProperty(QuestCorePreferences.OBTAIN_FROM_ONTOLOGY, "false");
		props.setProperty(QuestCorePreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
		props.setProperty(QuestCorePreferences.STORAGE_LOCATION, QuestConstants.INMEMORY);

		return QuestConfiguration.defaultBuilder()
				.properties(props)
				.build();
	}
}
