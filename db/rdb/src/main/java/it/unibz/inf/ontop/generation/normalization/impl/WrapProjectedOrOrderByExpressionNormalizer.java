package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
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

    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    protected WrapProjectedOrOrderByExpressionNormalizer(CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory());
        this.termFactory = coreSingletons.getTermFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return transform(tree);
    }

    @Override
    public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        IQTree newChild = transform(child);

        Substitution<ImmutableTerm> initialSubstitution = rootNode.getSubstitution();
        Substitution<ImmutableTerm> newSubstitution = initialSubstitution
                .transform(t -> (t instanceof ImmutableExpression)
                        ? transformExpression((ImmutableExpression) t)
                        : t);

        ConstructionNode newRootNode = newSubstitution.equals(initialSubstitution)
                ? rootNode
                : iqTreeTools.replaceSubstitution(rootNode, newSubstitution);

        return ((newRootNode == rootNode) && (child == newChild))
                ? tree
                : iqFactory.createUnaryIQTree(newRootNode, newChild);
    }

    @Override
    public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
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
