package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
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
    protected final IQTreeTools iqTreeTools;

    protected AbstractTypingNullsDialectExtraNormalizer(CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory());
        this.termFactory = coreSingletons.getTermFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(this);
    }

    protected ImmutableSet<Variable> extractNullVariables(ConstructionNode constructionNode) {
        return constructionNode.getSubstitution().getPreImage(ImmutableTerm::isNull);
    }

    /**
     * Replaces NULL bindings in top construction nodes if a type is defined
     */
    protected IQTree updateSubQuery(IQTree child, Substitution<ImmutableFunctionalTerm> typedNullMap) {
        var construction = IQTreeTools.UnaryIQTreeDecomposition.of(child, ConstructionNode.class);
        if (construction.isPresent()) {
            return iqFactory.createUnaryIQTree(
                    iqTreeTools.replaceSubstitution(construction.getNode(),
                            s -> s.builder()
                                    .transformOrRetain(typedNullMap::get, (t, n) -> t.isNull() ? n : t)
                                    .build()),
                    construction.getChild());
        }
        return child;
    }
}
