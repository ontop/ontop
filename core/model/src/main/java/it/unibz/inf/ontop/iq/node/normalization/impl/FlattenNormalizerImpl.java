package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.FlattenNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.normalization.FlattenNormalizer;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.Stream;

public class FlattenNormalizerImpl implements FlattenNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private FlattenNormalizerImpl(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public IQTree normalizeForOptimization(FlattenNode flattenNode, IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        IQTree normalizedChild = child.normalizeForOptimization(variableGenerator);
        QueryNode newChildRoot = normalizedChild.getRootNode();

        if (newChildRoot instanceof ConstructionNode) {
            ConstructionNode cn = (ConstructionNode) newChildRoot;
            ImmutableMap<Boolean, ImmutableMap<Variable, ImmutableTerm>> splitSub = splitSubstitution(
                    cn,
                    flattenNode.getFlattenedVariable()
            );

            IQTreeCache rootTreeCache = treeCache.declareAsNormalizedForOptimizationWithoutEffect();

            // if nothing can be lifted
            if(splitSub.get(false).isEmpty()){
                return iqFactory.createUnaryIQTree(
                        flattenNode,
                        normalizedChild,
                        rootTreeCache
                );
            }

            ConstructionNode newParent = getParent(flattenNode, cn, splitSub.get(false));

            IQTree updatedChild = getChild(flattenNode, splitSub.get(true), newParent, normalizedChild.getChildren().get(0));

            return iqFactory.createUnaryIQTree(
                    newParent,
                    iqFactory.createUnaryIQTree(
                            flattenNode,
                            updatedChild
                    ),
                    rootTreeCache
            );
        }

        return iqFactory.createUnaryIQTree(
                flattenNode,
                normalizedChild,
                treeCache.declareAsNormalizedForOptimizationWithoutEffect()
        );
    }

    private IQTree getChild(FlattenNode fn, ImmutableMap<Variable, ImmutableTerm> flattenedVarDef, ConstructionNode parentCn, IQTree grandChild) {
            return flattenedVarDef.isEmpty() ?
                    grandChild:
                    iqFactory.createUnaryIQTree(
                            getChildCn(
                                    flattenedVarDef,
                                    parentCn,
                                    fn
                            ),
                            grandChild
                    );
    }

    private ImmutableMap<Boolean, ImmutableMap<Variable, ImmutableTerm>> splitSubstitution(ConstructionNode cn, Variable flattenedVar) {
        return cn.getSubstitution().getImmutableMap().entrySet().stream().collect(
                ImmutableCollectors.partitioningBy(
                        e -> (e.getKey().equals(flattenedVar)),
                        ImmutableCollectors.toMap(
                                ImmutableMap.Entry::getKey,
                                ImmutableMap.Entry::getValue
                        )));
    }

    /**
     * Let c be the root of the child tree before lift, of the form CONSTRUCT[V, S].
     * Let f be the flattened variable.
     * Let o be the output variable of flattening.
     * <p>
     * We create a new parent for the flatten node, lifting the substitution S of c,
     * minus possibly the definition of f in c.
     * <p>
     * Partition S into S_f and S', where S_f is the definition of f
     * Then c' is
     *    CONSTRUCT[V', S']
     * where V' is defined as
     *    (V \ {f}) union {o}
     */
    private ConstructionNode getParent(FlattenNode fn, ConstructionNode cn, ImmutableMap<Variable, ImmutableTerm> filteredSub) {

        return iqFactory.createConstructionNode(
                Stream.concat(
                        fn.getLocallyDefinedVariables().stream(),
                        cn.getVariables().stream()
                                .filter(v -> !v.equals(fn.getFlattenedVariable()))
                ).collect(ImmutableCollectors.toSet()),
                substitutionFactory.getSubstitution(filteredSub)
        );
    }

    private ConstructionNode getChildCn(ImmutableMap<Variable, ImmutableTerm> flattenedVarDef, ConstructionNode parentCn,
                                        FlattenNode fn) {
        ImmutableSet<Variable> fnDefinedVars = fn.getLocallyDefinedVariables();
        return iqFactory.createConstructionNode(
                Stream.concat(
                        Stream.of(fn.getFlattenedVariable()),
                        parentCn.getLocallyRequiredVariables().stream()
                                .filter(v -> !fnDefinedVars.contains(v))
                ).collect(ImmutableCollectors.toSet()),
                substitutionFactory.getSubstitution(flattenedVarDef)
        );
    }

}
