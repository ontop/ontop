package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;

public class TypingNullsInUnionDialectExtraNormalizer extends AbstractTypingNullsDialectExtraNormalizer {

    protected final SingleTermTypeExtractor uniqueTermTypeExtractor;

    @Inject
    protected TypingNullsInUnionDialectExtraNormalizer(CoreSingletons coreSingletons,
                                                       SingleTermTypeExtractor uniqueTermTypeExtractor) {
        super(coreSingletons);
        this.uniqueTermTypeExtractor = uniqueTermTypeExtractor;
    }

    @Override
    public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> updatedChildren = children.stream()
                .map(this::transform)
                .collect(ImmutableCollectors.toList());

        ImmutableSet<Variable> nullVariables = updatedChildren.stream()
                .map(IQTree::getRootNode)
                .filter(c -> c instanceof ConstructionNode)
                .map(c -> (ConstructionNode) c)
                .flatMap(this::extractNullVariables)
                .collect(ImmutableCollectors.toSet());

        if (nullVariables.isEmpty())
            return updatedChildren.equals(children) ? tree : iqFactory.createNaryIQTree(rootNode, updatedChildren);

        ImmutableMap<Variable, ImmutableFunctionalTerm> typedNullMap = extractTypedNullMap(tree, nullVariables);

        ImmutableList<IQTree> newChildren = updatedChildren.stream()
                .map(c -> updateSubQuery(c, typedNullMap))
                .collect(ImmutableCollectors.toList());

        return iqFactory.createNaryIQTree(rootNode, newChildren);
    }

    private ImmutableMap<Variable, ImmutableFunctionalTerm> extractTypedNullMap(IQTree tree, ImmutableSet<Variable> nullVariables) {
        ImmutableMap<Variable, Optional<TermType>> typeMap = nullVariables.stream()
                .collect(ImmutableCollectors.toMap(
                        v -> v,
                        v -> uniqueTermTypeExtractor.extractSingleTermType(v, tree)));

        return typeMap.entrySet().stream()
                .filter(e -> e.getValue().isPresent())
                .filter(e -> e.getValue()
                        .filter(t -> t instanceof DBTermType)
                        .isPresent())
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> termFactory.getTypedNull((DBTermType) e.getValue().get())));
    }
}
