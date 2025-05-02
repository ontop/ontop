package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.impl.StringDBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

/**
 * DBFunctionSymbol for Custom GeoSPARQL Functions with targeted denormalization.
 */
public class DBGeoFunctionSymbolWithSerializerImpl extends DBFunctionSymbolWithSerializerImpl {

    protected DBGeoFunctionSymbolWithSerializerImpl(String name, ImmutableList<TermType> inputDBTypes,
                                                 DBTermType targetType,
                                                 boolean isAlwaysInjective,
                                                 DBFunctionSymbolSerializer serializer) {
        super(name, inputDBTypes, targetType, isAlwaysInjective, serializer);
    }

    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableList<ImmutableTerm> simplifiedTerms = terms.stream().map(this::unwrapSTAsText).collect(ImmutableCollectors.toList());
        return super.simplify(simplifiedTerms, termFactory, variableNullability);
    }

    // if term is TEXTToGEOMETRY(ST_ASTEXT(arg)), returns arg, otherwise the term itself
    private ImmutableTerm unwrapSTAsText(ImmutableTerm term) {
        return Optional.of(term)
                // term is a function
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                // the function symbol is TEXTToGEOMETRY
                .filter(this::isGeometryOrGeographyCast)
                // check if first argument is ST_ASTEXT
                .filter(t -> t.getTerm(0) instanceof ImmutableFunctionalTerm
                        && ((ImmutableFunctionalTerm) t.getTerm(0)).getFunctionSymbol().getName().startsWith("ST_ASTEXT"))
                // if ST_ASTEXT found in subterm, extract its argument
                .map(t -> ((ImmutableFunctionalTerm) t.getTerm(0)).getTerm(0))
                // otherwise return the original term
                .orElse(term);
    }

    private boolean isGeometryOrGeographyCast(ImmutableFunctionalTerm term) {
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
