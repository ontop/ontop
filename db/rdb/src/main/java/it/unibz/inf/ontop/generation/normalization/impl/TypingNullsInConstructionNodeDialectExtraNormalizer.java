package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * To be called AFTER the TypingNullsInUnionDialectExtraNormalizer (if relevant), NEVER BEFORE
 */
public class TypingNullsInConstructionNodeDialectExtraNormalizer implements DialectExtraNormalizer {

    private final CoreSingletons coreSingletons;
    private final DBTermType defaultType;
    private final Transformer transformer;

    @Inject
    protected TypingNullsInConstructionNodeDialectExtraNormalizer(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.defaultType = coreSingletons.getTypeFactory().getDBTypeFactory().getDBStringType();
        this.transformer = new Transformer();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return transformer.transform(tree);
    }

    private class Transformer extends AbstractTypingNullsTransformer {
        Transformer() {
            super(coreSingletons);
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
