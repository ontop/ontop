package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.exception.QueryNodeSubstitutionException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.stream.Stream;

/**
 * TODO: explain
 */
@Singleton
public class ConstructionNodeTools {

    private final ImmutableUnificationTools unificationTools;
    private final ImmutableSubstitutionTools substitutionTools;

    @Inject
    private ConstructionNodeTools(ImmutableUnificationTools unificationTools,
                                  ImmutableSubstitutionTools substitutionTools) {
        this.unificationTools = unificationTools;
        this.substitutionTools = substitutionTools;
    }

    public ImmutableSet<Variable> computeNewProjectedVariables(
            ImmutableSubstitution<? extends ImmutableTerm> descendingSubstitution, ImmutableSet<Variable> projectedVariables) {
        ImmutableSet<Variable> tauDomain = descendingSubstitution.getDomain();

        Stream<Variable> remainingVariableStream = projectedVariables.stream()
                .filter(v -> !tauDomain.contains(v));

        Stream<Variable> newVariableStream = descendingSubstitution.getImmutableMap().entrySet().stream()
                .filter(e -> projectedVariables.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(ImmutableTerm::getVariableStream);

        return Stream.concat(newVariableStream, remainingVariableStream)
                .collect(ImmutableCollectors.toSet());
    }

    /**
     *
     * TODO: explain
     *
     */
    public NewSubstitutionPair traverseConstructionNode(
            ImmutableSubstitution<? extends ImmutableTerm> tau,
            ImmutableSubstitution<? extends ImmutableTerm> formerTheta,
            ImmutableSet<Variable> formerV, ImmutableSet<Variable> newV) throws QueryNodeSubstitutionException {

        ImmutableSubstitution<ImmutableTerm> eta = unificationTools.computeMGUS(formerTheta, tau)
                .orElseThrow(() -> new QueryNodeSubstitutionException("The descending substitution " + tau
                        + " is incompatible with " + formerTheta));
        /*
         * Normalizes eta so as to avoid projected variables to be substituted by non-projected variables.
         *
         * This normalization can be understood as a way to select a MGU (eta) among a set of equivalent MGUs.
         * Such a "selection" is done a posteriori.
         *
         * Due to the current implementation of MGUS, the normalization should have no effect
         * (already in a normal form). Here for safety.
         */
        ImmutableSubstitution<ImmutableTerm> normalizedEta = substitutionTools.prioritizeRenaming(eta, newV);

        ImmutableSubstitution<ImmutableTerm> newTheta = normalizedEta.filter(newV::contains);

        ImmutableSubstitution<ImmutableTerm> delta = normalizedEta
                .filter(k -> !formerTheta.isDefining(k) && (!newTheta.isDefining(k) || formerV.contains(k)));

        return new NewSubstitutionPair(newTheta, delta);
    }


    /**
     * TODO: find a better name
     */
    public static class NewSubstitutionPair {
        public final ImmutableSubstitution<ImmutableTerm> bindings;
        public final ImmutableSubstitution<ImmutableTerm> propagatedSubstitution;

        private NewSubstitutionPair(ImmutableSubstitution<ImmutableTerm> bindings,
                                    ImmutableSubstitution<ImmutableTerm> propagatedSubstitution) {
            this.bindings = bindings;
            this.propagatedSubstitution = propagatedSubstitution;
        }
    }


}
