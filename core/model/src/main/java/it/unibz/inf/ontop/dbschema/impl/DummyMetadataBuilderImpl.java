package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;


/**
 * A dummy DummyDBMetadataBuilder for tests only
 */
public class DummyMetadataBuilderImpl implements DummyDBMetadataBuilder {

    private final DBTypeFactory dbTypeFactory;
    private final QuotedIDFactory idFactory;

    @Inject
    private DummyMetadataBuilderImpl(TypeFactory typeFactory) {
        this.idFactory = new SQLStandardQuotedIDFactory();
        this.dbTypeFactory = typeFactory.getDBTypeFactory();
    }

    @Override
    public DatabaseRelationDefinition createDatabaseRelation(RelationDefinition.AttributeListBuilder builder) {
        return new DatabaseRelationDefinition(builder);
    }

    @Override
    public DatabaseRelationDefinition createDatabaseRelation(String relation, String attribute1, DBTermType type1, boolean isNullable1) {
        return new DatabaseRelationDefinition(
                new RelationDefinition.AttributeListBuilder(idFactory.createRelationID(null, relation))
                .addAttribute(idFactory.createAttributeID(attribute1), type1, isNullable1));
    }

    @Override
    public DatabaseRelationDefinition createDatabaseRelation(String relation, String attribute1, DBTermType type1, boolean isNullable1, String attribute2, DBTermType type2, boolean isNullable2) {
        return new DatabaseRelationDefinition(
                new RelationDefinition.AttributeListBuilder(idFactory.createRelationID(null, relation))
                        .addAttribute(idFactory.createAttributeID(attribute1), type1, isNullable1)
                        .addAttribute(idFactory.createAttributeID(attribute2), type2, isNullable2));
    }

    @Override
    public DatabaseRelationDefinition createDatabaseRelation(String relation, String attribute1, DBTermType type1, boolean isNullable1, String attribute2, DBTermType type2, boolean isNullable2, String attribute3, DBTermType type3, boolean isNullable3) {
        return new DatabaseRelationDefinition(
                new RelationDefinition.AttributeListBuilder(idFactory.createRelationID(null, relation))
                        .addAttribute(idFactory.createAttributeID(attribute1), type1, isNullable1)
                        .addAttribute(idFactory.createAttributeID(attribute2), type2, isNullable2)
                        .addAttribute(idFactory.createAttributeID(attribute3), type3, isNullable3));
    }

    @Override
    public DatabaseRelationDefinition createDatabaseRelation(String relation, String attribute1, DBTermType type1, boolean isNullable1, String attribute2, DBTermType type2, boolean isNullable2, String attribute3, DBTermType type3, boolean isNullable3, String attribute4, DBTermType type4, boolean isNullable4) {
        return new DatabaseRelationDefinition(
                new RelationDefinition.AttributeListBuilder(idFactory.createRelationID(null, relation))
                        .addAttribute(idFactory.createAttributeID(attribute1), type1, isNullable1)
                        .addAttribute(idFactory.createAttributeID(attribute2), type2, isNullable2)
                        .addAttribute(idFactory.createAttributeID(attribute3), type3, isNullable3)
                        .addAttribute(idFactory.createAttributeID(attribute4), type4, isNullable4));
    }

    @Override
    public DatabaseRelationDefinition createDatabaseRelation(String relation, String attribute1, DBTermType type1, boolean isNullable1, String attribute2, DBTermType type2, boolean isNullable2, String attribute3, DBTermType type3, boolean isNullable3, String attribute4, DBTermType type4, boolean isNullable4, String attribute5, DBTermType type5, boolean isNullable5) {
        return new DatabaseRelationDefinition(
                new RelationDefinition.AttributeListBuilder(idFactory.createRelationID(null, relation))
                        .addAttribute(idFactory.createAttributeID(attribute1), type1, isNullable1)
                        .addAttribute(idFactory.createAttributeID(attribute2), type2, isNullable2)
                        .addAttribute(idFactory.createAttributeID(attribute3), type3, isNullable3)
                        .addAttribute(idFactory.createAttributeID(attribute4), type4, isNullable4)
                        .addAttribute(idFactory.createAttributeID(attribute5), type5, isNullable5));
    }

    @Override
    public DatabaseRelationDefinition createDatabaseRelation(String relation, String attribute1, DBTermType type1, boolean isNullable1, String attribute2, DBTermType type2, boolean isNullable2, String attribute3, DBTermType type3, boolean isNullable3, String attribute4, DBTermType type4, boolean isNullable4, String attribute5, DBTermType type5, boolean isNullable5, String attribute6, DBTermType type6, boolean isNullable6) {
        return new DatabaseRelationDefinition(
                new RelationDefinition.AttributeListBuilder(idFactory.createRelationID(null, relation))
                        .addAttribute(idFactory.createAttributeID(attribute1), type1, isNullable1)
                        .addAttribute(idFactory.createAttributeID(attribute2), type2, isNullable2)
                        .addAttribute(idFactory.createAttributeID(attribute3), type3, isNullable3)
                        .addAttribute(idFactory.createAttributeID(attribute4), type4, isNullable4)
                        .addAttribute(idFactory.createAttributeID(attribute5), type5, isNullable5)
                        .addAttribute(idFactory.createAttributeID(attribute6), type6, isNullable6));
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
    public DBParameters getDBParameters() {
        return new DBParameters() {
            @Override
            public QuotedIDFactory getQuotedIDFactory() {
                return idFactory;
            }

            @Override
            public DBTypeFactory getDBTypeFactory() {
                return dbTypeFactory;
            }

            @Override
            public String getDriverName() {
                return null;
            }

            @Override
            public String getDriverVersion() {
                return null;
            }

            @Override
            public String getDbmsProductName() {
                return null;
            }

            @Override
            public String getDbmsVersion() {
                return null;
            }
        };
    }
}
