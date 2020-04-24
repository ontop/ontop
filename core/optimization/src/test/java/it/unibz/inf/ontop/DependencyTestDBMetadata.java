package it.unibz.inf.ontop;

import it.unibz.inf.ontop.model.atom.RelationPredicate;

import static it.unibz.inf.ontop.OptimizationTestingTools.createPKRelationPredicate;

public class DependencyTestDBMetadata {

    public static final RelationPredicate PK_TABLE1_AR1;
    public static final RelationPredicate PK_TABLE2_AR1;
    public static final RelationPredicate PK_TABLE3_AR1;
    public static final RelationPredicate PK_TABLE4_AR1;
    public static final RelationPredicate PK_TABLE5_AR1;

    public static final RelationPredicate PK_TABLE1_AR2;
    public static final RelationPredicate PK_TABLE2_AR2;
    public static final RelationPredicate PK_TABLE3_AR2;
    public static final RelationPredicate PK_TABLE4_AR2;
    public static final RelationPredicate PK_TABLE5_AR2;
    public static final RelationPredicate PK_TABLE6_AR2;

    public static final RelationPredicate PK_TABLE1_AR3;
    public static final RelationPredicate PK_TABLE2_AR3;
    public static final RelationPredicate PK_TABLE3_AR3;
    public static final RelationPredicate PK_TABLE4_AR3;
    public static final RelationPredicate PK_TABLE5_AR3;
    public static final RelationPredicate PK_TABLE6_AR3;

    public static final RelationPredicate PK_TABLE7_AR4;

    static {
        PK_TABLE1_AR1 = createPKRelationPredicate(1, 1);
        PK_TABLE2_AR1 = createPKRelationPredicate(2, 1);
        PK_TABLE3_AR1 = createPKRelationPredicate(3, 1);
        PK_TABLE4_AR1 = createPKRelationPredicate(4, 1);
        PK_TABLE5_AR1 = createPKRelationPredicate(5, 1);

        PK_TABLE1_AR2 = createPKRelationPredicate(1, 2);
        PK_TABLE2_AR2 = createPKRelationPredicate(2, 2);
        PK_TABLE3_AR2 = createPKRelationPredicate(3, 2);
        PK_TABLE4_AR2 = createPKRelationPredicate(4, 2);
        PK_TABLE5_AR2 = createPKRelationPredicate(5, 2);
        PK_TABLE6_AR2 = createPKRelationPredicate(6, 2);

        PK_TABLE1_AR3 = createPKRelationPredicate(1, 3);
        PK_TABLE2_AR3 = createPKRelationPredicate(2, 3);
        PK_TABLE3_AR3 = createPKRelationPredicate(3, 3);
        PK_TABLE4_AR3 = createPKRelationPredicate(4, 3);
        PK_TABLE5_AR3 = createPKRelationPredicate(5, 3);
        PK_TABLE6_AR3 = createPKRelationPredicate(6, 3);

        PK_TABLE7_AR4 = createPKRelationPredicate(7, 4);
    }
}
