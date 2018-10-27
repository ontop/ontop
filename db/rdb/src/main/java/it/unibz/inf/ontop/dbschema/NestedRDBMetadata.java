package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.Optional;

public class NestedRDBMetadata extends RDBMetadata {

    /**
     * Constructs an initial metadata with some general information about the
     * database, e.g., the driver name, the database engine name.
     * <p>
     * DO NOT USE THIS CONSTRUCTOR -- USE MetadataExtractor METHODS INSTEAD
     */
    NestedRDBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion, QuotedIDFactory idfac, JdbcTypeMapper jdbcTypeMapper, TypeFactory typeFactory) {
        super(driverName, driverVersion, databaseProductName, databaseVersion, idfac, jdbcTypeMapper, typeFactory);
    }

    public NestedDatabaseRelationDefinition createNestedDatabaseRelation(RelationID id,
                                                                         Optional<NestedDatabaseRelationDefinition> parentRelation,
                                                                         Optional<Integer> indexInParentRelation) {
        if (!isStillMutable) {
            throw new IllegalStateException("Too late, cannot create a DB relation");
        }
        NestedDatabaseRelationDefinition relation = new NestedDatabaseRelationDefinition(id, typeMapper, parentRelation, indexInParentRelation);
        add(relation, relations);
        return relation;
    }
}
