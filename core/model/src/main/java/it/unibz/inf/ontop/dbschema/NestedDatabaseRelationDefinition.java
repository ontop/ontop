package it.unibz.inf.ontop.dbschema;


import it.unibz.inf.ontop.exception.OntopInternalBugException;
import java.util.Optional;

public class NestedDatabaseRelationDefinition extends DatabaseRelationDefinition{

    private final Optional<Parent> parent;

    public NestedDatabaseRelationDefinition(RelationID id,
                                            TypeMapper typeMapper,
                                            Optional<NestedDatabaseRelationDefinition> parentRelation,
                                            Optional<Integer> indexInParentRelation) {
        super(id, typeMapper);
        parent = extractParentRelation(parentRelation, indexInParentRelation);
    }

    private Optional<Parent> extractParentRelation(Optional<NestedDatabaseRelationDefinition> parentRelation, Optional<Integer> indexInParentRelation) {
        if(parentRelation.isPresent()){
            if (indexInParentRelation.isPresent())
                return Optional.of(new Parent(parentRelation.get(), indexInParentRelation.get()));
            throw new NestedRelationDefinitionException("Both parent relation and index in parent relation should be provided, or none.");
        }
        return Optional.empty();
    }

    private class NestedRelationDefinitionException extends OntopInternalBugException{
        public NestedRelationDefinitionException(String message) {
            super(message);
        }
    }

    public class Parent {
        private final NestedDatabaseRelationDefinition parentRelation;
        private final Integer indexInParentRelation;

        public Parent(NestedDatabaseRelationDefinition parentRelation, Integer indexInParentRelation) {
            this.parentRelation = parentRelation;
            this.indexInParentRelation = indexInParentRelation;
        }

        public NestedDatabaseRelationDefinition getRelation() {
            return parentRelation;
        }

        public Integer getIndexInParent() {
            return indexInParentRelation;
        }
    }
}
