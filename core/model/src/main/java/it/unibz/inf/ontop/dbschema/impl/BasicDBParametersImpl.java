package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;

public class BasicDBParametersImpl implements DBParameters {

    private final QuotedIDFactory idFactory;

    /**
     * TODO: make it protected
     */
    public BasicDBParametersImpl(QuotedIDFactory idFactory) {
        this.idFactory = idFactory;
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return idFactory;
    }
}
