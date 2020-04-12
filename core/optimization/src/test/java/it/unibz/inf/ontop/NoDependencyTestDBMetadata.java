package it.unibz.inf.ontop;

import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.DatabaseTableDefinition;
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

    public static RelationPredicate createStringRelationPredicate(int tableNumber, int arity, boolean canBeNull) {
        return createRelationPredicate(tableNumber, arity, DEFAULT_DUMMY_DB_METADATA.getDBTypeFactory().getDBStringType(), "STR_", canBeNull);
    }

    public static RelationPredicate createStringRelationPredicate(int tableNumber, int arity) {
        return createStringRelationPredicate(tableNumber, arity, false);
    }

    public static RelationPredicate createIntRelationPredicate(int tableNumber, int arity, boolean canBeNull) {
        return createRelationPredicate(tableNumber, arity, DEFAULT_DUMMY_DB_METADATA.getDBTypeFactory().getDBLargeIntegerType(), "INT_", canBeNull);
    }

    public static RelationPredicate createIntRelationPredicate(int tableNumber, int arity) {
        return createIntRelationPredicate(tableNumber, arity, false);
    }

    public static RelationPredicate createRelationPredicate(int tableNumber, int arity, DBTermType termType, String prefix,
                                                            boolean canBeNull) {

        QuotedIDFactory idFactory = DEFAULT_DUMMY_DB_METADATA.getQuotedIDFactory();
        RelationDefinition.AttributeListBuilder builder =  DatabaseTableDefinition.attributeListBuilder();
        for (int i = 1; i <= arity; i++) {
            builder.addAttribute(idFactory.createAttributeID("col" + i), termType, canBeNull);
        }
        DatabaseRelationDefinition tableDef = DEFAULT_DUMMY_DB_METADATA.createDatabaseRelation(
                idFactory.createRelationID(null, prefix + "TABLE" + tableNumber + "AR" + arity), builder);
        return tableDef.getAtomPredicate();
    }

    static {
        TABLE1_AR1 = createStringRelationPredicate( 1, 1);
        TABLE2_AR1 = createStringRelationPredicate( 2, 1);
        TABLE3_AR1 = createStringRelationPredicate( 3, 1);
        TABLE4_AR1 = createStringRelationPredicate( 4, 1);
        TABLE5_AR1 = createStringRelationPredicate( 5, 1);

        TABLE1_AR2 = createStringRelationPredicate( 1, 2);
        TABLE2_AR2 = createStringRelationPredicate( 2, 2);
        TABLE3_AR2 = createStringRelationPredicate( 3, 2);
        TABLE4_AR2 = createStringRelationPredicate( 4, 2);
        TABLE5_AR2 = createStringRelationPredicate( 5, 2);
        TABLE6_AR2 = createStringRelationPredicate( 6, 2);

        TABLE1_AR3 = createStringRelationPredicate( 1, 3);
        TABLE2_AR3 = createStringRelationPredicate( 2, 3);
        TABLE3_AR3 = createStringRelationPredicate( 3, 3);
        TABLE4_AR3 = createStringRelationPredicate( 4, 3);
        TABLE5_AR3 = createStringRelationPredicate( 5, 3);
        TABLE6_AR3 = createStringRelationPredicate(6, 3);

        TABLE7_AR4 = createStringRelationPredicate(7, 4);

        INT_TABLE1_AR2 = createIntRelationPredicate(1, 2);
        INT_TABLE2_AR2 = createIntRelationPredicate( 2, 2);

        INT_TABLE1_NULL_AR2 = createIntRelationPredicate(1, 2, true);
        INT_TABLE2_NULL_AR2 = createIntRelationPredicate(2, 2, true);
        INT_TABLE1_NULL_AR3 = createIntRelationPredicate(1, 3, true);
    }
}
