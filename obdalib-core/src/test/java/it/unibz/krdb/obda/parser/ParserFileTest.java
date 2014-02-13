package it.unibz.krdb.obda.parser;

/*
 * #%L
 * ontop-obdalib-core
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

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.sql.api.VisitedQuery;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;

import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserFileTest extends TestCase {
	private static final String ROOT = "src/test/resources/scenario/";

	final static Logger log = LoggerFactory
			.getLogger(ParserFileTest.class);

	// @Test
	public void testStockExchange_Pgsql() throws URISyntaxException {
		OBDAModel model = load(ROOT + "virtual/stockexchange-pgsql.owl");
		execute(model, new URI("RandBStockExchange"));
	}

	// @Test
	public void testImdbGroup4_Pgsql() throws URISyntaxException {
		OBDAModel model = load(ROOT + "virtual/imdb-group4-pgsql.owl");
		execute(model, new URI("kbdb_imdb"));
	}

	// @Test
	public void testImdbGroup4_Oracle() throws URISyntaxException {
		OBDAModel model = load(ROOT + "virtual/imdb-group4-oracle.owl");
		execute(model, new URI("kbdb_imdb"));
	}

	// @Test
	public void testAdolenaSlim_Pgsql() throws URISyntaxException {
		OBDAModel model = load(ROOT + "virtual/adolena-slim-pgsql.owl");
		execute(model, new URI("nap"));
	}

	// @Test
	public void testBooksApril20_Pgsql() throws URISyntaxException {
		OBDAModel model = load(ROOT + "virtual/books-april20-pgsql.owl");
		execute(model, new URI("datasource"));
	}

	// @Test
	public void testHgt090303_Mysql() throws URISyntaxException {
		OBDAModel model = load(ROOT + "virtual/hgt-090303-mysql.owl");
		execute(model, new URI("HGT"));
	}

	// @Test
	public void testHgt090324_Pgsql() throws URISyntaxException {
		OBDAModel model = load(ROOT + "virtual/hgt-090324-pgsql.owl");
		execute(model, new URI("HGT"));
	}

	// @Test
	public void testHgt091007_Oracle() throws URISyntaxException {
		OBDAModel model = load(ROOT + "virtual/hgt-091007-oracle.owl");
		execute(model, new URI("HGT"));
	}

	// @Test
	public void testMpsOntologiaGcc_DB2() throws URISyntaxException {
		OBDAModel model = load(ROOT + "virtual/mps-ontologiagcc-db2.owl");
		execute(model, new URI("sourceGCC"));
	}

	// @Test
	public void testOperationNoyauV5_Oracle() throws URISyntaxException {
		OBDAModel model = load(ROOT + "virtual/operation-noyau-v5-oracle.owl");
		execute(model, new URI("PgmOpe"));
	}

	// @Test
	public void testOperationNoyauV6_Oracle() throws URISyntaxException {
		OBDAModel model = load(ROOT + "virtual/operation-noyau-v6-oracle.owl");
		execute(model, new URI("CORIOLIS-CRAQ"));
		execute(model, new URI("PROGOS-CRAQ"));
	}

	// ------- Utility methods

	private void execute(OBDAModel model, URI identifier) {
		OBDAModel controller = model;
		Hashtable<URI, ArrayList<OBDAMappingAxiom>> mappingList = controller.getMappings();
		ArrayList<OBDAMappingAxiom> mappings = mappingList.get(identifier);

		log.debug("=========== " + identifier + " ===========");
		for (OBDAMappingAxiom axiom : mappings) {
			String query = axiom.getSourceQuery().toString();
			boolean result = parse(query);

			if (!result) {
				log.error("Cannot parse query: " + query);
				assertFalse(result);
			} else {
				assertTrue(result);
			}
		}
	}

	private OBDAModel load(String file) {
		final String obdafile = file.substring(0, file.length() - 3) + "obda";
		OBDADataFactory factory = OBDADataFactoryImpl.getInstance();
		OBDAModel model = factory.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(model);
		try {
			ioManager.load(obdafile);
		} catch (Exception e) {
			log.debug(e.toString());
		}
		return model;
	}

	private static boolean parse(String input) {
		VisitedQuery queryP; 
		
		try {
			queryP = new VisitedQuery(input);
		} catch (JSQLParserException e) {
			log.debug(e.getMessage());
			return false;
		}

		
		return true;
	}
}
