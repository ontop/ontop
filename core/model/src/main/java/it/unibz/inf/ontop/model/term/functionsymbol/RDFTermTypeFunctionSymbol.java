package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableBiMap;
import it.unibz.inf.ontop.iq.tools.TypeConstantDictionary;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;

/**
 * Builds a RDFTermTypeConstant out of a DBConstant
 */
public interface RDFTermTypeFunctionSymbol extends FunctionSymbol {

    ImmutableBiMap<DBConstant, RDFTermTypeConstant> getConversionMap();

    TypeConstantDictionary getDictionary();
}
