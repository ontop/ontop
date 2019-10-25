package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.type.UniqueTermTypeExtractor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.NotYetTypedEqualityFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingEqualityTransformer;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

public class MappingEqualityTransformerImpl implements MappingEqualityTransformer {

    private final ProvenanceMappingFactory mappingFactory;
    private final IQTreeTransformer expressionTransformer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected MappingEqualityTransformerImpl(ProvenanceMappingFactory mappingFactory,
                                             UniqueTermTypeExtractor typeExtractor, CoreSingletons coreSingletons) {
        this.mappingFactory = mappingFactory;
        this.expressionTransformer = new ExpressionTransformer(typeExtractor, coreSingletons);
        this.iqFactory = coreSingletons.getIQFactory();
    }

    @Override
    public MappingWithProvenance transform(MappingWithProvenance mapping) {
        ImmutableMap<IQ, PPMappingAssertionProvenance> newProvenanceMap = mapping.getProvenanceMap().entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> transformMappingAssertion(e.getKey()),
                        Map.Entry::getValue));
        return mappingFactory.create(newProvenanceMap, mapping.getMetadata());
    }

    private IQ transformMappingAssertion(IQ mappingAssertion) {
        IQTree initialTree = mappingAssertion.getTree();
        IQTree newTree = expressionTransformer.transform(initialTree);
        return (newTree.equals(initialTree))
                ? mappingAssertion
                : iqFactory.createIQ(mappingAssertion.getProjectionAtom(), newTree);
    }


    protected static class ExpressionTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        protected final UniqueTermTypeExtractor typeExtractor;
        protected final TermFactory termFactory;
        private final SubstitutionFactory substitutionFactory;

        protected ExpressionTransformer(UniqueTermTypeExtractor typeExtractor, CoreSingletons coreSingletons) {
            super(coreSingletons);
            this.typeExtractor = typeExtractor;
            this.termFactory = coreSingletons.getTermFactory();
            this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
            IQTree newChild = transform(child);

            ImmutableSubstitution<ImmutableTerm> initialSubstitution = rootNode.getSubstitution();

            ImmutableSubstitution<ImmutableTerm> newSubstitution = substitutionFactory.getSubstitution(
                    initialSubstitution.getImmutableMap().entrySet().stream()
                            .collect(ImmutableCollectors.toMap(
                                    Map.Entry::getKey,
                                    e -> transformTerm(e.getValue(), child))));

            return (newChild.equals(child) && newSubstitution.equals(initialSubstitution))
                    ? tree
                    : iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(rootNode.getVariables(), newSubstitution),
                            newChild);
        }

        @Override
        public IQTree transformAggregation(IQTree tree, AggregationNode rootNode, IQTree child) {
            IQTree newChild = transform(child);

            ImmutableSubstitution<ImmutableFunctionalTerm> initialSubstitution = rootNode.getSubstitution();

            ImmutableSubstitution<ImmutableFunctionalTerm> newSubstitution = substitutionFactory.getSubstitution(
                    initialSubstitution.getImmutableMap().entrySet().stream()
                            .collect(ImmutableCollectors.toMap(
                                    Map.Entry::getKey,
                                    e -> transformFunctionalTerm(e.getValue(), child))));

            return (newChild.equals(child) && newSubstitution.equals(initialSubstitution))
                    ? tree
                    : iqFactory.createUnaryIQTree(
                        iqFactory.createAggregationNode(rootNode.getGroupingVariables(), newSubstitution),
                        newChild);
        }

        @Override
        public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
            IQTree newChild = transform(child);
            ImmutableExpression initialExpression = rootNode.getFilterCondition();
            ImmutableExpression newExpression = transformExpression(initialExpression, tree);

            FilterNode newFilterNode = newExpression.equals(initialExpression)
                    ? rootNode
                    : rootNode.changeFilterCondition(newExpression);

            return (newFilterNode.equals(rootNode) && newChild.equals(child))
                    ? tree
                    : iqFactory.createUnaryIQTree(newFilterNode, newChild);
        }

        @Override
        public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
            IQTree newChild = transform(child);

            ImmutableList<OrderByNode.OrderComparator> initialComparators = rootNode.getComparators();

            ImmutableList<OrderByNode.OrderComparator> newComparators = initialComparators.stream()
                    .map(c -> iqFactory.createOrderComparator(
                            transformNonGroundTerm(c.getTerm(), tree),
                            c.isAscending()))
                    .collect(ImmutableCollectors.toList());

            return (newComparators.equals(initialComparators) && newChild.equals(child))
                    ? tree
                    : iqFactory.createUnaryIQTree(
                            iqFactory.createOrderByNode(newComparators),
                            newChild);
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            IQTree newLeftChild = transform(leftChild);
            IQTree newRightChild = transform(rightChild);
            Optional<ImmutableExpression> initialExpression = rootNode.getOptionalFilterCondition();
            Optional<ImmutableExpression> newExpression = initialExpression
                    .map(e -> transformExpression(e, tree));

            LeftJoinNode newLeftJoinNode = newExpression.equals(initialExpression)
                    ? rootNode
                    : rootNode.changeOptionalFilterCondition(newExpression);

            return (newLeftJoinNode.equals(rootNode) && newLeftChild.equals(leftChild) && newRightChild.equals(rightChild))
                    ? tree
                    : iqFactory.createBinaryNonCommutativeIQTree(newLeftJoinNode, newLeftChild, newRightChild);
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<IQTree> newChildren = children.stream()
                    .map(this::transform)
                    .collect(ImmutableCollectors.toList());

            Optional<ImmutableExpression> initialExpression = rootNode.getOptionalFilterCondition();
            Optional<ImmutableExpression> newExpression = initialExpression
                    .map(e -> transformExpression(e, tree));

            InnerJoinNode newJoinNode = newExpression.equals(initialExpression)
                    ? rootNode
                    : rootNode.changeOptionalFilterCondition(newExpression);

            return (newJoinNode.equals(rootNode) && newChildren.equals(children))
                    ? tree
                    : iqFactory.createNaryIQTree(newJoinNode, newChildren);
        }

        protected ImmutableTerm transformTerm(ImmutableTerm term, IQTree tree) {
            return (term instanceof ImmutableFunctionalTerm)
                    ? transformFunctionalTerm((ImmutableFunctionalTerm)term, tree)
                    : term;
        }

        protected NonGroundTerm transformNonGroundTerm(NonGroundTerm term, IQTree tree) {
            return (term instanceof ImmutableFunctionalTerm)
                    ? (NonGroundTerm) transformFunctionalTerm((ImmutableFunctionalTerm)term, tree)
                    : term;
        }

        protected ImmutableExpression transformExpression(ImmutableExpression expression, IQTree tree) {
            return (ImmutableExpression) transformFunctionalTerm(expression, tree);
        }

        /**
         * Recursive
         */
        protected ImmutableFunctionalTerm transformFunctionalTerm(ImmutableFunctionalTerm functionalTerm, IQTree tree) {
            ImmutableList<? extends ImmutableTerm> initialTerms = functionalTerm.getTerms();
            ImmutableList<ImmutableTerm> newTerms = initialTerms.stream()
                    .map(t -> (t instanceof ImmutableFunctionalTerm)
                            // Recursive
                            ? transformFunctionalTerm((ImmutableFunctionalTerm) t, tree)
                            : t)
                    .collect(ImmutableCollectors.toList());

            FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();

            if (functionSymbol instanceof NotYetTypedEqualityFunctionSymbol) {
                return transformEquality(newTerms, tree);
            }
            else
                return newTerms.equals(initialTerms)
                        ? functionalTerm
                        : termFactory.getImmutableFunctionalTerm(functionSymbol, newTerms);
        }

        /**
         * NB: It tries to reduce string and integer equalities into strict equalities as these kinds
         * types are often used to build IRIs.
         */
        protected ImmutableExpression transformEquality(ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            if (newTerms.size() != 2)
                throw new MinorOntopInternalBugException("Was expecting the not yet typed equalities to be binary");

            ImmutableTerm term1 = newTerms.get(0);
            ImmutableTerm term2 = newTerms.get(1);

            ImmutableList<Optional<TermType>> extractedTypes = newTerms.stream()
                    .map(t -> typeExtractor.extractUniqueTermType(t, tree))
                    .collect(ImmutableCollectors.toList());

            if (extractedTypes.stream()
                    .allMatch(type -> type
                            .filter(t -> t instanceof DBTermType)
                            .isPresent())) {
                ImmutableList<DBTermType> types = extractedTypes.stream()
                        .map(Optional::get)
                        .map(t -> (DBTermType) t)
                        .collect(ImmutableCollectors.toList());

                DBTermType type1 = types.get(0);
                DBTermType type2 = types.get(1);

                DBTermType.Category category1 = type1.getCategory();
                DBTermType.Category category2 = type2.getCategory();

                switch (category1) {
                    case STRING:
                        if (category2 == DBTermType.Category.STRING) {
                            return transformStringEquality(term1, term2, type1, type2);
                        }
                        break;
                    case INTEGER:
                        switch (category2) {
                            case INTEGER:
                                return transformIntegerEquality(term1, term2, type1, type2);
                            case DECIMAL:
                            case FLOAT_DOUBLE:
                                return termFactory.getDBNonStrictNumericEquality(term1, term2);
                            default:
                                break;
                        }
                        break;
                    case DECIMAL:
                    case FLOAT_DOUBLE:
                        switch (category2) {
                            case INTEGER:
                            case DECIMAL:
                            case FLOAT_DOUBLE:
                                return termFactory.getDBNonStrictNumericEquality(term1, term2);
                            default:
                                break;
                        }
                        break;
                    case DATETIME:
                        if (category2 == DBTermType.Category.DATETIME) {
                            return termFactory.getDBNonStrictDatetimeEquality(term1, term2);
                        }
                        break;
                    // TODO: try to simplify booleans
                    case BOOLEAN:
                    case OTHER:
                    default:
                        break;
                }
            }
            // Default case
            return termFactory.getDBNonStrictDefaultEquality(term1, term2);
        }

        protected ImmutableExpression transformStringEquality(ImmutableTerm term1, ImmutableTerm term2,
                                                            DBTermType type1, DBTermType type2) {
            return tryToMakeTheEqualityStrict(term1, term2, type1, type2);
        }

        protected ImmutableExpression tryToMakeTheEqualityStrict(ImmutableTerm term1, ImmutableTerm term2,
                                                                 DBTermType type1, DBTermType type2) {
            if (type1.equals(type2))
                return termFactory.getStrictEquality(term1, term2);

            // Change the datatype of the first found constant
            if (term1 instanceof DBConstant)
                return termFactory.getStrictEquality(
                        termFactory.getDBConstant(((DBConstant) term1).getValue(), type2),
                        term2);
            if (term2 instanceof DBConstant)
                return termFactory.getStrictEquality(
                        term1,
                        termFactory.getDBConstant(((DBConstant) term2).getValue(), type1));
            else
                // Fall-back case
                return termFactory.getDBNonStrictDefaultEquality(term1, term2);
        }


        /**
         * Assumes that the integer values are normalized, that is they don't contain a "+"
         * TODO: clean the potential "+" from constants?
         */
        protected ImmutableExpression transformIntegerEquality(ImmutableTerm term1, ImmutableTerm term2,
                                                             DBTermType type1, DBTermType type2) {
            return tryToMakeTheEqualityStrict(term1, term2, type1, type2);
        }
    }

}
