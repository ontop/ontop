package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.VariableImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Common abstract class for ImmutableSubstitutionImpl and Var2VarSubstitutionImpl
 */
public abstract class AbstractImmutableSubstitutionImpl<T  extends ImmutableTerm> extends LocallyImmutableSubstitutionImpl
        implements ImmutableSubstitution<T> {

    /**
     *
     * TODO: what about anonymous variables?
     */
    @Override
    public ImmutableTerm apply(ImmutableTerm term) {
        if (term instanceof Constant) {
            return term;
        }
        else if (term instanceof VariableImpl) {
            return applyToVariable((VariableImpl) term);
        }
        else if (term instanceof ImmutableFunctionalTerm) {
            return applyToFunctionalTerm((ImmutableFunctionalTerm) term);
        }
        else {
            throw new IllegalArgumentException("Unexpected kind of term: " + term.getClass());
        }
    }

    @Override
    public ImmutableFunctionalTerm applyToFunctionalTerm(ImmutableFunctionalTerm functionalTerm) {
        if (isEmpty())
            return functionalTerm;

        ImmutableList.Builder<ImmutableTerm> subTermsBuilder = ImmutableList.builder();

        for (ImmutableTerm subTerm : functionalTerm.getImmutableTerms()) {
            subTermsBuilder.add(apply(subTerm));
        }

        OBDADataFactory factory = OBDADataFactoryImpl.getInstance();
        Predicate functionSymbol = functionalTerm.getFunctionSymbol();

        /**
         * Distinguishes the BooleanExpression from the other functional terms.
         */
        if (functionSymbol instanceof BooleanOperationPredicate) {
            return factory.getImmutableBooleanExpression((BooleanOperationPredicate) functionSymbol,
                    subTermsBuilder.build());
        }
        else {
            return factory.getImmutableFunctionalTerm(functionSymbol, subTermsBuilder.build());
        }
    }

    /**
     *" "this o g"
     *
     * Equivalent to the function x -> this.apply(g.apply(x))
     *
     * Follows the formal definition of a the composition of two substitutions.
     *
     */
    @Override
    public ImmutableSubstitution<ImmutableTerm> composeWith(ImmutableSubstitution<? extends ImmutableTerm> g) {
        if (isEmpty()) {
            return (ImmutableSubstitution<ImmutableTerm>)g;
        }
        if (g.isEmpty()) {
            return (ImmutableSubstitution<ImmutableTerm>)this;
        }

        Map<VariableImpl, ImmutableTerm> substitutionMap = new HashMap<>();

        /**
         * For all variables in the domain of g
         */

        for (Map.Entry<VariableImpl, ? extends ImmutableTerm> gEntry :  g.getImmutableMap().entrySet()) {
            substitutionMap.put(gEntry.getKey(), apply(gEntry.getValue()));
        }

        /**
         * For the other variables (in the local domain but not in g)
         */
        for (Map.Entry<VariableImpl, ? extends ImmutableTerm> localEntry :  getImmutableMap().entrySet()) {
            VariableImpl localVariable = localEntry.getKey();

            if (substitutionMap.containsKey(localVariable))
                continue;

            substitutionMap.put(localVariable, localEntry.getValue());
        }


        return new ImmutableSubstitutionImpl<>(ImmutableMap.copyOf(substitutionMap));
    }

}
