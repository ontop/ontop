package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.FlattenNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.normalization.FlattenNormalizer;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class FlattenNormalizerImpl implements FlattenNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private FlattenNormalizerImpl(IntermediateQueryFactory iqFactory, TermFactory termFactory,
                                  SubstitutionFactory substitutionFactory) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public IQTree normalizeForOptimization(FlattenNode flattenNode, IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {

        IQTree normalizedChild = child.normalizeForOptimization(variableGenerator);
        QueryNode childRoot = normalizedChild.getRootNode();

        if (childRoot instanceof ConstructionNode) {
            ConstructionNode cn = (ConstructionNode) childRoot;
            ImmutableMap<Boolean, ImmutableMap<Variable, ImmutableTerm>> splitSub = splitSubstitution(
                    cn,
                    flattenNode.getFlattenedVariable()
            );
            IQTree grandChild = normalizedChild.getChildren().get(0);
            IQTree flattenTree = getFlattenTree(flattenNode, splitSub.get(true), cn, grandChild);
            Optional<ConstructionNode> parent = getParent(flattenNode, splitSub.get(false));
            return parent.isPresent() ?
                    iqFactory.createUnaryIQTree(
                            parent.get(),
                            flattenTree
                    ) :
                    flattenTree;
        }
        return iqFactory.createUnaryIQTree(
                flattenNode,
                child
        );
    }

    private IQTree getFlattenTree(FlattenNode fn, ImmutableMap<Variable, ImmutableTerm> flattenedVarDef, ConstructionNode cn, IQTree grandChild) {
        return iqFactory.createUnaryIQTree(
                        fn,
                        flattenedVarDef.isEmpty() ?
                                grandChild :
                                iqFactory.createUnaryIQTree(
                                        iqFactory.createConstructionNode(
                                                cn.getVariables(),
                                                substitutionFactory.getSubstitution(flattenedVarDef)
                                        ),
                                        grandChild
                                ));
    }

    private ImmutableMap<Boolean, ImmutableMap<Variable, ImmutableTerm>> splitSubstitution(ConstructionNode cn, Variable flattenedVar) {
        return cn.getSubstitution().getImmutableMap().entrySet().stream().collect(
                        ImmutableCollectors.partitioningBy(
                                e -> (e.getKey().equals(flattenedVar)),
                                ImmutableCollectors.toMap(
                                        ImmutableMap.Entry::getKey,
                                        ImmutableMap.Entry::getValue
                                ))
                );

    }

    private Optional<ConstructionNode> getParent(FlattenNode flattenNode, ImmutableMap<Variable, ImmutableTerm> filteredSub) {
        return filteredSub.isEmpty()?
                Optional.empty():
                Optional.of(iqFactory.createConstructionNode(
                        flattenNode.getVariables(filteredSub.keySet()),
                        substitutionFactory.getSubstitution(filteredSub)
                ));
    }

}
