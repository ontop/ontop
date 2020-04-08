package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.model.type.DBTypeFactory;

public interface MetadataLookup {
    RelationDefinition get(RelationID id) throws RelationNotFoundException;

    QuotedIDFactory getQuotedIDFactory();

    DBTypeFactory getDBTypeFactory();
}
