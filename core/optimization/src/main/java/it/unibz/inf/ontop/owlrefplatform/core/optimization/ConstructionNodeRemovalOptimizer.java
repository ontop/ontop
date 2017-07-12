package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ImmutableQueryModifiers;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.ConstructionNodeRemovalProposal;
import it.unibz.inf.ontop.iq.proposal.impl.ConstructionNodeRemovalProposalImpl;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATA_FACTORY;


/**
 * Gets rid of unnecessary ConstructionNodes.
 * <p>
 * The algorithm searches the query q depth-first.
 * <p>
 * When a ConstructionNode is c_1 encountered,
 * find the highest value i such that:
 * .c_1, .., c_n is a chain composed of ConstructionNodes only,
 * with c1 as root,
 * c2 as child of c_1,
 * c3 child of c_2,
 * etc., and
 * .c_2, .., c_i do not carry query modifiers, and
 * .for each 0 < j < i,
 * c_j or c_{j+1} has a trivial (identity) substitution,
 * or a substitution of variables names only.
 * <p>
 * Note that n = 1 may hold.
 * <p>
 * Then proceed as follows:
 * .Let t be the subtree rooted in the child of c_n
 * .Compute the appropriate composition s of the substitutions of all c_i
 * .If s is trivial (identity),
 * and if c1 is not the root node,
 * replace in q the subtree rooted in c1 by t
 * .Otherwise create the construction node c' with substitution s,
 * and replace in q the subtree rooted in c1 by the subtree rooted in c',
 * such that the child subtree of c' is t.
 * <p>
 * TODO: make it more robust (handle complex substitutions, modifiers) ?
 */
public class ConstructionNodeRemovalOptimizer extends NodeCentricDepthFirstOptimizer<ConstructionNodeRemovalProposal> {


    public ConstructionNodeRemovalOptimizer() {
        super(false);
    }

    @Override
    protected Optional<ConstructionNodeRemovalProposal> evaluateNode(QueryNode node, IntermediateQuery query) {
        if (node instanceof ConstructionNode) {
            ConstructionNode castNode = (ConstructionNode) node;
            return makeProposal(query, castNode, castNode.getSubstitution(), castNode, query.getFirstChild(castNode).get());
        }
        return Optional.empty();
    }

    private Optional<ConstructionNodeRemovalProposal> makeProposal(IntermediateQuery query,
                                                                   ConstructionNode constructionNodeChainRoot,
                                                                   ImmutableSubstitution substitution,
                                                                   ConstructionNode currentParentNode,
                                                                   QueryNode currentChildNode) {

        if (isCandidateForMerging(currentChildNode)) {
            ConstructionNode castChild = (ConstructionNode) currentChildNode;
            Optional<ImmutableSubstitution> substitutionComposition = composeSubstitutions(
                    substitution,
                    castChild.getSubstitution()
            );
            if (substitutionComposition.isPresent()) {
                return makeProposal(
                        query,
                        constructionNodeChainRoot,
                        substitutionComposition.get(),
                        castChild,
                        query.getFirstChild(castChild).get()
                );
            }
        }

        /** Non-mergeable, or substitution composition is not supported **/

        boolean deleteConstructionNodeChain = substitution.getImmutableMap().isEmpty() &&
                isCandidateForMerging(constructionNodeChainRoot) &&
                !constructionNodeChainRoot.equals(query.getRootConstructionNode());

        /* special case of a non-deletable unary chain */
        if (currentParentNode.equals(constructionNodeChainRoot) && !deleteConstructionNodeChain) {
            return Optional.empty();
        }
        return Optional.of(
                new ConstructionNodeRemovalProposalImpl(
                        constructionNodeChainRoot,
                        substitution,
                        currentChildNode,
                        deleteConstructionNodeChain
                ));
    }

    private boolean isCandidateForMerging(QueryNode currentChildNode) {
        if (currentChildNode instanceof ConstructionNode) {
            Optional<ImmutableQueryModifiers> optionalModifiers = ((ConstructionNode) currentChildNode)
                    .getOptionalModifiers();
            if (optionalModifiers.isPresent()) {
                ImmutableQueryModifiers modifiers = optionalModifiers.get();
                return !modifiers.hasLimit() &&
                        !modifiers.hasOffset() &&
                        !modifiers.hasOrder() &&
                        !modifiers.isDistinct();
            }
            return true;
        }
        return false;
    }


    //TODO: make more robust (only covers the case where at least one substitution is Var2Var)
    private Optional<ImmutableSubstitution> composeSubstitutions(ImmutableSubstitution parentSubstitution,
                                                                 ImmutableSubstitution childNodeSubstitution) {

        if (isVariableSubstitution(childNodeSubstitution)) {
            return Optional.of(
                    applySubstitutionToRange(
                            childNodeSubstitution,
                            parentSubstitution
                    ));
        }

        Optional<ImmutableMultimap<Variable, Variable>> inverseVar2VarMultimap = getInverseVar2VarMultiMap(
                parentSubstitution.getImmutableMap());
        if (inverseVar2VarMultimap.isPresent()) {
            return Optional.of(
                    applyInverseSubstitutionToDomain(
                            inverseVar2VarMultimap.get(),
                            childNodeSubstitution
                    ));
        }
        return Optional.empty();
    }

    private Optional<ImmutableMultimap<Variable, Variable>> getInverseVar2VarMultiMap(ImmutableMap<Variable, ImmutableTerm> inputMap) {
        ImmutableMultimap.Builder<Variable, Variable> multimap = ImmutableMultimap.builder();
        for (ImmutableMap.Entry<Variable, ImmutableTerm> entry : inputMap.entrySet()) {
            if (entry.getValue() instanceof Variable) {
                multimap.put((Variable) entry.getValue(), entry.getKey());
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(multimap.build());
    }

    private boolean isVariableSubstitution(ImmutableSubstitution<ImmutableTerm> substitution) {
        return substitution.getImmutableMap().values().stream()
                .allMatch(t -> t instanceof Variable);
    }

    private ImmutableSubstitution applySubstitutionToRange(ImmutableSubstitution appliedSubstitution,
                                                           ImmutableSubstitution targetSubstitution) {

        ImmutableMap<Variable, ImmutableTerm> targetSubstitutionMap = targetSubstitution.getImmutableMap();
        return DATA_FACTORY.getSubstitution(
                targetSubstitutionMap.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                e -> e.getKey(),
                                e -> appliedSubstitution.apply(e.getValue())
                        )));
    }

    private ImmutableSubstitution applyInverseSubstitutionToDomain(ImmutableMultimap<Variable, Variable> appliedSubstitution,
                                                                   ImmutableSubstitution targetSubstitution) {

        ImmutableMap<Variable, ImmutableTerm> targetSubstitutionMap = targetSubstitution.getImmutableMap();
        return DATA_FACTORY.getSubstitution(
                targetSubstitutionMap.entrySet().stream()
                        .flatMap(e -> applyInverseSubstitutionToVariable(
                                appliedSubstitution.get(e.getKey()),
                                e.getValue()
                        ))
                        .collect(ImmutableCollectors.toMap())
        );
    }

    private Stream<ImmutableMap.Entry<Variable, ImmutableTerm>> applyInverseSubstitutionToVariable(Collection<Variable> keys, ImmutableTerm value) {
        return keys.stream()
                .map(k -> new AbstractMap.SimpleImmutableEntry<>(k, value));
    }
}
