/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.quest.sparql;

import junit.framework.Test;

import org.openrdf.query.Dataset;
import org.openrdf.repository.Repository;

import sesameWrapper.SesameClassicInMemoryRepo;

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
