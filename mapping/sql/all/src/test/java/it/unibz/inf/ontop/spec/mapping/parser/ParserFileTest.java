package it.unibz.inf.ontop.spec.mapping.parser;

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


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import junit.framework.TestCase;

import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.utils.SQLAllMappingTestingTools.*;

public class ParserFileTest extends TestCase {
	private static final String ROOT = "src/test/resources/scenario/";

	final static Logger log = LoggerFactory
			.getLogger(ParserFileTest.class);

	private final SQLMappingParser mappingParser;

	public ParserFileTest() {
		OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
				.jdbcUrl("jdbc:h2:mem:fake")
				.jdbcUser("fake_user")
				.jdbcPassword("fake_password")
				.build();
		Injector injector = configuration.getInjector();
		mappingParser = injector.getInstance(SQLMappingParser.class);
    }

	// @Test
	public void testStockExchange_Pgsql() throws URISyntaxException, InvalidMappingException, IOException {
		SQLPPMapping ppMapping = load(ROOT + "virtual/stockexchange-pgsql.owl");
		execute(ppMapping, new URI("RandBStockExchange"));
	}

	// @Test
	public void testImdbGroup4_Pgsql() throws URISyntaxException, InvalidMappingException, IOException {
		SQLPPMapping ppMapping = load(ROOT + "virtual/imdb-group4-pgsql.owl");
		execute(ppMapping, new URI("kbdb_imdb"));
	}

	// @Test
	public void testImdbGroup4_Oracle() throws URISyntaxException, InvalidMappingException, IOException {
		SQLPPMapping ppMapping = load(ROOT + "virtual/imdb-group4-oracle.owl");
		execute(ppMapping, new URI("kbdb_imdb"));
	}

	// @Test
	public void testAdolenaSlim_Pgsql() throws URISyntaxException, InvalidMappingException, IOException {
		SQLPPMapping ppMapping = load(ROOT + "virtual/adolena-slim-pgsql.owl");
		execute(ppMapping, new URI("nap"));
	}

	// @Test
	public void testBooksApril20_Pgsql() throws URISyntaxException, InvalidMappingException, IOException {
		SQLPPMapping ppMapping = load(ROOT + "virtual/books-april20-pgsql.owl");
		execute(ppMapping, new URI("datasource"));
	}

	// @Test
	public void testHgt090303_Mysql() throws URISyntaxException, InvalidMappingException, IOException {
		SQLPPMapping ppMapping = load(ROOT + "virtual/hgt-090303-mysql.owl");
		execute(ppMapping, new URI("HGT"));
	}

	// @Test
	public void testHgt090324_Pgsql() throws URISyntaxException, InvalidMappingException, IOException {
		SQLPPMapping ppMapping = load(ROOT + "virtual/hgt-090324-pgsql.owl");
		execute(ppMapping, new URI("HGT"));
	}

	// @Test
	public void testHgt091007_Oracle() throws URISyntaxException, InvalidMappingException, IOException {
		SQLPPMapping ppMapping = load(ROOT + "virtual/hgt-091007-oracle.owl");
		execute(ppMapping, new URI("HGT"));
	}

	// @Test
	public void testMpsOntologiaGcc_DB2() throws URISyntaxException, InvalidMappingException, IOException {
		SQLPPMapping ppMapping = load(ROOT + "virtual/mps-ontologiagcc-db2.owl");
		execute(ppMapping, new URI("sourceGCC"));
	}

	// @Test
	public void testOperationNoyauV5_Oracle() throws URISyntaxException, InvalidMappingException, IOException {
		SQLPPMapping ppMapping = load(ROOT + "virtual/operation-noyau-v5-oracle.owl");
		execute(ppMapping, new URI("PgmOpe"));
	}

	// @Test
	public void testOperationNoyauV6_Oracle() throws URISyntaxException, InvalidMappingException, IOException {
		SQLPPMapping ppMapping = load(ROOT + "virtual/operation-noyau-v6-oracle.owl");
		execute(ppMapping, new URI("CORIOLIS-CRAQ"));
		execute(ppMapping, new URI("PROGOS-CRAQ"));
	}

	// ------- Utility methods

	private void execute(SQLPPMapping ppMapping, URI identifier) {
		OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();

        /**
         * Problems found in the mapping file.
         * --> Do nothing.
         *
         * Before, tests were analyzing the incomplete ppMapping.
         * TODO: discuss this difference due to the new interface.
         *
         */
        if (ppMapping == null) {
            return;
        }

		SQLPPMapping controller = ppMapping;
		ImmutableList<SQLPPTriplesMap> mappings = controller.getTripleMaps();

		log.debug("=========== " + identifier + " ===========");
		for (SQLPPTriplesMap axiom : mappings) {
			String query = axiom.getSourceQuery().toString();
			boolean result = parse(query, builder.getQuotedIDFactory());

			if (!result) {
				log.error("Cannot parse query: " + query);
				assertFalse(result);
			} else {
				assertTrue(result);
			}
		}
	}

	private SQLPPMapping load(String file)  {
		final String obdafile = file.substring(0, file.length() - 3) + "obda";
        try {
            return mappingParser.parse(new File(obdafile));
        }
        catch (Exception e) {
            log.debug(e.toString());
        }
        return null;
	}

	private static boolean parse(String input, QuotedIDFactory idfac) {

		
		return true;
	}
}
