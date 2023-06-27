package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.atom.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractRelationDefinition implements RelationDefinition  {

    private final ImmutableList<Attribute> attributes;
    private final ImmutableMap<QuotedID, Attribute> map;
    private final RelationPredicate predicate;

    protected AbstractRelationDefinition(String predicateName, RelationDefinition.AttributeListBuilder builder) {
        this.attributes = builder.build(this);
        this.map = attributes.stream()
                .collect(ImmutableCollectors.toMap(Attribute::getID, Function.identity()));
        this.predicate = new RelationPredicateImpl(predicateName);
    }

    /**
     * gets the attribute with the specified position
     *
     * @param index is position <em>starting at 1</em>
     * @return attribute at the position
     */

    public Attribute getAttribute(int index) { return attributes.get(index - 1); }

    /**
     * gets the attribute with the specified ID
     *
     * @param id
     * @return
     */

    public Attribute getAttribute(QuotedID id) throws AttributeNotFoundException {
        Attribute attribute = map.get(id);
        if (attribute == null)
            throw new AttributeNotFoundException(this, id);

        return attribute;
    }

    /**
     * the list of attributes
     *
     * @return list of attributes
     */
    @JsonProperty("columns")
    public ImmutableList<Attribute> getAttributes() { return attributes; }

    @JsonIgnore
    public RelationPredicate getAtomPredicate() { return predicate; }




    private class RelationPredicateImpl extends AtomPredicateImpl implements RelationPredicate {

        public RelationPredicateImpl(String name) {
            super(name,
                    getAttributes().stream()
                            .map(Attribute::getTermType)
                            .collect(ImmutableCollectors.toList()));
        }

        @Override
        public RelationDefinition getRelationDefinition() {
            return AbstractRelationDefinition.this;
        }
    }


    public static AttributeListBuilder attributeListBuilder() { return new AttributeListBuilderImpl(); }


    private static final class AttributeInfo {
        private final QuotedID id;
        private final int index;
        private final DBTermType termType;
        private final String typeName;
        private final boolean isNullable;

        AttributeInfo(QuotedID id, int index, DBTermType termType, String typeName, boolean isNullable) {
            this.id = id;
            this.index = index;
            this.termType = termType;
            this.typeName = typeName;
            this.isNullable = isNullable;
        }
    }

    private static class AttributeListBuilderImpl implements AttributeListBuilder {
        private final List<AttributeInfo> list = new ArrayList<>();

        public AttributeListBuilder addAttribute(QuotedID id, DBTermType termType, String typeName, boolean isNullable) {
            list.add(new AttributeInfo(id, list.size() + 1, termType, typeName, isNullable));
            return this;
        }

        public AttributeListBuilder addAttribute(QuotedID id, DBTermType termType, boolean isNullable) {
            list.add(new AttributeInfo(id, list.size() + 1, termType, termType.getName(), isNullable));
            return this;
        }

        public ImmutableList<Attribute> build(RelationDefinition relation) {
            return list.stream()
                    .map(a -> new AttributeImpl(relation, a.id, a.index, a.typeName, a.termType, a.isNullable))
                    .collect(ImmutableCollectors.toList());
        }
    }

}
