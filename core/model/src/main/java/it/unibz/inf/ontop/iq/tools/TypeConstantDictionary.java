package it.unibz.inf.ontop.iq.tools;

import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;

public interface TypeConstantDictionary {
    DBConstant convert(RDFTermTypeConstant termTypeConstant);

    RDFTermTypeConstant convert(DBConstant constant);
}
