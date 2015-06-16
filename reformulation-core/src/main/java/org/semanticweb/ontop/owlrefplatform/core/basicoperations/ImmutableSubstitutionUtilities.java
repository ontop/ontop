package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.collect.ImmutableMap;
import fj.P;
import fj.P2;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.Map;

/**
 * Utilities for the new generation of (immutable) substitutions
 */
public class ImmutableSubstitutionUtilities {

    /**
     * Extracts the sub-set of the substitution entries that are var-to-var mappings.
     */
    public static Var2VarSubstitution extractVar2VarSubstitution(Substitution substitution) {
        /**
         * Saves an unnecessary computation.
         */
        if (substitution instanceof Var2VarSubstitution)
            return (Var2VarSubstitution) substitution;

        ImmutableMap.Builder<VariableImpl, VariableImpl> substitutionMapBuilder = ImmutableMap.builder();

        for (Map.Entry<VariableImpl, Term> entry : substitution.getMap().entrySet()) {
            Term target = entry.getValue();
            if (target instanceof VariableImpl) {
                substitutionMapBuilder.put(entry.getKey(), (VariableImpl) target);
            }
        }
        return new Var2VarSubstitutionImpl(substitutionMapBuilder.build());
    }

    /**
     * Splits the substitution into two substitutions:
     *         (i) One without functional term
     *         (ii) One containing the rest
     */
    public static P2<ImmutableSubstitution<NonFunctionalTerm>, ImmutableSubstitution<ImmutableFunctionalTerm>> splitFunctionFreeSubstitution(
            ImmutableSubstitution substitution) {

        ImmutableMap.Builder<VariableImpl, NonFunctionalTerm> functionFreeMapBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<VariableImpl, ImmutableFunctionalTerm> otherMapBuilder = ImmutableMap.builder();

        for (Map.Entry<VariableImpl, Term> entry : substitution.getMap().entrySet()) {
            Term target = entry.getValue();
            if (target instanceof NonFunctionalTerm) {
                functionFreeMapBuilder.put(entry.getKey(), (NonFunctionalTerm) target);
            } else if (target instanceof ImmutableFunctionalTerm) {
                otherMapBuilder.put(entry.getKey(), (ImmutableFunctionalTerm) target);
            }
            else {
                throw new IllegalArgumentException("Unknown type of term detected in the substitution: "
                        + target.getClass());
            }
        }

        ImmutableSubstitution<NonFunctionalTerm> functionFreeSubstitution = new ImmutableSubstitutionImpl<>(
                functionFreeMapBuilder.build());

        // TODO: consider adding typing to the ImmutableSubstitutionImpl.
        ImmutableSubstitution<ImmutableFunctionalTerm> otherSubstitution = new ImmutableSubstitutionImpl<>(otherMapBuilder.build());

        return P.p(functionFreeSubstitution, otherSubstitution);
    }


}
