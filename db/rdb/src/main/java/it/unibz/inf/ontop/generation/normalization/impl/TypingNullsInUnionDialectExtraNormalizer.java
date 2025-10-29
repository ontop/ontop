package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultDelegatingIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;

import static it.unibz.inf.ontop.iq.impl.UnaryIQTreeTools.UnaryIQTreeDecomposition;

public class TypingNullsInUnionDialectExtraNormalizer extends DefaultDelegatingIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected TypingNullsInUnionDialectExtraNormalizer(CoreSingletons coreSingletons, SingleTermTypeExtractor uniqueTermTypeExtractor) {
        super(new Transformer(coreSingletons, uniqueTermTypeExtractor)::transform);
    }

    private static class Transformer extends AbstractTypingNullsTransformer {
        private final SingleTermTypeExtractor uniqueTermTypeExtractor;

        Transformer(CoreSingletons coreSingletons,
                    SingleTermTypeExtractor uniqueTermTypeExtractor) {
            super(coreSingletons);
            this.uniqueTermTypeExtractor = uniqueTermTypeExtractor;
        }

        @Override
        public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<IQTree> updatedChildren = NaryIQTreeTools.transformChildren(children, this::transform);

            ImmutableSet<Variable> nullVariables = UnaryIQTreeDecomposition.getNodeStream(
                            UnaryIQTreeDecomposition.of(updatedChildren, ConstructionNode.class))
                    .map(this::extractNullVariables)
                    .flatMap(Collection::stream)
                    .collect(ImmutableCollectors.toSet());

            if (nullVariables.isEmpty())
                return withTransformedChildren(tree, updatedChildren);

            var typedNullMap = extractTypedNullMap(nullVariables,
                    v -> uniqueTermTypeExtractor.extractSingleTermType(v, tree)
                            .filter(t -> t instanceof DBTermType)
                            .map(t -> (DBTermType) t));

            ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(updatedChildren,
                    c -> updateSubTree(c, typedNullMap));

            return iqFactory.createNaryIQTree(rootNode, newChildren);
        }
    }
}
