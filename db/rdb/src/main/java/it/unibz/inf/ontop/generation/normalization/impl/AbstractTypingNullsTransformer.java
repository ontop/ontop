package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.function.Function;

import static it.unibz.inf.ontop.iq.impl.UnaryIQTreeTools.UnaryIQTreeDecomposition;

/*public*/ abstract class AbstractTypingNullsTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

    protected final IQTreeTools iqTreeTools;
    protected final TermFactory termFactory;

    protected AbstractTypingNullsTransformer(CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory());
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.termFactory = coreSingletons.getTermFactory();
    }

    protected final ImmutableSet<Variable> extractNullVariables(ConstructionNode constructionNode) {
        return constructionNode.getSubstitution().getPreImage(ImmutableTerm::isNull);
    }

    protected final ImmutableMap<Variable, ImmutableFunctionalTerm> extractTypedNullMap(ImmutableSet<Variable> nullVariables, Function<Variable, Optional<DBTermType>> typeMapper) {
        return nullVariables.stream()
                .map(v -> typeMapper.apply(v)
                        .map(termFactory::getTypedNull)
                        .map(t -> Maps.immutableEntry(v, t)))
                .flatMap(Optional::stream)
                .collect(ImmutableCollectors.toMap());
    }

    /**
     * Replaces NULL bindings in top construction nodes if a type is defined
     */
    protected final IQTree updateSubTree(IQTree child, ImmutableMap<Variable, ImmutableFunctionalTerm> typedNullMap) {
        var construction = UnaryIQTreeDecomposition.of(child, ConstructionNode.class);
        if (construction.isPresent()) {
            return iqFactory.createUnaryIQTree(
                    iqTreeTools.replaceSubstitution(construction.getNode(),
                            s -> s.builder()
                                    .transformOrRetain(typedNullMap::get, (t, n) -> t.isNull() ? n : t)
                                    .build()),
                    construction.getTail());
        }
        return child;
    }
}
