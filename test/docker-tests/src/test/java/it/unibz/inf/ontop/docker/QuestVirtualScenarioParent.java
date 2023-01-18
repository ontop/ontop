package it.unibz.inf.ontop.docker;

/*
 * #%L
 * ontop-test
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

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import org.eclipse.rdf4j.repository.Repository;

public abstract class QuestVirtualScenarioParent extends QuestScenarioParent {

	public QuestVirtualScenarioParent(String testURI, String name,
			String queryFileURL, String resultFileURL, String owlFileURL,
			String obdaFileURL, String parameterFileURL) {
		super(testURI, name, queryFileURL, resultFileURL, owlFileURL, obdaFileURL, parameterFileURL);
	}
	
	@Override
	protected Repository createRepository() {
		OntopSQLOWLAPIConfiguration.Builder<?> configBuilder = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFileURL)
				.enableTestMode()
				.nativeOntopMappingFile(obdaFileURL);

		if (parameterFileURL != null && (!parameterFileURL.isEmpty())) {
			configBuilder.propertyFile(parameterFileURL);
		}

//		try {
			OntopRepository repo = OntopRepository.defaultRepository(configBuilder.build());
			repo.init();
			return repo;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
	}
}
