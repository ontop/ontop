package it.unibz.inf.ontop.model.type.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.term.functionsymbol.BNodePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.DatatypePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.exception.IncompatibleTermException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.Optional;

@Singleton
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TermTypeInferenceTools {

    private final Optional<TermType> optionalObjectTermType;
    private final Optional<TermType> optionalBnodeTermType;
    private final Optional<TermType> optionalUnboundTermType;
    
    private final ValueConstant valueNull;
    private final TypeFactory typeFactory;
    private final TermFactory termFactory;

    /**
     * TODO: make it private
     */
    @Inject
    public TermTypeInferenceTools(TypeFactory typeFactory, TermFactory termFactory) {
        valueNull = termFactory.getNullConstant();
        optionalObjectTermType = Optional.of(typeFactory.getIRITermType());
        optionalBnodeTermType = Optional.of(typeFactory.getBlankNodeType());
        optionalUnboundTermType = Optional.of(typeFactory.getUnboundTermType());
        this.typeFactory = typeFactory;
        this.termFactory = termFactory;
    }

    /**
     * TODO: simplify this method
     */
    public Optional<TermType> inferType(ImmutableTerm term) throws IncompatibleTermException {
        if(term instanceof ImmutableFunctionalTerm){
            ImmutableFunctionalTerm f = (ImmutableFunctionalTerm) term;
            Predicate typePred = f.getFunctionSymbol();

            /*
             * TODO: generalize this
             */
            if(f instanceof ImmutableExpression) {
                return ((ImmutableExpression) f).getOptionalTermType(termFactory, typeFactory);
            }
            else if (typePred instanceof DatatypePredicate){
                return Optional.of(((DatatypePredicate) typePred).getReturnedType());

            } else if (typePred instanceof URITemplatePredicate) {
                return optionalObjectTermType;
            } else if (typePred instanceof BNodePredicate){
                return optionalBnodeTermType;
            }
            else {
                throw new IllegalArgumentException("Unexpected functional term: " + term);
            }
        }
        else if(term instanceof Variable){
            return Optional.empty();
        } else if(term instanceof ValueConstant){
            /**
             * Deals with the ugly definition of the NULL constant.
             * COL_TYPE of NULL should be NULL!
             */
            if (term == valueNull) {
                return optionalUnboundTermType;
            }
            else {
                return Optional.of(((ValueConstant) term).getType());
            }
        } else if(term instanceof URIConstant){
            return optionalObjectTermType;
        }
        else {
            throw new IllegalStateException("Unexpected term: " + term);
        }
    }
}
