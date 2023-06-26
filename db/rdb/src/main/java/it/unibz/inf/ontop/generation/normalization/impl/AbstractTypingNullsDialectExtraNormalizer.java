package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

public abstract class AbstractTypingNullsDialectExtraNormalizer extends DefaultRecursiveIQTreeVisitingTransformer
        implements DialectExtraNormalizer {

    protected final TermFactory termFactory;
    protected final SubstitutionFactory substitutionFactory;

    protected AbstractTypingNullsDialectExtraNormalizer(CoreSingletons coreSingletons) {
        super(coreSingletons);
        this.termFactory = coreSingletons.getTermFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();

    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return transform(tree);
    }

    protected ImmutableSet<Variable> extractNullVariables(ConstructionNode constructionNode) {
        return constructionNode.getSubstitution().getPreImage(ImmutableTerm::isNull);
    }

    /**
     * Replaces NULL bindings in top construction nodes if a type is defined
     */
    protected IQTree updateSubQuery(IQTree child, ImmutableMap<Variable, ImmutableFunctionalTerm> typedNullMap) {
        if (child.getRootNode() instanceof ConstructionNode) {
            ConstructionNode constructionNode = (ConstructionNode) child.getRootNode();

            Substitution<ImmutableTerm> newSubstitution = constructionNode.getSubstitution().builder()
                    .transformOrRetain(typedNullMap::get, (t, n) -> t.isNull() ? n : t)
                    .build();

            ConstructionNode newConstructionNode = iqFactory.createConstructionNode(
                    constructionNode.getVariables(), newSubstitution);

            return iqFactory.createUnaryIQTree(newConstructionNode, ((UnaryIQTree) child).getChild());
        }
        else
            return child;
    }
}
