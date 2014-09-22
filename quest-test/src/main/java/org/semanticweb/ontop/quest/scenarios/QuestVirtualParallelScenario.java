package org.semanticweb.ontop.quest.scenarios;

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

import org.openrdf.repository.Repository;
import org.semanticweb.ontop.sesame.SesameVirtualRepo;

import java.util.List;

public abstract class QuestVirtualParallelScenario extends QuestParallelScenario {

	public QuestVirtualParallelScenario(String suiteName, List<String> testURIs, List<String> names, List<String> queryFileURLs,
                                        List<String> resultFileURLs, String owlFileURL, String obdaFileURL,
                                        String parameterFileURL){
        super(suiteName, testURIs, names,  queryFileURLs, resultFileURLs, owlFileURL, obdaFileURL, parameterFileURL);
	}
	
	@Override
	protected Repository createRepository() throws Exception {
        SesameVirtualRepo repo = new SesameVirtualRepo(getClass().getName(), owlFileURL, obdaFileURL, parameterFileURL);
        repo.initialize();
        return repo;
	}
}
