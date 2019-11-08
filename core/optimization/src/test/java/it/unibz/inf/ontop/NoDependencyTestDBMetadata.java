package it.unibz.inf.ontop;

import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;


import static it.unibz.inf.ontop.OptimizationTestingTools.TYPE_FACTORY;
import static it.unibz.inf.ontop.OptimizationTestingTools.createDummyMetadata;

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

    public static final BasicDBMetadata DB_METADATA;

    public static RelationPredicate createStringRelationPredicate(BasicDBMetadata dbMetadata, DBTypeFactory dbTypeFactory,
                                                                   QuotedIDFactory idFactory,
                                                                   int tableNumber, int arity, boolean canBeNull) {
        return createRelationPredicate(dbMetadata, idFactory, tableNumber, arity, dbTypeFactory.getDBStringType(), "STR_", canBeNull);
    }

    public static RelationPredicate createStringRelationPredicate(BasicDBMetadata dbMetadata, DBTypeFactory dbTypeFactory,
                                                                  QuotedIDFactory idFactory,
                                                                  int tableNumber, int arity) {
        return createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, tableNumber, arity, false);
    }

    public static RelationPredicate createIntRelationPredicate(BasicDBMetadata dbMetadata, DBTypeFactory dbTypeFactory, QuotedIDFactory idFactory,
                                                               int tableNumber, int arity, boolean canBeNull) {
        return createRelationPredicate(dbMetadata, idFactory, tableNumber, arity, dbTypeFactory.getDBLargeIntegerType(), "INT_", canBeNull);
    }

    public static RelationPredicate createIntRelationPredicate(BasicDBMetadata dbMetadata, DBTypeFactory dbTypeFactory, QuotedIDFactory idFactory,
                                                                int tableNumber, int arity) {
        return createIntRelationPredicate(dbMetadata, dbTypeFactory, idFactory, tableNumber, arity, false);
    }

    public static RelationPredicate createRelationPredicate(BasicDBMetadata dbMetadata, QuotedIDFactory idFactory,
                                                                   int tableNumber, int arity, DBTermType termType, String prefix,
                                                            boolean canBeNull) {
        DatabaseRelationDefinition tableDef = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,
                prefix + "TABLE" + tableNumber + "AR" + arity));


        for (int i = 1; i <= arity; i++) {
            tableDef.addAttribute(idFactory.createAttributeID("col" + i), termType.getName(), termType, canBeNull);
        }
        return tableDef.getAtomPredicate();
    }

    static {
        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();

        TABLE1_AR1 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 1, 1);
        TABLE2_AR1 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 2, 1);
        TABLE3_AR1 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 3, 1);
        TABLE4_AR1 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 4, 1);
        TABLE5_AR1 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 5, 1);

        TABLE1_AR2 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 1, 2);
        TABLE2_AR2 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 2, 2);
        TABLE3_AR2 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 3, 2);
        TABLE4_AR2 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 4, 2);
        TABLE5_AR2 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 5, 2);
        TABLE6_AR2 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 6, 2);

        TABLE1_AR3 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 1, 3);
        TABLE2_AR3 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 2, 3);
        TABLE3_AR3 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 3, 3);
        TABLE4_AR3 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 4, 3);
        TABLE5_AR3 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 5, 3);
        TABLE6_AR3 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 6, 3);

        TABLE7_AR4 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 7, 4);

        INT_TABLE1_AR2 = createIntRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 1, 2);
        INT_TABLE2_AR2 = createIntRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 2, 2);

        INT_TABLE1_NULL_AR2 = createIntRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 1, 2, true);
        INT_TABLE2_NULL_AR2 = createIntRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 2, 2, true);
        INT_TABLE1_NULL_AR3 = createIntRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 1, 3, true);

        dbMetadata.freeze();
        DB_METADATA = dbMetadata;
    }

}
