package it.unibz.inf.ontop.model.type.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.exception.IncompatibleTermException;

import java.util.Optional;

@Singleton
public class TermTypeInferenceTools {

    /**
     * TODO: make it private
     */
    @Inject
    public TermTypeInferenceTools() {
    }

    /**
     * TODO: simplify this method
     */
    public Optional<TermType> inferType(ImmutableTerm term) throws IncompatibleTermException {
        if(term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm f = (ImmutableFunctionalTerm) term;
            Predicate typePred = f.getFunctionSymbol();

            /*
             * TODO: generalize this
             */
            if(f instanceof ImmutableExpression) {
                return ((ImmutableExpression) f).getOptionalTermType();
            }
            else if (typePred instanceof RDFTermConstructionSymbol){
                return Optional.of(((RDFTermConstructionSymbol) typePred).getReturnedType());
            }
            else {
                throw new IllegalArgumentException("Unexpected functional term: " + term);
            }
        }
        else if(term instanceof Variable){
            return Optional.empty();
        } else if(term instanceof RDFConstant){
            return Optional.of(((RDFConstant) term).getType());
        }
        else {
            throw new IllegalStateException("Unexpected term: " + term);
        }
    }
}
