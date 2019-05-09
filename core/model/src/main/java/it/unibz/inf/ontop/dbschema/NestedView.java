package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.exception.OntopInternalBugException;

public class NestedView extends DatabaseRelationDefinition{

    private final DatabaseRelationDefinition parentRelation;
    private final FlattenNodeRelationDefinition nestedRelation;
    private final Integer indexInParentRelation;
//    private final ImmutableBiMap<Integer, Integer> view2ParentRelationIndexMap;

    /**
     * Used only in DBMetadata
     */
    protected NestedView(RelationID name, TypeMapper typeMapper, DatabaseRelationDefinition parentRelation,
                         FlattenNodeRelationDefinition nestedRelation, Integer indexInParentRelation){
        super(name, typeMapper);
        this.parentRelation =  parentRelation;
        this.nestedRelation = nestedRelation;
        this.indexInParentRelation = indexInParentRelation;
    }

    public DatabaseRelationDefinition getParentRelation() {
        return parentRelation;
    }

    public FlattenNodeRelationDefinition getNestedRelation() {
        return nestedRelation;
    }

    public Integer getIndexInParentRelation() {
        return indexInParentRelation;
    }

    private static class NestedViewException extends OntopInternalBugException {
        NestedViewException(String message) {
            super(message);
        }
    }
}
