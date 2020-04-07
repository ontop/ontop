package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;


/**
 * A dummy DummyDBMetadataBuilder for tests only
 */
public class DummyDBMetadataBuilderImpl implements DummyDBMetadataBuilder {

    private final DBTypeFactory dbTypeFactory;
    private final QuotedIDFactory idFactory;

    @Inject
    private DummyDBMetadataBuilderImpl(TypeFactory typeFactory) {
        this.idFactory = new SQLStandardQuotedIDFactory();
        this.dbTypeFactory = typeFactory.getDBTypeFactory();
    }

    @Override
    public DatabaseRelationDefinition createDatabaseRelation(RelationDefinition.AttributeListBuilder builder) {
        return new DatabaseRelationDefinition(builder);
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return idFactory;
    }

    @Override
    public DBTypeFactory getDBTypeFactory() {
        return dbTypeFactory;
    }

    @Override
    public DBParameters getDBParameters() { return new BasicDBParametersImpl(null, null, null, null, idFactory, dbTypeFactory); }

}
