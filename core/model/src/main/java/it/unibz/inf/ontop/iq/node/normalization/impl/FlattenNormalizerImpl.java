package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
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
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;


public class FlattenNormalizerImpl implements FlattenNormalizer {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private FlattenNormalizerImpl(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public IQTree normalizeForOptimization(FlattenNode flattenNode, IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        IQTree normalizedChild = child.normalizeForOptimization(variableGenerator);
        QueryNode newChildNode = normalizedChild.getRootNode();

        // Among the trees that are created, the outermost (aka output) tree is declared as normalized.
        // The other ones (if any) are normalized immediately after they are created.
        IQTreeCache outputTreeCache = treeCache.declareAsNormalizedForOptimizationWithoutEffect();

        if (newChildNode instanceof ConstructionNode) {
            ConstructionNode cn = (ConstructionNode) newChildNode;

            /*
             * Let c be the root of the child tree before lift, of the form CONSTRUCT[V, S].
             * Let f be the flattened variable.
             * Let o be the output variable of flattening.
             * Let p be the (optional) index variable of flattening.
             * <p>
             * We split S into S_f and S', where S_f contains:
             *    - the definition of f
             *    - variable definitions that depend on f.
             * Note that these two cases are exclusive.
             * Note also that S_f may be empty.
             *
             * If S' is nonempty, then we lift it above the flatten node,
             * i.e. we create a parent CONSTRUCT[V',S'],
             * where
             *   V' = (V minus {f}) union {o,i}
             */
            Variable flattenedVar = flattenNode.getFlattenedVariable();

            Substitution<ImmutableTerm> flattenedVarSubstitution = cn.getSubstitution().builder()
                    .restrict((v, t) -> v.equals(flattenedVar) || t.getVariableStream().anyMatch(tv -> tv.equals(flattenedVar)))
                    .build();

            Substitution<ImmutableTerm> primeSubstitution = cn.getSubstitution()
                    .removeFromDomain(flattenedVarSubstitution.getDomain());

            // Nothing can be lifted, declare the new tree normalized
            if (primeSubstitution.isEmpty()) {
                return iqFactory.createUnaryIQTree(flattenNode, normalizedChild, outputTreeCache);
            }

            ConstructionNode newParentCn = iqFactory.createConstructionNode(
                    Sets.union(
                            flattenNode.getLocallyDefinedVariables(),
                            Sets.difference(cn.getVariables(), ImmutableSet.of(flattenedVar))
                    ).immutableCopy(),
                    primeSubstitution);

            IQTree grandChild = normalizedChild.getChildren().get(0);
            IQTree updatedChild = flattenedVarSubstitution.isEmpty()
                ? grandChild
                : iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(
                                Sets.union(
                                        ImmutableSet.of(flattenedVar),
                                        Sets.difference(newParentCn.getLocallyRequiredVariables(), flattenNode.getLocallyDefinedVariables())
                                ).immutableCopy(),
                                flattenedVarSubstitution),
                        grandChild
                ).normalizeForOptimization(variableGenerator);

            return iqFactory.createUnaryIQTree(
                    newParentCn,
                    iqFactory.createUnaryIQTree(flattenNode, updatedChild).normalizeForOptimization(variableGenerator),
                    outputTreeCache);
        }

        // Nothing can be lifted, declare the new tree normalized
        return iqFactory.createUnaryIQTree(flattenNode, normalizedChild, outputTreeCache);
    }

}
