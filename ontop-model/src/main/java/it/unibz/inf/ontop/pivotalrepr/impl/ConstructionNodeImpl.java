package it.unibz.inf.ontop.pivotalrepr.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.model.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;
import static it.unibz.inf.ontop.pivotalrepr.SubstitutionResults.LocalAction.DECLARE_AS_EMPTY;
import static it.unibz.inf.ontop.pivotalrepr.impl.ConstructionNodeTools.computeNewProjectedVariables;
import static it.unibz.inf.ontop.pivotalrepr.impl.ConstructionNodeTools.extractRelevantDescendingSubstitution;

public class ConstructionNodeImpl extends QueryNodeImpl implements ConstructionNode {


    /**
     * TODO: find a better name
     */
    private static class NewSubstitutionPair {
        public final ImmutableSubstitution<ImmutableTerm> bindings;
        public final ImmutableSubstitution<? extends ImmutableTerm> propagatedSubstitution;

        private NewSubstitutionPair(ImmutableSubstitution<ImmutableTerm> bindings,
                                    ImmutableSubstitution<? extends ImmutableTerm> propagatedSubstitution) {
            this.bindings = bindings;
            this.propagatedSubstitution = propagatedSubstitution;
        }
    }




    private static Logger LOGGER = LoggerFactory.getLogger(ConstructionNodeImpl.class);
    private static int CONVERGENCE_BOUND = 5;

    private final Optional<ImmutableQueryModifiers> optionalModifiers;
    private final ImmutableSet<Variable> projectedVariables;
    private final ImmutableSubstitution<ImmutableTerm> substitution;

    private static final String CONSTRUCTION_NODE_STR = "CONSTRUCT";

