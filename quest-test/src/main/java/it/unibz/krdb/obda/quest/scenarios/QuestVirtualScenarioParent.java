/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.quest.scenarios;

import org.openrdf.repository.Repository;

import sesameWrapper.SesameVirtualRepo;

public abstract class QuestVirtualScenarioParent extends QuestScenarioParent {

	public QuestVirtualScenarioParent(String testURI, String name,
			String queryFileURL, String resultFileURL, String owlFileURL,
			String obdaFileURL, String parameterFileURL) {
		super(testURI, name, queryFileURL, resultFileURL, owlFileURL, obdaFileURL, parameterFileURL);
	}
	
	@Override
	protected Repository createRepository() throws Exception {
//		try {
			SesameVirtualRepo repo = new SesameVirtualRepo(getName(), owlFileURL, obdaFileURL, parameterFileURL);
			repo.initialize();
			return repo;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
	}
}
