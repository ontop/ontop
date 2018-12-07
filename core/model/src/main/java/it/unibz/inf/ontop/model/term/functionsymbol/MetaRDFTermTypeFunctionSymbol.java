package it.unibz.inf.ontop.model.term.functionsymbol;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;

public interface MetaRDFTermTypeFunctionSymbol extends FunctionSymbol {

    ImmutableMap<DBConstant, RDFTermTypeConstant> getConversionMap();
}
