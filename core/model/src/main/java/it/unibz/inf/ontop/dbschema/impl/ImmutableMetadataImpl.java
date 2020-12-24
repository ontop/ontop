package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;

public class ImmutableMetadataImpl implements ImmutableMetadata {
    private final DBParameters dbParameters;
    private final ImmutableList<NamedRelationDefinition> relations;

    ImmutableMetadataImpl(DBParameters dbParameters, ImmutableList<NamedRelationDefinition> relations) {
        this.dbParameters = dbParameters;
        this.relations = relations;
    }

    @Override
    public ImmutableList<NamedRelationDefinition> getAllRelations() {
        return relations;
    }

    @Override
    public DBParameters getDBParameters() {
        return dbParameters;
    }

    @Override
    public String toString() {
        StringBuilder bf = new StringBuilder();
        for (NamedRelationDefinition r : relations)
            bf.append(r.getAllIDs()).append("=").append(r).append("\n");

        // Prints all primary keys
        bf.append("\n====== constraints ==========\n");
        for (NamedRelationDefinition r : relations) {
            for (UniqueConstraint uc : r.getUniqueConstraints())
                bf.append(uc).append(";\n");
            bf.append("\n");
            for (ForeignKeyConstraint fk : r.getForeignKeys())
                bf.append(fk).append(";\n");
            bf.append("\n");
        }
        return bf.toString();
    }
}
