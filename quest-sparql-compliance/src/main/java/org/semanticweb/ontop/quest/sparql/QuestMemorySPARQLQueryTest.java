package org.semanticweb.ontop.quest.sparql;

/*
 * #%L
 * ontop-sparql-compliance
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

import junit.framework.Test;

import org.openrdf.query.Dataset;
import org.openrdf.repository.Repository;
import org.semanticweb.ontop.sesame.SesameClassicInMemoryRepo;

public class QuestMemorySPARQLQueryTest extends SPARQLQueryParent {

	public static Test suite() throws Exception {
		return QuestManifestTestUtils.suite(new Factory() {
			public QuestMemorySPARQLQueryTest createSPARQLQueryTest(
					String testURI, String name, String queryFileURL,
					String resultFileURL, Dataset dataSet,
					boolean laxCardinality) {
				return createSPARQLQueryTest(testURI, name, queryFileURL,
						resultFileURL, dataSet, laxCardinality, false);
			}

			public QuestMemorySPARQLQueryTest createSPARQLQueryTest(
					String testURI, String name, String queryFileURL,
					String resultFileURL, Dataset dataSet,
					boolean laxCardinality, boolean checkOrder) {
				return new QuestMemorySPARQLQueryTest(testURI, name,
						queryFileURL, resultFileURL, dataSet, laxCardinality,
						checkOrder);
			}
		});
	}

	protected QuestMemorySPARQLQueryTest(String testURI, String name,
			String queryFileURL, String resultFileURL, Dataset dataSet,
			boolean laxCardinality) {
		this(testURI, name, queryFileURL, resultFileURL, dataSet,
				laxCardinality, false);
	}

	protected QuestMemorySPARQLQueryTest(String testURI, String name,
			String queryFileURL, String resultFileURL, Dataset dataSet,
			boolean laxCardinality, boolean checkOrder) {
		super(testURI, name, queryFileURL, resultFileURL, dataSet,
				laxCardinality, checkOrder);
	}

	@Override
	protected Repository newRepository() {
		try {
			SesameClassicInMemoryRepo repo = new SesameClassicInMemoryRepo(
					"QuestSPARQLTest", dataset);
			repo.initialize();
			return repo;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
