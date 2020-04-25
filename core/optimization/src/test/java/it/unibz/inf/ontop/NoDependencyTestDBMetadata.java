package it.unibz.inf.ontop;

import it.unibz.inf.ontop.dbschema.RelationDefinition;

import static it.unibz.inf.ontop.OptimizationTestingTools.createMetadataProviderBuilder;

public class NoDependencyTestDBMetadata {

    public static final RelationDefinition TABLE1_AR1;
    public static final RelationDefinition TABLE2_AR1;
    public static final RelationDefinition TABLE3_AR1;
    public static final RelationDefinition TABLE4_AR1;
    public static final RelationDefinition TABLE5_AR1;

    public static final RelationDefinition TABLE1_AR2;
    public static final RelationDefinition TABLE2_AR2;
    public static final RelationDefinition TABLE3_AR2;
    public static final RelationDefinition TABLE4_AR2;
    public static final RelationDefinition TABLE5_AR2;
    public static final RelationDefinition TABLE6_AR2;

    public static final RelationDefinition TABLE1_AR3;
    public static final RelationDefinition TABLE2_AR3;
    public static final RelationDefinition TABLE3_AR3;
    public static final RelationDefinition TABLE4_AR3;
    public static final RelationDefinition TABLE5_AR3;
    public static final RelationDefinition TABLE6_AR3;

    public static final RelationDefinition TABLE7_AR4;

    public static final RelationDefinition INT_TABLE1_AR2;
    public static final RelationDefinition INT_TABLE2_AR2;
    public static final RelationDefinition INT_TABLE1_NULL_AR2;
    public static final RelationDefinition INT_TABLE2_NULL_AR2;
    public static final RelationDefinition INT_TABLE1_NULL_AR3;


    static {
        OptimizationTestingTools.OfflineMetadataProviderBuilder3 builder = createMetadataProviderBuilder();
        TABLE1_AR1 = builder.createStringRelationPredicate(1, 1, false);
        TABLE2_AR1 = builder.createStringRelationPredicate(2, 1, false);
        TABLE3_AR1 = builder.createStringRelationPredicate(3, 1, false);
        TABLE4_AR1 = builder.createStringRelationPredicate(4, 1, false);
        TABLE5_AR1 = builder.createStringRelationPredicate(5, 1, false);

        TABLE1_AR2 = builder.createStringRelationPredicate(1, 2, false);
        TABLE2_AR2 = builder.createStringRelationPredicate(2, 2, false);
        TABLE3_AR2 = builder.createStringRelationPredicate(3, 2, false);
        TABLE4_AR2 = builder.createStringRelationPredicate(4, 2, false);
        TABLE5_AR2 = builder.createStringRelationPredicate(5, 2, false);
        TABLE6_AR2 = builder.createStringRelationPredicate(6, 2, false);

        TABLE1_AR3 = builder.createStringRelationPredicate(1, 3, false);
        TABLE2_AR3 = builder.createStringRelationPredicate(2, 3, false);
        TABLE3_AR3 = builder.createStringRelationPredicate(3, 3, false);
        TABLE4_AR3 = builder.createStringRelationPredicate(4, 3, false);
        TABLE5_AR3 = builder.createStringRelationPredicate(5, 3, false);
        TABLE6_AR3 = builder.createStringRelationPredicate(6, 3, false);

        TABLE7_AR4 = builder.createStringRelationPredicate(7, 4, false);

        INT_TABLE1_AR2 = builder.createIntRelationPredicate(1, 2, false);
        INT_TABLE2_AR2 = builder.createIntRelationPredicate(2, 2, false);

        INT_TABLE1_NULL_AR2 = builder.createIntRelationPredicate(1, 2, true);
        INT_TABLE2_NULL_AR2 = builder.createIntRelationPredicate(2, 2, true);
        INT_TABLE1_NULL_AR3 = builder.createIntRelationPredicate(1, 3, true);
    }
}
