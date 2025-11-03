package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultDelegatingIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;

/**
 * To be called AFTER the TypingNullsInUnionDialectExtraNormalizer (if relevant), NEVER BEFORE
 */
public class TypingNullsInConstructionNodeDialectExtraNormalizer extends DefaultDelegatingIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected TypingNullsInConstructionNodeDialectExtraNormalizer(CoreSingletons coreSingletons) {
        super(new Transformer(coreSingletons)::transform);
    }

    private static class Transformer extends AbstractTypingNullsTransformer {
        private final DBTermType defaultType;

        Transformer(CoreSingletons coreSingletons) {
            super(coreSingletons);
            this.defaultType = coreSingletons.getTypeFactory().getDBTypeFactory().getDBStringType();
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            ImmutableSet<Variable> nullVariables = extractNullVariables(rootNode);

            if (nullVariables.isEmpty())
                return super.transformConstruction(tree, rootNode, child);

            var typedNullMap = extractTypedNullMap(nullVariables,
                    v -> Optional.of(defaultType));

            IQTree newChild = transform(child);
            return updateSubTree(iqFactory.createUnaryIQTree(rootNode, newChild), typedNullMap);
        }
    }
}
