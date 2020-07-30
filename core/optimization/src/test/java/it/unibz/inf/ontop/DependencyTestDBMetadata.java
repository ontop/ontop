package it.unibz.inf.ontop;

import it.unibz.inf.ontop.dbschema.RelationDefinition;

import static it.unibz.inf.ontop.OptimizationTestingTools.createMetadataProviderBuilder;

public class DependencyTestDBMetadata {

    public static final RelationDefinition PK_TABLE1_AR1;
    public static final RelationDefinition PK_TABLE2_AR1;
    public static final RelationDefinition PK_TABLE3_AR1;
    public static final RelationDefinition PK_TABLE4_AR1;
    public static final RelationDefinition PK_TABLE5_AR1;

    public static final RelationDefinition PK_TABLE1_AR2;
    public static final RelationDefinition PK_TABLE2_AR2;
    public static final RelationDefinition PK_TABLE3_AR2;
    public static final RelationDefinition PK_TABLE4_AR2;
    public static final RelationDefinition PK_TABLE5_AR2;
    public static final RelationDefinition PK_TABLE6_AR2;

    public static final RelationDefinition PK_TABLE1_AR3;
    public static final RelationDefinition PK_TABLE2_AR3;
    public static final RelationDefinition PK_TABLE3_AR3;
    public static final RelationDefinition PK_TABLE4_AR3;
    public static final RelationDefinition PK_TABLE5_AR3;
    public static final RelationDefinition PK_TABLE6_AR3;

    public static final RelationDefinition PK_TABLE7_AR4;

    static {
        OptimizationTestingTools.OfflineMetadataProviderBuilder3 builder = createMetadataProviderBuilder();
        PK_TABLE1_AR1 = builder.createRelationWithPK(1, 1);
        PK_TABLE2_AR1 = builder.createRelationWithPK(2, 1);
        PK_TABLE3_AR1 = builder.createRelationWithPK(3, 1);
        PK_TABLE4_AR1 = builder.createRelationWithPK(4, 1);
        PK_TABLE5_AR1 = builder.createRelationWithPK(5, 1);

        PK_TABLE1_AR2 = builder.createRelationWithPK(1, 2);
        PK_TABLE2_AR2 = builder.createRelationWithPK(2, 2);
        PK_TABLE3_AR2 = builder.createRelationWithPK(3, 2);
        PK_TABLE4_AR2 = builder.createRelationWithPK(4, 2);
        PK_TABLE5_AR2 = builder.createRelationWithPK(5, 2);
        PK_TABLE6_AR2 = builder.createRelationWithPK(6, 2);

        PK_TABLE1_AR3 = builder.createRelationWithPK(1, 3);
        PK_TABLE2_AR3 = builder.createRelationWithPK(2, 3);
        PK_TABLE3_AR3 = builder.createRelationWithPK(3, 3);
        PK_TABLE4_AR3 = builder.createRelationWithPK(4, 3);
        PK_TABLE5_AR3 = builder.createRelationWithPK(5, 3);
        PK_TABLE6_AR3 = builder.createRelationWithPK(6, 3);

        PK_TABLE7_AR4 = builder.createRelationWithPK(7, 4);
    }
}
