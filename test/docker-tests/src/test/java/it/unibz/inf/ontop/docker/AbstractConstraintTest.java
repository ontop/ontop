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
	
	private static final String TB_BOOK = "Book";
	private static final String TB_WRITER = "Writer";
	private static final String TB_EDITION = "Edition";
	private static final String TB_BOOKWRITER = "BookWriter";

	private String propertyFile;
	private Properties properties;

	
	private static Logger log = LoggerFactory.getLogger(AbstractConstraintTest.class);
	
	public AbstractConstraintTest(String method, String propertyFile) {
		super(method);
		this.propertyFile = propertyFile;
	}
	
	@Override
	public void setUp() {
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
			List<UniqueConstraint> ucs = tBook.getUniqueConstraints();
			assertEquals(1, ucs.size());
			assertEquals(1, ucs.get(0).getAttributes().size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_BOOK + " is not found");

		DatabaseRelationDefinition tBookWriter = metadata.getDatabaseRelation(ID_FACTORY.createRelationID(null, TB_BOOKWRITER));
		if (tBookWriter != null) {
			List<UniqueConstraint> ucs = tBookWriter.getUniqueConstraints();
			assertEquals(0, ucs.size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_BOOKWRITER + " is not found");

		DatabaseRelationDefinition tEdition = metadata.getDatabaseRelation(ID_FACTORY.createRelationID(null, TB_EDITION));
		if (tEdition != null) {
			List<UniqueConstraint> ucs = tEdition.getUniqueConstraints();
			assertEquals(1, ucs.size());
			assertEquals(1, ucs.get(0).getAttributes().size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_EDITION + " is not found");

		DatabaseRelationDefinition tWriter = metadata.getDatabaseRelation(ID_FACTORY.createRelationID(null, TB_WRITER));
		if (tWriter != null) {
			List<UniqueConstraint> ucs = tWriter.getUniqueConstraints();
			assertEquals(1, ucs.size());
			assertEquals(1, ucs.get(0).getAttributes().size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_WRITER + " is not found");
	}
	
	public void testForeignKey() {
		log.info("==== FOREIGN KEY ====");

		DatabaseRelationDefinition tBook = metadata.getDatabaseRelation(ID_FACTORY.createRelationID(null, TB_BOOK));
		if (tBook != null) {
			List<ForeignKeyConstraint> fks =  tBook.getForeignKeys();
			assertEquals(0, fks.size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_BOOK + " is not found");

		DatabaseRelationDefinition tBookWriter = metadata.getDatabaseRelation(ID_FACTORY.createRelationID(null, TB_BOOKWRITER));
		if (tBookWriter != null) {
			List<ForeignKeyConstraint> fks =  tBook.getForeignKeys();
			assertEquals(2, fks.size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_BOOKWRITER + " is not found");

		DatabaseRelationDefinition tEdition = metadata.getDatabaseRelation(ID_FACTORY.createRelationID(null, TB_EDITION));
		if (tEdition != null) {
			List<ForeignKeyConstraint> fks =  tBook.getForeignKeys();
			assertEquals(1, fks.size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_EDITION + " is not found");

		DatabaseRelationDefinition tWriter = metadata.getDatabaseRelation(ID_FACTORY.createRelationID(null, TB_WRITER));
		if (tWriter != null) {
			List<ForeignKeyConstraint> fks =  tBook.getForeignKeys();
			assertEquals(0, fks.size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_WRITER + " is not found");

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
}
