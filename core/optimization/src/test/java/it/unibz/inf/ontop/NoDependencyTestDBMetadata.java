package it.unibz.inf.ontop;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;

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

    public static final RelationPredicate TABLE1_AR4;

    public static final RelationPredicate TABLE1_AR2_NESTED;

    public static final RelationPredicate NESTED_REL_PRED_AR1;
    public static final RelationPredicate NESTED_REL_PRED_AR2;
    public static final RelationPredicate NESTED_REL_PRED_AR3;
    public static final RelationPredicate NESTED_REL_PRED_AR4;

    public static final BasicDBMetadata DB_METADATA;

    private static RelationPredicate createRelationPredicate(BasicDBMetadata dbMetadata, QuotedIDFactory idFactory,
                                                             int tableNumber, int arity) {
        DatabaseRelationDefinition tableDef = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,
                "TABLE" + tableNumber + "AR" + arity));
        for (int i=1 ; i <= arity; i++) {
            tableDef.addAttribute(idFactory.createAttributeID("col" + i), TYPE_FACTORY.getUnderspecifiedDBType(), null, false);
        }
        return tableDef.getAtomPredicate();
    }

    private static RelationPredicate createNestedRelationPredicate(BasicDBMetadata dbMetadata,
                                                                   DatabaseRelationDefinition parentRelation,
                                                                   Integer indexInParentRelation,
                                                                   QuotedIDFactory idFactory,
                                                                   int tableNumber, int arity) {
        DatabaseRelationDefinition tableDef = dbMetadata.createNestedView(
                idFactory.createRelationID(
                        null,
                        "TABLE" + tableNumber + "AR" + arity),
                parentRelation,
                indexInParentRelation
        );
        for (int i=1 ; i <= arity; i++) {
            tableDef.addAttribute(idFactory.createAttributeID("col" + i), TYPE_FACTORY.getUnderspecifiedDBType(), null, false);
        }
        return tableDef.getAtomPredicate();
    }

    private static RelationPredicate createFlattenNodePredicate(QuotedIDFactory idFac, int arity){
        RelationID id = idFac.createRelationID(null, String.format("nestedRel_arity_%s", arity));
        ImmutableList<QuotedID> attributeIds = createAttributeIds(idFac, arity);
        return new FlattenNodeRelationDefinition(
                id,
                attributeIds,
                TYPE_FACTORY.getUnderspecifiedDBType(),
                TYPE_FACTORY.getDefaultRDFDatatype()
        ).getAtomPredicate();
    }

    private static ImmutableList<QuotedID> createAttributeIds(QuotedIDFactory idFac, int arity) {
        return IntStream.range(1, arity+1).boxed()
                .map(i -> idFac.createAttributeID("col"+i))
                .collect(ImmutableCollectors.toList());
    }

    static {
        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        TABLE1_AR1 = createRelationPredicate(dbMetadata, idFactory, 1, 1);
        TABLE2_AR1 = createRelationPredicate(dbMetadata, idFactory, 2, 1);
        TABLE3_AR1 = createRelationPredicate(dbMetadata, idFactory, 3, 1);
        TABLE4_AR1 = createRelationPredicate(dbMetadata, idFactory, 4, 1);
        TABLE5_AR1 = createRelationPredicate(dbMetadata, idFactory, 5, 1);

        TABLE1_AR2 = createRelationPredicate(dbMetadata, idFactory, 1, 2);
        TABLE2_AR2 = createRelationPredicate(dbMetadata, idFactory, 2, 2);
        TABLE3_AR2 = createRelationPredicate(dbMetadata, idFactory, 3, 2);
        TABLE4_AR2 = createRelationPredicate(dbMetadata, idFactory, 4, 2);
        TABLE5_AR2 = createRelationPredicate(dbMetadata, idFactory, 5, 2);
        TABLE6_AR2 = createRelationPredicate(dbMetadata, idFactory, 6, 2);

        TABLE1_AR3 = createRelationPredicate(dbMetadata, idFactory, 1, 3);
        TABLE2_AR3 = createRelationPredicate(dbMetadata, idFactory, 2, 3);
        TABLE3_AR3 = createRelationPredicate(dbMetadata, idFactory, 3, 3);
        TABLE4_AR3 = createRelationPredicate(dbMetadata, idFactory, 4, 3);
        TABLE5_AR3 = createRelationPredicate(dbMetadata, idFactory, 5, 3);
        TABLE6_AR3 = createRelationPredicate(dbMetadata, idFactory, 6, 3);

        TABLE1_AR4 = createRelationPredicate(dbMetadata, idFactory, 1, 4);

        TABLE1_AR2_NESTED = createNestedRelationPredicate(dbMetadata, (DatabaseRelationDefinition) TABLE1_AR2.getRelationDefinition(),
                2, idFactory, 1, 2);

        NESTED_REL_PRED_AR1 = createFlattenNodePredicate(idFactory, 1);
        NESTED_REL_PRED_AR2 = createFlattenNodePredicate(idFactory, 2);
        NESTED_REL_PRED_AR3 = createFlattenNodePredicate(idFactory, 3);
        NESTED_REL_PRED_AR4 = createFlattenNodePredicate(idFactory, 4);

        dbMetadata.freeze();
        DB_METADATA = dbMetadata;
    }

}
