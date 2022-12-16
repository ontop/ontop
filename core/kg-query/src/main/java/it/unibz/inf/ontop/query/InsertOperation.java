package it.unibz.inf.ontop.query;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OntopInvalidKGQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.query.translation.KGQueryTranslator;

public interface InsertOperation {

    String getOriginalString();

    ImmutableSet<IQ> translate(KGQueryTranslator translator) throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException;

}
