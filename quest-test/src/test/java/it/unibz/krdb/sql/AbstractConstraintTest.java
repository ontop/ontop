package it.unibz.krdb.sql;

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

import it.unibz.krdb.sql.api.Attribute;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConstraintTest extends TestCase {
	
	private DBMetadata metadata;
	
	private static final String TB_BOOK = "Book";
	private static final String TB_WRITER = "Writer";
	private static final String TB_EDITION = "Edition";
	private static final String TB_BOOKWRITER = "BookWriter";
	
	private static Logger log = LoggerFactory.getLogger(AbstractConstraintTest.class);
	
	public AbstractConstraintTest(String method) {
		super(method);
	}
	
	@Override
	public void setUp() {

		try {
			log.info(getConnectionString() + "\n");
			Connection conn = DriverManager.getConnection(getConnectionString(), getConnectionUsername(), getConnectionPassword());
			metadata = JDBCConnectionManager.getMetaData(conn);
		} catch (SQLException e) { 
			e.printStackTrace();
		}
	}
	
	public void testPrimaryKey() {
		log.info("==== PRIMARY KEY ====");
		
		Collection<TableDefinition> tables = metadata.getTables();
		for (TableDefinition t : tables) {
			List<Attribute> pk =  t.getPrimaryKeys();
			if (checkName(t, TB_BOOK)) {
				assertEquals(1, pk.size());
			} else if (checkName(t, TB_BOOKWRITER)) {
				assertEquals(0, pk.size());
			} else if (checkName(t, TB_EDITION)) {
				assertEquals(1, pk.size());
			} else if (checkName(t, TB_WRITER)) {
				assertEquals(1, pk.size());
			}
			writeLog(t.getName(), pk);
		}
		log.info("\n");
	}
	
	public void testForeignKey() {
		log.info("==== FOREIGN KEY ====");
		
		Collection<TableDefinition> tables = metadata.getTables();
		for (TableDefinition t : tables) {
			Map<String, List<Attribute>> fk =  t.getForeignKeys();
			if (checkName(t, TB_BOOK)) {
				assertEquals(0, fk.size());
			} else if (checkName(t, TB_BOOKWRITER)) {
				assertEquals(2, fk.size());
			} else if (checkName(t, TB_EDITION)) {
				assertEquals(1, fk.size());
			} else if (checkName(t, TB_WRITER)) {
				assertEquals(0, fk.size());
			}
			writeLog(t.getName(), fk);
		}
		log.info("\n");
	}
	
	private boolean checkName(TableDefinition table, String value) {
		final String tableName = table.getName();
		return tableName.equalsIgnoreCase(value);
	}
	
	private void writeLog(String tableName, Object keys) {
		log.info(String.format("%s(%s)", tableName, keys.toString()));
	}
	
	protected abstract String getDriverName();
	protected abstract String getConnectionString();
	protected abstract String getConnectionUsername();
	protected abstract String getConnectionPassword();
}
