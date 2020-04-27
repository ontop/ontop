package it.unibz.inf.ontop.generation.algebra.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.generation.algebra.SQLTable;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

public class SQLTableImpl implements SQLTable {

    private final RelationDefinition relationDefinition;
    private final ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap;

    @AssistedInject
    private SQLTableImpl(@Assisted RelationDefinition relationDefinition,
                         @Assisted ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap) {
        this.relationDefinition = relationDefinition;
        this.argumentMap = argumentMap;
    }

    @Override
    public <T> T acceptVisitor(SQLRelationVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public RelationDefinition getRelationDefinition() {
        return relationDefinition;
    }

    @Override
    public ImmutableMap<Integer, ? extends VariableOrGroundTerm> getArgumentMap() {
        return argumentMap;
    }
}
