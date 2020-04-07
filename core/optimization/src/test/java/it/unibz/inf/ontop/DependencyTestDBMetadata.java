package it.unibz.inf.ontop;

import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.type.DBTermType;

import static it.unibz.inf.ontop.OptimizationTestingTools.DEFAULT_DUMMY_DB_METADATA;

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

    private static RelationPredicate createRelationPredicate(DBMetadataBuilder dbMetadata,
                                                             int tableNumber, int arity) {
        QuotedIDFactory idFactory = dbMetadata.getDBParameters().getQuotedIDFactory();
        DBTermType stringDBType = dbMetadata.getDBParameters().getDBTypeFactory().getDBStringType();
        RelationDefinition.AttributeListBuilder builder = new RelationDefinition.AttributeListBuilder(idFactory.createRelationID(null,
                "PK_TABLE" + tableNumber + "AR" + arity));
        for (int i = 1; i <= arity; i++) {
            builder.addAttribute(idFactory.createAttributeID("col" + i), stringDBType, false);
        }
        DatabaseRelationDefinition tableDef = dbMetadata.createDatabaseRelation(builder);

        tableDef.addUniqueConstraint(UniqueConstraint.primaryKeyOf(tableDef.getAttribute(1)));
        return tableDef.getAtomPredicate();
    }

    static {
        DBMetadataBuilder dbMetadata = DEFAULT_DUMMY_DB_METADATA;

        PK_TABLE1_AR1 = createRelationPredicate(dbMetadata, 1, 1);
        PK_TABLE2_AR1 = createRelationPredicate(dbMetadata, 2, 1);
        PK_TABLE3_AR1 = createRelationPredicate(dbMetadata, 3, 1);
        PK_TABLE4_AR1 = createRelationPredicate(dbMetadata, 4, 1);
        PK_TABLE5_AR1 = createRelationPredicate(dbMetadata, 5, 1);

        PK_TABLE1_AR2 = createRelationPredicate(dbMetadata, 1, 2);
        PK_TABLE2_AR2 = createRelationPredicate(dbMetadata, 2, 2);
        PK_TABLE3_AR2 = createRelationPredicate(dbMetadata, 3, 2);
        PK_TABLE4_AR2 = createRelationPredicate(dbMetadata, 4, 2);
        PK_TABLE5_AR2 = createRelationPredicate(dbMetadata, 5, 2);
        PK_TABLE6_AR2 = createRelationPredicate(dbMetadata, 6, 2);

        PK_TABLE1_AR3 = createRelationPredicate(dbMetadata, 1, 3);
        PK_TABLE2_AR3 = createRelationPredicate(dbMetadata, 2, 3);
        PK_TABLE3_AR3 = createRelationPredicate(dbMetadata, 3, 3);
        PK_TABLE4_AR3 = createRelationPredicate(dbMetadata, 4, 3);
        PK_TABLE5_AR3 = createRelationPredicate(dbMetadata, 5, 3);
        PK_TABLE6_AR3 = createRelationPredicate(dbMetadata, 6, 3);

        PK_TABLE7_AR4 = createRelationPredicate(dbMetadata, 7, 4);
    }

}
