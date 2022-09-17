package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

/**
 * To be called AFTER the TypingNullsInUnionDialectExtraNormalizer (if relevant), NEVER BEFORE
 */
public class TypingNullsInConstructionNodeDialectExtraNormalizer extends AbstractTypingNullsDialectExtraNormalizer {

    private final DBTermType defaultType;

    @Inject
    protected TypingNullsInConstructionNodeDialectExtraNormalizer(CoreSingletons coreSingletons) {
        super(coreSingletons);
        defaultType = coreSingletons.getTypeFactory().getDBTypeFactory().getDBStringType();
    }

    @Override
    public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
        IQTree newChild = child.acceptTransformer(this);

        ImmutableSet<Variable> nullVariables = extractNullVariables(rootNode)
                .collect(ImmutableCollectors.toSet());

        if (nullVariables.isEmpty())
            return newChild.equals(child) ? tree : iqFactory.createUnaryIQTree(rootNode, newChild);

        ImmutableMap<Variable, ImmutableFunctionalTerm> typedNullMap = nullVariables.stream()
                .collect(ImmutableCollectors.toMap(
                        v -> v,
                        v -> termFactory.getTypedNull(defaultType)));

        return updateSubQuery(iqFactory.createUnaryIQTree(rootNode, newChild), typedNullMap);
    }
}
