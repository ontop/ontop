package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.node.DefaultQueryNodeTransformer;
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
    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTransformer transformer;

    @Inject
    protected WrapProjectedOrOrderByExpressionNormalizer(CoreSingletons coreSingletons) {
        this.termFactory = coreSingletons.getTermFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.iqFactory = coreSingletons.getIQFactory();
        this.transformer = new Transformer().treeTransformer();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return transformer.transform(tree);
    }

    private class Transformer extends DefaultQueryNodeTransformer {

        Transformer() {
            super(WrapProjectedOrOrderByExpressionNormalizer.this.iqFactory);
        }

        @Override
        public ConstructionNode transform(ConstructionNode constructionNode, UnaryIQTree tree) {
            return iqTreeTools.replaceSubstitution(
                    constructionNode, s -> s.transform(this::transformTerm));
        }

        @Override
        public OrderByNode transform(OrderByNode rootNode, UnaryIQTree tree) {
            var transformedComparators = iqTreeTools.transformComparators(rootNode.getComparators(), this::transformTerm);
            if (rootNode.getComparators().size() != transformedComparators.size())
                throw new MinorOntopInternalBugException("expected same-length comparator lists");
            return iqFactory.createOrderByNode(transformedComparators);
        }

        private ImmutableTerm transformTerm(ImmutableTerm term) {
            if (term instanceof ImmutableExpression) {
                ImmutableExpression definition = (ImmutableExpression) term;
                return termFactory.getDBCaseElseNull(Stream.of(
                        Maps.immutableEntry(definition, termFactory.getDBBooleanConstant(true)),
                        Maps.immutableEntry(termFactory.getDBNot(definition), termFactory.getDBBooleanConstant(false))), false);
            }

            return term;
        }
    }
}
