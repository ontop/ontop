package it.unibz.inf.ontop.iq.tools;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;

import java.util.Collection;

public interface TypeConstantDictionary {
    DBConstant convert(RDFTermTypeConstant termTypeConstant);

    RDFTermTypeConstant convert(DBConstant constant);

    ImmutableMap<DBConstant, RDFTermTypeConstant> createConversionMap(Collection<RDFTermTypeConstant> termTypeConstants);
}
