package it.unibz.krdb.obda.quest.service;

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

import info.aduna.io.IOUtil;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBStatement;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import junit.framework.TestCase;
import sesameWrapper.SesameVirtualRepo;

/**
 * Test class using StockExchange scenario in MySQL
 */
public class QuestSPARQLRewriterTest extends TestCase {
	
	private static final String ROOT_LOCATION = "/testcases-scenarios/virtual-mode/stockexchange/simplecq/";
	private static final String OWL_FILE_LOCATION = ROOT_LOCATION + "stockexchange.owl";
	private static final String OBDA_FILE_LOCATION = ROOT_LOCATION + "stockexchange-mysql.obda";
	
	protected SesameVirtualRepo repository;
	
	@Override
	protected void setUp() throws Exception {
		try {
			final URL owlFileUrl = QuestSPARQLRewriterTest.class.getResource(OWL_FILE_LOCATION);
			final URL obdaFileUrl = QuestSPARQLRewriterTest.class.getResource(OBDA_FILE_LOCATION);
			repository = new SesameVirtualRepo(getName(), owlFileUrl.toString(), obdaFileUrl.toString(), "");
			repository.initialize();
		} catch (Exception exc) {
			repository.shutDown();
		}
	}
	
	private QuestDBStatement getStatement() {
		try {
			return repository.getQuestConnection().createStatement();
		} catch (OBDAException e) {
			throw new RuntimeException("Cannot retrieve Quest statement");
		}
	}

	public void testQueryStockTraders() {
		final String query = readQueryFile("stocktraders.rq");
		expandAndDisplayOutput("Query stock traders", query);
	}

	public void testQueryAddressID() {
		final String query = readQueryFile("addresses-id.rq");
		expandAndDisplayOutput("Query address ID", query);
	}

	public void testQueryAddress() {
		final String query = readQueryFile("addresses.rq");
		expandAndDisplayOutput("Query address", query);
	}

	public void testQueryPersonAddresses() {
		final String query = readQueryFile("person-addresses.rq");
		expandAndDisplayOutput("Query person addresses", query);
	}

	public void testQueryBrokersWorkForLegalPhysical() {
		final String query = readQueryFile("brokers-workfor-legal-physical.rq");
		expandAndDisplayOutput("Query brokers work for legal physical", query);
	}

	public void testQueryBrokersWorkForLegal() {
		final String query = readQueryFile("brokers-workfor-legal.rq");
		expandAndDisplayOutput("Query brokers work for legal", query);
	}

	public void testQueryBrokersWorkForPhysical() {
		final String query = readQueryFile("brokers-workfor-physical.rq");
		expandAndDisplayOutput("Query brokers work for physical", query);
	}

	public void testQueryBrokersWorkForThemselves() {
		final String query = readQueryFile("brokers-workfor-themselves.rq");
		expandAndDisplayOutput("Query brokers work for themselves", query);
	}

	public void testQueryTransactionOfferStock() {
		final String query = readQueryFile("transaction-offer-stock.rq");
		expandAndDisplayOutput("Query transaction offer stock", query);
	}

	public void testQueryTransactionStockType() {
		final String query = readQueryFile("transaction-stock-type.rq");
		expandAndDisplayOutput("Query transaction stock type", query);
	}

	public void testQueryTransactionFinancialInstrument() {
		final String query = readQueryFile("transactions-finantialinstrument.rq"); // typo!
		expandAndDisplayOutput("Query transaction financial instrument", query);
	}

	private void expandAndDisplayOutput(String title, String sparqlInput) {
		String sparqlOutput = getSPARQLRewriting(sparqlInput);
		StringBuilder sb = new StringBuilder();
		sb.append("\n\n" + title);
		sb.append("\n====================================================================================\n");
		sb.append(sparqlInput);
		sb.append("\n------------------------------------------------------------------------------------\n");
		sb.append(sparqlOutput);
		sb.append("\n====================================================================================\n");
		System.out.println(sb.toString());
	}

	private String getSPARQLRewriting(String sparqlInput) {
		String sparqlOutput;
		try {
			sparqlOutput = getStatement().getSPARQLRewriting(sparqlInput);
		} catch (OBDAException e) {
			sparqlOutput = "NULL";
		}
		return sparqlOutput;
	}

	private String readQueryFile(String queryFile) {
		String queryFileLocation = ROOT_LOCATION + queryFile;
		URL queryFileUrl = QuestSPARQLRewriterTest.class.getResource(queryFileLocation);
		try {
			InputStream stream = queryFileUrl.openStream();
			return IOUtil.readString(new InputStreamReader(stream, "UTF-8"));
		} catch (IOException e) { 
			throw new RuntimeException("Cannot read input file");
		}
	}
}
