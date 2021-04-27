package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import it.unibz.inf.ontop.dbschema.impl.OntopViewDefinitionImpl;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.FlattenNodePredicate;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.List;
import java.util.stream.IntStream;

public class OntopFlattenedViewDefinitionImpl extends OntopViewDefinitionImpl {

//
//    public OntopFlattenedViewDefinitionImpl(ImmutableList<RelationID> allIds, AttributeListBuilder builder,
//                                   IQ iqWithTemporaryAtomPredicate, int level, CoreSingletons coreSingletons) {
//        super(allIds, builder, iqWithTemporaryAtomPredicate, level, coreSingletons);
//        this.iq = replaceAtomPredicate(getAtomPredicate(), iqWithTemporaryAtomPredicate, coreSingletons);
//        this.level = level;
//        if (level < 1)
//            throw new IllegalArgumentException("Minimum level for a view is 1");
//    }
//
//    private static IQ replaceAtomPredicate(RelationPredicate newAtomPredicate, IQ iqWithTemporaryAtomPredicate,
//                                           CoreSingletons coreSingletons) {
//        DistinctVariableOnlyDataAtom newProjectionAtom = coreSingletons.getAtomFactory().getDistinctVariableOnlyDataAtom(
//                newAtomPredicate, iqWithTemporaryAtomPredicate.getProjectionAtom().getArguments());
//
//        return coreSingletons.getIQFactory().createIQ(newProjectionAtom, iqWithTemporaryAtomPredicate.getTree());
//    }
//
//    @Override
//    public IQ getIQ() {
//        return iq;
//    }
//
//    @Override
//    public int getLevel() {
//        return level;
//    }
//}
////    private final ImmutableList<Attribute> attributes;
////    private FlattenNodePredicate predicate;
//
////    protected OntopFlattenedViewDefinitionImpl(RelationID id, ImmutableList<QuotedID> attributeIds, ImmutableList<Integer> attributeType, ImmutableList<Boolean> canNull, TypeMapper typeMapper) {
////        super(id);
////        this.attributes = createAttributes(attributeIds, attributeType, canNull, typeMapper);
////    }
//
//    private ImmutableList<Attribute> createAttributes(ImmutableList<QuotedID> ids, ImmutableList<Integer> attributeTypes, ImmutableList<Boolean> canNull, TypeMapper typeMapper) {
//        UnmodifiableIterator<QuotedID> idIt = ids.iterator();
//        UnmodifiableIterator<Integer> typeIt = attributeTypes.iterator();
//        UnmodifiableIterator<Boolean> canNullIt = canNull.iterator();
//        return IntStream.range(0, ids.size()).boxed()
//                .map(i -> createAttribute(i+1, idIt.next(), typeIt.next(), canNullIt.next(), typeMapper))
//                .collect(ImmutableCollectors.toList());
//    }
//
//    private Attribute createAttribute(Integer index, QuotedID id, Integer type, Boolean canNull, TypeMapper typeMapper) {
//        return new Attribute(
//                this,
//                new QualifiedAttributeID(
//                        getID(),
//                        id),
//                index,
//                type,
//                null,
//                canNull,
//                typeMapper.getTermType(type)
//        );
//    }
//
//    public FlattenNodePredicate getAtomPredicate() {
//        if (predicate == null)
//            predicate = new FlattenNodePredicateImpl(this);
//        return predicate;
//    }
//
//    @Override
//    public Attribute getAttribute(int index) {
//        return attributes.get(index - 1);
//    }
//
//    @Override
//    public List<Attribute> getAttributes() {
//        return attributes;
//    }
//
//    @Override
//    public ImmutableList<UniqueConstraint> getUniqueConstraints() {
//        return ImmutableList.of();
//    }
//
//    @Override
//    public ImmutableList<FunctionalDependency> getOtherFunctionalDependencies() {
//        return ImmutableList.of();
//    }
//
//    @Override
//    public UniqueConstraint getPrimaryKey() {
//        throw new FlattenNodeRelationDefinitionException("This method should not be called");
//    }
//
//    @Override
//    public ImmutableList<ForeignKeyConstraint> getForeignKeys() {
//        return ImmutableList.of();
//    }
//
//    private class FlattenNodeRelationDefinitionException extends OntopInternalBugException {
//        FlattenNodeRelationDefinitionException  (String message) {
//            super(message);
//        }
//    }
}
