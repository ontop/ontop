package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableBiMap;
import it.unibz.inf.ontop.exception.OntopInternalBugException;

public class NestedView extends DatabaseRelationDefinition{

    private final DatabaseRelationDefinition parentRelation;
    private final FlattenNodeRelationDefinition nestedRelation;
    private final Integer indexInParentRelation;
    private final ImmutableBiMap<Integer, Integer> view2ParentRelationIndexMap;

    /**
     * Used only in DBMetadata
     */
    protected NestedView(RelationID name, TypeMapper typeMapper, DatabaseRelationDefinition parentRelation,
                         FlattenNodeRelationDefinition nestedRelation, Integer indexInParentRelation,
                         ImmutableBiMap<Integer, Integer> view2ParentRelationIndexMap) {
        super(name, typeMapper);
        this.parentRelation =  parentRelation;
        this.nestedRelation = nestedRelation;
        this.indexInParentRelation = indexInParentRelation;
        this.view2ParentRelationIndexMap = view2ParentRelationIndexMap;
        if(view2ParentRelationIndexMap.values().contains(indexInParentRelation)){
            throw new NestedViewException("The index of the nested view is used for another attribute");
        }
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

    public ImmutableBiMap<Integer, Integer> getView2ParentRelationIndexMap() {
        return view2ParentRelationIndexMap;
    }

    private static class NestedViewException extends OntopInternalBugException {
        NestedViewException(String message) {
            super(message);
        }
    }
}
