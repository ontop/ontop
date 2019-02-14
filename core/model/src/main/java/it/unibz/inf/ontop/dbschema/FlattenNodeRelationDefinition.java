package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.atom.FlattenNodePredicate;
import it.unibz.inf.ontop.model.atom.impl.FlattenNodePredicateImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.List;
import java.util.stream.IntStream;

public class FlattenNodeRelationDefinition extends RelationDefinition {

    private final ImmutableList<Attribute> attributes;
    private FlattenNodePredicate predicate;

    protected FlattenNodeRelationDefinition(RelationID id, ImmutableList<QuotedID> attributeIds, ImmutableList<Integer> attributeType, ImmutableList<Boolean> canNull, TypeMapper typeMapper) {
        super(id);
        this.attributes = createAttributes(attributeIds, attributeType, canNull, typeMapper);
    }

    private ImmutableList<Attribute> createAttributes(ImmutableList<QuotedID> ids, ImmutableList<Integer> attributeTypes, ImmutableList<Boolean> canNull, TypeMapper typeMapper) {
        UnmodifiableIterator<QuotedID> idIt = ids.iterator();
        UnmodifiableIterator<Integer> typeIt = attributeTypes.iterator();
        UnmodifiableIterator<Boolean> canNullIt = canNull.iterator();
        return IntStream.range(0, ids.size()).boxed()
                .map(i -> createAttribute(i+1, idIt.next(), typeIt.next(), canNullIt.next(), typeMapper))
                .collect(ImmutableCollectors.toList());
    }

    private Attribute createAttribute(Integer index, QuotedID id, Integer type, Boolean canNull, TypeMapper typeMapper) {
        return new Attribute(
                this,
                new QualifiedAttributeID(
                        getID(),
                        id),
                index,
                type,
                null,
                canNull,
                typeMapper.getTermType(type)
        );
    }

    public FlattenNodePredicate getAtomPredicate() {
        if (predicate == null)
            predicate = new FlattenNodePredicateImpl(this);
        return predicate;
    }

    @Override
    public Attribute getAttribute(int index) {
        return attributes.get(index - 1);
    }

    @Override
    public List<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public ImmutableList<UniqueConstraint> getUniqueConstraints() {
        return ImmutableList.of();
    }

    @Override
    public ImmutableList<FunctionalDependency> getOtherFunctionalDependencies() {
        return ImmutableList.of();
    }

    @Override
    public UniqueConstraint getPrimaryKey() {
        throw new FlattenNodeRelationDefinitionException("This method should not be called");
    }

    @Override
    public ImmutableList<ForeignKeyConstraint> getForeignKeys() {
        return ImmutableList.of();
    }

    private class FlattenNodeRelationDefinitionException extends OntopInternalBugException {
        FlattenNodeRelationDefinitionException  (String message) {
            super(message);
        }
    }
}
