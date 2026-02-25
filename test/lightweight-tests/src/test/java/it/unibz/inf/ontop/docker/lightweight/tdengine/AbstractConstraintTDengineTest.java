package it.unibz.inf.ontop.docker.lightweight.tdengine;

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
import org.junit.jupiter.api.Disabled;
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

public abstract class AbstractConstraintTDengineTest {

    private static ImmutableMap<String, RelationDefinition> relations;

    private static final String TB_PRESSURE = "PRESSURE_MEASUREMENTS";
    private static final String TB_TEMPERATURE = "TEMPERATURE_MEASUREMENTS";;

    private static Properties properties;

    private static final Logger log = LoggerFactory.getLogger(AbstractConstraintTDengineTest.class);

    protected abstract String getPropertiesFile();

    @BeforeEach
    public void setUp() throws IOException, SQLException, MetadataExtractionException {
        properties = new Properties();
        try (InputStream pStream = getClass().getResourceAsStream(getPropertiesFile())) {
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
                        .filter(id -> ImmutableSet.of(TB_PRESSURE, TB_TEMPERATURE)
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
    public void testPrimaryKeyTemperature() {
        List<UniqueConstraint> ucs = relations.get(TB_TEMPERATURE).getUniqueConstraints();
        assertEquals(1, ucs.size());
        assertEquals(1, ucs.get(0).getAttributes().size());
    }

    @Test
    @Disabled("Composite primary keys are supported in TDEngine, but there is no way to retrieve them from the metadata." +
            "Only the mandatory timestamp primary key is returned")
    public void testCompositePrimaryKeyPressure() {
        List<UniqueConstraint> ucs = relations.get(TB_PRESSURE).getUniqueConstraints();
        assertEquals(1, ucs.size());
        assertEquals(2, ucs.get(0).getAttributes().size());
    }

    @Test
    @Disabled("TDEngine does not support foreign keys")
    public void testForeignKeyTemperature() {
        List<ForeignKeyConstraint> fks = relations.get(TB_TEMPERATURE).getForeignKeys();
        assertEquals(1, fks.size());
    }
}
