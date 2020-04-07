package it.unibz.inf.ontop;

import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.type.DBTermType;


import static it.unibz.inf.ontop.OptimizationTestingTools.DEFAULT_DUMMY_DB_METADATA;

public class NoDependencyTestDBMetadata {

    public static final RelationPredicate TABLE1_AR1;
    public static final RelationPredicate TABLE2_AR1;
    public static final RelationPredicate TABLE3_AR1;
    public static final RelationPredicate TABLE4_AR1;
    public static final RelationPredicate TABLE5_AR1;

    public static final RelationPredicate TABLE1_AR2;
    public static final RelationPredicate TABLE2_AR2;
    public static final RelationPredicate TABLE3_AR2;
    public static final RelationPredicate TABLE4_AR2;
    public static final RelationPredicate TABLE5_AR2;
    public static final RelationPredicate TABLE6_AR2;

    public static final RelationPredicate TABLE1_AR3;
    public static final RelationPredicate TABLE2_AR3;
    public static final RelationPredicate TABLE3_AR3;
    public static final RelationPredicate TABLE4_AR3;
    public static final RelationPredicate TABLE5_AR3;
    public static final RelationPredicate TABLE6_AR3;

    public static final RelationPredicate TABLE7_AR4;

    public static final RelationPredicate INT_TABLE1_AR2;
    public static final RelationPredicate INT_TABLE2_AR2;
    public static final RelationPredicate INT_TABLE1_NULL_AR2;
    public static final RelationPredicate INT_TABLE2_NULL_AR2;
    public static final RelationPredicate INT_TABLE1_NULL_AR3;

    public static RelationPredicate createStringRelationPredicate(DBMetadataBuilder dbMetadata,
                                                                  int tableNumber, int arity, boolean canBeNull) {
        return createRelationPredicate(dbMetadata, tableNumber, arity, dbMetadata.getDBParameters().getDBTypeFactory().getDBStringType(), "STR_", canBeNull);
    }

    public static RelationPredicate createStringRelationPredicate(DBMetadataBuilder dbMetadata,
                                                                  int tableNumber, int arity) {
        return createStringRelationPredicate(dbMetadata, tableNumber, arity, false);
    }

    public static RelationPredicate createIntRelationPredicate(DBMetadataBuilder dbMetadata,
                                                               int tableNumber, int arity, boolean canBeNull) {
        return createRelationPredicate(dbMetadata, tableNumber, arity, dbMetadata.getDBParameters().getDBTypeFactory().getDBLargeIntegerType(), "INT_", canBeNull);
    }

    public static RelationPredicate createIntRelationPredicate(DBMetadataBuilder dbMetadata,
                                                               int tableNumber, int arity) {
        return createIntRelationPredicate(dbMetadata, tableNumber, arity, false);
    }

    public static RelationPredicate createRelationPredicate(DBMetadataBuilder dbMetadata,
                                                            int tableNumber, int arity, DBTermType termType, String prefix,
                                                            boolean canBeNull) {

        QuotedIDFactory idFactory = dbMetadata.getDBParameters().getQuotedIDFactory();
        RelationDefinition.AttributeListBuilder builder =  new RelationDefinition.AttributeListBuilder(idFactory.createRelationID(null,
                prefix + "TABLE" + tableNumber + "AR" + arity));
        for (int i = 1; i <= arity; i++) {
            builder.addAttribute(idFactory.createAttributeID("col" + i), termType, canBeNull);
        }
        DatabaseRelationDefinition tableDef = dbMetadata.createDatabaseRelation(builder);
        return tableDef.getAtomPredicate();
    }

    static {
        DBMetadataBuilder dbMetadata = DEFAULT_DUMMY_DB_METADATA;
        TABLE1_AR1 = createStringRelationPredicate(dbMetadata, 1, 1);
        TABLE2_AR1 = createStringRelationPredicate(dbMetadata, 2, 1);
        TABLE3_AR1 = createStringRelationPredicate(dbMetadata, 3, 1);
        TABLE4_AR1 = createStringRelationPredicate(dbMetadata, 4, 1);
        TABLE5_AR1 = createStringRelationPredicate(dbMetadata, 5, 1);

        TABLE1_AR2 = createStringRelationPredicate(dbMetadata, 1, 2);
        TABLE2_AR2 = createStringRelationPredicate(dbMetadata, 2, 2);
        TABLE3_AR2 = createStringRelationPredicate(dbMetadata, 3, 2);
        TABLE4_AR2 = createStringRelationPredicate(dbMetadata, 4, 2);
        TABLE5_AR2 = createStringRelationPredicate(dbMetadata, 5, 2);
        TABLE6_AR2 = createStringRelationPredicate(dbMetadata, 6, 2);

        TABLE1_AR3 = createStringRelationPredicate(dbMetadata, 1, 3);
        TABLE2_AR3 = createStringRelationPredicate(dbMetadata, 2, 3);
        TABLE3_AR3 = createStringRelationPredicate(dbMetadata, 3, 3);
        TABLE4_AR3 = createStringRelationPredicate(dbMetadata, 4, 3);
        TABLE5_AR3 = createStringRelationPredicate(dbMetadata, 5, 3);
        TABLE6_AR3 = createStringRelationPredicate(dbMetadata, 6, 3);

        TABLE7_AR4 = createStringRelationPredicate(dbMetadata, 7, 4);

        INT_TABLE1_AR2 = createIntRelationPredicate(dbMetadata,1, 2);
        INT_TABLE2_AR2 = createIntRelationPredicate(dbMetadata, 2, 2);

        INT_TABLE1_NULL_AR2 = createIntRelationPredicate(dbMetadata, 1, 2, true);
        INT_TABLE2_NULL_AR2 = createIntRelationPredicate(dbMetadata, 2, 2, true);
        INT_TABLE1_NULL_AR3 = createIntRelationPredicate(dbMetadata, 1, 3, true);
    }
}