    @AssistedInject
    private ConstructionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                                @Assisted ImmutableSubstitution<ImmutableTerm> substitution,
                                @Assisted Optional<ImmutableQueryModifiers> optionalQueryModifiers) {
        this.projectedVariables = projectedVariables;
        this.substitution = substitution;
        this.optionalModifiers = optionalQueryModifiers;
    }

    /**
     * Without modifiers nor substitution.
     */
    @AssistedInject
    private ConstructionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables) {
        this.projectedVariables = projectedVariables;
        this.substitution = DATA_FACTORY.getSubstitution();
        this.optionalModifiers = Optional.empty();
    }

    @AssistedInject
    private ConstructionNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                                 @Assisted ImmutableSubstitution<ImmutableTerm> substitution) {
        this.projectedVariables = projectedVariables;
        this.substitution = substitution;
        this.optionalModifiers = Optional.empty();
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> getSubstitution() {
        return substitution;
    }

    @Override
    public Optional<ImmutableQueryModifiers> getOptionalModifiers() {
        return optionalModifiers;
    }

    /**
     * Immutable fields, can be shared.
     */
    @Override
    public ConstructionNode clone() {
        return new ConstructionNodeImpl(projectedVariables, substitution, optionalModifiers);
    }

    @Override
    public ConstructionNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getChildVariables() {
        ImmutableSet<Variable> variableDefinedByBindings = substitution.getDomain();

        Stream<Variable> variablesRequiredByBindings = substitution.getImmutableMap().values().stream()
                .flatMap(t -> t.getVariableStream());

        //return only the variables that are also used in the bindings for the child of the construction node
        return Stream.concat(projectedVariables.stream(), variablesRequiredByBindings)
                .filter(v -> !variableDefinedByBindings.contains(v))
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        ImmutableSet.Builder<Variable> collectedVariableBuilder = ImmutableSet.builder();

        collectedVariableBuilder.addAll(projectedVariables);

        ImmutableMap<Variable, ImmutableTerm> substitutionMap = substitution.getImmutableMap();

        collectedVariableBuilder.addAll(substitutionMap.keySet());
        for (ImmutableTerm term : substitutionMap.values()) {
            if (term instanceof Variable) {
                collectedVariableBuilder.add((Variable)term);
            }
            else if (term instanceof ImmutableFunctionalTerm) {
                collectedVariableBuilder.addAll(((ImmutableFunctionalTerm)term).getVariables());
            }
        }

        return collectedVariableBuilder.build();
    }

    @Override
    public ImmutableSubstitution<ImmutableTerm> getDirectBindingSubstitution() {
        if (substitution.isEmpty())
            return substitution;

        // Non-final
        ImmutableSubstitution<ImmutableTerm> previousSubstitution;
        // Non-final
        ImmutableSubstitution<ImmutableTerm> newSubstitution = substitution;

        int i = 0;
        do {
            previousSubstitution = newSubstitution;
            newSubstitution = newSubstitution.composeWith(substitution);
            i++;
        } while ((i < CONVERGENCE_BOUND) && (!previousSubstitution.equals(newSubstitution)));

        if (i == CONVERGENCE_BOUND) {
            LOGGER.warn(substitution + " has not converged after " + CONVERGENCE_BOUND + " recursions over itself");
        }

        return newSubstitution;

    }

    /**
     * Creates a new ConstructionNode with a new substitution.
     * This substitution is obtained by composition and then cleaned (only defines the projected variables)
     *
     * Stops the propagation.
     */
    @Override
    public SubstitutionResults<ConstructionNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitutionToApply,
            QueryNode childNode, IntermediateQuery query) {

        ImmutableSubstitution<ImmutableTerm> localSubstitution = getSubstitution();
        ImmutableSet<Variable> boundVariables = localSubstitution.getImmutableMap().keySet();

        if (substitutionToApply.getImmutableMap().keySet().stream().anyMatch(boundVariables::contains)) {
            throw new IllegalArgumentException("An ascending substitution MUST NOT include variables bound by " +
                    "the substitution of the current construction node");
        }

        ImmutableSubstitution<ImmutableTerm> compositeSubstitution = substitutionToApply.composeWith(localSubstitution);

        /**
         * Cleans the composite substitution by removing non-projected variables
         */

        ImmutableMap.Builder<Variable, ImmutableTerm> newSubstitutionMapBuilder = ImmutableMap.builder();
        compositeSubstitution.getImmutableMap().entrySet().stream()
                .map(ConstructionNodeImpl::applyNullNormalization)
                .filter(e -> projectedVariables.contains(e.getKey()))
                .forEach(newSubstitutionMapBuilder::put);

        ImmutableSubstitution<ImmutableTerm> newSubstitution = DATA_FACTORY.getSubstitution(
                newSubstitutionMapBuilder.build());

        ConstructionNode newConstructionNode = new ConstructionNodeImpl(projectedVariables,
                newSubstitution, getOptionalModifiers());

        /**
         * Stops to propagate the substitution
         */
        return new SubstitutionResultsImpl<>(newConstructionNode);
    }

    /**
     * Most functional terms do not accept NULL as arguments. If this happens, they become NULL.
     */
    private static Map.Entry<Variable, ImmutableTerm> applyNullNormalization(
            Map.Entry<Variable, ImmutableTerm> substitutionEntry) {
        ImmutableTerm value = substitutionEntry.getValue();
        if (value instanceof ImmutableFunctionalTerm) {
            ImmutableTerm newValue = normalizeFunctionalTerm((ImmutableFunctionalTerm) value);
            return newValue.equals(value)
                    ? substitutionEntry
                    : new AbstractMap.SimpleEntry<>(substitutionEntry.getKey(), newValue);
        }
        return substitutionEntry;
    }

    private static ImmutableTerm normalizeFunctionalTerm(ImmutableFunctionalTerm functionalTerm) {
        if (isSupportingNullArguments(functionalTerm)) {
            return functionalTerm;
        }

        ImmutableList<ImmutableTerm> newArguments = functionalTerm.getArguments().stream()
                .map(arg -> (arg instanceof ImmutableFunctionalTerm)
                        ? normalizeFunctionalTerm((ImmutableFunctionalTerm) arg)
                        : arg)
                .collect(ImmutableCollectors.toList());
        if (newArguments.stream()
                .anyMatch(arg -> arg.equals(OBDAVocabulary.NULL))) {
            return OBDAVocabulary.NULL;
        }

        return DATA_FACTORY.getImmutableFunctionalTerm(functionalTerm.getFunctionSymbol(), newArguments);
    }

    /**
     * TODO: move it elsewhere
     */
    private static boolean isSupportingNullArguments(ImmutableFunctionalTerm functionalTerm) {
        Predicate functionSymbol = functionalTerm.getFunctionSymbol();
        if (functionSymbol instanceof ExpressionOperation) {
            switch((ExpressionOperation)functionSymbol) {
                case IS_NOT_NULL:
                case IS_NULL:
                    // TODO: add COALESCE, EXISTS, NOT EXISTS
                    return true;
                default:
                    return false;
            }
        }
        else if ((functionSymbol instanceof URITemplatePredicate)
                || (functionSymbol instanceof BNodePredicate)) {
            return false;
        }
        return true;
    }


    /**
     * TODO: explain
     */
    @Override
    public SubstitutionResults<ConstructionNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> descendingSubstitution, IntermediateQuery query) {

        ImmutableSubstitution<ImmutableTerm> relevantSubstitution = extractRelevantDescendingSubstitution(
                descendingSubstitution, projectedVariables);

        ImmutableSet<Variable> newProjectedVariables = computeNewProjectedVariables(relevantSubstitution,
                getVariables());

        /**
         * TODO: avoid using an exception
         */
        NewSubstitutionPair newSubstitutions;
        try {
            newSubstitutions = traverseConstructionNode(relevantSubstitution, substitution, projectedVariables,
                    newProjectedVariables);
        } catch (QueryNodeSubstitutionException e) {
            return new SubstitutionResultsImpl<>(DECLARE_AS_EMPTY);
        }

        ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate = newSubstitutions.propagatedSubstitution;

        Optional<ImmutableQueryModifiers> newOptionalModifiers = updateOptionalModifiers(optionalModifiers,
                descendingSubstitution, substitutionToPropagate);

        /**
         * The construction node is not needed anymore
         *
         * Currently, the root construction node is still required.
         */
        if (newSubstitutions.bindings.isEmpty() && !newOptionalModifiers.isPresent()) {
            if(query.getChildren(this).isEmpty()){
                return new SubstitutionResultsImpl<>(SubstitutionResults.LocalAction.DECLARE_AS_TRUE);
            }
            return new SubstitutionResultsImpl<>(SubstitutionResults.LocalAction.REPLACE_BY_CHILD, Optional.of(substitutionToPropagate));
        }

        /**
         * New construction node
         */
        else {
            ConstructionNode newConstructionNode = new ConstructionNodeImpl(newProjectedVariables,
                    newSubstitutions.bindings, newOptionalModifiers);

            return new SubstitutionResultsImpl<>(newConstructionNode, substitutionToPropagate);
        }
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return Optional.of(node)
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .filter(n -> n.getVariables().equals(projectedVariables))
                .filter(n -> n.getSubstitution().equals(substitution))
                .isPresent();
    }

    @Override
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {
        /**
         * A construction node has only one child
         */
        return new NodeTransformationProposalImpl(NodeTransformationProposedState.DECLARE_AS_EMPTY, projectedVariables);
    }

    @Override
    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueNode) {
        if (this.getVariables().isEmpty()){
           return new NodeTransformationProposalImpl(NodeTransformationProposedState.DECLARE_AS_TRUE, ImmutableSet.of());
        }
       return new NodeTransformationProposalImpl(NodeTransformationProposedState.NO_LOCAL_CHANGE, ImmutableSet.of());
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        // TODO: display the query modifiers
        return CONSTRUCTION_NODE_STR + " " + projectedVariables + " " + "[" + substitution + "]" ;
    }

    /**
     *
     * TODO: explain
     *
     */
    private NewSubstitutionPair traverseConstructionNode(
            ImmutableSubstitution<? extends ImmutableTerm> tau,
            ImmutableSubstitution<? extends ImmutableTerm> formerTheta,
            ImmutableSet<Variable> formerV, ImmutableSet<Variable> newV) throws QueryNodeSubstitutionException {

        Var2VarSubstitution tauR = tau.getVar2VarFragment();
        // Non-variable term
        ImmutableSubstitution<NonVariableTerm> tauO = extractTauO(tau);

        Var2VarSubstitution tauEq = extractTauEq(tauR);

        ImmutableSubstitution<? extends ImmutableTerm> tauC = tauO.unionHeterogeneous(tauEq)
                .orElseThrow(() -> new IllegalStateException("Bug: dom(tauG) must be disjoint with dom(tauEq)"));


        ImmutableSubstitution<ImmutableTerm> eta = ImmutableUnificationTools.computeMGUS(formerTheta, tauC)
                .orElseThrow(() -> new QueryNodeSubstitutionException("The descending substitution " + tau
                        + " is incompatible with " + this));

        ImmutableSubstitution<ImmutableTerm> etaB = extractEtaB(eta, formerV, newV, tauC);

        ImmutableSubstitution<ImmutableTerm> newTheta = tauR.applyToSubstitution(etaB)
                .orElseThrow(() -> new IllegalStateException("Bug: tauR does not rename etaB safely as excepted"));

        ImmutableSubstitution<? extends ImmutableTerm> delta = computeDelta(formerTheta, newTheta, eta, tauR, tauEq);

        return new NewSubstitutionPair(newTheta, delta);
    }

    private static ImmutableSubstitution<NonVariableTerm> extractTauO(ImmutableSubstitution<? extends ImmutableTerm> tau) {
        ImmutableMap<Variable, NonVariableTerm> newMap = tau.getImmutableMap().entrySet().stream()
                .filter(e -> e.getValue() instanceof NonVariableTerm)
                .map(e -> (Map.Entry<Variable, NonVariableTerm>) e)
                .collect(ImmutableCollectors.toMap());

        return DATA_FACTORY.getSubstitution(newMap);
    }

    /**
     * TODO: explain
     */
    private static Var2VarSubstitution extractTauEq(Var2VarSubstitution tauR) {
        int domainVariableCount = tauR.getDomain().size();
        if (domainVariableCount <= 1) {
            return DATA_FACTORY.getVar2VarSubstitution(ImmutableMap.of());
        }

        ImmutableMultimap<Variable, Variable> inverseMultimap = tauR.getImmutableMap().entrySet().stream()
                // Inverse
                .map(e -> (Map.Entry<Variable, Variable>) new AbstractMap.SimpleImmutableEntry<>(e.getValue(), e.getKey()))
                .collect(ImmutableCollectors.toMultimap());

        ImmutableMap<Variable, Variable> newMap = inverseMultimap.asMap().values().stream()
                // TODO: explain
                .filter(vars -> vars.size() >= 1)
                //
                .flatMap(vars -> {
                    List<Variable> sortedVariables = vars.stream()
                            .sorted()
                            .collect(Collectors.toList());
                    Variable largerVariable = sortedVariables.get(sortedVariables.size() - 1);
                    return sortedVariables.stream()
                            .limit(sortedVariables.size() - 1)
                            .map(v -> (Map.Entry<Variable, Variable>) new AbstractMap.SimpleEntry<>(v, largerVariable));
                })
                .collect(ImmutableCollectors.toMap());

        return DATA_FACTORY.getVar2VarSubstitution(newMap);
    }

    private static ImmutableSubstitution<ImmutableTerm> extractEtaB(ImmutableSubstitution<ImmutableTerm> eta,
                                                                    ImmutableSet<Variable> formerV,
                                                                    ImmutableSet<Variable> newV,
                                                                    ImmutableSubstitution<? extends ImmutableTerm> tauC) {

        ImmutableSet<Variable> tauCDomain = tauC.getDomain();

        ImmutableMap<Variable, ImmutableTerm> newMap = eta.getImmutableMap().entrySet().stream()
                .filter(e -> formerV.contains(e.getKey()) || newV.contains(e.getKey()))
                .filter(e -> !tauCDomain.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap());

        return DATA_FACTORY.getSubstitution(newMap);
    }

    private static ImmutableSubstitution<? extends ImmutableTerm> computeDelta(
            ImmutableSubstitution<? extends ImmutableTerm> formerTheta,
            ImmutableSubstitution<? extends ImmutableTerm> newTheta,
            ImmutableSubstitution<ImmutableTerm> eta, Var2VarSubstitution tauR,
            Var2VarSubstitution tauEq) {

        ImmutableSet<Map.Entry<Variable, Variable>> tauEqEntries = tauEq.getImmutableMap().entrySet();
        ImmutableSet<Variable> formerThetaDomain = formerTheta.getDomain();

        ImmutableMap<Variable, ImmutableTerm> newMap = Stream.concat(
                eta.getImmutableMap().entrySet().stream(),
                tauR.getImmutableMap().entrySet().stream())
                .filter(e -> !tauEqEntries.contains(e))
                .filter(e -> !formerThetaDomain.contains(e.getKey()))
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), newTheta.apply(e.getValue())))
                .distinct()
                .collect(ImmutableCollectors.toMap());

        return DATA_FACTORY.getSubstitution(newMap);
    }

    /**
     * TODO: explain
     */
    private static Optional<ImmutableQueryModifiers> updateOptionalModifiers(
            Optional<ImmutableQueryModifiers> optionalModifiers,
            ImmutableSubstitution<? extends ImmutableTerm> substitution1,
            ImmutableSubstitution<? extends ImmutableTerm> substitution2) {
        if (!optionalModifiers.isPresent()) {
            return Optional.empty();
        }
        ImmutableQueryModifiers previousModifiers = optionalModifiers.get();
        ImmutableList.Builder<OrderCondition> conditionBuilder = ImmutableList.builder();

        for (OrderCondition condition : previousModifiers.getSortConditions()) {
            ImmutableTerm newTerm = substitution2.apply(substitution1.apply(condition.getVariable()));
            /**
             * If after applying the substitution the term is still a variable,
             * "updates" the OrderCondition.
             *
             * Otherwise, forgets it.
             */
            if (newTerm instanceof Variable) {
                conditionBuilder.add(condition.newVariable((Variable) newTerm));
            }
        }
        return previousModifiers.newSortConditions(conditionBuilder.build());
    }

}
