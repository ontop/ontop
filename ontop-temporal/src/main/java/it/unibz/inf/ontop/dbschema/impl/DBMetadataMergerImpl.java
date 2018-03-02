package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.*;

public class DBMetadataMergerImpl implements DBMetadataMerger{

    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final JdbcTypeMapper jdbcTypeMapper;
    private final TypeFactory typeFactory;
    private final DatalogFactory datalogFactory;

    @Inject
    public DBMetadataMergerImpl(AtomFactory atomFactory, TermFactory termFactory, JdbcTypeMapper jdbcTypeMapper, TypeFactory typeFactory, DatalogFactory datalogFactory) {
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.jdbcTypeMapper = jdbcTypeMapper;
        this.typeFactory = typeFactory;
        this.datalogFactory = datalogFactory;
    }


    public RDBMetadata mergeDBMetadata(RDBMetadata temporalDBMetadata, RDBMetadata staticDBMetadata){

            if((staticDBMetadata.getDbmsProductName().equals(temporalDBMetadata.getDbmsProductName())) &&
                    staticDBMetadata.getDriverName().equals(temporalDBMetadata.getDriverName()) &&
                    staticDBMetadata.getDriverVersion().equals(temporalDBMetadata.getDriverVersion())){

                Map<RelationID, RelationDefinition> mergedRelations = new HashMap<>();
                mergedRelations.putAll(temporalDBMetadata.copyRelations());
                mergedRelations.putAll(staticDBMetadata.copyRelations());

                Map<RelationID, DatabaseRelationDefinition> mergedTables = new HashMap<>();
                mergedTables.putAll(temporalDBMetadata.copyTables());
                mergedTables.putAll(staticDBMetadata.copyTables());

                List<DatabaseRelationDefinition> mergedListOfTables = new ArrayList<>();

                temporalDBMetadata.getDatabaseRelations().forEach(databaseRelationDefinition -> {
                    if (mergedListOfTables.stream().noneMatch(d -> d.getID().toString().equals(databaseRelationDefinition.getID().toString())))
                        mergedListOfTables.add(databaseRelationDefinition);
                });

                staticDBMetadata.getDatabaseRelations().forEach(databaseRelationDefinition -> {
                    if (mergedListOfTables.stream().noneMatch(d -> d.getID().toString().equals(databaseRelationDefinition.getID().toString())))
                        mergedListOfTables.add(databaseRelationDefinition);
                });

                return new TemporalRDBMetadata(staticDBMetadata.getDriverName(),staticDBMetadata.getDriverVersion(),
                        staticDBMetadata.getDbmsProductName(), ((RDBMetadata) staticDBMetadata).getDbmsVersion(),
                        staticDBMetadata.getQuotedIDFactory(), mergedTables,mergedRelations,mergedListOfTables, 0,
                        jdbcTypeMapper, atomFactory, termFactory,
                        typeFactory, datalogFactory);
            }

        return temporalDBMetadata;
    }
}
