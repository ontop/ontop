package it.unibz.inf.ontop;

import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.type.DBTermType;

import static it.unibz.inf.ontop.OptimizationTestingTools.TYPE_FACTORY;
import static it.unibz.inf.ontop.OptimizationTestingTools.createDummyMetadata;

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

    public static final BasicDBMetadata DB_METADATA;

    private static RelationPredicate createRelationPredicate(BasicDBMetadata dbMetadata, QuotedIDFactory idFactory,
                                                             int tableNumber, int arity) {
        DBTermType dbStringTermType = TYPE_FACTORY.getDBTypeFactory().getDBStringType();
        RelationDefinition.AttributeListBuilder builder = new RelationDefinition.AttributeListBuilder(idFactory.createRelationID(null,
                "PK_TABLE" + tableNumber + "AR" + arity));
        for (int i=1 ; i <= arity; i++) {
            builder.addAttribute(idFactory.createAttributeID("col" + i), dbStringTermType.getName(), dbStringTermType, false);
        }
        DatabaseRelationDefinition tableDef = dbMetadata.createDatabaseRelation(builder);

        tableDef.addUniqueConstraint(UniqueConstraint.primaryKeyOf(tableDef.getAttribute(1)));
        return tableDef.getAtomPredicate();
    }

    static {
        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        PK_TABLE1_AR1 = createRelationPredicate(dbMetadata, idFactory, 1, 1);
        PK_TABLE2_AR1 = createRelationPredicate(dbMetadata, idFactory, 2, 1);
        PK_TABLE3_AR1 = createRelationPredicate(dbMetadata, idFactory, 3, 1);
        PK_TABLE4_AR1 = createRelationPredicate(dbMetadata, idFactory, 4, 1);
        PK_TABLE5_AR1 = createRelationPredicate(dbMetadata, idFactory, 5, 1);

        PK_TABLE1_AR2 = createRelationPredicate(dbMetadata, idFactory, 1, 2);
        PK_TABLE2_AR2 = createRelationPredicate(dbMetadata, idFactory, 2, 2);
        PK_TABLE3_AR2 = createRelationPredicate(dbMetadata, idFactory, 3, 2);
        PK_TABLE4_AR2 = createRelationPredicate(dbMetadata, idFactory, 4, 2);
        PK_TABLE5_AR2 = createRelationPredicate(dbMetadata, idFactory, 5, 2);
        PK_TABLE6_AR2 = createRelationPredicate(dbMetadata, idFactory, 6, 2);

        PK_TABLE1_AR3 = createRelationPredicate(dbMetadata, idFactory, 1, 3);
        PK_TABLE2_AR3 = createRelationPredicate(dbMetadata, idFactory, 2, 3);
        PK_TABLE3_AR3 = createRelationPredicate(dbMetadata, idFactory, 3, 3);
        PK_TABLE4_AR3 = createRelationPredicate(dbMetadata, idFactory, 4, 3);
        PK_TABLE5_AR3 = createRelationPredicate(dbMetadata, idFactory, 5, 3);
        PK_TABLE6_AR3 = createRelationPredicate(dbMetadata, idFactory, 6, 3);

        PK_TABLE7_AR4 = createRelationPredicate(dbMetadata, idFactory, 7, 4);

        dbMetadata.freeze();
        DB_METADATA = dbMetadata;
    }

}
