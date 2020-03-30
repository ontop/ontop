package it.unibz.inf.ontop.spec.dbschema.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.dbschema.RDBMetadataExtractor;
import it.unibz.inf.ontop.spec.dbschema.PreProcessedImplicitRelationalDBConstraintExtractor;
import it.unibz.inf.ontop.spec.dbschema.PreProcessedImplicitRelationalDBConstraintSet;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

import static it.unibz.inf.ontop.spec.dbschema.impl.SQLTableNameExtractor.getRealTables;

/**
 * DBMetadataExtractor for JDBC-enabled DBs.
 */
public class DefaultRDBMetadataExtractor implements RDBMetadataExtractor {

    /**
     * If we have to parse the full metadata or just the table list in the mappings.
     */
    private final Boolean obtainFullMetadata;

    /**
     * This represents user-supplied constraints, i.e. primary
     * and foreign keys not present in the database metadata
     *
     * Can be useful for eliminating self-joins
     */
    private final PreProcessedImplicitRelationalDBConstraintExtractor implicitDBConstraintExtractor;
    private final TypeFactory typeFactory;

    @Inject
    private DefaultRDBMetadataExtractor(OntopMappingSQLSettings settings,
                                        PreProcessedImplicitRelationalDBConstraintExtractor implicitDBConstraintExtractor,
                                        TypeFactory typeFactory) {
        this.obtainFullMetadata = settings.isFullMetadataExtractionEnabled();
        this.implicitDBConstraintExtractor = implicitDBConstraintExtractor;
        this.typeFactory = typeFactory;
    }

    @Override
    public BasicDBMetadata extract(SQLPPMapping ppMapping, Connection connection, Optional<File> constraintFile)
            throws DBMetadataExtractionException {
        try {
            BasicDBMetadata metadata = RDBMetadataExtractionTools.createMetadata(connection, typeFactory.getDBTypeFactory());
            return extract(ppMapping, connection, metadata, constraintFile);
        }
        catch (SQLException e) {
            throw new DBMetadataExtractionException(e.getMessage());
        }
    }

    @Override
    public BasicDBMetadata extract(SQLPPMapping ppMapping, @Nullable Connection connection,
                               DBMetadata partiallyDefinedMetadata, Optional<File> constraintFile)
            throws DBMetadataExtractionException {

        if (!(partiallyDefinedMetadata instanceof BasicDBMetadata)) {
            throw new IllegalArgumentException("Was expecting a DBMetadata");
        }

        Optional<PreProcessedImplicitRelationalDBConstraintSet> implicitConstraints = constraintFile.isPresent()
                ? Optional.of(implicitDBConstraintExtractor.extract(constraintFile.get()))
                : Optional.empty();

        try {
            BasicDBMetadata metadata = (BasicDBMetadata) partiallyDefinedMetadata;

            // if we have to parse the full metadata or just the table list in the mappings
            if (obtainFullMetadata) {
                RDBMetadataExtractionTools.loadMetadata(metadata, typeFactory.getDBTypeFactory(), connection, null);
            }
            else {
                try {
                    // This is the NEW way of obtaining part of the metadata
                    // (the schema.table names) by parsing the mappings

                    // Parse mappings. Just to get the table names in use

                    Set<RelationID> realTables = getRealTables(metadata.getDBParameters().getQuotedIDFactory(), ppMapping.getTripleMaps());
                    implicitConstraints.ifPresent(c -> {
                        // Add the tables referred to by user-supplied foreign keys
                        Set<RelationID> referredTables = c.getReferredTables(metadata.getDBParameters().getQuotedIDFactory());
                        realTables.addAll(referredTables);
                    });

                    RDBMetadataExtractionTools.loadMetadata(metadata, typeFactory.getDBTypeFactory(), connection, ImmutableList.copyOf(realTables));
                }
                catch (SQLException e) {
                    System.out.println("Error obtaining the metadata " + e);
                }
            }

            implicitConstraints.ifPresent(c ->  {
                c.insertUniqueConstraints(metadata);
                c.insertForeignKeyConstraints(metadata);
            });

            return metadata;

        }
        catch (SQLException e) {
            throw new DBMetadataExtractionException(e.getMessage());
        }
    }
}
