package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.*;

import java.util.function.Function;

/**
 * Builds a RDFTermTypeConstant out of a DBConstant
 */
public interface RDFTermTypeFunctionSymbol extends FunctionSymbol {

    ImmutableBiMap<DBConstant, RDFTermTypeConstant> getConversionMap();

    TypeConstantDictionary getDictionary();

    /**
     * Builds a DB CASE functional term with an "entry" for possible DBConstant value.
     * Returns NULL in the default case
     */
    ImmutableTerm lift(ImmutableList<? extends ImmutableTerm> terms,
                       Function<RDFTermTypeConstant, ImmutableTerm> caseTermFct,
                       TermFactory termFactory);

    /**
     * Builds a boolean DB CASE functional term with an "entry" for possible DBConstant value.
     * Returns IS_TRUE(NULL) in the default case
     */
    ImmutableExpression liftExpression(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<RDFTermTypeConstant, ImmutableExpression> caseExpressionFct,
                                       TermFactory termFactory);
}
