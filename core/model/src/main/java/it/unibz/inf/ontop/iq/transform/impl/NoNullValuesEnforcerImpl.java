package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;


public class NoNullValuesEnforcerImpl implements NoNullValueEnforcer {

    private final IntermediateQueryFactory iQFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final CoreUtilsFactory coreUtilsFactory;

    @Inject
    private NoNullValuesEnforcerImpl(IntermediateQueryFactory iQFactory, TermFactory termFactory,
                                     SubstitutionFactory substitutionFactory, CoreUtilsFactory coreUtilsFactory) {
        this.iQFactory = iQFactory;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.coreUtilsFactory = coreUtilsFactory;
    }

    @Override
    public IQTree transform(IQTree tree) {
        return transform(tree, coreUtilsFactory.createVariableGenerator(tree.getKnownVariables()));
    }

    private IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        Optional<ImmutableExpression> condition = termFactory.getDBIsNotNull(tree.getVariables().stream());

        return condition
                .map(iQFactory::createFilterNode)
                .map(n -> iQFactory.createUnaryIQTree(n, tree))
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
     * In a bottom-up manner, NULLIF(b,0) would instead require to known that "b" is non-null *and different from 0*
     *  to simplify itself. Such information is only partially provided by the VariableNullability data structure.
     */
    protected IQTree declareTopVariablesNotNull(IQTree tree) {
        NotNullTopVariablePropagator transformer = new NotNullTopVariablePropagator(iQFactory, substitutionFactory, tree.getVariables());
        return transformer.transform(tree);
    }

    protected static class NotNullTopVariablePropagator extends DefaultNonRecursiveIQTreeTransformer {

        protected final IntermediateQueryFactory iqFactory;
        protected final ImmutableSet<Variable> nonNullVariables;
        private final SubstitutionFactory substitutionFactory;

        protected NotNullTopVariablePropagator(IntermediateQueryFactory iqFactory,
                                               SubstitutionFactory substitutionFactory,
                                               ImmutableSet<Variable> nonNullVariables) {
            this.iqFactory = iqFactory;
            this.substitutionFactory = substitutionFactory;
            this.nonNullVariables = nonNullVariables;
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
            ImmutableSubstitution<ImmutableTerm> initialSubstitution = rootNode.getSubstitution();

            ImmutableMap<Variable, FunctionalTermSimplification> updatedEntryMap = initialSubstitution.getFragment(ImmutableFunctionalTerm.class).getImmutableMap().entrySet().stream()
                    .filter(e -> nonNullVariables.contains(e.getKey()))
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().simplifyAsGuaranteedToBeNonNull()
                    ));

            ImmutableMap<Variable, ImmutableTerm> newSubstitutionMap = initialSubstitution.getImmutableMap().entrySet().stream()
                    .map(e -> Optional.ofNullable(updatedEntryMap.get(e.getKey()))
                            .map(s -> Maps.immutableEntry(e.getKey(), s.getSimplifiedTerm()))
                            .orElse(e))
                    .collect(ImmutableCollectors.toMap());

            ConstructionNode newConstructionNode = initialSubstitution.getImmutableMap().equals(newSubstitutionMap)
                    ? rootNode
                    : iqFactory.createConstructionNode(rootNode.getVariables(), substitutionFactory.getSubstitution(newSubstitutionMap));

            ImmutableSet<Variable> simplifiableChildVariables = Sets.union(
                    Sets.difference(rootNode.getVariables(), initialSubstitution.getDomain()),
                    updatedEntryMap.values().stream()
                            .flatMap(s -> s.getSimplifiableVariables().stream())
                            .collect(ImmutableCollectors.toSet()))
                    .immutableCopy();

            // "Recursive"
            IQTree newChild = simplifiableChildVariables.isEmpty()
                    ? child
                    : (new NotNullTopVariablePropagator(iqFactory, substitutionFactory, simplifiableChildVariables)).transform(child);

            if (newConstructionNode == rootNode && newChild == child)
                return tree;

            return iqFactory.createUnaryIQTree(newConstructionNode, newChild);
        }

        /**
         * Propagates
         */
        @Override
        public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
            IQTree newChild = this.transform(child);
            return newChild.equals(child)
                    ? tree
                    : iqFactory.createUnaryIQTree(rootNode, newChild);
        }
    }


}
