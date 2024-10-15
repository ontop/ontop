package it.unibz.inf.ontop;

import com.google.common.collect.ImmutableSet;
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
    public static final RelationDefinition PK_TABLE8_AR5;

    public static final RelationDefinition FD_TABLE1_AR2;
    public static final RelationDefinition FD_TABLE2_AR2;
    public static final RelationDefinition FD_TABLE1_AR5;
    public static final RelationDefinition FD_TABLE1_AR6;

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
        PK_TABLE8_AR5 = builder.createRelationWithPK(8, 5);

        FD_TABLE1_AR2 = builder.createRelationWithFD(1, 2, false);
        FD_TABLE2_AR2 = builder.createRelationWithFD(2, 2, false);
        FD_TABLE1_AR5 = builder.createRelationWithCompositeFD(1, 5, false,
                ImmutableSet.of(1, 2), ImmutableSet.of(3, 4));
        FD_TABLE1_AR6 = builder.createRelationWithCompositeFD(1, 6, false,
                ImmutableSet.of(1, 2), ImmutableSet.of(3, 4));
    }
}
