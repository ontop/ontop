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
    ImmutableFunctionalTerm lift(ImmutableList<? extends ImmutableTerm> terms,
                       Function<RDFTermTypeConstant, ImmutableTerm> caseTermFct,
                       TermFactory termFactory);

    /**
     * Builds a boolean DB CASE functional term with an "entry" for possible DBConstant value.
     * Returns IS_TRUE(NULL) in the default case
     */
    ImmutableExpression liftExpression(ImmutableList<? extends ImmutableTerm> terms,
                                       Function<RDFTermTypeConstant, ImmutableExpression> caseExpressionFct,
                                       TermFactory termFactory);

    default ImmutableFunctionalTerm lift(ImmutableList<? extends ImmutableTerm> terms,
                                         Function<RDFTermTypeConstant, ? extends ImmutableTerm> caseTermFct,
                                         TermFactory termFactory, boolean isBoolean) {
        return isBoolean
                ? liftExpression(terms, (Function<RDFTermTypeConstant, ImmutableExpression>) caseTermFct, termFactory)
                : lift(terms, (Function<RDFTermTypeConstant, ImmutableTerm>) caseTermFct, termFactory);
    }

    /**
     * By default, RDFTermTypeFunctionSymbol cannot be simplified and are therefore not post-processable.
     * This is needed for lifting them above UNIONs using the standard binding lifting mechanisms.
     *
     * However, RDFTermTypeFunctionSymbol that arrive to the top construction node needs to replaced
     * by "simplifiable" versions so as to be post-processed.
     *
     * Observe that we expect RDFTermTypeFunctionSymbol to either reach the top construction node or to
     * be eliminated, as they cannot be delegated to the DB engine.
     */
    RDFTermTypeFunctionSymbol getSimplifiableVariant();


}
