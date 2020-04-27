package it.unibz.inf.ontop.generation.serializer.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;

public class DefaultAttributeAliasFactory implements AttributeAliasFactory {

    private final QuotedIDFactory rawIdFactory;

    DefaultAttributeAliasFactory(QuotedIDFactory idFactory) {
        rawIdFactory = new RawQuotedIDFactory(idFactory);
    }

    @Override
    public QuotedID createAttributeAlias(String variable) {
        return rawIdFactory.createAttributeID(variable);
    }
}
