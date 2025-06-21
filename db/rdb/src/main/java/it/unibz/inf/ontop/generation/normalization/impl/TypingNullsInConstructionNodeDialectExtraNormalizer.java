package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.Substitution;

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
    public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        ImmutableSet<Variable> nullVariables = extractNullVariables(rootNode);

        if (nullVariables.isEmpty())
            return super.transformConstruction(tree, rootNode, child);

        Substitution<ImmutableFunctionalTerm> typedNullMap = nullVariables.stream()
                .collect(substitutionFactory.toSubstitution(
                        v -> v,
                        v -> termFactory.getTypedNull(defaultType)));

        IQTree newChild = transformChild(child);
        return updateSubQuery(iqFactory.createUnaryIQTree(rootNode, newChild), typedNullMap);
    }
}
