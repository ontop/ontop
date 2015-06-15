package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 * Common abstract class for ImmutableSubstitutionImpl and Var2VarSubstitutionImpl
 */
public abstract class AbstractImmutableSubstitutionImpl extends LocallyImmutableSubstitutionImpl
        implements ImmutableSubstitution {

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

        ImmutableList.Builder<ImmutableTerm> subTermsBuilder = ImmutableList.builder();

        for (ImmutableTerm subTerm : functionalTerm.getImmutableTerms()) {
            subTermsBuilder.add(apply(subTerm));
        }

        OBDADataFactory factory = OBDADataFactoryImpl.getInstance();
        return factory.getImmutableFunctionalTerm(functionalTerm.getFunctionSymbol(), subTermsBuilder.build());
    }
}
