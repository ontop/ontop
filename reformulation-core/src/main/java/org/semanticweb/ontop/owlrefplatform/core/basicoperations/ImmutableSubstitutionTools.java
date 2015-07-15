package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fj.P;
import fj.P2;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.ImmutabilityTools;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.Map;

import static org.semanticweb.ontop.model.impl.GroundTermTools.isGroundTerm;

/**
 * Tools for the new generation of (immutable) substitutions
 */
public class ImmutableSubstitutionTools {

    private static ImmutableSubstitution<ImmutableTerm> EMPTY_SUBSTITUTION = new NeutralSubstitution();


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

    /**
     * TODO: explain
     */
    public static ImmutableSubstitution<ImmutableTerm> convertSubstitution(Substitution substitution) {
        ImmutableMap.Builder<VariableImpl, ImmutableTerm> substitutionMapBuilder = ImmutableMap.builder();
        for (Map.Entry<VariableImpl, Term> entry : substitution.getMap().entrySet()) {
            ImmutableTerm immutableValue = ImmutabilityTools.convertIntoImmutableTerm(entry.getValue());

            substitutionMapBuilder.put(entry.getKey(), immutableValue);

        }
        return new ImmutableSubstitutionImpl<>(substitutionMapBuilder.build());
    }


    /**
     * Returns a substitution theta (if it exists) such as :
     *    theta(s) = t
     *
     * with
     *    s : source term
     *    t: target term
     *
     */
    public static Optional<ImmutableSubstitution<ImmutableTerm>> computeUnidirectionalSubstitution(ImmutableTerm sourceTerm,
                                                                                                   ImmutableTerm targetTerm) {
        /**
         * Variable
         */
        if (sourceTerm instanceof VariableImpl) {
            VariableImpl sourceVariable = (VariableImpl) sourceTerm;

            // Constraint
            if ((!sourceVariable.equals(targetTerm)) && targetTerm.getReferencedVariables().contains(sourceVariable)) {
                return Optional.absent();
            }

            ImmutableSubstitution<ImmutableTerm> substitution = new ImmutableSubstitutionImpl<>(
                    ImmutableMap.of(sourceVariable, targetTerm));
            return Optional.of(substitution);
        }
        /**
         * Functional term
         */
        else if (sourceTerm instanceof ImmutableFunctionalTerm) {
            if (targetTerm instanceof ImmutableFunctionalTerm) {
                return computeUnidirectionalSubstitutionOfFunctionalTerms((ImmutableFunctionalTerm) sourceTerm,
                        (ImmutableFunctionalTerm) targetTerm);
            }
            else {
                return Optional.absent();
            }
        }
        /**
         * Constant
         */
        else if(sourceTerm.equals(targetTerm)) {
            return Optional.of(EMPTY_SUBSTITUTION);
        }
        else {
            return Optional.absent();
        }
    }

    private static Optional<ImmutableSubstitution<ImmutableTerm>> computeUnidirectionalSubstitutionOfFunctionalTerms(
            ImmutableFunctionalTerm sourceFunctionalTerm, ImmutableFunctionalTerm targetFunctionalTerm) {

        /**
         * Function symbol equality
         */
        if (!sourceFunctionalTerm.getFunctionSymbol().equals(
                targetFunctionalTerm.getFunctionSymbol())) {
            return Optional.absent();
        }


        /**
         * Source is ground term
         */
        if (isGroundTerm(sourceFunctionalTerm)) {
            if (sourceFunctionalTerm.equals(targetFunctionalTerm)) {
                return Optional.of(EMPTY_SUBSTITUTION);
            }
            else {
                return Optional.absent();
            }
        }

        ImmutableList<ImmutableTerm> sourceChildren = sourceFunctionalTerm.getImmutableTerms();
        ImmutableList<ImmutableTerm> targetChildren = targetFunctionalTerm.getImmutableTerms();

        /**
         * Arity equality
         */
        int sourceArity = sourceChildren.size();
        if (sourceArity != targetChildren.size()) {
            return Optional.absent();
        }

        /**
         * Children
         */
        // Non-final
        ImmutableSubstitution<ImmutableTerm> unifier = EMPTY_SUBSTITUTION;
        for(int i=0; i < sourceArity ; i++) {

            /**
             * Recursive call
             */
            Optional<ImmutableSubstitution<ImmutableTerm>> optionalChildUnifier = computeUnidirectionalSubstitution(
                    sourceChildren.get(i), targetChildren.get(i));

            if (!optionalChildUnifier.isPresent())
                return Optional.absent();

            ImmutableSubstitution<ImmutableTerm> childUnifier = optionalChildUnifier.get();

            Optional<ImmutableSubstitution<ImmutableTerm>> optionalMergedUnifier = unifier.union(childUnifier);
            if (optionalMergedUnifier.isPresent()) {
                unifier = optionalMergedUnifier.get();
            }
            else {
                return Optional.absent();
            }
        }

        // Present optional
        return Optional.of(unifier);
    }

    /**
     * TODO: explain
     */
    public static ImmutableSubstitution<ImmutableTerm> renameSubstitution(final ImmutableSubstitution<ImmutableTerm> substitutionToRename,
                                                                          final ImmutableList<InjectiveVar2VarSubstitution> renamingSubstitutions) {

        // Non-final
        ImmutableSubstitution<ImmutableTerm> renamedSubstitution = substitutionToRename;
        for (InjectiveVar2VarSubstitution renamingSubstitution : renamingSubstitutions) {
            renamedSubstitution = renamingSubstitution.applyRenaming(renamedSubstitution);
        }

        return renamedSubstitution;
    }
}
