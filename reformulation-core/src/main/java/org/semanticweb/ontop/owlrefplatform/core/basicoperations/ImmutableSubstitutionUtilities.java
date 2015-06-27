package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fj.P;
import fj.P2;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.ImmutabilityTools;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.model.impl.AtomPredicateImpl;

import java.util.Map;

/**
 * Utilities for the new generation of (immutable) substitutions
 */
public class ImmutableSubstitutionUtilities {

    private static String PREDICATE_STR = "pred";


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
     * TODO: explain
     *
     */
    public static Optional<ImmutableSubstitution<ImmutableTerm>> computeMGU(ImmutableFunctionalTerm term1, ImmutableFunctionalTerm term2) {
        Substitution mutableSubstitution = UnifierUtilities.getMGU(term1, term2);

        if (mutableSubstitution == null) {
            return Optional.absent();
        }
        return Optional.of(convertSubstitution(mutableSubstitution));
    }

    /**
     * TODO: explain
     *
     */
    public static Optional<ImmutableSubstitution<ImmutableTerm>> computeMGUU(ImmutableSubstitution<? extends ImmutableTerm> substitution1,
                                                                             ImmutableSubstitution<? extends ImmutableTerm> substitution2) {

        ImmutableList.Builder<ImmutableTerm> firstArgListBuilder = ImmutableList.builder();
        ImmutableList.Builder<ImmutableTerm> secondArgListBuilder = ImmutableList.builder();

        for (Map.Entry<VariableImpl, ? extends ImmutableTerm> entry : substitution1.getImmutableMap().entrySet()) {
            firstArgListBuilder.add(entry.getKey());
            secondArgListBuilder.add(entry.getValue());
        }

        for (Map.Entry<VariableImpl, ? extends ImmutableTerm> entry : substitution2.getImmutableMap().entrySet()) {
            firstArgListBuilder.add(entry.getKey());
            secondArgListBuilder.add(entry.getValue());
        }

        ImmutableList<ImmutableTerm> firstArgList = firstArgListBuilder.build();
        ImmutableList<ImmutableTerm> secondArgList = secondArgListBuilder.build();

        OBDADataFactory factory = OBDADataFactoryImpl.getInstance();
        Predicate predicate = new AtomPredicateImpl(PREDICATE_STR, firstArgList.size());

        ImmutableFunctionalTerm functionalTerm1 = factory.getImmutableFunctionalTerm(predicate, firstArgList);
        ImmutableFunctionalTerm functionalTerm2 = factory.getImmutableFunctionalTerm(predicate, secondArgList);

        return computeMGU(functionalTerm1, functionalTerm2);
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
