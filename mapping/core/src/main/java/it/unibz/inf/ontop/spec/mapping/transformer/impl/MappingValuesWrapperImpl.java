package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.AbstractRelationDefinition;
import it.unibz.inf.ontop.dbschema.impl.LensImpl;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingValuesWrapper;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.UUID;
import java.util.stream.IntStream;

public class MappingValuesWrapperImpl implements MappingValuesWrapper {

    @Inject
    protected MappingValuesWrapperImpl() {
    }

    @Override
    public ImmutableList<MappingAssertion> normalize(ImmutableList<MappingAssertion> mapping, DBParameters dbParameters) {
        var transformer = new Transformer(dbParameters);
        return mapping.stream()
                .map(transformer::transformMappingAssertion)
                .collect(ImmutableCollectors.toList());
    }

    protected static class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        private final QuotedIDFactory rawQuotedIqFactory;
        private final DBParameters dbParameters;
        private final AtomFactory atomFactory;

        protected Transformer(DBParameters dbParameters) {
            super(dbParameters.getCoreSingletons());
            this.dbParameters = dbParameters;
            this.rawQuotedIqFactory = new RawQuotedIDFactory(dbParameters.getQuotedIDFactory());
            this.atomFactory = dbParameters.getCoreSingletons().getAtomFactory();
        }

        @Override
        public IQTree transformValues(ValuesNode valuesNode) {
            var uniqueConstraints = valuesNode.inferUniqueConstraints();

            // No wrapping if no unique constraints
            if (uniqueConstraints.isEmpty())
                return valuesNode;

            var relationId = rawQuotedIqFactory.createRelationID("(" + valuesNode + ")");

            var orderedVariables = valuesNode.getOrderedVariables();
            int arity = orderedVariables.size();

            var projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(
                    createTemporaryPredicate(relationId, arity, dbParameters.getCoreSingletons()),
                    orderedVariables);

            var newIQ = iqFactory.createIQ(projectionAtom, valuesNode);

            var attributeBuilder = createAttributeBuilder(newIQ, dbParameters);

            Lens lens = new LensImpl(ImmutableList.of(relationId), attributeBuilder, newIQ, 1,
                    dbParameters.getCoreSingletons());

            uniqueConstraints.forEach(uc -> insertUniqueConstraints(lens, uc, orderedVariables));

            return iqFactory.createExtensionalDataNode(lens, IntStream.range(0, arity)
                    .mapToObj(i -> Maps.immutableEntry(i, orderedVariables.get(i)))
                    .collect(ImmutableCollectors.toMap()));
        }

        private void insertUniqueConstraints(Lens lens, ImmutableSet<Variable> uniqueConstraint,
                                             ImmutableList<Variable> orderedVariables) {
            var builder = UniqueConstraint.builder(lens, "uc" + UUID.randomUUID());

            uniqueConstraint.stream()
                    .map(v -> orderedVariables.indexOf(v) + 1)
                    .forEach(builder::addDeterminant);

            builder.build();

        }

        protected RelationDefinition.AttributeListBuilder createAttributeBuilder(IQ iq, DBParameters dbParameters)  {
            SingleTermTypeExtractor uniqueTermTypeExtractor = dbParameters.getCoreSingletons().getUniqueTermTypeExtractor();

            RelationDefinition.AttributeListBuilder builder = AbstractRelationDefinition.attributeListBuilder();

            IQTree iqTree = iq.getTree();

            for (Variable v : iq.getProjectionAtom().getArguments()) {
                QuotedID attributeId = rawQuotedIqFactory.createAttributeID(v.getName());

                builder.addAttribute(attributeId,
                        (DBTermType) uniqueTermTypeExtractor.extractSingleTermType(v, iqTree)
                                .orElseGet(() -> dbParameters.getDBTypeFactory().getAbstractRootDBType()),
                        iqTree.getVariableNullability().isPossiblyNullable(v));
            }
            return builder;
        }

        protected AtomPredicate createTemporaryPredicate(RelationID relationId, int arity, CoreSingletons coreSingletons) {
            DBTermType dbRootType = coreSingletons.getTypeFactory().getDBTypeFactory().getAbstractRootDBType();

            return new TemporaryLensPredicate(
                    relationId.getSQLRendering(),
                    // No precise base DB type for the temporary predicate
                    IntStream.range(0, arity)
                            .mapToObj(i -> dbRootType).collect(ImmutableCollectors.toList()));
        }

        public MappingAssertion transformMappingAssertion(MappingAssertion mappingAssertion) {
            var initialIQ = mappingAssertion.getQuery();
            var initialTree = initialIQ.getTree();

            var newTree = transform(initialTree);

            return newTree.equals(initialTree)
                    ? mappingAssertion
                    : new MappingAssertion(
                            iqFactory.createIQ(initialIQ.getProjectionAtom(), newTree),
                            mappingAssertion.getProvenance());
        }
    }

    protected static class TemporaryLensPredicate extends AtomPredicateImpl {

        protected TemporaryLensPredicate(String name, ImmutableList<TermType> baseTypesForValidation) {
            super(name, baseTypesForValidation);
        }
    }

}
