package it.unibz.inf.ontop.model.term.functionsymbol.db;

import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;

/**
 * Slightly more general abstraction than a regular DB cast,
 * as it MAY perform some normalization to a specific format.
 */
public interface DBTypeConversionFunctionSymbol extends DBFunctionSymbol {

    DBTermType getTargetType();

    Optional<DBTermType> getInputType();

    boolean isTemporary();

    /**
     * Returns true if does not transform the string representation of the value
     * (i.e. no normalization).
     *
     * Useful for simplifying nested casts ( A-to-B(B-to-A(x)) === x if both casts are simple)
     */
    boolean isSimple();

    static boolean isTemporary(FunctionSymbol fs) {
        return (fs instanceof DBTypeConversionFunctionSymbol
                && (((DBTypeConversionFunctionSymbol) fs).isTemporary())
                && fs.getArity() == 1);
    }

    static ImmutableTerm uncast(ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm ift = (ImmutableFunctionalTerm)term;
            if (isTemporary(ift.getFunctionSymbol()))
                return ift.getTerm(0);
        }
        return term;
    }

}
