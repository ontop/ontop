package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;

public class TypingNullsDialectExtraNormalizer extends DefaultRecursiveIQTreeVisitingTransformer
        implements DialectExtraNormalizer {

    private final SingleTermTypeExtractor uniqueTermTypeExtractor;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    protected TypingNullsDialectExtraNormalizer(CoreSingletons coreSingletons,
                                                SingleTermTypeExtractor uniqueTermTypeExtractor) {
        super(coreSingletons);
        this.uniqueTermTypeExtractor = uniqueTermTypeExtractor;
        this.termFactory = coreSingletons.getTermFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return transform(tree);
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
                .flatMap(c -> c.getSubstitution().getImmutableMap().entrySet().stream())
                .filter(e -> e.getValue().isNull())
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());

        if (nullVariables.isEmpty())
            return updatedChildren.equals(children) ? tree : iqFactory.createNaryIQTree(rootNode, updatedChildren);

        ImmutableMap<Variable, Optional<TermType>> typeMap = nullVariables.stream()
                .collect(ImmutableCollectors.toMap(
                        v -> v,
                        v -> uniqueTermTypeExtractor.extractSingleTermType(v, tree)));

        ImmutableMap<Variable, ImmutableFunctionalTerm> typedNullMap = typeMap.entrySet().stream()
                .filter(e -> e.getValue().isPresent())
                .filter(e -> e.getValue()
                        .filter(t -> t instanceof DBTermType)
                        .isPresent())
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> termFactory.getTypedNull((DBTermType) e.getValue().get())));

        ImmutableList<IQTree> newChildren = updatedChildren.stream()
                .map(c -> updateChild(c, typedNullMap))
                .collect(ImmutableCollectors.toList());

        return iqFactory.createNaryIQTree(rootNode, newChildren);
    }

    /**
     * Replaces NULL bindings in top construction nodes if a type is defined
     */
    private IQTree updateChild(IQTree child, ImmutableMap<Variable, ImmutableFunctionalTerm> typedNullMap) {
        if (child.getRootNode() instanceof ConstructionNode) {
            ConstructionNode constructionNode = (ConstructionNode) child.getRootNode();

            ImmutableMap<Variable, ImmutableTerm> newSubstitutionMap = constructionNode.getSubstitution().getImmutableMap().entrySet().stream()
                    .map(e -> Optional.ofNullable(typedNullMap.get(e.getKey()))
                            .filter(n -> e.getValue().isNull())
                            .map(n -> Maps.immutableEntry(e.getKey(), (ImmutableTerm) n))
                            .orElse(e))
                    .collect(ImmutableCollectors.toMap());

            ConstructionNode newConstructionNode = iqFactory.createConstructionNode(
                    constructionNode.getVariables(),
                    substitutionFactory.getSubstitution(newSubstitutionMap));

            return iqFactory.createUnaryIQTree(newConstructionNode, ((UnaryIQTree) child).getChild());
        }
        else
            return child;
    }
}
