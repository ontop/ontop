package it.unibz.inf.ontop.spec.dbschema.impl;


import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
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
    private final AtomFactory atomFactory;
    private final Relation2Predicate relation2Predicate;
    private final TermFactory termFactory;
    private final DatalogFactory datalogFactory;
    private final JdbcTypeMapper jdbcTypeMapper;

    @Inject
    private DefaultRDBMetadataExtractor(OntopMappingSQLSettings settings,
                                        PreProcessedImplicitRelationalDBConstraintExtractor implicitDBConstraintExtractor,
                                        AtomFactory atomFactory, Relation2Predicate relation2Predicate,
                                        TermFactory termFactory, DatalogFactory datalogFactory,
                                        JdbcTypeMapper jdbcTypeMapper) {
        this.obtainFullMetadata = settings.isFullMetadataExtractionEnabled();
        this.implicitDBConstraintExtractor = implicitDBConstraintExtractor;
        this.atomFactory = atomFactory;
        this.relation2Predicate = relation2Predicate;
        this.termFactory = termFactory;
        this.datalogFactory = datalogFactory;
        this.jdbcTypeMapper = jdbcTypeMapper;
    }

    @Override
    public RDBMetadata extract(SQLPPMapping ppMapping, Connection connection, Optional<File> constraintFile)
            throws DBMetadataExtractionException {
        try {
            RDBMetadata metadata = RDBMetadataExtractionTools.createMetadata(connection, termFactory, datalogFactory,
                    atomFactory, relation2Predicate, jdbcTypeMapper);
            return extract(ppMapping, connection, metadata, constraintFile);
        } catch (SQLException e) {
            throw new DBMetadataExtractionException(e.getMessage());
        }
    }

    @Override
    public RDBMetadata extract(SQLPPMapping ppMapping, @Nullable Connection connection,
                               DBMetadata partiallyDefinedMetadata, Optional<File> constraintFile)
            throws DBMetadataExtractionException {

        if (!(partiallyDefinedMetadata instanceof RDBMetadata)) {
            throw new IllegalArgumentException("Was expecting a DBMetadata");
        }

        Optional<PreProcessedImplicitRelationalDBConstraintSet> implicitConstraints = constraintFile.isPresent()
                ? Optional.of(implicitDBConstraintExtractor.extract(constraintFile.get()))
                : Optional.empty();

        try {
            RDBMetadata metadata = (RDBMetadata) partiallyDefinedMetadata;

            // if we have to parse the full metadata or just the table list in the mappings
            if (obtainFullMetadata) {
                RDBMetadataExtractionTools.loadMetadata(metadata, connection, null);
            }
            else {
                try {
                    // This is the NEW way of obtaining part of the metadata
                    // (the schema.table names) by parsing the mappings

                    // Parse mappings. Just to get the table names in use

                    Set<RelationID> realTables = getRealTables(metadata.getQuotedIDFactory(), ppMapping.getTripleMaps());
                    implicitConstraints.ifPresent(c -> {
                        // Add the tables referred to by user-supplied foreign keys
                        Set<RelationID> referredTables = c.getReferredTables(metadata.getQuotedIDFactory());
                        realTables.addAll(referredTables);
                    });

                    RDBMetadataExtractionTools.loadMetadata(metadata, connection, realTables);
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

        } catch (SQLException e) {
            throw new DBMetadataExtractionException(e.getMessage());
        }    }
}
