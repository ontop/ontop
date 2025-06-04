package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.BelowDistinctJoinWithClassUnionOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.IQTreeTransformerAdapter;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.Stream;

public class BelowDistinctJoinWithClassUnionOptimizerImpl implements BelowDistinctJoinWithClassUnionOptimizer {

    private final IQVisitor<IQTree> lookForDistinctTransformer;
    private final IntermediateQueryFactory iqFactory;
    private final CoreSingletons coreSingletons;
    private final RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor;

    @Inject
    protected BelowDistinctJoinWithClassUnionOptimizerImpl(CoreSingletons coreSingletons,
                                                           RequiredExtensionalDataNodeExtractor requiredExtensionalDataNodeExtractor) {
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
        this.requiredExtensionalDataNodeExtractor = requiredExtensionalDataNodeExtractor;
        this.lookForDistinctTransformer = new LookForDistinctOrLimit1TransformerImpl(
                p -> new IQTreeTransformerAdapter(new JoinWithClassUnionTransformer(p)),
                coreSingletons);
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();
        IQTree newTree = initialTree.acceptVisitor(lookForDistinctTransformer);
        return (newTree.equals(initialTree))
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree)
                .normalizeForOptimization();
    }

    protected class JoinWithClassUnionTransformer extends AbstractBelowDistinctInnerJoinTransformer {

        protected JoinWithClassUnionTransformer(IQTreeTransformer lookForDistinctTransformer) {
            super(lookForDistinctTransformer, BelowDistinctJoinWithClassUnionOptimizerImpl.this.coreSingletons);
        }

        /**
         * Should not return any false positives
         */
        protected boolean isDetectedAsRedundant(IQTree child, Stream<IQTree> otherChildrenStream) {
            ImmutableSet<IQTree> otherChildren = otherChildrenStream.collect(ImmutableCollectors.toSet());

            var union = NaryIQTreeTools.UnionDecomposition.of(child);
            if (union.isPresent())
                return union.getChildren().stream()
                    .flatMap(c -> extractExtensionalNode(c).stream())
                    .anyMatch(c -> otherChildren.stream()
                            .flatMap(requiredExtensionalDataNodeExtractor::transform)
                            .anyMatch(o -> isDetectedAsRedundant(c, o)));
            return false;
        }

        private Optional<ExtensionalDataNode> extractExtensionalNode(IQTree child) {
            /*
             * Filters just make much the variables are non-null can be eliminating,
             * because we are interested in cases where we join over these variables
             */
            var filter = IQTreeTools.UnaryIQTreeDecomposition.of(child, FilterNode.class);
            if (filter.isPresent()) {
                VariableNullability variableNullability = coreSingletons.getCoreUtilsFactory()
                        .createEmptyVariableNullability(child.getVariables());

                ImmutableExpression filterCondition = filter.getNode().getFilterCondition();

                return filterCondition.evaluate2VL(variableNullability)
                        .getValue()
                        .filter(b -> b.equals(ImmutableExpression.Evaluation.BooleanValue.TRUE))
                        // Continue to the child
                        .flatMap(b -> extractExtensionalNode(filter.getChild()));
            }
            if (child instanceof ExtensionalDataNode)
                return Optional.of((ExtensionalDataNode) child);

            return Optional.empty();
        }
    }
}
