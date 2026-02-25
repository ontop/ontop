package it.unibz.inf.ontop.utils;

import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.DefaultSimpleDBCastFunctionSymbol;
import it.unibz.inf.ontop.model.type.impl.StringDBTermType;

import java.util.Optional;

public class GeoTermUtils {

    //TODO: Temporary solution, denormalization to be handled robustly
    // if term is TEXTToGEOMETRY(ST_ASTEXT(arg)), returns arg, otherwise the term itself
    public static ImmutableTerm unwrapSTAsText(ImmutableTerm term) {
        return Optional.of(term)
                // term is a function
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                // the function symbol is TEXTToGEOMETRY
                .filter(GeoTermUtils::isGeometryOrGeographyCast)
                // check if first argument is ST_ASTEXT
                .filter(t -> t.getTerm(0) instanceof ImmutableFunctionalTerm
                        && ((ImmutableFunctionalTerm) t.getTerm(0)).getFunctionSymbol().getName().startsWith("ST_ASTEXT"))
                // if ST_ASTEXT found in subterm, extract its argument
                .map(t -> ((ImmutableFunctionalTerm) t.getTerm(0)).getTerm(0))
                // otherwise return the original term
                .orElse(term);
    }

    private static boolean isGeometryOrGeographyCast(ImmutableFunctionalTerm term) {
        if (!(term.getFunctionSymbol() instanceof DefaultSimpleDBCastFunctionSymbol)) {
            return false;
        }

        DefaultSimpleDBCastFunctionSymbol castSymbol = (DefaultSimpleDBCastFunctionSymbol) term.getFunctionSymbol();

        // Check input type is string
        boolean isStringInput = castSymbol.getInputType()
                .map(type -> type instanceof StringDBTermType)
                .orElse(false);

        // Check target type is GEOMETRY or GEOGRAPHY
        String targetType = castSymbol.getTargetType().getName();
        boolean isGeometryTarget = "GEOMETRY".equals(targetType) || "GEOGRAPHY".equals(targetType);

        return isStringInput && isGeometryTarget;
    }
}
