package it.unibz.inf.ontop.docker.lightweight;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.DelegatingMetadataProvider;
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractConstraintTest  {
	
	private ImmutableMap<String, RelationDefinition> relations;

	private static final String TB_BOOK = "BOOK";
	private static final String TB_WRITER = "WRITER";
	private static final String TB_EDITION = "EDITION";
	private static final String TB_BOOKWRITER = "BOOKWRITER";

	private final String propertyFile;
	protected Properties properties;

	private static final Logger log = LoggerFactory.getLogger(AbstractConstraintTest.class);
	
	public AbstractConstraintTest(String propertyFile) {
		this.propertyFile = propertyFile;
	}
	
	@BeforeEach
	public void setUp() throws IOException, SQLException, MetadataExtractionException {
		properties = new Properties();
		try (InputStream pStream = getClass().getResourceAsStream(propertyFile)) {
			properties.load(pStream);
		}

		log.info(getConnectionString() + "\n");
		Connection conn = createConnection();

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

		relations = metadata.getAllRelations().stream()
				.collect(ImmutableCollectors.toMap(r -> r.getID().getComponents().get(RelationID.TABLE_INDEX).getName().toUpperCase(), Function.identity()));

		System.out.println(metadata);
	}

	protected Connection createConnection() throws SQLException {
		return DriverManager.getConnection(getConnectionString(), getConnectionUsername(), getConnectionPassword());
	}

	protected String getConnectionPassword() {
		return properties.getProperty("jdbc.password");
	}

	protected String getConnectionString() {
		return properties.getProperty("jdbc.url");
	}

	protected String getConnectionUsername() {
		return properties.getProperty("jdbc.user");
	}


	@Test
	public void testPrimaryBook() {
		List<UniqueConstraint> ucs = relations.get(TB_BOOK).getUniqueConstraints();
		assertEquals(1, ucs.size());
		assertEquals(1, ucs.get(0).getAttributes().size());
	}

	@Test
	public void testPrimaryKeyBookWriter() {
		List<UniqueConstraint> ucs = relations.get(TB_BOOKWRITER).getUniqueConstraints();
		assertEquals(0, ucs.size());
	}

	@Test
	public void testPrimaryKeyEdition() {
		List<UniqueConstraint> ucs = relations.get(TB_EDITION).getUniqueConstraints();
		assertEquals(1, ucs.size());
		assertEquals(1, ucs.get(0).getAttributes().size());
	}

	@Test
	public void testPrimaryKeyWriter() {
		List<UniqueConstraint> ucs = relations.get(TB_WRITER).getUniqueConstraints();
		assertEquals(1, ucs.size());
		assertEquals(1, ucs.get(0).getAttributes().size());
	}

	@Test
	public void testForeignKeyBook() {
		List<ForeignKeyConstraint> fks = relations.get(TB_BOOK).getForeignKeys();
		assertEquals(0, fks.size());
	}

	@Test
	public void testForeignKeyBookWriter() {
		List<ForeignKeyConstraint> fks = relations.get(TB_BOOKWRITER).getForeignKeys();
		assertEquals(2, fks.size());
	}

	@Test
	public void testForeignKeyEdition() {
		List<ForeignKeyConstraint> fks = relations.get(TB_EDITION).getForeignKeys();
		assertEquals(1, fks.size());
	}

	@Test
	public void testForeignKeyWriter() {
		List<ForeignKeyConstraint> fks = relations.get(TB_WRITER).getForeignKeys();
		assertEquals(0, fks.size());
	}
}
