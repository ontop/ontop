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

	private final String PROPERTIES_FILE;
	private Properties properties;

	private static final Logger log = LoggerFactory.getLogger(AbstractConstraintTest.class);
	
	public AbstractConstraintTest(String method, String propertyFile) {
		super(method);
		this.PROPERTIES_FILE = propertyFile;
	}
	
	@Override
	public void setUp() throws IOException, SQLException, MetadataExtractionException {
		InputStream pStream = this.getClass().getResourceAsStream(PROPERTIES_FILE);
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

	public void testPrimaryKey1() {
		if (tBook.isPresent()) {
			List<UniqueConstraint> ucs = tBook.get().getUniqueConstraints();
			assertEquals(1, ucs.size());
			assertEquals(1, ucs.get(0).getAttributes().size());
		}
		else
			fail("AbstractConstraintTest: " + TB_BOOK + " is not found");
	}

	public void testPrimaryKey2() {
		if (tBookWriter.isPresent()) {
			List<UniqueConstraint> ucs = tBookWriter.get().getUniqueConstraints();
			assertEquals(0, ucs.size());
		}
		else
			fail("AbstractConstraintTest: " + TB_BOOKWRITER + " is not found");
	}

	public void testPrimaryKey3() {
		if (tEdition.isPresent()) {
			List<UniqueConstraint> ucs = tEdition.get().getUniqueConstraints();
			assertEquals(1, ucs.size());
			assertEquals(1, ucs.get(0).getAttributes().size());
		}
		else
			fail("AbstractConstraintTest: " + TB_EDITION + " is not found");
	}

	public void testPrimaryKey4() {
		if (tWriter.isPresent()) {
			List<UniqueConstraint> ucs = tWriter.get().getUniqueConstraints();
			assertEquals(1, ucs.size());
			assertEquals(1, ucs.get(0).getAttributes().size());
		}
		else
			fail("AbstractConstraintTest: " + TB_WRITER + " is not found");
	}

	public void testForeignKey1() {
		if (tBook.isPresent()) {
			List<ForeignKeyConstraint> fks =  tBook.get().getForeignKeys();
			assertEquals(0, fks.size());
		}
		else
			fail("AbstractConstraintTest: " + TB_BOOK + " is not found");
	}

	public void testForeignKey2() {
		if (tBookWriter.isPresent()) {
			List<ForeignKeyConstraint> fks =  tBookWriter.get().getForeignKeys();
			assertEquals(2, fks.size());
		}
		else
			fail("AbstractConstraintTest: " + TB_BOOKWRITER + " is not found");
	}

	public void testForeignKey3() {
		if (tEdition.isPresent()) {
			List<ForeignKeyConstraint> fks =  tEdition.get().getForeignKeys();
			assertEquals(1, fks.size());
		}
		else
			fail("AbstractConstraintTest: " + TB_EDITION + " is not found");
	}

	public void testForeignKey4() {
		if (tWriter.isPresent()) {
			List<ForeignKeyConstraint> fks =  tWriter.get().getForeignKeys();
			assertEquals(0, fks.size());
		}
		else
			fail("AbstractConstraintTest: " + TB_WRITER + " is not found");
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
