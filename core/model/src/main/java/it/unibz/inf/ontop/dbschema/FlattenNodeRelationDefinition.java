package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;

public class FlattenNodeRelationDefinition extends RelationDefinition {

    private final ImmutableList<Attribute> attributes;
    @Nullable

    public FlattenNodeRelationDefinition(RelationID id, ImmutableList<QuotedID> attributeIds, int attributesType) {
        super(id);
        this.attributes = createAttributes(attributeIds, attributesType);
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

    private ImmutableList<Attribute> createAttributes(ImmutableList<QuotedID> ids, int attributeType) {
        return IntStream.range(1, ids.size()+1).boxed()
                .map(i -> createAttribute(i, attributeType, ids.get(i)))
                .collect(ImmutableCollectors.toList());
    }

    private Attribute createAttribute(Integer index, int attributeType, QuotedID id) {
        return new Attribute(this, new QualifiedAttributeID(getID(), id),
                index, attributeType, null, true, null);
    }
}
