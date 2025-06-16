package it.unibz.inf.ontop.generation.normalization.impl;

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
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Stream;

/**
 * For DBMS such as SQLServer and Oracle that do not expect boolean expressions to be projected
 */
@Singleton
public class WrapProjectedOrOrderByExpressionNormalizer implements DialectExtraNormalizer {

    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;
    private final Transformer transformer;

    @Inject
    protected WrapProjectedOrOrderByExpressionNormalizer(CoreSingletons coreSingletons) {
        this.termFactory = coreSingletons.getTermFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.transformer = new Transformer(coreSingletons.getIQFactory());
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(transformer);
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {

        Transformer(IntermediateQueryFactory iqFactory) {
            super(iqFactory);
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            IQTree newChild = transformChild(child);
            ConstructionNode newRootNode = iqTreeTools.replaceSubstitution(
                    rootNode, s -> s.transform(this::transformTerm));

            return newRootNode.equals(rootNode) && (child == newChild)
                    ? tree
                    : iqFactory.createUnaryIQTree(newRootNode, newChild);
        }

        @Override
        public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
            IQTree newChild = transformChild(child);
            OrderByNode newRootNode = iqFactory.createOrderByNode(
                    rootNode.getComparators().stream()
                            .map(c -> iqFactory.createOrderComparator(
                                    (NonGroundTerm) transformTerm(c.getTerm()), c.isAscending()))
                            .collect(ImmutableCollectors.toList()));

            return newRootNode.equals(rootNode) && (child == newChild)
                    ? tree
                    : iqFactory.createUnaryIQTree(newRootNode, newChild);
        }

        private ImmutableTerm transformTerm(ImmutableTerm term) {
            if (term instanceof ImmutableExpression)
                return transformExpression((ImmutableExpression) term);

            return term;
        }

        protected ImmutableFunctionalTerm transformExpression(ImmutableExpression definition) {
            return termFactory.getDBCaseElseNull(Stream.of(
                    Maps.immutableEntry(definition, termFactory.getDBBooleanConstant(true)),
                    Maps.immutableEntry(termFactory.getDBNot(definition), termFactory.getDBBooleanConstant(false))), false);
        }
    }
}
