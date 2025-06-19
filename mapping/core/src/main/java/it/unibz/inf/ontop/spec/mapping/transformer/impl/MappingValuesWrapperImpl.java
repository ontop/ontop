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
import it.unibz.inf.ontop.injection.OntopMappingSettings;
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

    private final OntopMappingSettings settings;

    @Inject
    protected MappingValuesWrapperImpl(OntopMappingSettings settings) {
        this.settings = settings;
    }

    @Override
    public ImmutableList<MappingAssertion> normalize(ImmutableList<MappingAssertion> mapping, DBParameters dbParameters) {
        if (settings.isValuesNodesWrapInLensesInMappingEnabled()) {
            var transformer = new Transformer(dbParameters);
            return mapping.stream()
                    .map(transformer::transformMappingAssertion)
                    .collect(ImmutableCollectors.toList());
        }
        else
            return mapping;
    }

    private static class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        private final QuotedIDFactory rawQuotedIqFactory;
        private final DBParameters dbParameters;
        private final AtomFactory atomFactory;

        Transformer(DBParameters dbParameters) {
            super(dbParameters.getCoreSingletons().getIQFactory());
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

        private RelationDefinition.AttributeListBuilder createAttributeBuilder(IQ iq, DBParameters dbParameters)  {
            SingleTermTypeExtractor uniqueTermTypeExtractor = dbParameters.getCoreSingletons().getUniqueTermTypeExtractor();
            var builder = AbstractRelationDefinition.attributeListBuilder();

            var iqTree = iq.getTree();
            var variableNullability = iqTree.getVariableNullability();
            var rootType= dbParameters.getDBTypeFactory().getAbstractRootDBType();

            for (Variable v : iq.getProjectionAtom().getArguments()) {
                var attributeId = rawQuotedIqFactory.createAttributeID(v.getName());
                var inferredType = uniqueTermTypeExtractor.extractSingleTermType(v, iqTree)
                        .orElse(rootType);

                var datatype = (inferredType instanceof DBTermType)
                        ? (DBTermType) inferredType
                        // Temporary (as the lenses are unfolded)
                        : rootType;

                builder.addAttribute(attributeId, datatype, variableNullability.isPossiblyNullable(v));
            }
            return builder;
        }

        private AtomPredicate createTemporaryPredicate(RelationID relationId, int arity, CoreSingletons coreSingletons) {
            DBTermType dbRootType = coreSingletons.getTypeFactory().getDBTypeFactory().getAbstractRootDBType();

            return new TemporaryLensPredicate(
                    relationId.getSQLRendering(),
                    // No precise base DB type for the temporary predicate
                    IntStream.range(0, arity)
                            .mapToObj(i -> dbRootType).collect(ImmutableCollectors.toList()));
        }

        private MappingAssertion transformMappingAssertion(MappingAssertion mappingAssertion) {
            var initialTree = mappingAssertion.getQuery().getTree();

            var newTree = transformChild(initialTree);

            return newTree.equals(initialTree)
                    ? mappingAssertion
                    : mappingAssertion.copyOf(iqFactory.createIQ(mappingAssertion.getProjectionAtom(), newTree));
        }
    }

    protected static class TemporaryLensPredicate extends AtomPredicateImpl {

        protected TemporaryLensPredicate(String name, ImmutableList<TermType> baseTypesForValidation) {
            super(name, baseTypesForValidation);
        }
    }

}
