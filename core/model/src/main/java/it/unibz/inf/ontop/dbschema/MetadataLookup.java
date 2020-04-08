package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.model.type.DBTypeFactory;

public interface MetadataLookup {
    DatabaseRelationDefinition get(RelationID id) throws RelationNotFoundException;

    QuotedIDFactory getQuotedIDFactory();

    DBTypeFactory getDBTypeFactory();
}
