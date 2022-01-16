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

    public static final RelationDefinition TABLE13_AR2;

    public static final RelationDefinition TABLE7_AR4;

    public static final RelationDefinition INT_TABLE1_AR2;
    public static final RelationDefinition INT_TABLE2_AR2;
    public static final RelationDefinition INT_TABLE1_AR3;
    public static final RelationDefinition INT_TABLE1_NULL_AR2;
    public static final RelationDefinition INT_TABLE2_NULL_AR2;
    public static final RelationDefinition INT_TABLE1_NULL_AR3;

    public static final RelationDefinition UUID_TABLE1_AR3;


    static {
        OptimizationTestingTools.OfflineMetadataProviderBuilder3 builder = createMetadataProviderBuilder();
        TABLE1_AR1 = builder.createRelationWithStringAttributes(1, 1, false);
        TABLE2_AR1 = builder.createRelationWithStringAttributes(2, 1, false);
        TABLE3_AR1 = builder.createRelationWithStringAttributes(3, 1, false);
        TABLE4_AR1 = builder.createRelationWithStringAttributes(4, 1, false);
        TABLE5_AR1 = builder.createRelationWithStringAttributes(5, 1, false);

        TABLE1_AR2 = builder.createRelationWithStringAttributes(1, 2, false);
        TABLE2_AR2 = builder.createRelationWithStringAttributes(2, 2, false);
        TABLE3_AR2 = builder.createRelationWithStringAttributes(3, 2, false);
        TABLE4_AR2 = builder.createRelationWithStringAttributes(4, 2, false);
        TABLE5_AR2 = builder.createRelationWithStringAttributes(5, 2, false);
        TABLE6_AR2 = builder.createRelationWithStringAttributes(6, 2, false);

        TABLE1_AR3 = builder.createRelationWithStringAttributes(1, 3, false);
        TABLE2_AR3 = builder.createRelationWithStringAttributes(2, 3, false);
        TABLE3_AR3 = builder.createRelationWithStringAttributes(3, 3, false);
        TABLE4_AR3 = builder.createRelationWithStringAttributes(4, 3, false);
        TABLE5_AR3 = builder.createRelationWithStringAttributes(5, 3, false);
        TABLE6_AR3 = builder.createRelationWithStringAttributes(6, 3, false);

        TABLE7_AR4 = builder.createRelationWithStringAttributes(7, 4, false);


        TABLE13_AR2 = builder.createRelationWithStringAttributes(13, 2, true);

        INT_TABLE1_AR2 = builder.createRelationWithIntAttributes(1, 2, false);
        INT_TABLE2_AR2 = builder.createRelationWithIntAttributes(2, 2, false);
        INT_TABLE1_AR3 = builder.createRelationWithIntAttributes(1, 3, false);

        UUID_TABLE1_AR3 = builder.createRelationWithUuidAttributes(1, 3, false);

        INT_TABLE1_NULL_AR2 = builder.createRelationWithIntAttributes(1, 2, true);
        INT_TABLE2_NULL_AR2 = builder.createRelationWithIntAttributes(2, 2, true);
        INT_TABLE1_NULL_AR3 = builder.createRelationWithIntAttributes(1, 3, true);
    }
}
