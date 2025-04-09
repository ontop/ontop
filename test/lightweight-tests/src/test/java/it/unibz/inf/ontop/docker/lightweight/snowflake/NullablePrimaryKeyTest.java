package it.unibz.inf.ontop.docker.lightweight.snowflake;

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

public class NullablePrimaryKeyTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-nullable-pk-snowflake.properties";
    private static final String TB_OCCURRENCE = "OCCURRENCE";

    private Properties properties;
    private RelationDefinition occurrence;

    private static final Logger log = LoggerFactory.getLogger(NullablePrimaryKeyTest.class);

    @BeforeEach
    public void setUp() throws IOException, SQLException, MetadataExtractionException {
        properties = new Properties();
        try (InputStream pStream = getClass().getResourceAsStream(PROPERTIES_FILE)) {
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
                        .filter(id -> ImmutableSet.of(TB_OCCURRENCE)
                                .contains(id.getComponents().get(RelationID.TABLE_INDEX).getName().toUpperCase()))
                        .collect(ImmutableCollectors.toList());
            }
        };

        ImmutableMetadata metadata = ImmutableMetadata.extractImmutableMetadata(filteredMetadataLoader);

        ImmutableMap<String, RelationDefinition> relations = metadata.getAllRelations().stream()
                .collect(ImmutableCollectors.toMap(r -> r.getID().getComponents().get(RelationID.TABLE_INDEX).getName().toUpperCase(), Function.identity()));

        occurrence = relations.get(TB_OCCURRENCE);

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
    public void testPrimaryKey() {
        List<UniqueConstraint> ucs = occurrence.getUniqueConstraints();
        assertEquals(1, ucs.size());
        assertEquals(1, ucs.get(0).getAttributes().size());
    }
}
