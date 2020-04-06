package it.unibz.inf.ontop.spec.dbschema.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.dbschema.RDBMetadataExtractor;
import it.unibz.inf.ontop.spec.dbschema.ImplicitDBConstraintsProviderFactory;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
    private final boolean obtainFullMetadata;

    /**
     * This represents user-supplied constraints, i.e. primary
     * and foreign keys not present in the database metadata
     *
     * Can be useful for eliminating self-joins
     */
    private final ImplicitDBConstraintsProviderFactory implicitDBConstraintExtractor;
    private final TypeFactory typeFactory;

    @Inject
    private DefaultRDBMetadataExtractor(OntopMappingSQLSettings settings,
                                        ImplicitDBConstraintsProviderFactory implicitDBConstraintExtractor,
                                        TypeFactory typeFactory) {
        this.obtainFullMetadata = settings.isFullMetadataExtractionEnabled();
        this.implicitDBConstraintExtractor = implicitDBConstraintExtractor;
        this.typeFactory = typeFactory;
    }

    @Override
    public BasicDBMetadata extract(SQLPPMapping ppMapping, @Nullable Connection connection,
                               Optional<DBMetadata> optionalMetadata, Optional<File> constraintFile)
            throws MetadataExtractionException {

        try {
            DBParameters dbParameters;
            if (!optionalMetadata.isPresent()) {
                dbParameters = RDBMetadataExtractionTools.createDBParameters(connection, typeFactory.getDBTypeFactory());
            }
            else {
                if (!(optionalMetadata.get() instanceof BasicDBMetadata)) {
                    throw new IllegalArgumentException("Was expecting a DBMetadata");
                }
                dbParameters = optionalMetadata.get().getDBParameters();
            }

            MetadataProvider implicitConstraints = implicitDBConstraintExtractor.extract(
                    constraintFile, dbParameters.getQuotedIDFactory());

            RDBMetadataProvider metadataLoader = RDBMetadataExtractionTools.getMetadataProvider(connection, dbParameters);
            BasicDBMetadata metadata = RDBMetadataExtractionTools.createMetadata(dbParameters);

            // if we have to parse the full metadata or just the table list in the mappings
            ImmutableList<RelationID> seedRelationIds;
            if (obtainFullMetadata) {
                seedRelationIds = metadataLoader.getRelationIDs();
            }
            else {
                // This is the NEW way of obtaining part of the metadata
                // (the schema.table names) by parsing the mappings
                // Parse mappings. Just to get the table names in use

                Set<RelationID> realTables = getRealTables(dbParameters.getQuotedIDFactory(), ppMapping.getTripleMaps());
                realTables.addAll(implicitConstraints.getRelationIDs());
                seedRelationIds = realTables.stream()
                        .map(metadataLoader::getRelationCanonicalID)
                        .collect(ImmutableCollectors.toList());
            }
            List<DatabaseRelationDefinition> extractedRelations2 = new ArrayList<>();
            for (RelationID seedId : seedRelationIds) {
                for (RelationDefinition.AttributeListBuilder r : metadataLoader.getRelationAttributes(seedId)) {
                    DatabaseRelationDefinition relation = metadata.createDatabaseRelation(r);
                    extractedRelations2.add(relation);
                }
            }

            for (DatabaseRelationDefinition relation : extractedRelations2)
                metadataLoader.insertIntegrityConstraints(relation, metadata);

            implicitConstraints.insertIntegrityConstraints(metadata);
            return metadata;
        }
        catch (SQLException e) {
            throw new MetadataExtractionException(e.getMessage());
        }
    }
}
