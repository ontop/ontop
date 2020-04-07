package it.unibz.inf.ontop.dbschema;


import it.unibz.inf.ontop.model.type.DBTypeFactory;

public interface DummyDBMetadataBuilder {

    DatabaseRelationDefinition createDatabaseRelation(RelationDefinition.AttributeListBuilder builder);

    QuotedIDFactory getQuotedIDFactory();

    DBTypeFactory getDBTypeFactory();

    DBParameters getDBParameters();
}
