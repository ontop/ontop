package it.unibz.inf.ontop.iq.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;


public abstract class AbstractExpressionTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

    protected final SingleTermTypeExtractor typeExtractor;
    protected final TermFactory termFactory;
    protected final IQTreeTools iqTreeTools;

    // this constructor is needed because some uses are in the "parts" of CoreSingletons,
    // which would introduce a cyclic dependency for Guice
    protected AbstractExpressionTransformer(IntermediateQueryFactory iqFactory,
                                            SingleTermTypeExtractor typeExtractor,
                                            TermFactory termFactory, IQTreeTools iqTreeTools) {
        super(iqFactory);
        this.typeExtractor = typeExtractor;
        this.termFactory = termFactory;
        this.iqTreeTools = iqTreeTools;
    }

    protected AbstractExpressionTransformer(CoreSingletons coreSingletons) {
        this(coreSingletons.getIQFactory(),
                coreSingletons.getUniqueTermTypeExtractor(),
                coreSingletons.getTermFactory(),
                coreSingletons.getIQTreeTools());
    }

    @Override
    public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        IQTree newChild = transform(child);
        ConstructionNode newConstruction = iqTreeTools.replaceSubstitution(rootNode, s -> s.transform(t -> transformTerm(t, child)));

        return newConstruction.equals(rootNode) && newChild.equals(child)
                ? tree
                : iqFactory.createUnaryIQTree(newConstruction, newChild);
    }

    @Override
    public IQTree transformAggregation(UnaryIQTree tree, AggregationNode rootNode, IQTree child) {
        IQTree newChild = transform(child);
        AggregationNode newAggregation = iqFactory.createAggregationNode(
                rootNode.getGroupingVariables(),
                rootNode.getSubstitution()
                        .transform(t -> transformFunctionalTerm(t, child)));

        return newAggregation.equals(rootNode) && newChild.equals(child)
                ? tree
                : iqFactory.createUnaryIQTree(newAggregation, newChild);
    }

    @Override
    public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
        IQTree newChild = transform(child);
        FilterNode newFilterNode = iqFactory.createFilterNode(
                transformExpression(rootNode.getFilterCondition(), tree));

        return newFilterNode.equals(rootNode) && newChild.equals(child)
                ? tree
                : iqFactory.createUnaryIQTree(newFilterNode, newChild);
    }

    @Override
    public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
        IQTree newChild = transform(child);
        OrderByNode newOrderBy = iqFactory.createOrderByNode(rootNode.getComparators().stream()
                .map(c -> iqFactory.createOrderComparator(
                        transformNonGroundTerm(c.getTerm(), tree),
                        c.isAscending()))
                .collect(ImmutableCollectors.toList()));

        return newOrderBy.equals(rootNode) && newChild.equals(child)
                ? tree
                : iqFactory.createUnaryIQTree(newOrderBy, newChild);
    }

    @Override
    public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        IQTree newLeftChild = transform(leftChild);
        IQTree newRightChild = transform(rightChild);

        LeftJoinNode newLeftJoinNode = iqFactory.createLeftJoinNode(
                rootNode.getOptionalFilterCondition()
                        .map(e -> transformExpression(e, tree)));

        return newLeftJoinNode.equals(rootNode) && newLeftChild.equals(leftChild) && newRightChild.equals(rightChild)
                ? tree
                : iqFactory.createBinaryNonCommutativeIQTree(newLeftJoinNode, newLeftChild, newRightChild);
    }

    @Override
    public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children, this::transform);

        InnerJoinNode newJoinNode = iqFactory.createInnerJoinNode(
                rootNode.getOptionalFilterCondition()
                .map(e -> transformExpression(e, tree)));

        return newJoinNode.equals(rootNode) && newChildren.equals(children)
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
                // Recursive
                .map(t -> transformTerm(t, tree))
                .collect(ImmutableCollectors.toList());

        FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();

        if (isFunctionSymbolToReplace(functionSymbol)) {
            return replaceFunctionSymbol(functionSymbol, newTerms, tree);
        }
        else
            return newTerms.equals(initialTerms)
                    ? functionalTerm
                    : termFactory.getImmutableFunctionalTerm(functionSymbol, newTerms);
    }

    protected abstract boolean isFunctionSymbolToReplace(FunctionSymbol functionSymbol);

    protected abstract ImmutableFunctionalTerm replaceFunctionSymbol(FunctionSymbol functionSymbol, ImmutableList<ImmutableTerm> newTerms, IQTree tree);
}
