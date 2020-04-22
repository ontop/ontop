package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;

public interface AttributeAliasFactory {
    QuotedID createAttributeAlias(String variable);
}
