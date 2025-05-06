package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.Set;

/**
 * Adds NOT NULL filters to IQs in MappingAssertion (which is essentially a CQ).
 */

public class NoNullValuesEnforcerImpl implements NoNullValueEnforcer {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final CoreUtilsFactory coreUtilsFactory;

    @Inject
    private NoNullValuesEnforcerImpl(IntermediateQueryFactory iqFactory,
                                     TermFactory termFactory,
                                     CoreUtilsFactory coreUtilsFactory) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.coreUtilsFactory = coreUtilsFactory;
    }

    @Override
    public IQTree transform(IQTree tree) {
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(tree.getKnownVariables());
        Optional<ImmutableExpression> condition = termFactory.getDBIsNotNull(tree.getVariables().stream());

        return condition
                .map(iqFactory::createFilterNode)
                .map(n -> iqFactory.createUnaryIQTree(n, tree))
                .map(t -> t.normalizeForOptimization(variableGenerator))
                .map(this::declareTopVariablesNotNull)
                .orElse(tree);
    }

    /**
     * Now that the filter has been inserted, we have the guarantee that the top variables are not nullable.
     *
     * Few functions like NULLIF can be simplified when they are declared as not able to produce a NULL.
     * Not that such a simplification cannot always be performed using VariableNullability information (insufficient for NULLIF).
     *
     * For instance
     * CONSTRUCT s,p,o s/RDF(a,IRI), p/RDF("http://ex.org/p", IRI), o/RDF(NULLIF(b, 0),xsd:integer)
     *   T1(a,b)
     *
     *  becoming after inserting and pushing down the filter
     *  CONSTRUCT s,p,o s/RDF(a,IRI), p/RDF("http://ex.org/p", IRI), o/RDF(NULLIF(b, 0),xsd:integer)
     *      FILTER NOT(NON_STRICT_EQ(b, 0))
     *        T1(a,b)
     *
     *  Can be simplified as (by declaring "o" as non-null)
     * CONSTRUCT s,p,o s/RDF(a,IRI), p/RDF("http://ex.org/p", IRI), o/RDF(b,xsd:integer)
     *      FILTER NOT(NON_STRICT_EQ(b, 0))
     *        T1(a,b)
     *
     * DESIGN NOTE:
     * In a bottom-up manner, NULLIF(b,0) would instead require to know that "b" is non-null *and different from 0*
     *  to simplify itself. Such information is only partially provided by the VariableNullability data structure.
     */
    protected IQTree declareTopVariablesNotNull(IQTree tree) {
        NotNullTopVariablePropagator transformer = new NotNullTopVariablePropagator(tree.getVariables());
        return transformer.transform(tree);
    }

    protected class NotNullTopVariablePropagator extends DefaultNonRecursiveIQTreeTransformer {

        protected final ImmutableSet<Variable> nonNullVariables;

        protected NotNullTopVariablePropagator(ImmutableSet<Variable> nonNullVariables) {
            this.nonNullVariables = nonNullVariables;
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            Substitution<ImmutableTerm> initialSubstitution = rootNode.getSubstitution();

            ImmutableMap<Variable, FunctionalTermSimplification> updatedEntryMap = initialSubstitution.builder()
                    .restrictDomainTo(nonNullVariables)
                    .restrictRangeTo(ImmutableFunctionalTerm.class)
                    .toMap((v, t) -> t.simplifyAsGuaranteedToBeNonNull());

            Substitution<ImmutableTerm> newSubstitution = initialSubstitution.builder()
                    .transformOrRetain(updatedEntryMap::get, (t, u) -> u.getSimplifiedTerm())
                    .build();

            ConstructionNode newConstructionNode = initialSubstitution.equals(newSubstitution)
                    ? rootNode
                    : iqFactory.createConstructionNode(rootNode.getVariables(), newSubstitution);

            Set<Variable> simplifiableChildVariables = Sets.union(
                    Sets.difference(rootNode.getVariables(), initialSubstitution.getDomain()),
                    updatedEntryMap.values().stream()
                            .flatMap(s -> s.getSimplifiableVariables().stream())
                            .collect(ImmutableCollectors.toSet()));

            IQTree newChild = simplifiableChildVariables.isEmpty()
                    ? child
                    : declareTopVariablesNotNull(child);  // "Recursive"

            return lazyCreateUnaryIQTree(tree, newConstructionNode, newChild);
        }

        /**
         * Propagates
         */
        @Override
        public IQTree transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree child) {
            IQTree newChild = transformChild(child);
            return lazyCreateUnaryIQTree(tree, node, newChild);
        }

        private IQTree lazyCreateUnaryIQTree(UnaryIQTree tree, UnaryOperatorNode node, IQTree child) {
            return (node == tree.getRootNode() && child == tree.getChild())
                    ? tree
                    : iqFactory.createUnaryIQTree(node, child);
        }
    }
}
