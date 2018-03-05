package it.unibz.inf.ontop.answering.reformulation.generation.calcite;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.RelationID;
import org.apache.calcite.adapter.jdbc.JdbcConvention;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.avatica.AvaticaUtils;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlDialectFactory;
import org.apache.calcite.sql.SqlDialectFactoryImpl;

import javax.sql.DataSource;
import java.util.Map;

public class OntopJDBCSchema extends JdbcSchema{

    private final Map<RelationID, RelationDefinition> ontopViews;

    public Map<RelationID, RelationDefinition> getOntopViews() {
        return ImmutableMap.copyOf(ontopViews);
    }

//    public Map<RelationID, RelationDefinition> getOntopViews(SchemaPlus schemaPlus) {
//        schemaPlus.unwrap(OntopJDBCSchema.class).
//        return ImmutableMap.copyOf(ontopViews);
//    }

    public OntopJDBCSchema(DataSource dataSource,
                           SqlDialect dialect,
                           JdbcConvention convention,
                           String catalog,
                           String schema,
                           Map<RelationID, RelationDefinition> ontopViews) {
        super(dataSource, dialect, convention, catalog, schema);
        this.ontopViews = ontopViews;
    }

    public static OntopJDBCSchema create(
            SchemaPlus parentSchema,
            String name,
            DataSource dataSource,
            String catalog,
            String schema,
            Map<RelationID, RelationDefinition> ontopViews) {
        return create(parentSchema, name, dataSource,
                SqlDialectFactoryImpl.INSTANCE, catalog, schema, ontopViews);
    }

    public static OntopJDBCSchema create(
            SchemaPlus parentSchema,
            String name,
            DataSource dataSource,
            SqlDialectFactory dialectFactory,
            String catalog,
            String schema,
            Map<RelationID, RelationDefinition> ontopViews) {
        final Expression expression =
                Schemas.subSchemaExpression(parentSchema, name, JdbcSchema.class);
        final SqlDialect dialect = createDialect(dialectFactory, dataSource);
        final JdbcConvention convention =
                JdbcConvention.of(dialect, expression, name);
        return new OntopJDBCSchema(dataSource, dialect, convention, catalog, schema, ontopViews);
    }

    public static OntopJDBCSchema create(
            SchemaPlus parentSchema,
            String name,
            Map<String, Object> operand,
            Map<RelationID, RelationDefinition> ontopViews) {
        DataSource dataSource;
        try {
            final String dataSourceName = (String) operand.get("dataSource");
            if (dataSourceName != null) {
                dataSource =
                        AvaticaUtils.instantiatePlugin(DataSource.class, dataSourceName);
            } else {
                final String jdbcUrl = (String) operand.get("jdbcUrl");
                final String jdbcDriver = (String) operand.get("jdbcDriver");
                final String jdbcUser = (String) operand.get("jdbcUser");
                final String jdbcPassword = (String) operand.get("jdbcPassword");
                dataSource = dataSource(jdbcUrl, jdbcDriver, jdbcUser, jdbcPassword);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while reading dataSource", e);
        }
        String jdbcCatalog = (String) operand.get("jdbcCatalog");
        String jdbcSchema = (String) operand.get("jdbcSchema");
        String sqlDialectFactory = (String) operand.get("sqlDialectFactory");

        if (sqlDialectFactory == null || sqlDialectFactory.isEmpty()) {
            return OntopJDBCSchema.create(
                    parentSchema, name, dataSource, jdbcCatalog, jdbcSchema, ontopViews);
        } else {
            SqlDialectFactory factory = AvaticaUtils.instantiatePlugin(
                    SqlDialectFactory.class, sqlDialectFactory);
            return OntopJDBCSchema.create(
                    parentSchema, name, dataSource, factory, jdbcCatalog, jdbcSchema, ontopViews);
        }
    }
}
