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

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;

import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import org.junit.jupiter.api.Test;

import static it.unibz.inf.ontop.utils.SQLAllMappingTestingTools.*;
import static org.junit.jupiter.api.Assertions.*;

public class ParserFileTest {
	private static final String ROOT = "src/test/resources/scenario/virtual/";

	private final SQLMappingParser mappingParser;

	public ParserFileTest() {
		OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
				.jdbcUrl("dummy")
				.jdbcDriver("dummy")
				.build();
		Injector injector = configuration.getInjector();
		mappingParser = injector.getInstance(SQLMappingParser.class);
    }

	@Test
	public void testStockExchange_Pgsql() throws InvalidMappingException, MappingIOException {
		SQLPPMapping ppMapping = mappingParser.parse(new File(ROOT + "stockexchange-pgsql.obda"));
		execute(ppMapping, "RandBStockExchange");
	}

	@Test
	public void testImdbGroup4_Pgsql() throws InvalidMappingException, MappingIOException {
		SQLPPMapping ppMapping = mappingParser.parse(new File(ROOT + "imdb-group4-pgsql.obda"));
		execute(ppMapping, "kbdb_imdb");
	}

	@Test
	public void testImdbGroup4_Oracle() throws InvalidMappingException, MappingIOException {
		SQLPPMapping ppMapping = mappingParser.parse(new File(ROOT + "imdb-group4-oracle.obda"));
		execute(ppMapping, "kbdb_imdb");
	}

	@Test
	public void testAdolenaSlim_Pgsql() throws InvalidMappingException, MappingIOException {
		SQLPPMapping ppMapping = mappingParser.parse(new File(ROOT + "adolena-slim-pgsql.obda"));
		execute(ppMapping, "nap");
	}

	@Test
	public void testBooksApril20_Pgsql() throws InvalidMappingException, MappingIOException {
		SQLPPMapping ppMapping = mappingParser.parse(new File(ROOT + "books-april20-pgsql.obda"));
		execute(ppMapping, "datasource");
	}

	@Test
	public void testHgt090303_Mysql() throws InvalidMappingException, MappingIOException {
		SQLPPMapping ppMapping = mappingParser.parse(new File(ROOT + "hgt-090303-mysql.obda"));
		execute(ppMapping,"HGT");
	}

	@Test
	public void testHgt090324_Pgsql() throws InvalidMappingException, MappingIOException {
		SQLPPMapping ppMapping = mappingParser.parse(new File(ROOT + "hgt-090324-pgsql.obda"));
		execute(ppMapping, "HGT");
	}

	@Test
	public void testHgt091007_Oracle() throws InvalidMappingException, MappingIOException {
		SQLPPMapping ppMapping = mappingParser.parse(new File(ROOT + "hgt-091007-oracle.obda"));
		execute(ppMapping, "HGT");
	}

	@Test
	public void testMpsOntologiaGcc_DB2() throws InvalidMappingException, MappingIOException {
		SQLPPMapping ppMapping = mappingParser.parse(new File(ROOT + "mps-ontologiagcc-db2.obda"));
		execute(ppMapping, "sourceGCC");
	}

	@Test
	public void testOperationNoyauV5_Oracle() throws InvalidMappingException, MappingIOException {
		SQLPPMapping ppMapping = mappingParser.parse(new File(ROOT + "operation-noyau-v5-oracle.obda"));
		execute(ppMapping, "PgmOpe");
	}

	@Test
	public void testOperationNoyauV6_Oracle() throws InvalidMappingException, MappingIOException {
		SQLPPMapping ppMapping = mappingParser.parse(new File(ROOT + "operation-noyau-v6-oracle.obda"));
		execute(ppMapping, "CORIOLIS-CRAQ");
		execute(ppMapping, "PROGOS-CRAQ");
	}


	private void execute(SQLPPMapping ppMapping, String identifier) {
		OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();

		ImmutableList<SQLPPTriplesMap> mappings = ppMapping.getTripleMaps();

		for (SQLPPTriplesMap axiom : mappings) {
			String query = axiom.getSourceQuery().toString();
			boolean result = parse(query, builder.getQuotedIDFactory());

			assertTrue(result);
		}
	}

	private static boolean parse(String input, QuotedIDFactory idfac) {

		return true;
	}
}
