package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Stream;

/**
 * For DBMS such as SQLServer and Oracle that do not expect boolean expressions to be projected
 */
@Singleton
public class WrapProjectedOrOrderByExpressionNormalizer extends DefaultRecursiveIQTreeVisitingTransformer
        implements DialectExtraNormalizer {

    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @Inject
    protected WrapProjectedOrOrderByExpressionNormalizer(IntermediateQueryFactory iqFactory,
                                                         SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        super(iqFactory);
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return transform(tree);
    }

    @Override
    public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
        IQTree newChild = transform(child);

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = rootNode.getSubstitution();
        ImmutableSubstitution<ImmutableTerm> newSubstitution = rootNode.getSubstitution().transform(
                definition -> (definition instanceof ImmutableExpression)
                        ? transformExpression((ImmutableExpression) definition)
                        : definition);

        ConstructionNode newRootNode = newSubstitution.equals(initialSubstitution)
                ? rootNode
                : iqFactory.createConstructionNode(rootNode.getVariables(), newSubstitution);

        return ((newRootNode == rootNode) && (child == newChild))
                ? tree
                : iqFactory.createUnaryIQTree(newRootNode, newChild);
    }

    @Override
    public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
        IQTree newChild = transform(child);

        ImmutableList<OrderByNode.OrderComparator> initialComparators = rootNode.getComparators();
        ImmutableList<OrderByNode.OrderComparator> newComparators = initialComparators.stream()
                .map(c -> iqFactory.createOrderComparator(
                        transformOrderTerm(c.getTerm()), c.isAscending()))
                .collect(ImmutableCollectors.toList());

        OrderByNode newRootNode = initialComparators.equals(newComparators)
                ? rootNode
                : iqFactory.createOrderByNode(newComparators);

        return ((newRootNode == rootNode) && (child == newChild))
                ? tree
                : iqFactory.createUnaryIQTree(newRootNode, newChild);
    }

    private NonGroundTerm transformOrderTerm(NonGroundTerm term) {
        if (term instanceof ImmutableExpression) {
            return (NonGroundTerm) transformExpression((ImmutableExpression) term);
        }
        return term;
    }

    protected ImmutableFunctionalTerm transformExpression(ImmutableExpression definition) {
        return termFactory.getDBCaseElseNull(Stream.of(
                Maps.immutableEntry(definition, termFactory.getDBBooleanConstant(true)),
                Maps.immutableEntry(termFactory.getDBNot(definition), termFactory.getDBBooleanConstant(false))), false);
    }
}
