package it.unibz.inf.ontop.dbschema;


import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

public interface DummyDBMetadataBuilder {

    DatabaseRelationDefinition createDatabaseRelation(RelationDefinition.AttributeListBuilder builder);

    DatabaseRelationDefinition createDatabaseRelation(String relation,
                                                      String attribute1, DBTermType type1, boolean isNullable1);

    DatabaseRelationDefinition createDatabaseRelation(String relation,
                                                      String attribute1, DBTermType type1, boolean isNullable1,
                                                      String attribute2, DBTermType type2, boolean isNullable2);

    DatabaseRelationDefinition createDatabaseRelation(String relation,
                                                      String attribute1, DBTermType type1, boolean isNullable1,
                                                      String attribute2, DBTermType type2, boolean isNullable2,
                                                      String attribute3, DBTermType type3, boolean isNullable3);

    DatabaseRelationDefinition createDatabaseRelation(String relation,
                                                      String attribute1, DBTermType type1, boolean isNullable1,
                                                      String attribute2, DBTermType type2, boolean isNullable2,
                                                      String attribute3, DBTermType type3, boolean isNullable3,
                                                      String attribute4, DBTermType type4, boolean isNullable4);

    DatabaseRelationDefinition createDatabaseRelation(String relation,
                                                      String attribute1, DBTermType type1, boolean isNullable1,
                                                      String attribute2, DBTermType type2, boolean isNullable2,
                                                      String attribute3, DBTermType type3, boolean isNullable3,
                                                      String attribute4, DBTermType type4, boolean isNullable4,
                                                      String attribute5, DBTermType type5, boolean isNullable5);

    DatabaseRelationDefinition createDatabaseRelation(String relation,
                                                      String attribute1, DBTermType type1, boolean isNullable1,
                                                      String attribute2, DBTermType type2, boolean isNullable2,
                                                      String attribute3, DBTermType type3, boolean isNullable3,
                                                      String attribute4, DBTermType type4, boolean isNullable4,
                                                      String attribute5, DBTermType type5, boolean isNullable5,
                                                      String attribute6, DBTermType type6, boolean isNullable6);


    QuotedIDFactory getQuotedIDFactory();

    DBTypeFactory getDBTypeFactory();

    DBParameters getDBParameters();
}
