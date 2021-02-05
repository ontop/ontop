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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.DelegatingMetadataProvider;
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

public abstract class AbstractConstraintTest extends TestCase {
	
	private Optional<RelationDefinition> tBook, tBookWriter, tEdition, tWriter;

	private static final String TB_BOOK = "BOOK";
	private static final String TB_WRITER = "WRITER";
	private static final String TB_EDITION = "EDITION";
	private static final String TB_BOOKWRITER = "BOOKWRITER";

	private final String propertyFile;
	private Properties properties;

	private static final Logger log = LoggerFactory.getLogger(AbstractConstraintTest.class);
	
	public AbstractConstraintTest(String method, String propertyFile) {
		super(method);
		this.propertyFile = propertyFile;
	}
	
	@Override
	public void setUp() throws IOException, SQLException, MetadataExtractionException {
		InputStream pStream = this.getClass().getResourceAsStream(propertyFile);
		properties = new Properties();
		properties.load(pStream);

		log.info(getConnectionString() + "\n");
		Connection conn = DriverManager.getConnection(getConnectionString(), getConnectionUsername(), getConnectionPassword());

		OntopSQLCoreConfiguration defaultConfiguration = OntopSQLCoreConfiguration.defaultBuilder()
				.properties(properties)
				.build();
		JDBCMetadataProviderFactory metadataProviderFactory = defaultConfiguration.getInjector().getInstance(JDBCMetadataProviderFactory.class);

		MetadataProvider metadataLoader = metadataProviderFactory.getMetadataProvider(conn);

		MetadataProvider filteredMetadataLoader = new DelegatingMetadataProvider(metadataLoader) {
			@Override
			public ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException {
				return provider.getRelationIDs().stream()
						.filter(id -> ImmutableSet.of(TB_BOOK, TB_BOOKWRITER, TB_EDITION, TB_WRITER)
								.contains(id.getComponents().get(RelationID.TABLE_INDEX).getName().toUpperCase()))
						.collect(ImmutableCollectors.toList());
			}
		};

		ImmutableMetadata metadata = ImmutableMetadata.extractImmutableMetadata(filteredMetadataLoader);

		ImmutableMap<String, RelationDefinition> map = metadata.getAllRelations().stream()
				.collect(ImmutableCollectors.toMap(r -> r.getID().getComponents().get(RelationID.TABLE_INDEX).getName().toUpperCase(), Function.identity()));

		tBook = Optional.ofNullable(map.get(TB_BOOK));
		tBookWriter = Optional.ofNullable(map.get(TB_BOOKWRITER));
		tEdition = Optional.ofNullable(map.get(TB_EDITION));
		tWriter = Optional.ofNullable(map.get(TB_WRITER));

		System.out.println(metadata);
	}
	
	public void testPrimaryKey() {
		log.info("==== PRIMARY KEY ====");

		if (tBook.isPresent()) {
			List<UniqueConstraint> ucs = tBook.get().getUniqueConstraints();
			assertEquals(1, ucs.size());
			assertEquals(1, ucs.get(0).getAttributes().size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_BOOK + " is not found");

		if (tBookWriter.isPresent()) {
			List<UniqueConstraint> ucs = tBookWriter.get().getUniqueConstraints();
			assertEquals(0, ucs.size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_BOOKWRITER + " is not found");

		if (tEdition.isPresent()) {
			List<UniqueConstraint> ucs = tEdition.get().getUniqueConstraints();
			assertEquals(1, ucs.size());
			assertEquals(1, ucs.get(0).getAttributes().size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_EDITION + " is not found");

		if (tWriter.isPresent()) {
			List<UniqueConstraint> ucs = tWriter.get().getUniqueConstraints();
			assertEquals(1, ucs.size());
			assertEquals(1, ucs.get(0).getAttributes().size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_WRITER + " is not found");
	}
	
	public void testForeignKey() {
		log.info("==== FOREIGN KEY ====");

		if (tBook.isPresent()) {
			List<ForeignKeyConstraint> fks =  tBook.get().getForeignKeys();
			assertEquals(0, fks.size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_BOOK + " is not found");

		if (tBookWriter.isPresent()) {
			List<ForeignKeyConstraint> fks =  tBookWriter.get().getForeignKeys();
			assertEquals(2, fks.size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_BOOKWRITER + " is not found");

		if (tEdition.isPresent()) {
			List<ForeignKeyConstraint> fks =  tEdition.get().getForeignKeys();
			assertEquals(1, fks.size());
		}
		else
			System.out.println("AbstractConstraintTest: " + TB_EDITION + " is not found");

		if (tWriter.isPresent()) {
			List<ForeignKeyConstraint> fks =  tWriter.get().getForeignKeys();
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
