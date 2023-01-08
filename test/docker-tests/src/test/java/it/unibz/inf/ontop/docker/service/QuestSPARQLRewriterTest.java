package it.unibz.inf.ontop.docker.service;

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


import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.eclipse.rdf4j.common.io.IOUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Test class using StockExchange scenario in MySQL
 */
public class QuestSPARQLRewriterTest extends AbstractVirtualModeTest {
	
	private static final String ROOT_LOCATION = "/testcases-docker/virtual-mode/stockexchange/simplecq/";
	private static final String OWL_FILE_LOCATION = ROOT_LOCATION + "stockexchange.owl";
	private static final String OBDA_FILE_LOCATION = ROOT_LOCATION + "stockexchange-mysql.obda";
	private static final String PROPERTY_FILE_LOCATION = ROOT_LOCATION + "stockexchange-mysql.properties";


	private static EngineConnection CONNECTION;

	@BeforeClass
	public static void before() {
		CONNECTION = createReasoner(OWL_FILE_LOCATION, OBDA_FILE_LOCATION, PROPERTY_FILE_LOCATION);
	}

	@Override
	protected OntopOWLStatement createStatement() throws OWLException {
		return CONNECTION.createStatement();
	}

	@AfterClass
	public static void after() throws Exception {
		CONNECTION.close();
	}

	@Test
	public void testQueryStockTraders() {
		final String query = readQueryFile("stocktraders.rq");
		expandAndDisplayOutput("Query stock traders", query);
	}

	@Test
	public void testQueryAddressID() {
		final String query = readQueryFile("addresses-id.rq");
		expandAndDisplayOutput("Query address ID", query);
	}

	@Test
	public void testQueryAddress() {
		final String query = readQueryFile("addresses.rq");
		expandAndDisplayOutput("Query address", query);
	}

	@Test
	public void testQueryPersonAddresses() {
		final String query = readQueryFile("person-addresses.rq");
		expandAndDisplayOutput("Query person addresses", query);
	}

	@Test
	public void testQueryBrokersWorkForLegalPhysical() {
		final String query = readQueryFile("brokers-workfor-legal-physical.rq");
		expandAndDisplayOutput("Query brokers work for legal physical", query);
	}

	@Test
	public void testQueryBrokersWorkForLegal() {
		final String query = readQueryFile("brokers-workfor-legal.rq");
		expandAndDisplayOutput("Query brokers work for legal", query);
	}

	@Test
	public void testQueryBrokersWorkForPhysical() {
		final String query = readQueryFile("brokers-workfor-physical.rq");
		expandAndDisplayOutput("Query brokers work for physical", query);
	}

	@Test
	public void testQueryBrokersWorkForThemselves() {
		final String query = readQueryFile("brokers-workfor-themselves.rq");
		expandAndDisplayOutput("Query brokers work for themselves", query);
	}

	@Test
	public void testQueryTransactionOfferStock() {
		final String query = readQueryFile("transaction-offer-stock.rq");
		expandAndDisplayOutput("Query transaction offer stock", query);
	}

	@Test
	public void testQueryTransactionStockType() {
		final String query = readQueryFile("transaction-stock-type.rq");
		expandAndDisplayOutput("Query transaction stock type", query);
	}

	@Test
	public void testQueryTransactionFinancialInstrument() {
		final String query = readQueryFile("transactions-finantialinstrument.rq"); // typo!
		expandAndDisplayOutput("Query transaction financial instrument", query);
	}

	private void expandAndDisplayOutput(String title, String sparqlInput) {
		String sparqlOutput = getSPARQLRewriting(sparqlInput);
		String sb = "\n\n" + title +
				"\n====================================================================================\n" +
				sparqlInput +
				"\n------------------------------------------------------------------------------------\n" +
				sparqlOutput +
				"\n====================================================================================\n";
		System.out.println(sb);
	}

	private String getSPARQLRewriting(String sparqlInput) {
		try (OntopOWLStatement statement = createStatement()) {
			return statement.getRewritingRendering(sparqlInput);
		}
		catch (OWLException e) {
			return "NULL";
		}
	}

	private String readQueryFile(String queryFile) {
		String queryFileLocation = ROOT_LOCATION + queryFile;
		URL queryFileUrl = QuestSPARQLRewriterTest.class.getResource(queryFileLocation);
		try (InputStream stream = queryFileUrl.openStream()) {
			return IOUtil.readString(new InputStreamReader(stream, StandardCharsets.UTF_8));
		} catch (IOException e) { 
			throw new RuntimeException("Cannot read input file");
		}
	}
}
