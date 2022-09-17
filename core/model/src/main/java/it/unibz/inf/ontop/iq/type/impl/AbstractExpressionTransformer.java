package it.unibz.inf.ontop.iq.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

public abstract class AbstractExpressionTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

    protected final SingleTermTypeExtractor typeExtractor;
    protected final TermFactory termFactory;

    protected AbstractExpressionTransformer(IntermediateQueryFactory iqFactory,
                                            SingleTermTypeExtractor typeExtractor,
                                            TermFactory termFactory) {
        super(iqFactory);
        this.typeExtractor = typeExtractor;
        this.termFactory = termFactory;
    }

    @Override
    public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
        IQTree newChild = transform(child);

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = rootNode.getSubstitution();
        ImmutableSubstitution<ImmutableTerm> newSubstitution = initialSubstitution.transform(v -> transformTerm(v, child));

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
        ImmutableSubstitution<ImmutableFunctionalTerm> newSubstitution = initialSubstitution.transform(v -> transformFunctionalTerm(v, child));

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
