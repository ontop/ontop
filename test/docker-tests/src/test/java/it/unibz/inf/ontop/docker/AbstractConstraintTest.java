package it.unibz.inf.ontop.docker;

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

import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.model.type.TypeFactory;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public abstract class AbstractConstraintTest extends TestCase {
	
	private BasicDBMetadata metadata;
	private QuotedIDFactory ID_FACTORY;
	
	private static final String TB_BOOK = "BOOK";
	private static final String TB_WRITER = "WRITER";
	private static final String TB_EDITION = "EDITION";
	private static final String TB_BOOKWRITER = "BOOKWRITER";

	private String propertyFile;
	private Properties properties;

	
	private static Logger log = LoggerFactory.getLogger(AbstractConstraintTest.class);
	
	public AbstractConstraintTest(String method, String propertyFile) {
		super(method);
		this.propertyFile = propertyFile;
	}
	
	@Override
	public void setUp() throws Exception {
		try {
			InputStream pStream =this.getClass().getResourceAsStream(propertyFile);
			properties = new Properties();
			properties.load(pStream);

			log.info(getConnectionString() + "\n");
			Connection conn = DriverManager.getConnection(getConnectionString(), getConnectionUsername(), getConnectionPassword());

			OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder().build();
			TypeFactory typeFactory = defaultConfiguration.getTypeFactory();

			metadata = RDBMetadataExtractionTools.createMetadata(conn, typeFactory.getDBTypeFactory());
			RDBMetadataExtractionTools.loadMetadata(metadata, typeFactory.getDBTypeFactory(), conn, null);
			ID_FACTORY = metadata.getDBParameters().getQuotedIDFactory();
		}
		catch (IOException e) {
			log.error("IOException during setUp of propertyFile");
			e.printStackTrace();
		}
		catch (SQLException e) {
			log.error("SQL Exception during setUp of metadata");
			e.printStackTrace();
		}
	}
	
	public void testPrimaryKey() {
		log.info("==== PRIMARY KEY ====");

		System.out.println(metadata.getDatabaseRelations());

		DatabaseRelationDefinition tBook = metadata.getDatabaseRelation(ID_FACTORY.createRelationID(null, TB_BOOK));
		if (tBook != null) {
			List<UniqueConstraint> ucBook = tBook.getUniqueConstraints();
			assertEquals(1, ucBook.size());
			assertEquals(1, ucBook.get(0).getAttributes().size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_BOOK + " is not found");

		DatabaseRelationDefinition tBookWriter = metadata.getDatabaseRelation(ID_FACTORY.createRelationID(null, TB_BOOKWRITER));
		if (tBookWriter != null) {
			List<UniqueConstraint> ucBookWriter = tBookWriter.getUniqueConstraints();
			assertEquals(0, ucBookWriter.size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_BOOKWRITER + " is not found");

		DatabaseRelationDefinition tEdition = metadata.getDatabaseRelation(ID_FACTORY.createRelationID(null, TB_EDITION));
		if (tEdition != null) {
			List<UniqueConstraint> ucEdition = tEdition.getUniqueConstraints();
			assertEquals(1, ucEdition.size());
			assertEquals(1, ucEdition.get(0).getAttributes().size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_EDITION + " is not found");

		DatabaseRelationDefinition tWriter = metadata.getDatabaseRelation(ID_FACTORY.createRelationID(null, TB_WRITER));
		if (tWriter != null) {
			List<UniqueConstraint> ucWriter = tWriter.getUniqueConstraints();
			assertEquals(1, ucWriter.size());
			assertEquals(1, ucWriter.get(0).getAttributes().size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_WRITER + " is not found");
/*
		Collection<DatabaseRelationDefinition> tables = metadata.getDatabaseRelations();
		for (DatabaseRelationDefinition t : tables) {
			UniqueConstraint pkc = null;
			List<UniqueConstraint> pks = t.getUniqueConstraints();
			if (!pks.isEmpty())
				pkc = pks.get(0);
			if (checkName(t, TB_BOOK)) {
				assertEquals(1, pkc.getAttributes().size());
			} else if (checkName(t, TB_BOOKWRITER)) {
				assertTrue(pkc == null);
			} else if (checkName(t, TB_EDITION)) {
				assertEquals(1, pkc.getAttributes().size());
			} else if (checkName(t, TB_WRITER)) {
				assertEquals(1, pkc.getAttributes().size());
			}
			if (pkc != null)
				writeLog(t.getID().getSQLRendering(), pkc.getAttributes());
		}
		log.info("\n");
 */
	}
	
	public void testForeignKey() {
		log.info("==== FOREIGN KEY ====");
		
		Collection<DatabaseRelationDefinition> tables = metadata.getDatabaseRelations();
		for (DatabaseRelationDefinition t : tables) {
			List<ForeignKeyConstraint> fk =  t.getForeignKeys();
			if (checkName(t, TB_BOOK)) {
				assertEquals(0, fk.size());
			} else if (checkName(t, TB_BOOKWRITER)) {
				assertEquals(2, fk.size());
			} else if (checkName(t, TB_EDITION)) {
				assertEquals(1, fk.size());
			} else if (checkName(t, TB_WRITER)) {
				assertEquals(0, fk.size());
			}
			writeLog(t.getID().getSQLRendering(), fk);
		}
		log.info("\n");
	}
	
	private boolean checkName(DatabaseRelationDefinition table, String value) {
		final String tableName = table.getID().getSQLRendering();
		return tableName.equalsIgnoreCase(value);
	}
	
	private void writeLog(String tableName, Object keys) {
		log.info(String.format("%s(%s)", tableName, keys.toString()));
	}


	public String getConnectionPassword() {
		return properties.getProperty("jdbc.password");
	}


	public String getConnectionString() {
		return properties.getProperty("jdbc.url");
	}


	public String getConnectionUsername() {
		return properties.getProperty("jdbc.user");
	}


	public String getDriverName() {
		return properties.getProperty("jdbc.driver");
	}
}
